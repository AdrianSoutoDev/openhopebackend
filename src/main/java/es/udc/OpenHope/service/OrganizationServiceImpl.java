package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.mappers.OrganizationMapper;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.exception.DuplicateOrganizationException;
import es.udc.OpenHope.exception.MaxCategoriesExceededException;
import es.udc.OpenHope.model.Category;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.repository.AccountRepository;
import es.udc.OpenHope.repository.OrganizationRepository;
import es.udc.OpenHope.utils.Messages;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class OrganizationServiceImpl extends AccountServiceImpl implements OrganizationService {

  private static final int MAX_CATEGORIES_ALLOWED = 3;

  private final OrganizationRepository organizationRepository;
  private final ResourceService resourceService;
  private final CategoryService categoryService;

  public OrganizationServiceImpl(OrganizationRepository organizationRepository,
                                 BCryptPasswordEncoder bCryptPasswordEncoder, AccountRepository accountRepository,
                                 ResourceService resourceService, CategoryService categoryService) {
    super(bCryptPasswordEncoder, accountRepository);
    this.organizationRepository = organizationRepository;
    this.resourceService = resourceService;
    this.categoryService = categoryService;
  }

  @Override
  @Transactional
  public OrganizationDto create(String email, String password, String name, String description, List<String> categoryNames, MultipartFile image)
      throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException {

    if(email == null) throw new IllegalArgumentException( Messages.get("validation.email.null") );
    if(password == null) throw new IllegalArgumentException( Messages.get("validation.password.null") );
    if(name == null)  throw new IllegalArgumentException( Messages.get("validation.name.null") );

    if(accountExists(email)) throw new DuplicateEmailException( Messages.get("validation.email.duplicated") );
    if(organizationExists(name)) throw new DuplicateOrganizationException( Messages.get("validation.organization.duplicated") );

    if(categoryNames != null && categoryNames.size() > MAX_CATEGORIES_ALLOWED) {
      throw new MaxCategoriesExceededException( Messages.get("validation.organization.max.categories") );
    }

    String encryptedPassword = bCryptPasswordEncoder.encode(password);
    String imagePath = image != null ? resourceService.saveImage(image) : null;

    List<Category> categoriesMatched = categoryService.getCategoriesByName(categoryNames);
    Set<Category> categories = new HashSet<>(categoriesMatched);

    Organization organization = new Organization(email, encryptedPassword, name, description ,imagePath, categories);
    organizationRepository.save(organization);

    return OrganizationMapper.toOrganizationDto(organization);
  }

  @Override
  public OrganizationDto create(String email, String password, String name, String description, MultipartFile image) throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException {
    return create(email, password, name, description, new ArrayList<>(), image);
  }

  @Override
  public OrganizationDto getOrganizationById(Long id) {
    Optional<Organization> organization = organizationRepository.findById(id);
    if(organization.isEmpty()) {
      throw new NoSuchElementException("");
    }

    return OrganizationMapper.toOrganizationDto(organization.get());
  }

  private boolean organizationExists(String name) {
    return organizationRepository.existsByNameIgnoreCase(name);
  }
}
