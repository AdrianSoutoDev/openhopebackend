package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.BankAccountParams;
import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.DonationDto;
import es.udc.OpenHope.dto.SearchParamsDto;
import es.udc.OpenHope.dto.mappers.AspspMapper;
import es.udc.OpenHope.dto.mappers.BankAccountMapper;
import es.udc.OpenHope.dto.mappers.CampaignMapper;
import es.udc.OpenHope.dto.mappers.DonationMapper;
import es.udc.OpenHope.enums.CampaignFinalizeType;
import es.udc.OpenHope.enums.CampaignState;
import es.udc.OpenHope.enums.SortCriteria;
import es.udc.OpenHope.exception.DuplicatedCampaignException;
import es.udc.OpenHope.exception.MaxTopicsExceededException;
import es.udc.OpenHope.model.*;
import es.udc.OpenHope.repository.*;
import es.udc.OpenHope.utils.Messages;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

  private final CampaignRepository campaignRepository;
  private final OrganizationRepository organizationRepository;
  private final CategoryRepository categoryRepository;
  private final ResourceService resourceService;
  private final BankAccountRepository bankAccountRepository;
  private final AspspRepository aspspRepository;
  private final TopicService topicService;
  private final DonationRepository donationRepository;

  @Override
  @Transactional
  public CampaignDto create(Long organizationId, String owner, String name, String description, LocalDate startAt,
                            LocalDate dateLimit, Long economicTarget, Float minimumDonation, List<String> categoryNames,
                            List<String> topics, MultipartFile image) throws DuplicatedCampaignException, MaxTopicsExceededException {

    Optional<Organization> organization = organizationRepository.findById(organizationId);
    validateParamsCreate(organization, owner, name, startAt, dateLimit, economicTarget, topics);

    //create Campaign
    Date startAtDate = Date.valueOf(startAt);
    Date dateLimitDate = dateLimit != null ? Date.valueOf(dateLimit) : null;
    String imagePath = image != null ? resourceService.save(image) : null;

    Set<Category> categories = getCategoriesMatchOrganization(categoryNames, organization.get());

    Campaign campaign = new Campaign(name, startAtDate, dateLimitDate, economicTarget, minimumDonation, imagePath,
        organization.get(), description,  categories);

    campaignRepository.save(campaign);
    topicService.saveTopics(topics, campaign, owner);

    return toCampaignDto(campaign);
  }

  @Override
  @Transactional
  public Page<CampaignDto> getByOrganization(Long organizationId, int page, int size) {
    Optional<Organization> organization = organizationRepository.findById(organizationId);
    if(organization.isEmpty()) throw new NoSuchElementException(Messages.get("validation.organization.not.exists"));
    Pageable pageable = PageRequest.of(page, size);
    Date tomorrow = Date.valueOf(LocalDate.now().plusDays(1));
    Page<Campaign> campaignPage = campaignRepository.findByOrganizationAndStartAtLessThanOrderByStartAtDesc(organization.get(), tomorrow, pageable);
    return campaignPage.map(this::toCampaignDto);
  }

  @Override
  public CampaignDto get(Long id) {
    Optional<Campaign> campaign = campaignRepository.findById(id);
    if(campaign.isEmpty()) throw new NoSuchElementException(Messages.get("validation.campaign.not.exists"));
    return toCampaignDto(campaign.get());
  }

  @Override
  @Transactional
  public CampaignDto updateBankAccount(Long id, BankAccountParams bankAccountParams, String owner) {
    Optional<Campaign> campaign = campaignRepository.findById(id);

    if(campaign.isEmpty()) throw new NoSuchElementException(Messages.get("validation.campaign.not.exists"));

    Organization organization = campaign.get().getOrganization();

    if(!organization.getEmail().equals(owner)){
      throw new SecurityException(Messages.get("validation.campaign.update.not.allowed"));
    }

    Optional<BankAccount> bankAccount = bankAccountRepository.findByIbanAndAccount(bankAccountParams.getIban(), organization);
    Optional<Aspsp> aspsp = aspspRepository.findByProviderAndCode(bankAccountParams.getAspsp().getProvider(), bankAccountParams.getAspsp().getCode());

    if(bankAccount.isEmpty()){
      BankAccount newBankAccount = BankAccountMapper.toBankAccount(bankAccountParams);

      if(aspsp.isEmpty()){
        Aspsp newAspsp = AspspMapper.toAspsp(bankAccountParams.getAspsp());
        aspspRepository.save(newAspsp);
        aspsp = Optional.of(newAspsp);
      }

      newBankAccount.setAspsp(aspsp.get());
      newBankAccount.setAccount(organization);
      bankAccountRepository.save(newBankAccount);
      campaign.get().setBankAccount(newBankAccount);
    } else {
      campaign.get().setBankAccount(bankAccount.get());
    }

    campaignRepository.save(campaign.get());

    return toCampaignDto(campaign.get());
  }

  @Override
  public Page<CampaignDto> search(SearchParamsDto searchParamsDto, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Campaign> campaignPage = campaignRepository.findAll(getSearchSpecification(searchParamsDto), pageable);
    return campaignPage.map(this::toCampaignDto);
  }

  @Override
  public Page<DonationDto> getDonations(Long id, int page, int size) {
    Optional<Campaign> campaign = campaignRepository.findById(id);
    if(campaign.isEmpty()) throw new NoSuchElementException(Messages.get("validation.campaign.not.exists"));
    Pageable pageable = PageRequest.of(page, size);
    Page<Donation> donations = donationRepository.findByCampaignAndConfirmedTrueOrderByDateDesc(campaign.get(), pageable);
    return donations.map(DonationMapper::toDonationDto);
  }

  @Override
  @Transactional
  public DonationDto addDonation(Campaign campaign, BankAccount bankAccount, Float amount, Timestamp dateTime) {
    Donation donation = new Donation(campaign, bankAccount, amount, dateTime);
    donationRepository.save(donation);

    if(campaign.getEconomicTarget() != null && campaign.getEconomicTarget() > 0){
      List<Donation> donations = donationRepository.findByCampaignAndConfirmedTrue(campaign);
      Float amountCollected = sumDonations(donations);
      if(amountCollected > campaign.getEconomicTarget() && campaign.getFinalizedDate() == null) {
        campaign.setFinalizedDate(new Date(dateTime.getTime()));
        campaignRepository.save(campaign);
      }
    }

    return DonationMapper.toDonationDto(donation);
  }

  private Specification<Campaign> getSearchSpecification(SearchParamsDto searchParamsDto) {
    return (root, query, criteriaBuilder) -> {

      Predicate textPredicate = buildTextPredicate(root, criteriaBuilder, searchParamsDto.getText());
      Predicate categoriesPredicate = buildCategoriesPredicate(root, criteriaBuilder, searchParamsDto.getCategories());
      Predicate startDatePredicate = buildStartDatePredicate(root, criteriaBuilder, searchParamsDto.getStartDateFrom(), searchParamsDto.getStartDateTo());
      Predicate statePredicate = buildStatePredicate(root, criteriaBuilder, searchParamsDto.getCampaignState());
      Predicate finalizeTypePredicate = buildFinalizeType(root, criteriaBuilder, searchParamsDto.getCampaignFinalizeType());
      Predicate finalizeDatePredicate = buildFinalizeDatePredicate(root, criteriaBuilder, searchParamsDto.getFinalizeDateFrom(), searchParamsDto.getFinalizeDateTo());
      Predicate economicTagetPredicate = buildEconomicTagetPredicate(root, criteriaBuilder, searchParamsDto.getEconomicTargetFrom(), searchParamsDto.getEconomicTargetTo());
      Predicate minimumDonationPredicate = buildMinimumDonationPredicate(root, criteriaBuilder, searchParamsDto.getMinimumDonationFrom(), searchParamsDto.getMinimumDonationTo());
      Predicate hasMinimumDonationPredicate = buildHasMinimumDonationPredicate(root, criteriaBuilder, searchParamsDto.isHasMinimumDonation());

      Predicate combinedPredicate = criteriaBuilder.and(textPredicate, categoriesPredicate, startDatePredicate,
          statePredicate, finalizeTypePredicate, finalizeDatePredicate, economicTagetPredicate, minimumDonationPredicate, hasMinimumDonationPredicate);

      if (searchParamsDto.getSortCriteria() != null) {
        sortCampaignsBySortCriteria(searchParamsDto.getSortCriteria(), query, criteriaBuilder, root);
      } else {
        query.orderBy(criteriaBuilder.asc(root.get("name")));
      }

      return combinedPredicate;
    };
  }

  private Predicate buildTextPredicate(Root<Campaign> root, CriteriaBuilder criteriaBuilder, String text) {
    if (text == null || text.isBlank()) {
      return criteriaBuilder.conjunction();
    }

    String likePattern = "%" + text.toLowerCase() + "%";
    return criteriaBuilder.or(
        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern),
        criteriaBuilder.like(criteriaBuilder.lower(root.join("topics", JoinType.LEFT).get("name")), likePattern)
    );
  }

  private Predicate buildCategoriesPredicate(Root<Campaign> root, CriteriaBuilder criteriaBuilder, List<String> categories) {
    if (categories == null || categories.isEmpty()) {
      return criteriaBuilder.conjunction();
    }

    Join<Organization, Category> categoriesJoin = root.join("categories", JoinType.INNER);
    return categoriesJoin.get("name").in(categories);
  }

  private Predicate buildStartDatePredicate(Root<Campaign> root, CriteriaBuilder criteriaBuilder,
                                            LocalDate startDateFrom, LocalDate startDateTo) {
    if (startDateFrom == null && startDateTo == null) {
      return criteriaBuilder.conjunction();
    }

    if (startDateFrom == null){
      return criteriaBuilder.lessThanOrEqualTo(root.get("startAt"), startDateTo);
    }

    if (startDateTo == null){
      return criteriaBuilder.greaterThanOrEqualTo(root.get("startAt"), startDateFrom);
    }

    return criteriaBuilder.between(root.get("startAt"), startDateFrom, startDateTo);
  }

  private Predicate buildStatePredicate(Root<Campaign> root, CriteriaBuilder criteriaBuilder, CampaignState campaignState) {
    if (campaignState == null) {
      return criteriaBuilder.conjunction();
    }

    if(campaignState.equals(CampaignState.FINALIZED)) {
      return criteriaBuilder.or(
          criteriaBuilder.isNotNull(root.get("finalizedDate")),
          criteriaBuilder.lessThanOrEqualTo(root.get("dateLimit"), LocalDate.now())
      );
    }

    return criteriaBuilder.and(
        criteriaBuilder.isNull(root.get("finalizedDate")),
        criteriaBuilder.or(
          criteriaBuilder.isNull(root.get("dateLimit")),
          criteriaBuilder.greaterThan(root.get("dateLimit"), LocalDate.now())
        )
    );
  }

  private Predicate buildFinalizeType(Root<Campaign> root, CriteriaBuilder criteriaBuilder, CampaignFinalizeType finalizeType) {
    if (finalizeType == null) {
      return criteriaBuilder.conjunction();
    }

    if(finalizeType.equals(CampaignFinalizeType.DATE)) {
      return criteriaBuilder.isNotNull(root.get("dateLimit"));
    }

    return criteriaBuilder.isNotNull(root.get("economicTarget"));
  }

  private Predicate buildFinalizeDatePredicate(Root<Campaign> root, CriteriaBuilder criteriaBuilder,
                                            LocalDate finalizeDateFrom, LocalDate finalizeDateTo) {
    if (finalizeDateFrom == null && finalizeDateTo == null) {
      return criteriaBuilder.conjunction();
    }

    if (finalizeDateFrom == null){
      return criteriaBuilder.and(
          criteriaBuilder.isNotNull(root.get("dateLimit")),
          criteriaBuilder.lessThanOrEqualTo(root.get("dateLimit"), finalizeDateTo)
      );
    }

    if (finalizeDateTo == null){
      return criteriaBuilder.and(
          criteriaBuilder.isNotNull(root.get("dateLimit")),
          criteriaBuilder.greaterThanOrEqualTo(root.get("dateLimit"), finalizeDateFrom)
      );
    }

    return criteriaBuilder.and(
        criteriaBuilder.isNotNull(root.get("dateLimit")),
        criteriaBuilder.between(root.get("dateLimit"), finalizeDateFrom, finalizeDateTo)
    );
  }

  private Predicate buildEconomicTagetPredicate(Root<Campaign> root, CriteriaBuilder criteriaBuilder,
                                                Long economicTargetFrom, Long economicTargetTo) {
    if (economicTargetFrom == null && economicTargetTo == null) {
      return criteriaBuilder.conjunction();
    }

    if (economicTargetFrom == null){
      return criteriaBuilder.and(
          criteriaBuilder.isNotNull(root.get("economicTarget")),
          criteriaBuilder.lessThanOrEqualTo(root.get("economicTarget"), economicTargetTo)
      );
    }

    if (economicTargetTo == null){
      return criteriaBuilder.and(
          criteriaBuilder.isNotNull(root.get("economicTarget")),
          criteriaBuilder.greaterThanOrEqualTo(root.get("economicTarget"), economicTargetFrom)
      );
    }

    return criteriaBuilder.and(
        criteriaBuilder.isNotNull(root.get("economicTarget")),
        criteriaBuilder.between(root.get("economicTarget"), economicTargetFrom, economicTargetTo)
    );
  }

  private Predicate buildMinimumDonationPredicate(Root<Campaign> root, CriteriaBuilder criteriaBuilder,
                                                Long minimumDonationFrom, Long minimumDonationTo) {
    if (minimumDonationFrom == null && minimumDonationTo == null) {
      return criteriaBuilder.conjunction();
    }

    if (minimumDonationFrom == null){
      return criteriaBuilder.and(
          criteriaBuilder.isNotNull(root.get("minimumDonation")),
          criteriaBuilder.lessThanOrEqualTo(root.get("minimumDonation"), minimumDonationTo)
      );
    }

    if (minimumDonationTo == null){
      return criteriaBuilder.and(
          criteriaBuilder.isNotNull(root.get("minimumDonation")),
          criteriaBuilder.greaterThanOrEqualTo(root.get("minimumDonation"), minimumDonationFrom)
      );
    }

    return criteriaBuilder.and(
        criteriaBuilder.isNotNull(root.get("minimumDonation")),
        criteriaBuilder.between(root.get("minimumDonation"), minimumDonationFrom, minimumDonationTo)
    );
  }

  private Predicate buildHasMinimumDonationPredicate(Root<Campaign> root, CriteriaBuilder criteriaBuilder,
                                                  boolean hasMinimumDonation) {
    if (!hasMinimumDonation) {
      return criteriaBuilder.conjunction();
    }

    return criteriaBuilder.isNull(root.get("minimumDonation"));
  }

  private CriteriaQuery<?> sortCampaignsBySortCriteria(SortCriteria sortCriteria, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, Root<Campaign> root) {

    if(SortCriteria.NAME_DESC.equals(sortCriteria)) return query.orderBy(criteriaBuilder.desc(root.get("name")));

    if(SortCriteria.START_DATE_ASC.equals(sortCriteria)) return query.orderBy(criteriaBuilder.asc(root.get("startAt")));
    if(SortCriteria.START_DATE_DESC.equals(sortCriteria)) return query.orderBy(criteriaBuilder.desc(root.get("startAt")));

    if(SortCriteria.END_DATE_ASC.equals(sortCriteria)) return query.orderBy(criteriaBuilder.asc(root.get("dateLimit")));
    if(SortCriteria.END_DATE_DESC.equals(sortCriteria)) return query.orderBy(criteriaBuilder.desc(root.get("dateLimit")));

    if(SortCriteria.TARGET_AMOUNT_ASC.equals(sortCriteria)) return query.orderBy(criteriaBuilder.asc(root.get("economicTarget")));
    if(SortCriteria.TARGET_AMOUNT_DESC.equals(sortCriteria)) return query.orderBy(criteriaBuilder.desc(root.get("economicTarget")));

    if(SortCriteria.MIN_DONATION_ASC.equals(sortCriteria)) return query.orderBy(criteriaBuilder.asc(root.get("minimumDonation")));
    if(SortCriteria.MIN_DONATION_DESC.equals(sortCriteria)) return query.orderBy(criteriaBuilder.desc(root.get("minimumDonation")));

    // get sum donations
    Subquery<Float> donationSumSubquery = query.subquery(Float.class);
    Root<Donation> donationRoot = donationSumSubquery.from(Donation.class);
    donationSumSubquery.select(criteriaBuilder.coalesce(criteriaBuilder.sum(donationRoot.get("amount")), 0F));
    donationSumSubquery.where(criteriaBuilder.equal(donationRoot.get("campaign"), root));

    if (SortCriteria.CLOSEST_TO_GOAL.equals(sortCriteria)) return query.orderBy(criteriaBuilder.desc(
        criteriaBuilder.quot(donationSumSubquery.getSelection(), root.get("economicTarget")) ));

    if (SortCriteria.FARTHEST_FROM_GOAL.equals(sortCriteria)) return query.orderBy(criteriaBuilder.asc(
        criteriaBuilder.quot(donationSumSubquery.getSelection(), root.get("economicTarget")) ));

    //default
    return query.orderBy(criteriaBuilder.asc(root.get("name")));
  }

  private boolean campaignExists(String name) {
     Campaign campaign = campaignRepository.findByNameIgnoreCase(name);
     return campaign != null;
  }

  private Set<Category> getCategoriesMatchOrganization(List<String> categoryNames, Organization organization) {
    if(categoryNames == null || categoryNames.isEmpty()) {
      return new HashSet<>();
    }

    Set<Category> categoriesOrganization = organization.getCategories();

    List<String> categoriesMatched = categoryNames.stream().filter(c ->
        categoriesOrganization.stream().anyMatch(co -> co.getName().equals(c))
    ).toList();

    List<Category> categoriesFinded = categoryRepository.findByNameIn(categoriesMatched);
    return new HashSet<>(categoriesFinded);
  }

  private boolean isOnGoing(Campaign campaign) {

    if(campaign.getFinalizedDate() != null) return false;

    boolean itStated = LocalDate.now().isEqual(campaign.getStartAt().toLocalDate()) || LocalDate.now().isAfter(campaign.getStartAt().toLocalDate());
    boolean isBeforeDateLimit = campaign.getDateLimit() == null || LocalDate.now().isBefore(campaign.getDateLimit().toLocalDate());

    if(!isBeforeDateLimit) {
      campaign.setFinalizedDate(campaign.getDateLimit());
      campaignRepository.save(campaign);
      return false;
    }

    return itStated;
  }

  private Float sumDonations(List<Donation> donations) {
    return donations.stream()
                    .map(Donation::getAmount)
                    .reduce(0f, Float::sum);
  }

  private Float percentageCollected(Campaign campaign, Float amountCollected) {
    float percentageCollected = 0f;
    if (campaign.getEconomicTarget() != null && campaign.getEconomicTarget() > 0) {
      percentageCollected = (amountCollected / campaign.getEconomicTarget()) * 100;
    }
    return Math.min(percentageCollected, 100F);
  }

  private boolean hasBankAccount(Campaign campaign) {
    return campaign.getBankAccount() != null;
  }

  private List<Float> suggestions(Campaign campaign, List<Donation> donations, Float amountCollected) {
    List<Float> suggestions = new ArrayList<>();

    if(campaign.getMinimumDonation() != null && campaign.getMinimumDonation() > 0) {
      suggestions.add(campaign.getMinimumDonation());
    } else {
      suggestions.add(1f);
    }

    if(donations.isEmpty()) {
      suggestions.add(suggestions.get(0) * 2);
      suggestions.add(suggestions.get(1) * 5);
    } else {
      Float average = amountCollected / donations.size();
      Float mode = calculateMode(donations);

      if(!average.equals(suggestions.get(0))){
        suggestions.add(average);
      } else {
        suggestions.add(suggestions.get(0) * 2);
      }

      if( !mode.equals(suggestions.get(0)) && !mode.equals(suggestions.get(1)) ) {
        suggestions.add(mode);
      } else {
        suggestions.add(Float.max(suggestions.get(0), suggestions.get(1)) * 2);
      }
    }

    Collections.sort(suggestions);

    List<Float> roundedSuggestions = new ArrayList<>();
    for (Float suggestion : suggestions) {
      roundedSuggestions.add(Math.round(suggestion * 100f) / 100f);
    }
    return roundedSuggestions;
  }

  private Float calculateMode(List<Donation> donations) {
    return donations.stream()
        .map(Donation::getAmount)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet()
        .stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
  }

  private CampaignDto toCampaignDto(Campaign campaign) {
    List<Donation> donations = donationRepository.findByCampaignAndConfirmedTrue(campaign);
    Float amountCollected = sumDonations(donations);

    return CampaignMapper.toCampaignDto(campaign)
        .amountCollected(amountCollected)
        .percentageCollected(percentageCollected(campaign, amountCollected))
        .isOnGoing(isOnGoing(campaign))
        .hasBankAccount(hasBankAccount(campaign))
        .suggestions(suggestions(campaign, donations, amountCollected));
  }

  private void validateParamsCreate(Optional<Organization> organization, String owner, String name, LocalDate startAt,
                               LocalDate dateLimit, Long economicTarget, List<String> topics) throws DuplicatedCampaignException {

    if(organization.isEmpty()) throw new NoSuchElementException(Messages.get("validation.organization.not.exists"));

    if(!owner.equals(organization.get().getEmail())){
      throw new SecurityException(Messages.get("validation.campaign.create.not.allowed"));
    }

    if(name == null || name.isBlank()) throw new IllegalArgumentException( Messages.get("validation.name.null") );

    if(campaignExists(name))
      throw new DuplicatedCampaignException( Messages.get("validation.campaign.duplicated") );

    if(startAt == null || startAt.isBefore(LocalDate.now())){
      throw new IllegalArgumentException( Messages.get("validation.startat.invalid"));
    }

    if(dateLimit == null && economicTarget == null){
      throw new IllegalArgumentException( Messages.get("validation.datelimit.economictarget.needed"));
    }

    if(dateLimit != null && (startAt.isEqual(dateLimit) ||  startAt.isAfter(dateLimit)) ){
      throw new IllegalArgumentException( Messages.get("validation.datelimit.startAt.invalid"));
    }
  }
}
