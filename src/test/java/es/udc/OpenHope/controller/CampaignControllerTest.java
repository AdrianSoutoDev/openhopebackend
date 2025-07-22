package es.udc.OpenHope.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.udc.OpenHope.dto.*;
import es.udc.OpenHope.service.CampaignService;
import es.udc.OpenHope.service.OrganizationService;
import es.udc.OpenHope.utils.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

import static es.udc.OpenHope.utils.Constants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CampaignControllerTest {

  private final OrganizationService organizationService;
  private final CampaignService campaignService;
  private final MockMvc mockMvc;
  private final Utils utils;
  private final ObjectMapper objectMapper;

  @Autowired
  public CampaignControllerTest(final OrganizationService organizationService, final CampaignService campaignService,
                                final MockMvc mockMvc, final Utils utils, final ObjectMapper objectMapper) {
    this.organizationService = organizationService;
    this.campaignService = campaignService;
    this.mockMvc = mockMvc;
    this.utils = utils;
    this.objectMapper = objectMapper;
  }

  private ResultActions createCampaign(CampaignParamsDto params, String authToken) throws Exception {
    return createCampaign(params, null, authToken);
  }

  private ResultActions createCampaign(CampaignParamsDto params, MultipartFile file, String authToken) throws Exception {
    MockHttpServletRequestBuilder builder = file != null
        ? MockMvcRequestBuilders.multipart("/api/campaigns").file((MockMultipartFile) file)
        : MockMvcRequestBuilders.multipart("/api/campaigns");

    builder.param("name", params.getName())
        .param("organizationId", String.valueOf(params.getOrganizationId()))
        .param("startAt", String.valueOf(params.getStartAt()))
        .param("dateLimit", params.getDateLimit() == null ? "" : String.valueOf(params.getDateLimit()))
        .param("economicTarget", params.getEconomicTarget() == null ? "" : String.valueOf(params.getEconomicTarget()))
        .param("minimumDonation", params.getMinimumDonation() == null ? "" : String.valueOf(params.getMinimumDonation()))
        .param("description", params.getDescription())
        .param("categories", params.getCategories() != null ? String.join(",", params.getCategories()) : "")
        .param("topics", params.getTopics() != null ? String.join(",", params.getTopics()) : "")
        .header("Authorization", "Bearer " + authToken)
        .contentType(MediaType.MULTIPART_FORM_DATA);

    return mockMvc.perform(builder);
  }

  private ResultActions updateCampaign(Long id, String authToken) throws Exception {
    AspspParamsDto aspspParamsDto = Utils.getAspspParams();
    BankAccountParams bankAccountParams = Utils.getBankAccountParams();
    bankAccountParams.setAspsp(aspspParamsDto);

    String jsonContent = objectMapper.writeValueAsString(bankAccountParams);

    return mockMvc.perform(put("/api/campaigns/" + id.toString())
        .header("Authorization", "Bearer " + authToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonContent));
  }

  @Test
  public void createCampaignTest() throws Exception {
    utils.initCategories();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null, null);
    LoginDto loginDto =  organizationService.authenticate(ORG_EMAIL, PASSWORD);
    List<String> categories = utils.getCategoryNames().subList(0,1);

    CampaignParamsDto campaignParamsDto = new CampaignParamsDto();
    campaignParamsDto.setOrganizationId(organizationDto.getId());
    campaignParamsDto.setName(CAMPAIGN_NAME);
    campaignParamsDto.setDescription(CAMPAIGN_DESCRIPTION);
    campaignParamsDto.setStartAt(CAMPAIGN_START_AT);
    campaignParamsDto.setDateLimit(CAMPAIGN_DATE_LIMIT);
    campaignParamsDto.setCategories(categories);
    campaignParamsDto.setTopics(Utils.getTopics());

    ResultActions result = createCampaign(campaignParamsDto, loginDto.getToken());

    result.andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").exists())
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
        .andExpect(jsonPath("$.amountCollected").value(0F))
        .andExpect(jsonPath("$.percentageCollected").value(0F));
  }

  @Test
  public void createCampaignForOrganizationThatDoesntExistsTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    LoginDto loginDto =  organizationService.authenticate(ORG_EMAIL, PASSWORD);

    CampaignParamsDto campaignParamsDto = new CampaignParamsDto();
    campaignParamsDto.setOrganizationId(0L);
    campaignParamsDto.setName(CAMPAIGN_NAME);
    campaignParamsDto.setDescription(CAMPAIGN_DESCRIPTION);
    campaignParamsDto.setStartAt(CAMPAIGN_START_AT);
    campaignParamsDto.setDateLimit(CAMPAIGN_DATE_LIMIT);

    ResultActions result = createCampaign(campaignParamsDto, loginDto.getToken());
    result.andExpect(status().isNotFound());
  }

  @Test
  public void createCampaignWithIncorrectOwnerTest() throws Exception {
    organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    OrganizationDto organizationDto2 = organizationService.create("another_email@openhope.com", PASSWORD, "another_name", null, null, null, null);

    LoginDto loginDto =  organizationService.authenticate(ORG_EMAIL, PASSWORD);

    CampaignParamsDto campaignParamsDto = new CampaignParamsDto();
    campaignParamsDto.setOrganizationId(organizationDto2.getId());
    campaignParamsDto.setName(CAMPAIGN_NAME);
    campaignParamsDto.setDescription(CAMPAIGN_DESCRIPTION);
    campaignParamsDto.setStartAt(CAMPAIGN_START_AT);
    campaignParamsDto.setDateLimit(CAMPAIGN_DATE_LIMIT);

    ResultActions result = createCampaign(campaignParamsDto, loginDto.getToken());
    result.andExpect(status().isForbidden());
  }

  @Test
  public void createCampaignWithNameNullTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    LoginDto loginDto =  organizationService.authenticate(ORG_EMAIL, PASSWORD);

    CampaignParamsDto campaignParamsDto = new CampaignParamsDto();
    campaignParamsDto.setOrganizationId(organizationDto.getId());
    campaignParamsDto.setName(null);
    campaignParamsDto.setDescription(CAMPAIGN_DESCRIPTION);
    campaignParamsDto.setStartAt(CAMPAIGN_START_AT);
    campaignParamsDto.setDateLimit(CAMPAIGN_DATE_LIMIT);

    ResultActions result = createCampaign(campaignParamsDto, loginDto.getToken());
    result.andExpect(status().isBadRequest());
  }

  @Test
  public void createCampaignWithDuplicatedNameTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    LoginDto loginDto =  organizationService.authenticate(ORG_EMAIL, PASSWORD);

    CampaignParamsDto campaignParamsDto = new CampaignParamsDto();
    campaignParamsDto.setOrganizationId(organizationDto.getId());
    campaignParamsDto.setName(CAMPAIGN_NAME);
    campaignParamsDto.setDescription(CAMPAIGN_DESCRIPTION);
    campaignParamsDto.setStartAt(CAMPAIGN_START_AT);
    campaignParamsDto.setDateLimit(CAMPAIGN_DATE_LIMIT);

    ResultActions result = createCampaign(campaignParamsDto, loginDto.getToken());
    result.andExpect(status().isConflict());
  }


  @Test
  public void createCampaignWithStartAtNullTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);

    LoginDto loginDto =  organizationService.authenticate(ORG_EMAIL, PASSWORD);

    CampaignParamsDto campaignParamsDto = new CampaignParamsDto();
    campaignParamsDto.setOrganizationId(organizationDto.getId());
    campaignParamsDto.setName(CAMPAIGN_NAME);
    campaignParamsDto.setDescription(CAMPAIGN_DESCRIPTION);
    campaignParamsDto.setStartAt(null);
    campaignParamsDto.setDateLimit(CAMPAIGN_DATE_LIMIT);

    ResultActions result = createCampaign(campaignParamsDto, loginDto.getToken());
    result.andExpect(status().isBadRequest());
  }

  @Test
  public void createCampaignWithStartAtBeforeTodayTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);

    LoginDto loginDto =  organizationService.authenticate(ORG_EMAIL, PASSWORD);

    LocalDate StartAtBeforeToday = LocalDate.now().minusDays(1);

    CampaignParamsDto campaignParamsDto = new CampaignParamsDto();
    campaignParamsDto.setOrganizationId(organizationDto.getId());
    campaignParamsDto.setName(CAMPAIGN_NAME);
    campaignParamsDto.setDescription(CAMPAIGN_DESCRIPTION);
    campaignParamsDto.setStartAt(StartAtBeforeToday);
    campaignParamsDto.setDateLimit(CAMPAIGN_DATE_LIMIT);

    ResultActions result = createCampaign(campaignParamsDto, loginDto.getToken());
    result.andExpect(status().isBadRequest());
  }

  @Test
  public void createCampaignWithouthDateLimitAndEconomicTargetTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);

    LoginDto loginDto =  organizationService.authenticate(ORG_EMAIL, PASSWORD);

    CampaignParamsDto campaignParamsDto = new CampaignParamsDto();
    campaignParamsDto.setOrganizationId(organizationDto.getId());
    campaignParamsDto.setName(CAMPAIGN_NAME);
    campaignParamsDto.setDescription(CAMPAIGN_DESCRIPTION);
    campaignParamsDto.setStartAt(CAMPAIGN_START_AT);
    campaignParamsDto.setDateLimit(null);
    campaignParamsDto.setEconomicTarget(null);

    ResultActions result = createCampaign(campaignParamsDto, loginDto.getToken());
    result.andExpect(status().isBadRequest());
  }

  @Test
  public void createCampaignWithhDateLimitEqualsStartAtTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);

    LoginDto loginDto =  organizationService.authenticate(ORG_EMAIL, PASSWORD);

    LocalDate dateLimit = CAMPAIGN_START_AT;

    CampaignParamsDto campaignParamsDto = new CampaignParamsDto();
    campaignParamsDto.setOrganizationId(organizationDto.getId());
    campaignParamsDto.setName(CAMPAIGN_NAME);
    campaignParamsDto.setDescription(CAMPAIGN_DESCRIPTION);
    campaignParamsDto.setStartAt(CAMPAIGN_START_AT);
    campaignParamsDto.setDateLimit(dateLimit);

    ResultActions result = createCampaign(campaignParamsDto, loginDto.getToken());
    result.andExpect(status().isBadRequest());
  }

  @Test
  public void createCampaignWithDateLimitBeforeStartAtTest() throws Exception{
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);

    LoginDto loginDto = organizationService.authenticate(ORG_EMAIL, PASSWORD);

    LocalDate startAt = CAMPAIGN_START_AT.plusDays(5);
    LocalDate dateLimit = startAt.minusDays(1);

    CampaignParamsDto campaignParamsDto = new CampaignParamsDto();
    campaignParamsDto.setOrganizationId(organizationDto.getId());
    campaignParamsDto.setName(CAMPAIGN_NAME);
    campaignParamsDto.setDescription(CAMPAIGN_DESCRIPTION);
    campaignParamsDto.setStartAt(startAt);
    campaignParamsDto.setDateLimit(dateLimit);

    ResultActions result = createCampaign(campaignParamsDto, loginDto.getToken());
    result.andExpect(status().isBadRequest());
  }

  @Test
  void GetCampaignByIdTest() throws Exception {
    utils.initCategories();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null, null);

    List<String> categories = utils.getCategoryNames().subList(0,1);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, categories, null, null);

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
        .andExpect(jsonPath("$.amountCollected").value(0F))
        .andExpect(jsonPath("$.percentageCollected").value(0F));
  }

  @Test
  void GetCampaignByIdThatDoesntExistTest() throws Exception {
    ResultActions result = mockMvc.perform(get("/api/campaigns/{id}", 0));
    result.andExpect(status().isNotFound());
  }

  @Test
  public void updateCampaignBankAccountTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null,null, null, null);
    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    LoginDto loginDto =  organizationService.authenticate(ORG_EMAIL, PASSWORD);
    ResultActions result = updateCampaign(campaignDto.getId(), loginDto.getToken());

    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.hasBankAccount").value(true));
  }

  @Test
  public void updateCampaignBankAccountThatDoesntExistTest() throws Exception {
    organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null,null, null, null);
    LoginDto loginDto =  organizationService.authenticate(ORG_EMAIL, PASSWORD);
    ResultActions result = updateCampaign(-1L, loginDto.getToken());
    result.andExpect(status().isNotFound());
  }

  @Test
  public void updateCampaignBankAccountWithNoPermissionTest() throws Exception {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    organizationService.create("another_email@openhope.com", PASSWORD, "another org name", null, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    LoginDto loginDto =  organizationService.authenticate("another_email@openhope.com", PASSWORD);

    ResultActions result = updateCampaign(campaignDto.getId(), loginDto.getToken());
    result.andExpect(status().isForbidden());
  }
}
