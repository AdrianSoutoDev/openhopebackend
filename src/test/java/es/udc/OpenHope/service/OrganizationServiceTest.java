package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.repository.OrganizationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class OrganizationServiceTest {

  private static final String ORG_EMAIL = "org@openhope.com";
  private static final String ORG_NAME = "Apadan";
  private static final String ORG_DESCRIPTION = "Asociación Protectora de Animales Domésticos Abandonados del Noroeste";
  private static final String PASSWORD = "12345abc?";

  @Value("${upload.dir}")
  private String uploadDir;

  private String createdFileName = null;

  private final OrganizationRepository organizationRepository;
  private final OrganizationService organizationService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired
  public OrganizationServiceTest(final OrganizationService organizationService, final OrganizationRepository organizationRepository,
                                 final BCryptPasswordEncoder bCryptPasswordEncoder, ResourceService resourceService) {
    this.organizationService = organizationService;
    this.organizationRepository = organizationRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @AfterEach
  public void cleanUp() throws IOException {
    if (createdFileName != null) {
      deleteImg();
    }
  }

  private void deleteImg() throws IOException {
    Path filePath = Path.of(uploadDir, createdFileName);
    if (Files.exists(filePath)) {
      Files.delete(filePath);
    }
  }

  private MockMultipartFile getTestImg() throws IOException {
    ClassPathResource resource = new ClassPathResource("test-images/test-image.png");
    byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());
    return new MockMultipartFile(
            "file",
            "test-image.png",
            "image/png",
            fileContent
    );
  }

  @Test
  public void createOrganizationTest() throws DuplicateEmailException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null);
    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());

    assertTrue(organizationFinded.isPresent());

    assertEquals(ORG_EMAIL, organizationFinded.get().getEmail());
    assertEquals(ORG_NAME, organizationFinded.get().getName());
    assertEquals(ORG_DESCRIPTION, organizationFinded.get().getDescription());
    assertNull(organizationFinded.get().getImage());
  }

  @Test
  public void createOrganizationWithImgTest() throws IOException, DuplicateEmailException {
    MockMultipartFile testImage = getTestImg();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, testImage);

    createdFileName = organizationDto.getImage();

    Path filePath = Path.of(uploadDir, createdFileName);
    assertTrue(Files.exists(filePath));
  }

  @Test
  public void createOrganizationWithoutDescriptionTest() throws DuplicateEmailException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);
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
  public void createOrganizationWithEmailNullTest() {
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.create(null, PASSWORD, ORG_NAME, null, null));
  }

  @Test
  public void createOrganizationWithPasswordNullTest() throws DuplicateEmailException {
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.create(ORG_EMAIL, null, ORG_NAME, null, null));
  }

  @Test
  public void createOrganizationWithNameNullTest() throws DuplicateEmailException {
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.create(ORG_EMAIL, PASSWORD, null, null, null));
  }
}
