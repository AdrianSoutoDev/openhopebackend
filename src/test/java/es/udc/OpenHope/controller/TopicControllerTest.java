package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.repository.OrganizationRepository;
import es.udc.OpenHope.service.OrganizationService;
import es.udc.OpenHope.service.TopicService;
import es.udc.OpenHope.utils.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static es.udc.OpenHope.utils.Constants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TopicControllerTest {

  private final OrganizationService organizationService;
  private final OrganizationRepository organizationRepository;
  private final TopicService topicService;
  private final MockMvc mockMvc;

  @Autowired
  public TopicControllerTest(final OrganizationService organizationService, final OrganizationRepository organizationRepository,
                             final TopicService topicService, final MockMvc mockMvc) {
    this.organizationService = organizationService;
    this.organizationRepository = organizationRepository;
    this.topicService = topicService;
    this.mockMvc = mockMvc;
  }

  private ResultActions getTopics(Long id, String authToken) throws Exception {
    return mockMvc.perform(get("/api/topics?organization=" + id.toString())
        .header("Authorization", "Bearer " + authToken)
        .contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  public void getTopicsFromOrganization() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    List<String> topics = Utils.getTopics();
    Optional<Organization> organization = organizationRepository.findById(organizationDto.getId());
    topicService.saveTopics(topics, organization.get(), organizationDto.getEmail());
    String authToken = organizationService.authenticate(ORG_EMAIL, PASSWORD);

    ResultActions result = getTopics(organizationDto.getId(), authToken);
    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.topics").isArray())
        .andExpect(jsonPath("$.topics").isNotEmpty());
  }

  @Test
  public void getTopicsFromOrganizationTheDoesntExist() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    String authToken = organizationService.authenticate(ORG_EMAIL, PASSWORD);

    ResultActions result = getTopics(-1L, authToken);

    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.topics").isArray())
        .andExpect(jsonPath("$.topics").isEmpty());
  }

  @Test
  public void getTopicsFromOrganizationWithNoPermission() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    OrganizationDto organizationDto2 = organizationService.create("another_email@openHope.com", PASSWORD, "another name", null, null, null, null);

    List<String> topics = Utils.getTopics();
    Optional<Organization> organization = organizationRepository.findById(organizationDto.getId());
    topicService.saveTopics(topics, organization.get(), organizationDto.getEmail());

    String authToken = organizationService.authenticate(organizationDto2.getEmail(), PASSWORD);

    ResultActions result = getTopics(organizationDto.getId(), authToken);
    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.topics").isArray())
        .andExpect(jsonPath("$.topics").isEmpty());
  }
}
