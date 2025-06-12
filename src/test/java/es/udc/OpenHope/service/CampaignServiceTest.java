package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.*;
import es.udc.OpenHope.enums.CampaignState;
import es.udc.OpenHope.exception.*;
import es.udc.OpenHope.model.Campaign;
import es.udc.OpenHope.repository.CampaignRepository;
import es.udc.OpenHope.utils.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static es.udc.OpenHope.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class CampaignServiceTest {

  private static final int PAGE_SIZE = 10;

  @Value("${upload.dir}")
  private String uploadDir;

  private List<String> createdFileNames = new ArrayList<>();

  private final OrganizationService organizationService;
  private final CampaignService campaignService;
  private final CampaignRepository campaignRepository;
  private final ResourceService resourceService;
  private final Utils utils;

  @Autowired
  public CampaignServiceTest(final OrganizationService organizationService, final CampaignService campaignService,
                             final CampaignRepository campaignRepository, final ResourceService resourceService, final Utils utils) {
    this.organizationService = organizationService;
    this.campaignService = campaignService;
    this.campaignRepository = campaignRepository;
    this.resourceService = resourceService;
    this.utils = utils;
  }

  @AfterEach
  public void cleanUp() throws IOException {
    if (createdFileNames != null && !createdFileNames.isEmpty()) {
      createdFileNames.forEach(resourceService::remove);
    }
  }

  @Test
  public void createCampaignTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null,null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    Optional<Campaign> campaign = campaignRepository.findById(campaignDto.getId());

    assertTrue(campaign.isPresent());
    assertEquals(CAMPAIGN_NAME, campaignDto.getName());
    assertEquals(CAMPAIGN_START_AT, campaignDto.getStartAt());
    assertEquals(CAMPAIGN_DATE_LIMIT, campaignDto.getDateLimit());
    assertEquals(organizationDto.getId(), campaignDto.getOrganization().getId());
  }

  @Test
  public void createCampaignWithOptionalInfoTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, IOException, MaxTopicsExceededException {
    utils.initCategories();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null, null);

    List<String> categories = utils.getCategoryNames().subList(0,1);
    MockMultipartFile testImage = utils.getTestImg();

    List<String> topics = Utils.getTopics();

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, ECONOMIC_TARGET, MINIMUM_DONATION, categories, topics, testImage);

    createdFileNames.add(campaignDto.getImage());
    Path filePath = Path.of(uploadDir, createdFileNames.getFirst());

    Optional<Campaign> campaign = campaignRepository.findById(campaignDto.getId());

    assertTrue(campaign.isPresent());
    assertEquals(campaignDto.getId(), campaign.get().getId());
    assertEquals(CAMPAIGN_NAME, campaign.get().getName());
    assertEquals(CAMPAIGN_START_AT, campaign.get().getStartAt().toLocalDate());
    assertEquals(CAMPAIGN_DATE_LIMIT, campaign.get().getDateLimit().toLocalDate());
    assertEquals(CAMPAIGN_DESCRIPTION, campaign.get().getDescription());
    assertEquals(MINIMUM_DONATION, campaign.get().getMinimumDonation());
    assertEquals(ECONOMIC_TARGET, campaign.get().getEconomicTarget());
    assertTrue(campaign.get().getCategories().stream().anyMatch(c -> categories.contains(c.getName())));
    assertEquals(organizationDto.getId(), campaign.get().getOrganization().getId());
    assertTrue(Files.exists(filePath));
  }

  @Test
  public void createCampaignForOrganizationThatDoesntExistsTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    assertThrows(NoSuchElementException.class, () ->
        campaignService.create(0L, ORG_EMAIL, CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
            CAMPAIGN_DATE_LIMIT, null, null, null, null, null));
  }

  @Test
  public void createCampaignWithIncorrectOwnerTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null, null);

    assertThrows(SecurityException.class, () ->
        campaignService.create(organizationDto.getId(), "another-email@openhope.com", CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
            CAMPAIGN_DATE_LIMIT, null, null, null, null, null));
  }

  @Test
  public void createCampaignWithNameNullTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null, null);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), null, null, CAMPAIGN_START_AT,
            CAMPAIGN_DATE_LIMIT, null, null, null, null, null));
  }

  @Test
  public void createCampaignWithNameEmptyTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null, null);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "", null, CAMPAIGN_START_AT,
            CAMPAIGN_DATE_LIMIT, null, null, null, null, null));
  }

  @Test
  public void createCampaignWithNameBlankTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null, null);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), " ", null, CAMPAIGN_START_AT,
            CAMPAIGN_DATE_LIMIT, null, null, null, null, null));
  }

  @Test
  public void createCampaignWithDuplicatedNameTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    assertThrows(DuplicatedCampaignException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
            CAMPAIGN_DATE_LIMIT, null, null, null, null, null));
  }

  @Test
  public void createCampaignWithStartAtNullTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null, null);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, null,
            CAMPAIGN_DATE_LIMIT, null, null, null, null, null));
  }

  @Test
  public void createCampaignWithStartAtBeforeTodayTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null, null);

    LocalDate StartAtBeforeToday = LocalDate.now().minusDays(1);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, StartAtBeforeToday,
            CAMPAIGN_DATE_LIMIT, null, null, null, null, null));
  }

  @Test
  public void createCampaignWithouthDateLimitAndEconomicTagetTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null, null);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
            null, null, null, null, null, null));
  }

  @Test
  public void createCampaignWithhDateLimitEqualsStartAtTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null, null);

    LocalDate dateLimit = CAMPAIGN_START_AT;

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
            dateLimit, null, null, null, null, null));
  }

  @Test
  public void createCampaignWithDateLimitBeforeStartAtTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null, null);

    LocalDate startAt = CAMPAIGN_START_AT.plusDays(5);

    LocalDate dateLimit = startAt.minusDays(1);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, startAt,
            dateLimit, null, null, null, null, null));
  }

  @Test
  public void getCampaignsByOrganizationTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null,null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    Page<CampaignDto> campaignPage = campaignService.getByOrganization(organizationDto.getId(), 0 ,PAGE_SIZE);

    assertFalse(campaignPage.isEmpty());
    assertEquals(1,  campaignPage.getTotalPages());
    assertEquals(1,  campaignPage.getTotalElements());
    assertEquals(campaignDto, campaignPage.get().toList().getFirst());
  }

  @Test
  public void getCampaignsByOrganizationThatDoesntExistTest() {
    assertThrows(NoSuchElementException.class, () ->
        campaignService.getByOrganization(0L, 0 ,PAGE_SIZE));
  }

  @Test
  public void getCampaignTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null,null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    CampaignDto campaignFinded = campaignService.get(campaignDto.getId());

    assertNotNull(campaignFinded);
    assertEquals(CAMPAIGN_NAME, campaignFinded.getName());
    assertEquals(CAMPAIGN_START_AT, campaignFinded.getStartAt());
    assertEquals(CAMPAIGN_DATE_LIMIT, campaignFinded.getDateLimit());
    assertEquals(organizationDto.getId(), campaignFinded.getOrganization().getId());
  }

  @Test
  public void getCampaignThatDoesntExistsTest() {
    assertThrows(NoSuchElementException.class, () ->
        campaignService.get(0L));
  }

  @Test
  public void updateCampaignBankAccountTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null,null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    AspspParamsDto aspspParamsDto = Utils.getAspspParams();
    BankAccountParams bankAccountParams = Utils.getBankAccountParams();
    bankAccountParams.setAspsp(aspspParamsDto);

    CampaignDto campaignDtoUpdated = campaignService.updateBankAccount(campaignDto.getId(), bankAccountParams, ORG_EMAIL);
    assertTrue(campaignDtoUpdated.isHasBankAccount());
  }

  @Test
  public void updateCampaignBankAccountThatDoesntExistTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null,null, null, null);

    AspspParamsDto aspspParamsDto = Utils.getAspspParams();
    BankAccountParams bankAccountParams = Utils.getBankAccountParams();
    bankAccountParams.setAspsp(aspspParamsDto);

    assertThrows(NoSuchElementException.class, () ->
        campaignService.updateBankAccount(-1L, bankAccountParams, ORG_EMAIL));
  }

  @Test
  public void updateCampaignBankAccountWithNoPermissionTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, DuplicatedCampaignException, MaxTopicsExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    AspspParamsDto aspspParamsDto = Utils.getAspspParams();
    BankAccountParams bankAccountParams = Utils.getBankAccountParams();
    bankAccountParams.setAspsp(aspspParamsDto);

    assertThrows(SecurityException.class, () ->
        campaignService.updateBankAccount(campaignDto.getId(), bankAccountParams, "another_email@openhope.com"));
  }

  @Test
  public void searchCampaignWithNoFiltersTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);
    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);
    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME + " - 2", null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(2, page.getTotalElements());
  }

  @Test
  public void searchCampaignNameByTextTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);
    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);
    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setText("esteriliza");

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignDescriptionByTextTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setText("animales domesticos");

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignTopicsByTextTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    List<String> topics = Utils.getTopics();

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, topics, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setText("topic3");

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByCategoryTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    utils.initCategories();
    List<String> categoryNames = utils.getCategoryNames();

    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, categoryNames, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, categoryNames, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    List<String> searchCategories = new ArrayList<>();
    searchCategories.add(CATEGORY_2);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setCategories(searchCategories);

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByCategoryWithoutMatchesTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    utils.initCategories();
    List<String> categoryNames = utils.getCategoryNames();
    categoryNames.remove(CATEGORY_2);

    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, categoryNames, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, categoryNames, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    List<String> searchCategories = new ArrayList<>();
    searchCategories.add(CATEGORY_2);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setCategories(searchCategories);

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(0, page.getTotalElements());
  }

  @Test
  public void searchCampaignByFilterByStartDateFromTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT.plusMonths(3),
        null, ECONOMIC_TARGET, null, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setStartDateFrom(LocalDate.now().plusMonths(1));

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByStartDateToTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, null, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT.plusMonths(3),
        null, ECONOMIC_TARGET, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setStartDateTo(LocalDate.now().plusMonths(1));

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByStartDateBetweenTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT.plusMonths(1),
        null, ECONOMIC_TARGET, null, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT.plusMonths(3),
        null, ECONOMIC_TARGET, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setStartDateFrom(LocalDate.now());
    searchParamsDto.setStartDateTo(LocalDate.now().plusMonths(2));

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByStateFinalizedTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, null, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, null, null, null, null);

    Optional<Campaign> campaign = campaignRepository.findById(campaignDto.getId());
    campaign.get().setFinalizedDate(Date.valueOf(LocalDate.now()));
    campaignRepository.save(campaign.get());

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setCampaignState(CampaignState.FINALIZED);

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByStateOnGoingTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, null, null, null, null);

    CampaignDto campaignDto2 = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, null, null, null, null);

    Optional<Campaign> campaign = campaignRepository.findById(campaignDto2.getId());
    campaign.get().setFinalizedDate(Date.valueOf(LocalDate.now()));
    campaignRepository.save(campaign.get());

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setCampaignState(CampaignState.ONGOING);

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByHasMinimumDonationTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, 0.5F, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setHasMinimumDonation(true);

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByFinalizeDateFromTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, 0.5F, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT.minusDays(15), null, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setFinalizeDateFrom(CAMPAIGN_DATE_LIMIT.minusDays(10));

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByFinalizeDateToTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT.minusDays(15), null, 0.5F, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setFinalizeDateTo(CAMPAIGN_DATE_LIMIT.minusDays(10));

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByFinalizeDateBetweenTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT.minusDays(15), null, 0.5F, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setFinalizeDateFrom(CAMPAIGN_DATE_LIMIT.minusDays(20));
    searchParamsDto.setFinalizeDateTo(CAMPAIGN_DATE_LIMIT.minusDays(10));

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByEconomicTargetFromTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        null, 3000L, 0.5F, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        null, 2500L, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setEconomicTargetFrom(2700L);

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByEconomicTargetToTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        null, 2500L, 0.5F, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        null, 3000L, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setEconomicTargetTo(2700L);

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByEconomicTargetBetweenTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        null, 2500L, 0.5F, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        null, 3000L, null, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setEconomicTargetFrom(2400L);
    searchParamsDto.setEconomicTargetTo(2700L);

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByMinimumDonationFromTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, 5F, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, 1F, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setMinimumDonationFrom(3L);

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByMinimumDonationToTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, 1F, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, 5F, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setMinimumDonationTo(3L);

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

  @Test
  public void searchCampaignByFilterByMinimumDonationBetweenTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, MaxTopicsExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, 3F, null, null, null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "Another campaign", "Another description", CAMPAIGN_START_AT,
        null, ECONOMIC_TARGET, 5F, null, null, null);

    SearchParamsDto searchParamsDto = new SearchParamsDto();
    searchParamsDto.setMinimumDonationFrom(2L);
    searchParamsDto.setMinimumDonationTo(4L);

    Page<CampaignDto> page = campaignService.search(searchParamsDto, 0, 5);

    assertEquals(1, page.getTotalElements());
    assertEquals(campaignDto.getId(), page.getContent().getFirst().getId());
  }

}
