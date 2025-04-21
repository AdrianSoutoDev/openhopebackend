package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.service.CampaignService;
import es.udc.OpenHope.service.OrganizationService;
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

import static es.udc.OpenHope.utils.Constants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CampaignControllerTest {

  private final OrganizationService organizationService;
  private final CampaignService campaignService;
  private final MockMvc mockMvc;
  private final Utils utils;

  @Autowired
  public CampaignControllerTest(final OrganizationService organizationService, final CampaignService campaignService,
                                final MockMvc mockMvc, final Utils utils) {
    this.organizationService = organizationService;
    this.campaignService = campaignService;
    this.mockMvc = mockMvc;
    this.utils = utils;
  }

  //TODO testear crear campaña


  @Test
  void GetCampaignByIdTest() throws Exception {
    utils.initCategories();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null);

    List<String> categories = utils.getCategoryNames().subList(0,1);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, categories, null);

    ResultActions result = mockMvc.perform(get("/api/campaigns/{id}", campaignDto.getId()));

    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(campaignDto.getId()))
        .andExpect(jsonPath("$.name").value(CAMPAIGN_NAME))
        .andExpect(jsonPath("$.description").value(CAMPAIGN_DESCRIPTION))
        .andExpect(jsonPath("$.organization.id").value(organizationDto.getId()))
        .andExpect(jsonPath("$.startAt").value(String.valueOf(CAMPAIGN_START_AT)))
        .andExpect(jsonPath("$.dateLimit").value(String.valueOf(CAMPAIGN_DATE_LIMIT)))
        .andExpect(jsonPath("$.categories").exists())
        .andExpect(jsonPath("$.categories").isArray())
        .andExpect(jsonPath("$.categories").isNotEmpty())
        .andExpect(jsonPath("$.categories[0].name").value(categories.getFirst()))
        .andExpect(jsonPath("$.image").isEmpty())
        .andExpect(jsonPath("$.minimumDonation").isEmpty())
        .andExpect(jsonPath("$.economicTarget").isEmpty())
        .andExpect(jsonPath("$.isOnGoing").value(true))
        .andExpect(jsonPath("$.ammountCollected").value(0F))
        .andExpect(jsonPath("$.percentageCollected").value(0F));
  }

  @Test
  void GetCampaignByIdThatDoesntExistTest() throws Exception {
    ResultActions result = mockMvc.perform(get("/api/campaigns/{id}", 0));
    result.andExpect(status().isNotFound());
  }
}
