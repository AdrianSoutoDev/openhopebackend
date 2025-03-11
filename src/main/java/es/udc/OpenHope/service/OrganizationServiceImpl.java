package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.mappers.OrganizationMapper;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.repository.AccountRepository;
import es.udc.OpenHope.repository.OrganizationRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class OrganizationServiceImpl extends AccountServiceImpl implements OrganizationService {

  private final OrganizationRepository organizationRepository;

  public OrganizationServiceImpl(OrganizationRepository organizationRepository,
                                 BCryptPasswordEncoder bCryptPasswordEncoder, AccountRepository accountRepository) {
    super(bCryptPasswordEncoder, accountRepository);
    this.organizationRepository = organizationRepository;
  }

  @Override
  public OrganizationDto create(String email, String password, String name, String description, String image)
      throws DuplicateEmailException, IllegalArgumentException {

    if(email == null) throw new IllegalArgumentException("email cannot be null");
    if(password == null) throw new IllegalArgumentException("password cannot be null");
    if(name == null)  throw new IllegalArgumentException("name cannot be null");

    if(accountExists(email)) {
      throw new DuplicateEmailException("e-mail already exists");
    }

    String encryptedPassword = bCryptPasswordEncoder.encode(password);
    Organization organization = new Organization(email, encryptedPassword, name, description, image);

    organizationRepository.save(organization);
    return OrganizationMapper.toOrganizationDto(organization);
  }
}
