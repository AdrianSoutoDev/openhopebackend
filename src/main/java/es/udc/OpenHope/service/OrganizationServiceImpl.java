package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.mappers.OrganizationMapper;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.exception.DuplicateOrganizationException;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.repository.AccountRepository;
import es.udc.OpenHope.repository.OrganizationRepository;
import es.udc.OpenHope.utils.Messages;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OrganizationServiceImpl extends AccountServiceImpl implements OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final ResourceService resourceService;

  public OrganizationServiceImpl(OrganizationRepository organizationRepository,
                                 BCryptPasswordEncoder bCryptPasswordEncoder, AccountRepository accountRepository, ResourceService resourceService) {
    super(bCryptPasswordEncoder, accountRepository);
    this.organizationRepository = organizationRepository;
      this.resourceService = resourceService;
  }

  @Override
  public OrganizationDto create(String email, String password, String name, String description, MultipartFile image)
      throws DuplicateEmailException, DuplicateOrganizationException {

    if(email == null) throw new IllegalArgumentException( Messages.get("validation.email.null") );
    if(password == null) throw new IllegalArgumentException( Messages.get("validation.password.null") );
    if(name == null)  throw new IllegalArgumentException( Messages.get("validation.name.null") );

    if(accountExists(email)) throw new DuplicateEmailException( Messages.get("validation.email.duplicated") );
    if(organizationExists(name)) throw new DuplicateOrganizationException( Messages.get("validation.organization.duplicated") );

    String encryptedPassword = bCryptPasswordEncoder.encode(password);
    String imagePath = image != null ? resourceService.saveImage(image) : null;

    Organization organization = new Organization(email, encryptedPassword, name, description, imagePath);
    organizationRepository.save(organization);
    return OrganizationMapper.toOrganizationDto(organization);
  }

  private boolean organizationExists(String name) {
    return organizationRepository.existsByNameIgnoreCase(name);
  }
}
