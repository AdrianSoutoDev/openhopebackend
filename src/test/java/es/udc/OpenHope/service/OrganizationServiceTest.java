package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class OrganizationServiceTest {

  private static final String ORG_EMAIL = "org@openhope.com";
  private static final String ORG_NAME = "Apadan";
  private static final String ORG_DESCRIPTION = "Asociación Protectora de Animales Domésticos Abandonados del Noroeste";
  private static final String ORG_IMAGE = "c:\\openhope\\images\\organizations\\apadan.png";
  private static final String PASSWORD = "12345abc?";

  private final OrganizationRepository organizationRepository;
  private final OrganizationService organizationService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired
  public OrganizationServiceTest(final OrganizationService organizationService, final OrganizationRepository organizationRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.organizationService = organizationService;
    this.organizationRepository = organizationRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Test
  public void createOrganizationTest() throws DuplicateEmailException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, ORG_IMAGE);
    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());

    assertTrue(organizationFinded.isPresent());

    assertEquals(organizationFinded.get().getEmail(), ORG_EMAIL);
    assertEquals(organizationFinded.get().getName(), ORG_NAME);
    assertEquals(organizationFinded.get().getDescription(), ORG_DESCRIPTION);
    assertEquals(organizationFinded.get().getImage(), ORG_IMAGE);
  }

  @Test
  public void createOrganizationWithoutDescriptionTest() throws DuplicateEmailException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, ORG_IMAGE);
    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());
    assertTrue(organizationFinded.isPresent());
    assertNull(organizationFinded.get().getDescription());
  }

  @Test
  public void createOrganizationWithoutImageTest() throws DuplicateEmailException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);
    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());
    assertTrue(organizationFinded.isPresent());
    assertNull(organizationFinded.get().getImage());
  }

  @Test
  public void createOrganizationWithEncryptedPasswordTest() throws DuplicateEmailException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);
    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());
    assertTrue(organizationFinded.isPresent());
    boolean passwordsAreEquals = bCryptPasswordEncoder.matches(PASSWORD, organizationFinded.get().getEncryptedPassword());
    assertTrue(passwordsAreEquals);
  }

  @Test
  public void createOrganizationDuplicatedEmailTest() throws DuplicateEmailException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);
    assertThrows(DuplicateEmailException.class, () ->
        organizationService.create(ORG_EMAIL, "anotherPassword", "anotherName", null, null));
  }

  @Test
  public void createOrganizationDuplicatedEmailIgnoringCaseTest() throws DuplicateEmailException {
    OrganizationDto organizationDto = organizationService.create("org@openhope.com", PASSWORD, ORG_NAME, null, null);
    assertThrows(DuplicateEmailException.class, () ->
        organizationService.create("ORG@OpenHope.com", "anotherPassword", "anotherName", null, null));
  }

  @Test
  public void createOrganizationsWithDiferentEmailTest() throws DuplicateEmailException {
    OrganizationDto firstOrganizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);
    OrganizationDto secondOrganizationDto = organizationService.create("second_email@openHope.com", PASSWORD, ORG_NAME, null, null);

    List<Organization> organizations = organizationRepository.findAll();
    assertEquals(2, organizations.size());
  }

  @Test
  public void createOrganizationsWithEmailNullTest() {
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.create(null, PASSWORD, ORG_NAME, null, null));
  }

  @Test
  public void createOrganizationsWithPasswordNullTest() throws DuplicateEmailException {
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.create(ORG_EMAIL, null, ORG_NAME, null, null));
  }

  @Test
  public void createOrganizationsWithNameNullTest() throws DuplicateEmailException {
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.create(ORG_EMAIL, PASSWORD, null, null, null));
  }
}
