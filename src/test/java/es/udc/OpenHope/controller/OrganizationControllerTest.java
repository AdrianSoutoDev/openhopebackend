package es.udc.OpenHope.controller;

import com.jayway.jsonpath.JsonPath;
import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.EditOrganizationParamsDto;
import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.OrganizationParamsDto;
import es.udc.OpenHope.model.Category;
import es.udc.OpenHope.repository.CategoryRepository;
import es.udc.OpenHope.service.CampaignService;
import es.udc.OpenHope.service.OrganizationService;
import es.udc.OpenHope.service.ResourceService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static es.udc.OpenHope.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrganizationControllerTest {

  @Value("${upload.dir}")
  private String uploadDir;

  @Value("${server.port}")
  private String serverPort;

  private String createdFileName = null;

  private final MockMvc mockMvc;
  private final ResourceService resourceService;
  private final OrganizationService organizationService;
  private final CategoryRepository categoryRepository;
  private final CampaignService campaignService;

  @Autowired
  public OrganizationControllerTest(final MockMvc mockMvc, final ResourceService resourceService,
                                    final OrganizationService organizationService, final CategoryRepository categoryRepository,
                                    final CampaignService campaignService) {
    this.mockMvc = mockMvc;
    this.resourceService = resourceService;
    this.organizationService = organizationService;
    this.categoryRepository = categoryRepository;
    this.campaignService = campaignService;
  }

  @AfterEach
  public void cleanUp() throws IOException {
    if (createdFileName != null) {
      resourceService.remove(createdFileName);
    }
  }

  private void initCategories() {
    List<Category> categories = getCategories();
    categoryRepository.saveAll(categories);
  }

  private List<Category> getCategories() {
    List<Category> categories = new ArrayList<>();
    getCategoryNames().forEach(c -> categories.add(new Category(c)));
    return categories;
  }

  private List<String> getCategoryNames() {
    return new ArrayList<>(Arrays.asList(CATEGORY_1, CATEGORY_2, CATEGORY_3));
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

  private ResultActions registerOrganization(OrganizationParamsDto params) throws Exception {
    return registerOrganization(params, null);
  }

  private ResultActions registerOrganization(OrganizationParamsDto params, MultipartFile file) throws Exception {
    MockHttpServletRequestBuilder builder = file != null
        ? MockMvcRequestBuilders.multipart("/api/organizations").file((MockMultipartFile) file)
        : MockMvcRequestBuilders.multipart("/api/organizations");

    builder.param("email", params.getEmail())
        .param("password", params.getPassword())
        .param("name", params.getName())
        .param("description", params.getDescription())
        .param("categories", String.valueOf(params.getCategories()))
        .contentType(MediaType.MULTIPART_FORM_DATA);

    return mockMvc.perform(builder);
  }

  private ResultActions updateOrganization(EditOrganizationParamsDto params, String authToken) throws Exception {
    return updateOrganization(params, null, authToken);
  }

  private ResultActions updateOrganization(EditOrganizationParamsDto params, MultipartFile file, String authToken) throws Exception {
    MockHttpServletRequestBuilder builder = file != null
        ? MockMvcRequestBuilders.multipart("/api/organizations").file((MockMultipartFile) file).with(request -> {
      request.setMethod("PUT");
      return request;
    })
        : MockMvcRequestBuilders.multipart("/api/organizations").with(request -> {
      request.setMethod("PUT");
      return request;
    });

    builder.param("id", String.valueOf(params.getId()))
        .param("name", params.getName())
        .param("description", params.getDescription())
        .param("categories", String.valueOf(params.getCategories()))
        .header("Authorization", "Bearer " + authToken)
        .contentType(MediaType.MULTIPART_FORM_DATA);

    return mockMvc.perform(builder);
  }

  @Test
  void registerOrganizationResponseWithCorrectDataTest() throws Exception {
    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail(ORG_EMAIL);
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName(ORG_NAME);

    ResultActions result = registerOrganization(organizationParamsDto);
    result.andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.email").value(ORG_EMAIL))
        .andExpect(jsonPath("$.name").value(ORG_NAME))
        .andExpect(jsonPath("$.description").doesNotExist())
        .andExpect(jsonPath("$.image").doesNotExist());
  }

  @Test
  void registerOrganizationResponseWithImageTest() throws Exception {
    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail(ORG_EMAIL);
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName(ORG_NAME);

    MockMultipartFile testImage = getTestImg();

    String imageName = testImage.getOriginalFilename();
    String extension = imageName.substring(imageName.lastIndexOf("."));

    ResultActions result = registerOrganization(organizationParamsDto, testImage);
    result.andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value(ORG_EMAIL))
        .andExpect(jsonPath("$.name").value(ORG_NAME))
        .andExpect(jsonPath("$.description").doesNotExist())
        .andExpect(jsonPath("$.image").exists())
        .andExpect(jsonPath("$.image").isString())
        .andExpect(jsonPath("$.image").value(Matchers.endsWith(extension)))
        .andDo(r -> {
          //Get the image name to delete it when the test finilize.
          String response = r.getResponse().getContentAsString();
          createdFileName = JsonPath.parse(response).read("$.image");
        });
  }

  @Test
  void registerOrganizationDoesNotReturnPasswordTest() throws Exception {
    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail(ORG_EMAIL);
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName(ORG_NAME);
    organizationParamsDto.setDescription(ORG_DESCRIPTION);

    ResultActions result = registerOrganization(organizationParamsDto);
    result.andExpect(status().isCreated())
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  @Test
  void registerOrganizationWithEmailNullTest() throws Exception {
    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName(ORG_NAME);

    ResultActions result = registerOrganization(organizationParamsDto);
    result.andExpect(status().isBadRequest());
  }

  @Test
  void registerOrganizationWithEmailEmptyTest() throws Exception {
    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail("");
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName(ORG_NAME);

    ResultActions result = registerOrganization(organizationParamsDto);
    result.andExpect(status().isBadRequest());
  }

  @Test
  void registerOrganizationWithBadFormedEmailWithoutAtSymbolTest() throws Exception {
    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail("email_OpenHope.com");
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName(ORG_NAME);

    ResultActions result = registerOrganization(organizationParamsDto);
    result.andExpect(status().isBadRequest());
  }

  @Test
  void registerOrganizationWithBadFormedEmailWithoutTldTest() throws Exception {
    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail("email@OpenHopecom");
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName(ORG_NAME);

    ResultActions result = registerOrganization(organizationParamsDto);
    result.andExpect(status().isBadRequest());
  }

  @Test
  void registerOrganizationWithDuplicatedEmailTest() throws Exception {
    organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null,null);

    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail(ORG_EMAIL);
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName("Asociación protectora APADAN");

    ResultActions result = registerOrganization(organizationParamsDto);
    result.andExpect(status().isConflict());
  }

  @Test
  void registerOrganizationWithDuplicatedNameTest() throws Exception {
    organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null,null);

    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail("another_email@openhope.com");
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName(ORG_NAME);

    ResultActions result = registerOrganization(organizationParamsDto);
    result.andExpect(status().isConflict());
  }

  @Test
  void registerOrganizationWithNameEmptyTest() throws Exception {
    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail(ORG_EMAIL);
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName("");

    ResultActions result = registerOrganization(organizationParamsDto);
    result.andExpect(status().isBadRequest());
  }

  @Test
  void registerOrganizationWithNameNullTest() throws Exception {
    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail(ORG_EMAIL);
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName(null);

    ResultActions result = registerOrganization(organizationParamsDto);
    result.andExpect(status().isBadRequest());
  }

  @Test
  void registerOrganizationWithCategoriesTest() throws Exception {
    initCategories();

    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail(ORG_EMAIL);
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName(ORG_NAME);
    organizationParamsDto.setCategories(getCategoryNames());

    ResultActions result = registerOrganization(organizationParamsDto);
    result.andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.categories").exists())
        .andExpect(jsonPath("$.categories").isArray())
        .andExpect(jsonPath("$.categories").isNotEmpty());
  }

  @Test
  void registerOrganizationWithMoreThanThreeCategoriesTest() throws Exception {
    initCategories();

    List<String> categories = getCategoryNames();
    categories.add(CATEGORY_4);

    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail(ORG_EMAIL);
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName(ORG_NAME);
    organizationParamsDto.setCategories(categories);

    ResultActions result = registerOrganization(organizationParamsDto);
    result.andExpect(status().isBadRequest());
  }

  @Test
  void GetOrganizationByIdTest() throws Exception {
    initCategories();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, getCategoryNames(), null);
    ResultActions result = mockMvc.perform(get("/api/organizations/{id}", organizationDto.getId()));
    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value(ORG_EMAIL))
        .andExpect(jsonPath("$.name").value(ORG_NAME))
        .andExpect(jsonPath("$.password").doesNotExist())
        .andExpect(jsonPath("$.categories").exists())
        .andExpect(jsonPath("$.categories").isArray())
        .andExpect(jsonPath("$.categories").isNotEmpty());
  }

  @Test
  void GetOrganizationByIdThatDoesntExistTest() throws Exception {
    ResultActions result = mockMvc.perform(get("/api/organizations/{id}", 0));
    result.andExpect(status().isNotFound());
  }

  @Test
  void UpdateOrganizationTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null);
    String authToken = organizationService.authenticate(ORG_EMAIL, PASSWORD);

    EditOrganizationParamsDto editOrganizationParamsDto = new EditOrganizationParamsDto();
    editOrganizationParamsDto.setId(organizationDto.getId());
    editOrganizationParamsDto.setName("New Name");
    editOrganizationParamsDto.setDescription("New Description");

    ResultActions result = updateOrganization(editOrganizationParamsDto, authToken);

    OrganizationDto organizationFinded = organizationService.getById(organizationDto.getId());

    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(organizationDto.getId()))
        .andExpect(jsonPath("$.email").value(ORG_EMAIL))
        .andExpect(jsonPath("$.name").value("New Name"))
        .andExpect(jsonPath("$.description").value("New Description"))
        .andExpect(jsonPath("$.password").doesNotExist());

    assertEquals("New Name", organizationFinded.getName());
    assertEquals("New Description", organizationFinded.getDescription());
    assertEquals(organizationDto.getId(), organizationFinded.getId());
  }

  @Test
  public void updateOrganizationWithNoPermissionTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null);
    OrganizationDto organizationDto2 = organizationService.create("org2@openhope.com", PASSWORD, "another Name", null, null, null);

    String authToken = organizationService.authenticate("org2@openhope.com", PASSWORD);

    EditOrganizationParamsDto editOrganizationParamsDto = new EditOrganizationParamsDto();
    editOrganizationParamsDto.setId(organizationDto.getId());
    editOrganizationParamsDto.setName("New Name");

    ResultActions result = updateOrganization(editOrganizationParamsDto, authToken);
    result.andExpect(status().isForbidden());
  }

  @Test
  public void updateOrganizationThatNotExistTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null);
    String authToken = organizationService.authenticate(ORG_EMAIL, PASSWORD);

    EditOrganizationParamsDto editOrganizationParamsDto = new EditOrganizationParamsDto();
    editOrganizationParamsDto.setId(0L);
    editOrganizationParamsDto.setName("New Name");

    ResultActions result = updateOrganization(editOrganizationParamsDto, authToken);
    result.andExpect(status().isNotFound());
  }

  @Test
  public void updateOrganizationWithNameNullTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null);
    String authToken = organizationService.authenticate(ORG_EMAIL, PASSWORD);

    EditOrganizationParamsDto editOrganizationParamsDto = new EditOrganizationParamsDto();
    editOrganizationParamsDto.setId(organizationDto.getId());
    editOrganizationParamsDto.setName(null);

    ResultActions result = updateOrganization(editOrganizationParamsDto, authToken);
    result.andExpect(status().isBadRequest());
  }

  @Test
  public void updateOrganizationWithDuplicatedNameTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null);
    OrganizationDto organizationDto2 = organizationService.create("org2@openhope.com", PASSWORD, "another Name", null, null, null);

    String authToken = organizationService.authenticate(ORG_EMAIL, PASSWORD);

    EditOrganizationParamsDto editOrganizationParamsDto = new EditOrganizationParamsDto();
    editOrganizationParamsDto.setId(organizationDto.getId());
    editOrganizationParamsDto.setName("another Name");

    ResultActions result = updateOrganization(editOrganizationParamsDto, authToken);
    result.andExpect(status().isConflict());
  }

  @Test
  public void updateOrganizationWithMaxCategoriesExceededTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null);
    String authToken = organizationService.authenticate(ORG_EMAIL, PASSWORD);

    List<String> categories = getCategoryNames();
    categories.add(CATEGORY_4);

    EditOrganizationParamsDto editOrganizationParamsDto = new EditOrganizationParamsDto();
    editOrganizationParamsDto.setId(organizationDto.getId());
    editOrganizationParamsDto.setCategories(categories);

    ResultActions result = updateOrganization(editOrganizationParamsDto, authToken);
    result.andExpect(status().isBadRequest());
  }

  @Test
  public void getCampaignsByOrganization() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null);
    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null);

    ResultActions result = mockMvc.perform(get("/api/organizations/{id}/campaigns", organizationDto.getId())
        .param("page", String.valueOf(0))
        .param("size", String.valueOf(10)));

    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isNotEmpty())
        .andExpect(jsonPath("$.content[0].id").value(campaignDto.getId()))
        .andExpect(jsonPath("$.content[0].name").value(CAMPAIGN_NAME))
        .andExpect(jsonPath("$.content[0].startAt").value(String.valueOf(CAMPAIGN_START_AT)))
        .andExpect(jsonPath("$.content[0].dateLimit").value(String.valueOf(CAMPAIGN_DATE_LIMIT)));
  }

  @Test
  public void getCampaignsByOrganizationThatDoesntExistTest() throws Exception {
    ResultActions result = mockMvc.perform(get("/api/organizations/{id}/campaigns", 0L)
        .param("page", String.valueOf(0))
        .param("size", String.valueOf(10)));

    result.andExpect(status().isNotFound());
  }
}
