package es.udc.OpenHope.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.SearchParamsDto;
import es.udc.OpenHope.enums.EntityType;
import es.udc.OpenHope.service.CampaignService;
import es.udc.OpenHope.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static es.udc.OpenHope.utils.Constants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SearchControllerTest {

  private final OrganizationService organizationService;
  private final CampaignService campaignService;
  private final MockMvc mockMvc;
  private final ObjectMapper objectMapper;

  @Autowired
  public SearchControllerTest(final OrganizationService organizationService, final CampaignService campaignService,
                              final MockMvc mockMvc, ObjectMapper objectMapper) {
    this.organizationService = organizationService;
    this.campaignService = campaignService;
    this.mockMvc = mockMvc;
    this.objectMapper = objectMapper;
  }

  @Test
  public void searchEmptyTest() throws Exception {
    SearchParamsDto searchParamsDto = new SearchParamsDto();
    String jsonContent = objectMapper.writeValueAsString(searchParamsDto);

    ResultActions result = mockMvc.perform(post("/api/search")
        .content(jsonContent)
        .contentType(MediaType.APPLICATION_JSON));

    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isEmpty());
  }

  @Test
  public void searchOrganizationsTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    OrganizationDto organizationDto2 = organizationService.create("another_email@openHope.com", PASSWORD, "z-another name", null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setShow(EntityType.ORGANIZATION);

    String jsonContent = objectMapper.writeValueAsString(searchParamsDto);

    ResultActions result = mockMvc.perform(post("/api/search")
        .content(jsonContent)
        .contentType(MediaType.APPLICATION_JSON));

    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isNotEmpty())
        .andExpect(jsonPath("$.content[0].id").value(organizationDto.getId()))
        .andExpect(jsonPath("$.content[1].id").value(organizationDto2.getId()));
  }

  @Test
  public void searchCampaignsTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, null, null, null, null);

    CampaignDto campaignDto2 = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "z-Another campaign", "Another description", CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setShow(EntityType.CAMPAIGN);

    String jsonContent = objectMapper.writeValueAsString(searchParamsDto);
    ResultActions result = mockMvc.perform(post("/api/search")
        .content(jsonContent)
        .contentType(MediaType.APPLICATION_JSON));

    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isNotEmpty())
        .andExpect(jsonPath("$.content[0].id").value(campaignDto.getId()))
        .andExpect(jsonPath("$.content[1].id").value(campaignDto2.getId()));
  }
}
