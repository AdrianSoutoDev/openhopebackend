package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.mappers.OrganizationMapper;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.repository.AccountRepository;
import es.udc.OpenHope.repository.OrganizationRepository;
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
      throws DuplicateEmailException {

    if(email == null) throw new IllegalArgumentException("email cannot be null");
    if(password == null) throw new IllegalArgumentException("password cannot be null");
    if(name == null)  throw new IllegalArgumentException("name cannot be null");

    if(accountExists(email)) {
      throw new DuplicateEmailException("e-mail already exists");
    }

    String encryptedPassword = bCryptPasswordEncoder.encode(password);
    String imagePath = image != null ? resourceService.saveImage(image) : null;

    Organization organization = new Organization(email, encryptedPassword, name, description, imagePath);
    organizationRepository.save(organization);
    return OrganizationMapper.toOrganizationDto(organization);
  }
}
