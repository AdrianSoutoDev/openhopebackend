package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.BankAccountParams;
import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.mappers.CampaignMapper;
import es.udc.OpenHope.exception.DuplicatedCampaignException;
import es.udc.OpenHope.model.BankAccount;
import es.udc.OpenHope.model.Campaign;
import es.udc.OpenHope.model.Category;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.repository.BankAccountRepository;
import es.udc.OpenHope.repository.CampaignRepository;
import es.udc.OpenHope.repository.CategoryRepository;
import es.udc.OpenHope.repository.OrganizationRepository;
import es.udc.OpenHope.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

  private final CampaignRepository campaignRepository;
  private final OrganizationRepository organizationRepository;
  private final CategoryRepository categoryRepository;
  private final ResourceService resourceService;
  private final BankAccountRepository bankAccountRepository;

  @Override
  @Transactional
  public CampaignDto create(Long organizationId, String owner, String name, String description, LocalDate startAt,
                            LocalDate dateLimit, Long economicTarget, Float minimumDonation, List<String> categoryNames,
                            MultipartFile image) throws DuplicatedCampaignException {

    Optional<Organization> organization = organizationRepository.findById(organizationId);
    validateParamsCreate(organization, owner, name, startAt, dateLimit, economicTarget);

    //create Campaign
    Date startAtDate = Date.valueOf(startAt);
    Date dateLimitDate = dateLimit != null ? Date.valueOf(dateLimit) : null;
    String imagePath = image != null ? resourceService.save(image) : null;

    Set<Category> categories = getCategoriesMatchOrganization(categoryNames, organization.get());

    Campaign campaign = new Campaign(name, startAtDate, dateLimitDate, economicTarget, minimumDonation, imagePath,
        organization.get(), description,  categories);

    campaignRepository.save(campaign);

    return CampaignMapper.toCampaignDto(campaign).amountCollected(0F).percentageCollected(0F).isOnGoing(isOnGoing(campaign));
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
    if(!campaign.get().getOrganization().getEmail().equals(owner)){
      throw new SecurityException(Messages.get("validation.campaign.update.not.allowed"));
    }

    Optional<BankAccount> bankAccount = bankAccountRepository.findByIban(bankAccountParams.getIban());
    if(bankAccount.isEmpty()){
      BankAccount newBankAccount = new BankAccount();
      newBankAccount.setIban(bankAccountParams.getIban());
      newBankAccount.setName(bankAccountParams.getName());
      newBankAccount.setResourceId(bankAccountParams.getResourceId());
      newBankAccount.setOwnerName(bankAccountParams.getOwnerName());
      bankAccountRepository.save(newBankAccount);
      campaign.get().setBankAccount(newBankAccount);
    } else {
      campaign.get().setBankAccount(bankAccount.get());
    }

    campaignRepository.save(campaign.get());

    return CampaignMapper.toCampaignDto(campaign.get());
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

    //TODO Se debe eliminar esta comprobación cuando se establezca finalizedDate cuando se pase el target en las donaciones.
    boolean isUnderTarget = campaign.getEconomicTarget() == null || amountCollected(campaign) < campaign.getEconomicTarget();
    return itStated && isUnderTarget;
  }

  private Float amountCollected(Campaign campaign) {
    //TODO suma del importe de las donacionaciones
    return 0f;
  }

  private Float percentageCollected(Campaign campaign) {
    //TODO si tiene economicTarget, porcentaje entre el economicTarget y amountCollected();
    return 0f;
  }

  private boolean hasBankAccount(Campaign campaign) {
    return campaign.getBankAccount() != null;
  }

  private CampaignDto toCampaignDto(Campaign campaign) {
    return CampaignMapper.toCampaignDto(campaign)
        .amountCollected(amountCollected(campaign))
        .percentageCollected(percentageCollected(campaign))
        .isOnGoing(isOnGoing(campaign))
        .hasBankAccount(hasBankAccount(campaign));
  }

  private void validateParamsCreate(Optional<Organization> organization, String owner, String name, LocalDate startAt,
                               LocalDate dateLimit, Long economicTarget) throws DuplicatedCampaignException {

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
