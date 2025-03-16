package es.udc.OpenHope.controller;

import com.jayway.jsonpath.JsonPath;
import es.udc.OpenHope.dto.OrganizationParamsDto;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;

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

  @Value("${upload.dir}")
  private String uploadDir;

  @Value("${server.port}")
  private String serverPort;

  private String createdFileName = null;

  private final MockMvc mockMvc;
  private final ResourceService resourceService;
  private final OrganizationService organizationService;

  @Autowired
  public OrganizationControllerTest(final MockMvc mockMvc, final ResourceService resourceService, OrganizationService organizationService) {
    this.mockMvc = mockMvc;
    this.resourceService = resourceService;
    this.organizationService = organizationService;
  }

  @AfterEach
  public void cleanUp() throws IOException {
    if (createdFileName != null) {
      resourceService.removeImage(createdFileName);
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

  private ResultActions registerOrganization(OrganizationParamsDto params) throws Exception {
    MockHttpServletRequestBuilder builder = params.getFile() != null
        ? MockMvcRequestBuilders.multipart("/api/organizations").file((MockMultipartFile) params.getFile())
        : MockMvcRequestBuilders.multipart("/api/organizations");

    builder.param("email", params.getEmail())
        .param("password", params.getPassword())
        .param("name", params.getName())
        .param("description", params.getDescription())
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
    organizationParamsDto.setFile(testImage);

    String uriStarts = ServletUriComponentsBuilder
        .fromCurrentContextPath()
        .port(serverPort)
        .path("/api/resources/")
        .toUriString();

    String imageName = testImage.getOriginalFilename();
    String extension = imageName.substring(imageName.lastIndexOf("."));

    ResultActions result = registerOrganization(organizationParamsDto);
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
}
