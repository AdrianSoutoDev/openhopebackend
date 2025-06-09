package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.searcher.SearchParamsDto;
import es.udc.OpenHope.dto.searcher.SearchResultDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.exception.DuplicateOrganizationException;
import es.udc.OpenHope.exception.MaxCategoriesExceededException;
import es.udc.OpenHope.exception.MaxTopicsExceededException;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.model.Topic;
import es.udc.OpenHope.repository.OrganizationRepository;
import es.udc.OpenHope.repository.TopicRepository;
import es.udc.OpenHope.utils.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static es.udc.OpenHope.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class OrganizationServiceTest {

  @Value("${upload.dir}")
  private String uploadDir;

  private List<String> createdFileNames = new ArrayList<>();

  private final OrganizationRepository organizationRepository;
  private final OrganizationService organizationService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final ResourceService resourceService;
  private final Utils utils;
  private final TopicRepository topicRepository;

  @Autowired
  public OrganizationServiceTest(final OrganizationService organizationService, final OrganizationRepository organizationRepository,
                                 final BCryptPasswordEncoder bCryptPasswordEncoder, final ResourceService resourceService,
                                 final ResourceService resourceService1 , final Utils utils, final TopicRepository topicRepository) {
    this.organizationService = organizationService;
    this.organizationRepository = organizationRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.resourceService = resourceService;
    this.utils = utils;
    this.topicRepository = topicRepository;
  }

  @AfterEach
  public void cleanUp() throws IOException {
    if (createdFileNames != null && !createdFileNames.isEmpty()) {
      createdFileNames.forEach(resourceService::remove);
    }
  }

  @Test
  public void createOrganizationTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null,null, null);
    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());

    assertTrue(organizationFinded.isPresent());

    assertEquals(ORG_EMAIL, organizationFinded.get().getEmail());
    assertEquals(ORG_NAME, organizationFinded.get().getName());
    assertEquals(ORG_DESCRIPTION, organizationFinded.get().getDescription());
    assertNull(organizationFinded.get().getImage());
  }

  @Test
  public void createOrganizationWithImgTest() throws IOException, DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, MaxTopicsExceededException {
    MockMultipartFile testImage = utils.getTestImg();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, testImage);

    createdFileNames.add(organizationDto.getImage());

    Path filePath = Path.of(uploadDir, createdFileNames.getFirst());
    assertTrue(Files.exists(filePath));
  }

  @Test
  public void createOrganizationWithoutDescriptionTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null,null, null);
    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());
    assertTrue(organizationFinded.isPresent());
    assertNull(organizationFinded.get().getDescription());
  }

  @Test
  public void createOrganizationWithEncryptedPasswordTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null,null, null);
    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());
    assertTrue(organizationFinded.isPresent());
    boolean passwordsAreEquals = bCryptPasswordEncoder.matches(PASSWORD, organizationFinded.get().getEncryptedPassword());
    assertTrue(passwordsAreEquals);
  }

  @Test
  public void createOrganizationDuplicatedEmailTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null,null, null);
    assertThrows(DuplicateEmailException.class, () ->
        organizationService.create(ORG_EMAIL, "anotherPassword", "anotherName", null, null,null, null));
  }

  @Test
  public void createOrganizationDuplicatedEmailIgnoringCaseTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create("org@openhope.com", PASSWORD, ORG_NAME, null,null, null, null);
    assertThrows(DuplicateEmailException.class, () ->
        organizationService.create("ORG@OpenHope.com", "anotherPassword", "anotherName", null, null,null, null));
  }

  @Test
  public void createOrganizationsWithDiferentEmailTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, MaxTopicsExceededException {
    OrganizationDto firstOrganizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null,null, null);
    OrganizationDto secondOrganizationDto = organizationService.create("second_email@openHope.com", PASSWORD, "another org name", null, null,null, null);

    List<Organization> organizations = organizationRepository.findAll();
    assertEquals(2, organizations.size());
  }

  @Test
  public void createOrganizationDuplicatedNameTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null,null, null);
    assertThrows(DuplicateOrganizationException.class, () ->
        organizationService.create("another_email@openhope.com", PASSWORD, ORG_NAME, null, null,null, null));
  }

  @Test
  public void createOrganizationDuplicatedNameIgnoringCaseTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, "Asociación protectora APADAN", null, null,null, null);
    assertThrows(DuplicateOrganizationException.class, () ->
        organizationService.create("another_email@openhope.com", PASSWORD, "asociación protectora apadan", null, null,null, null));
  }

  @Test
  public void createOrganizationsWithDiferentNameTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, MaxTopicsExceededException {
    OrganizationDto firstOrganizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null,null, null);
    OrganizationDto secondOrganizationDto = organizationService.create("second_email@openHope.com", PASSWORD, "another org name", null, null,null, null);

    List<Organization> organizations = organizationRepository.findAll();
    assertEquals(2, organizations.size());
  }

  @Test
  public void createOrganizationWithEmailNullTest() {
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.create(null, PASSWORD, ORG_NAME, null, null,null, null));
  }

  @Test
  public void createOrganizationWithPasswordNullTest() {
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.create(ORG_EMAIL, null, ORG_NAME, null, null,null, null));
  }

  @Test
  public void createOrganizationWithNameNullTest() {
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.create(ORG_EMAIL, PASSWORD, null, null, null,null, null));
  }

  @Test
  public void createOrganizationWithCategoriesAndTopics() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException {
    utils.initCategories();

    List<String> topics = Utils.getTopics();
    List<String> categoryNames = utils.getCategoryNames();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, categoryNames, topics, null);
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
    List<String> categoryNames = utils.getCategoryNames();
    categoryNames.add(CATEGORY_4);
    assertThrows(MaxCategoriesExceededException.class, () ->
        organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, categoryNames, null, null));
  }

  @Test
  public void getByIdTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException {
    utils.initCategories();
    List<String> categoryNames = utils.getCategoryNames();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, categoryNames, null, null);
    OrganizationDto organizationDtoFinded = organizationService.get(organizationDto.getId());

    assertNotNull(organizationDtoFinded);
    assertEquals(organizationDto.getId(), organizationDtoFinded.getId());
    assertEquals(ORG_EMAIL, organizationDtoFinded.getEmail());
    assertEquals(ORG_NAME, organizationDtoFinded.getName());
    assertFalse(organizationDtoFinded.getCategories().isEmpty());
    assertEquals(3, organizationDtoFinded.getCategories().size());
  }

  @Test
  public void getByIdThatDoesntExistsTest() {
    assertThrows(NoSuchElementException.class, () ->
        organizationService.get(0L));
  }

  @Test
  public void updateWithouthChangeTheImageTest() throws IOException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException {
    MockMultipartFile testImage = utils.getTestImg();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, testImage);

    createdFileNames.add(organizationDto.getImage());
    organizationService.update(organizationDto.getId(), "New Name", "New Description",
        null, null, testImage, organizationDto.getEmail());

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
  public void updateChangingTopicsTest() throws IOException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException {
    MockMultipartFile testImage = utils.getTestImg();
    List<String> topics = Utils.getTopics();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, topics, testImage);
    createdFileNames.add(organizationDto.getImage());
    List<String> newTopics = Utils.getAnotherTopics();

    organizationService.update(organizationDto.getId(), "New Name", "New Description",
        null, newTopics, testImage, organizationDto.getEmail());

    Optional<Organization> organizationFinded = organizationRepository.findById(organizationDto.getId());

    List<Topic> topicFinded = topicRepository.findByOrganization(organizationFinded.get());

    assertEquals(topicFinded.size(), newTopics.size());
    assertTrue(topicFinded.stream().anyMatch(t -> t.getName().equals(newTopics.getFirst())));
    assertTrue(topicFinded.stream().anyMatch(t -> t.getName().equals(newTopics.get(1))));
    assertTrue(topicFinded.stream().anyMatch(t -> t.getName().equals(newTopics.get(2))));
    assertTrue(topicFinded.stream().anyMatch(t -> t.getName().equals(newTopics.getLast())));
  }

  @Test
  public void updateChangingTheImageTest() throws IOException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException {
    MockMultipartFile testImage = utils.getTestImg();
    MockMultipartFile testImage2 = utils.getTestImg("test-image-2.png");

    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, testImage);
    createdFileNames.add(organizationDto.getImage());

    OrganizationDto organizationUpdated = organizationService.update(organizationDto.getId(), "New Name", "New Description",
        null, null, testImage2, organizationDto.getEmail());
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
  public void updateWithNoPermissionTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null,null, null);
    assertThrows(SecurityException.class, () ->
        organizationService.update(organizationDto.getId(), "New Name", ORG_DESCRIPTION,
            null, null, null, "another@email.com"));
  }

  @Test
  public void updateThatNotExistTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    assertThrows(NoSuchElementException.class, () ->
        organizationService.update(0L, ORG_NAME, ORG_DESCRIPTION,
            null, null, null, ORG_EMAIL));
  }

  @Test
  public void updateWithNameNullTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null,null, null);
    assertThrows(IllegalArgumentException.class, () ->
        organizationService.update(organizationDto.getId(), null, ORG_DESCRIPTION,
            null, null, null, ORG_EMAIL));
  }

  @Test
  public void updateWithDuplicatedNameTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null,null, null);
    OrganizationDto organizationDto2 = organizationService.create("org2@openhope.com", PASSWORD, "Another name", ORG_DESCRIPTION, null,null, null);

    assertThrows(DuplicateOrganizationException.class, () ->
        organizationService.update(organizationDto2.getId(), ORG_NAME, ORG_DESCRIPTION,
            null, null, null, "org2@openhope.com"));
  }

  @Test
  public void updateWithMaxCategoriesExceededTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null,null, null);

    List<String> categories = utils.getCategoryNames();
    categories.add(CATEGORY_4);

    assertThrows(MaxCategoriesExceededException.class, () ->
        organizationService.update(organizationDto.getId(), ORG_NAME, ORG_DESCRIPTION,
            categories, null, null, ORG_EMAIL));
  }

  @Test
  public void searchOrganiztionByTextTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException {
    utils.initCategories();
    List<String> topics = Utils.getTopics();
    List<String> categoryNames = utils.getCategoryNames();

    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, categoryNames, topics, null);

    List<String> searchCategories = new ArrayList<>(List.of(CATEGORY_2));

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setCategories(searchCategories);

    Page<SearchResultDto> page = organizationService.search(searchParamsDto, 0, 3);
    assertEquals(organizationDto.getName(), page.getContent().getFirst().getName());
  }
}
