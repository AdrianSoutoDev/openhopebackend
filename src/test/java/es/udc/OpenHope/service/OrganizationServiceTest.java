package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.exception.DuplicateOrganizationException;
import es.udc.OpenHope.exception.MaxCategoriesExceededException;
import es.udc.OpenHope.model.Category;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.repository.CategoryRepository;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class OrganizationServiceTest {

  private static final String ORG_EMAIL = "org@openhope.com";
  private static final String ORG_NAME = "Apadan";
  private static final String ORG_DESCRIPTION = "Asociación Protectora de Animales Domésticos Abandonados del Noroeste";
  private static final String PASSWORD = "12345abc?";

  private static final String CATEGORY_1 = "CATEGORY 1";
  private static final String CATEGORY_2 = "CATEGORY 2";
  private static final String CATEGORY_3 = "CATEGORY 3";
  private static final String CATEGORY_4 = "CATEGORY 4";

  @Value("${upload.dir}")
  private String uploadDir;

  private List<String> createdFileNames = new ArrayList<>();

  private final OrganizationRepository organizationRepository;
  private final OrganizationService organizationService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final ResourceService resourceService;
  private final CategoryRepository categoryRepository;

  @Autowired
  public OrganizationServiceTest(final OrganizationService organizationService, final OrganizationRepository organizationRepository,
                                 final BCryptPasswordEncoder bCryptPasswordEncoder, final ResourceService resourceService,
                                 final ResourceService resourceService1, final CategoryRepository categoryRepository) {
    this.organizationService = organizationService;
    this.organizationRepository = organizationRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.resourceService = resourceService1;
    this.categoryRepository = categoryRepository;
  }

  @AfterEach
  public void cleanUp() throws IOException {
    if (createdFileNames != null && !createdFileNames.isEmpty()) {
      createdFileNames.forEach(file -> resourceService.removeImage(file));
    }
  }

  private void initCategories() {
    List<String> categoryNames = new ArrayList<>(Arrays.asList(CATEGORY_1, CATEGORY_2, CATEGORY_3));
    List<Category> categories = getCategories(categoryNames);
    categoryRepository.saveAll(categories);
  }

  private List<Category> getCategories(List<String> categoryNames){
    List<Category> categories = new ArrayList<>();
    categoryNames.forEach(c -> categories.add(new Category(c)));
    return categories;
  }

  private List<String> getCategoryNames() {
    return new ArrayList<>(Arrays.asList(CATEGORY_1, CATEGORY_2, CATEGORY_3));
  }

  private MockMultipartFile getTestImg(String fileName) throws IOException {
    ClassPathResource resource = new ClassPathResource("test-images/" + fileName);
    byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());
    return new MockMultipartFile(
            "file",
            "test-image.png",
            "image/png",
            fileContent
    );
  }

  private MockMultipartFile getTestImg() throws IOException {
    return getTestImg("test-image.png");
  }

  @Test
  public void createOrganizationTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null);
    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());

    assertTrue(organizationFinded.isPresent());

    assertEquals(ORG_EMAIL, organizationFinded.get().getEmail());
    assertEquals(ORG_NAME, organizationFinded.get().getName());
    assertEquals(ORG_DESCRIPTION, organizationFinded.get().getDescription());
    assertNull(organizationFinded.get().getImage());
  }

  @Test
  public void createOrganizationWithImgTest() throws IOException, DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException {
    MockMultipartFile testImage = getTestImg();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, testImage);

    createdFileNames.add(organizationDto.getImage());

    Path filePath = Path.of(uploadDir, createdFileNames.get(0));
    assertTrue(Files.exists(filePath));
  }

  @Test
  public void createOrganizationWithoutDescriptionTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);
    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());
    assertTrue(organizationFinded.isPresent());
    assertNull(organizationFinded.get().getDescription());
  }

  @Test
  public void createOrganizationWithEncryptedPasswordTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);
    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());
    assertTrue(organizationFinded.isPresent());
    boolean passwordsAreEquals = bCryptPasswordEncoder.matches(PASSWORD, organizationFinded.get().getEncryptedPassword());
    assertTrue(passwordsAreEquals);
  }

  @Test
  public void createOrganizationDuplicatedEmailTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);
    assertThrows(DuplicateEmailException.class, () ->
        organizationService.create(ORG_EMAIL, "anotherPassword", "anotherName", null, null));
  }

  @Test
  public void createOrganizationDuplicatedEmailIgnoringCaseTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create("org@openhope.com", PASSWORD, ORG_NAME, null, null);
    assertThrows(DuplicateEmailException.class, () ->
        organizationService.create("ORG@OpenHope.com", "anotherPassword", "anotherName", null, null));
  }

  @Test
  public void createOrganizationsWithDiferentEmailTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException {
    OrganizationDto firstOrganizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);
    OrganizationDto secondOrganizationDto = organizationService.create("second_email@openHope.com", PASSWORD, "another org name", null, null);

    List<Organization> organizations = organizationRepository.findAll();
    assertEquals(2, organizations.size());
  }

  @Test
  public void createOrganizationDuplicatedNameTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);
    assertThrows(DuplicateOrganizationException.class, () ->
        organizationService.create("another_email@openhope.com", PASSWORD, ORG_NAME, null, null));
  }

  @Test
  public void createOrganizationDuplicatedNameIgnoringCaseTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, "Asociación protectora APADAN", null, null);
    assertThrows(DuplicateOrganizationException.class, () ->
        organizationService.create("another_email@openhope.com", PASSWORD, "asociación protectora apadan", null, null));
  }

  @Test
  public void createOrganizationsWithDiferentNameTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException {
    OrganizationDto firstOrganizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);
    OrganizationDto secondOrganizationDto = organizationService.create("second_email@openHope.com", PASSWORD, "another org name", null, null);

    List<Organization> organizations = organizationRepository.findAll();
    assertEquals(2, organizations.size());
  }

  @Test
  public void createOrganizationWithEmailNullTest() {
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.create(null, PASSWORD, ORG_NAME, null, null));
  }

  @Test
  public void createOrganizationWithPasswordNullTest() {
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.create(ORG_EMAIL, null, ORG_NAME, null, null));
  }

  @Test
  public void createOrganizationWithNameNullTest() {
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.create(ORG_EMAIL, PASSWORD, null, null, null));
  }

  @Test
  public void createOrganizationWithCategories() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    initCategories();

    List<String> categoryNames = getCategoryNames();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, categoryNames, null);
    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());

    assertFalse(organizationDto.getCategories().isEmpty());
    assertEquals(3, organizationDto.getCategories().size());

    assertTrue(organizationDto.getCategories().stream().anyMatch(c -> c.getName().equals(CATEGORY_1)));
    assertTrue(organizationDto.getCategories().stream().anyMatch(c -> c.getName().equals(CATEGORY_2)));
    assertTrue(organizationDto.getCategories().stream().anyMatch(c -> c.getName().equals(CATEGORY_3)));

    assertTrue(organizationFinded.isPresent());
    assertFalse(organizationFinded.get().getCategories().isEmpty());
    assertEquals(3, organizationFinded.get().getCategories().size());
    assertTrue(organizationFinded.get().getCategories().stream().anyMatch(c -> c.getName().equals(CATEGORY_1)));
    assertTrue(organizationFinded.get().getCategories().stream().anyMatch(c -> c.getName().equals(CATEGORY_2)));
    assertTrue(organizationFinded.get().getCategories().stream().anyMatch(c -> c.getName().equals(CATEGORY_3)));
  }

  @Test
  public void createOrganizationWithMaxCategoriesExceeded() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    List<String> categoryNames = getCategoryNames();
    categoryNames.add(CATEGORY_4);
    assertThrows(MaxCategoriesExceededException.class, () ->
        organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, categoryNames, null));
  }

  @Test
  public void getOrganizationByIdTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    initCategories();
    List<String> categoryNames = getCategoryNames();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, categoryNames, null);
    OrganizationDto organizationDtoFinded = organizationService.getOrganizationById(organizationDto.getId());

    assertNotNull(organizationDtoFinded);
    assertEquals(organizationDto.getId(), organizationDtoFinded.getId());
    assertEquals(ORG_EMAIL, organizationDtoFinded.getEmail());
    assertEquals(ORG_NAME, organizationDtoFinded.getName());
    assertFalse(organizationDtoFinded.getCategories().isEmpty());
    assertEquals(3, organizationDtoFinded.getCategories().size());
  }

  @Test
  public void getOrganizationByIdThatDoesntExisteTest() {
    assertThrows(NoSuchElementException.class, () ->
        organizationService.getOrganizationById(0L));
  }

  @Test
  public void updateOrganizationWithouthChangeTheImageTest() throws IOException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    MockMultipartFile testImage = getTestImg();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, testImage);

    createdFileNames.add(organizationDto.getImage());

    organizationService.updateOrganization(organizationDto.getId(), "New Name", "New Description",
        null, testImage, organizationDto.getEmail());

    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());

    Path direcotryPath = Path.of(uploadDir);
    long imageCount = Files.list(direcotryPath)
        .filter(path -> {
          String fileName = path.toString().toLowerCase();
          return fileName.endsWith(".png");
        })
        .count();

    assertTrue(organizationFinded.isPresent());
    assertEquals(1, imageCount);
    assertEquals("New Name", organizationFinded.get().getName());
    assertEquals("New Description", organizationFinded.get().getDescription());
  }

  @Test
  public void updateOrganizationChangingTheImageTest() throws IOException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    MockMultipartFile testImage = getTestImg();
    MockMultipartFile testImage2 = getTestImg("test-image-2.png");

    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, testImage);
    createdFileNames.add(organizationDto.getImage());

    OrganizationDto organizationUpdated = organizationService.updateOrganization(organizationDto.getId(), "New Name", "New Description",
        null, testImage2, organizationDto.getEmail());
    createdFileNames.add(organizationUpdated.getImage());

    Path direcotryPath = Path.of(uploadDir);
    long imageCount = Files.list(direcotryPath)
        .filter(path -> {
          String fileName = path.toString().toLowerCase();
          return fileName.endsWith(".png");
        })
        .count();

    assertEquals(1, imageCount);
  }

  @Test
  public void updateOrganizationWithNoPermissionTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null);
    assertThrows(SecurityException.class, () ->
        organizationService.updateOrganization(organizationDto.getId(), "New Name", ORG_DESCRIPTION,
            null, null, "another@email.com"));
  }

  @Test
  public void updateOrganizationThatNotExistTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    assertThrows(NoSuchElementException.class, () ->
        organizationService.updateOrganization(0L, ORG_NAME, ORG_DESCRIPTION,
            null, null, ORG_EMAIL));
  }

  @Test
  public void updateOrganizationWithNameNullTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null);
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.updateOrganization(organizationDto.getId(), null, ORG_DESCRIPTION,
            null, null, ORG_EMAIL));
  }

  @Test
  public void updateOrganizationWithDuplicatedNameTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null);
    OrganizationDto organizationDto2 = organizationService.create("org2@openhope.com", PASSWORD, "Another name", ORG_DESCRIPTION, null);

    assertThrows(DuplicateOrganizationException.class, () ->
        organizationService.updateOrganization(organizationDto2.getId(), ORG_NAME, ORG_DESCRIPTION,
            null, null, "org2@openhope.com"));
  }

  @Test
  public void updateOrganizationWithMaxCategoriesExceededTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null);

    List<String> categories = getCategoryNames();
    categories.add(CATEGORY_4);

    assertThrows(MaxCategoriesExceededException.class, () ->
        organizationService.updateOrganization(organizationDto.getId(), ORG_NAME, ORG_DESCRIPTION,
            categories, null, ORG_EMAIL));
  }
}
