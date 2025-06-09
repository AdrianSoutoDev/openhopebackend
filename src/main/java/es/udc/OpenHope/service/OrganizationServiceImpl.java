package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.mappers.OrganizationMapper;
import es.udc.OpenHope.dto.mappers.SearchResultMapper;
import es.udc.OpenHope.dto.searcher.SearchParamsDto;
import es.udc.OpenHope.dto.searcher.SearchResultDto;
import es.udc.OpenHope.enums.SortCriteria;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.exception.DuplicateOrganizationException;
import es.udc.OpenHope.exception.MaxCategoriesExceededException;
import es.udc.OpenHope.exception.MaxTopicsExceededException;
import es.udc.OpenHope.model.Category;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.model.Topic;
import es.udc.OpenHope.repository.AccountRepository;
import es.udc.OpenHope.repository.CategoryRepository;
import es.udc.OpenHope.repository.OrganizationRepository;
import es.udc.OpenHope.repository.TopicRepository;
import es.udc.OpenHope.utils.Messages;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class OrganizationServiceImpl extends AccountServiceImpl implements OrganizationService {

  private static final int MAX_CATEGORIES_ALLOWED = 3;

  private final OrganizationRepository organizationRepository;
  private final ResourceService resourceService;
  private final CategoryRepository categoryRepository;
  private final TopicService topicService;

  public OrganizationServiceImpl(OrganizationRepository organizationRepository,
                                 BCryptPasswordEncoder bCryptPasswordEncoder, AccountRepository accountRepository,
                                 ResourceService resourceService, CategoryRepository categoryRepository,
                                 TokenService tokenService, TopicRepository topicRepository, TopicService topicService) {
    super(bCryptPasswordEncoder, accountRepository, tokenService);
    this.organizationRepository = organizationRepository;
    this.resourceService = resourceService;
    this.categoryRepository = categoryRepository;
    this.topicService = topicService;
  }

  @Override
  @Transactional
  public OrganizationDto create(String email, String password, String name, String description, List<String> categoryNames, List<String> topics, MultipartFile image)
      throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, MaxTopicsExceededException {

    validateParamsCreate(email, password, name, categoryNames, topics);

    //Create organization
    String encryptedPassword = bCryptPasswordEncoder.encode(password);
    String imagePath = image != null ? resourceService.save(image) : null;

    List<Category> categoriesMatched = categoryRepository.findByNameIn(categoryNames);
    Set<Category> categories = new HashSet<>(categoriesMatched);

    Organization organization = new Organization(email, encryptedPassword, name, description ,imagePath, categories);
    organizationRepository.save(organization);
    topicService.saveTopics(topics, organization, email);
    return OrganizationMapper.toOrganizationDto(organization);
  }

  @Override
  public OrganizationDto get(Long id) {
    Optional<Organization> organization = organizationRepository.findById(id);
    if(organization.isEmpty())  throw new NoSuchElementException(Messages.get("validation.organization.not.exists"));
    return OrganizationMapper.toOrganizationDto(organization.get());
  }

  @Override
  @Transactional
  public OrganizationDto update(Long id, String name, String description, List<String> categoryNames, List<String> topics, MultipartFile image, String owner) throws DuplicateOrganizationException, MaxCategoriesExceededException, IOException, MaxTopicsExceededException {

    Optional<Organization> organization = organizationRepository.findById(id);
    validateParamsUpdate(organization, name, categoryNames, topics, owner);

    //Update organization
    if(image != null) {
      boolean areTheSameImage = false;
      if(organization.get().getImage() != null) {
        areTheSameImage = resourceService.areEquals(image, organization.get().getImage());
      }

      if(!areTheSameImage) {
        String newImage = resourceService.save(image);
        resourceService.remove(organization.get().getImage());
        organization.get().setImage(newImage);
      }
    }

    List<Category> categoriesMatched = categoryRepository.findByNameIn(categoryNames);
    Set<Category> categories = new HashSet<>(categoriesMatched);

    organization.get().setName(name);
    organization.get().setDescription(description);
    organization.get().setCategories(categories);

    organizationRepository.save(organization.get());
    topicService.updateTopics(topics, organization.get(), owner);
    return OrganizationMapper.toOrganizationDto(organization.get());
  }

  private boolean organizationExists(String name) {
    return organizationRepository.existsByNameIgnoreCase(name);
  }

  private boolean organizationExists(String name, Long id) {
    Organization organization = organizationRepository.findByNameIgnoreCase(name);
    return organization != null && !organization.getId().equals(id);
  }

  private void validateParamsCreate(String email, String password, String name, List<String> categoryNames, List<String> topics) throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException {
    if(email == null || email.isBlank()) throw new IllegalArgumentException( Messages.get("validation.email.null") );
    if(password == null || password.isBlank()) throw new IllegalArgumentException( Messages.get("validation.password.null") );
    if(name == null || name.isBlank())  throw new IllegalArgumentException( Messages.get("validation.name.null") );

    if(accountExists(email)) throw new DuplicateEmailException( Messages.get("validation.email.duplicated") );
    if(organizationExists(name)) throw new DuplicateOrganizationException( Messages.get("validation.organization.duplicated") );

    if(categoryNames != null && categoryNames.size() > MAX_CATEGORIES_ALLOWED) {
      throw new MaxCategoriesExceededException( Messages.get("validation.organization.max.categories") );
    }
  }

  private void validateParamsUpdate(Optional<Organization> organization, String name, List<String> categoryNames, List<String> topics,  String owner) throws DuplicateOrganizationException, MaxCategoriesExceededException {
    if(organization.isEmpty()) throw new NoSuchElementException(Messages.get("validation.organization.not.exists"));

    if(!owner.equals(organization.get().getEmail())){
      throw new SecurityException(Messages.get("validation.organization.update.not.allowed"));
    }

    if(name == null || name.isBlank()) throw new IllegalArgumentException( Messages.get("validation.name.null") );

    if(organizationExists(name, organization.get().getId()))
      throw new DuplicateOrganizationException( Messages.get("validation.organization.duplicated") );

    if(categoryNames != null && categoryNames.size() > MAX_CATEGORIES_ALLOWED) {
      throw new MaxCategoriesExceededException( Messages.get("validation.organization.max.categories") );
    }
  }

  public Page<SearchResultDto> search(SearchParamsDto searchParamsDto, int page, int size){
    Pageable pageable = PageRequest.of(page, size);
    Page<Organization> organizationPage = organizationRepository.findAll(getSearchSpecification(searchParamsDto), pageable);
    return organizationPage.map(SearchResultMapper::toOrganizationSearchResultDto);
  }

  private Specification<Organization> getSearchSpecification(SearchParamsDto searchParamsDto) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicatesText = new ArrayList<>();
      List<Predicate> predicatesCategories = new ArrayList<>();

      if (searchParamsDto.getText() != null && !searchParamsDto.getText().isBlank()) {
        String likePattern = "%" + searchParamsDto.getText() + "%";

        predicatesText.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern.toLowerCase()));

        predicatesText.add(criteriaBuilder.like(
            criteriaBuilder.lower(root.get("description")),
            likePattern.toLowerCase()
        ));

        Join<Organization, Topic> topicsJoin = root.join("topics", JoinType.LEFT);
        predicatesText.add(criteriaBuilder.like(criteriaBuilder.lower(topicsJoin.get("name")), searchParamsDto.getText().toLowerCase()));
      }

      if (searchParamsDto.getCategories() != null && !searchParamsDto.getCategories().isEmpty()) {
        Join<Organization, Category> categoriesJoin = root.join("categories", JoinType.INNER);
        predicatesCategories.add(categoriesJoin.get("name").in(searchParamsDto.getCategories()));
      }

      Predicate finalPredicate;
      if (!predicatesText.isEmpty() && !predicatesCategories.isEmpty()) {
        finalPredicate = criteriaBuilder.and(
            criteriaBuilder.or(predicatesText.toArray(new Predicate[0])),
            criteriaBuilder.and(predicatesCategories.toArray(new Predicate[0]))
        );
      } else if (!predicatesText.isEmpty()) {
        finalPredicate = criteriaBuilder.or(predicatesText.toArray(new Predicate[0]));
      } else if (!predicatesCategories.isEmpty()) {
        finalPredicate = criteriaBuilder.and(predicatesCategories.toArray(new Predicate[0]));
      } else {
        finalPredicate = criteriaBuilder.conjunction();
      }

      if(searchParamsDto.getSortCriteria() != null && searchParamsDto.getSortCriteria().equals(SortCriteria.NAME_DESC)) {
        query.orderBy(criteriaBuilder.desc(root.get("name")));
      } else {
        query.orderBy(criteriaBuilder.asc(root.get("name")));
      }

      return finalPredicate;
    };
  }
}
