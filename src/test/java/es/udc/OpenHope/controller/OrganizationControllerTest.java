package es.udc.OpenHope.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.udc.OpenHope.dto.OrganizationParamsDto;
import es.udc.OpenHope.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class OrganizationControllerTest {

  private static final String ORG_EMAIL = "org@openhope.com";
  private static final String ORG_NAME = "Apadan";
  private static final String ORG_DESCRIPTION = "Asociación Protectora de Animales Domésticos Abandonados del Noroeste";
  private static final String ORG_IMAGE = "c:\\openhope\\images\\organizations\\apadan.png";
  private static final String PASSWORD = "12345abc?";

  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;
  private final OrganizationRepository organizationRepository;

  @Autowired
  public OrganizationControllerTest(final MockMvc mockMvc, final ObjectMapper objectMapper, OrganizationRepository organizationRepository) {
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
    this.organizationRepository = organizationRepository;
  }

  private ResultActions registerOrganization(OrganizationParamsDto params) throws Exception {
    String jsonContent = objectMapper.writeValueAsString(params);

    return mockMvc.perform(post("/api/organization")
        .content(jsonContent)
        .contentType(MediaType.APPLICATION_JSON));
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
        .andExpect(jsonPath("$.email").value(ORG_EMAIL))
        .andExpect(jsonPath("$.name").value(ORG_NAME))
        .andExpect(jsonPath("$.description").doesNotExist())
        .andExpect(jsonPath("$.image").doesNotExist());
  }

  @Test
  void registerOrganizationDoesNotReturnPasswordTest() throws Exception {
    OrganizationParamsDto organizationParamsDto = new OrganizationParamsDto();
    organizationParamsDto.setEmail(ORG_EMAIL);
    organizationParamsDto.setPassword(PASSWORD);
    organizationParamsDto.setName(ORG_NAME);

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


}
