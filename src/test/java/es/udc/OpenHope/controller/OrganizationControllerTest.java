package es.udc.OpenHope.controller;

import com.jayway.jsonpath.JsonPath;
import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.OrganizationParamsDto;
import es.udc.OpenHope.model.Category;
import es.udc.OpenHope.repository.CategoryRepository;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrganizationControllerTest {

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

  @Value("${server.port}")
  private String serverPort;

  private String createdFileName = null;

  private final MockMvc mockMvc;
  private final ResourceService resourceService;
  private final OrganizationService organizationService;
  private final CategoryRepository categoryRepository;

  @Autowired
  public OrganizationControllerTest(final MockMvc mockMvc, final ResourceService resourceService,
                                    final OrganizationService organizationService, final CategoryRepository categoryRepository) {
    this.mockMvc = mockMvc;
    this.resourceService = resourceService;
    this.organizationService = organizationService;
    this.categoryRepository = categoryRepository;
  }

  @AfterEach
  public void cleanUp() throws IOException {
    if (createdFileName != null) {
      resourceService.removeImage(createdFileName);
    }
  }

  private void initCategories() {
    List<Category> categories = getCategories();
    categoryRepository.saveAll(categories);
  }

  private List<Category> getCategories(){
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

    String uriStarts = ServletUriComponentsBuilder
        .fromCurrentContextPath()
        .port(serverPort)
        .path("/api/resources/")
        .toUriString();

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
        .andExpect(jsonPath("$.image").value(Matchers.startsWith(uriStarts)))
        .andExpect(jsonPath("$.image").value(Matchers.endsWith(extension)))
        .andDo(r ->{
          //Get the image name to delete it when the test finilize.
          String response = r.getResponse().getContentAsString();
          String image = JsonPath.parse(response).read("$.image");
          createdFileName = image.replace(uriStarts, "");
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
    organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);

    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail(ORG_EMAIL);
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName("Asociación protectora APADAN");

    ResultActions result = registerOrganization(organizationParamsDto);
    result.andExpect(status().isConflict());
  }

  @Test
  void registerOrganizationWithDuplicatedNameTest() throws Exception {
    organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);

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
    ResultActions result = mockMvc.perform(get("/api/organizations/{id}",  0));
    result.andExpect(status().isNotFound());
  }
}
