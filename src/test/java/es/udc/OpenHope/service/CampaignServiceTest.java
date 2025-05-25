package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.AspspParamsDto;
import es.udc.OpenHope.dto.BankAccountParams;
import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.exception.DuplicateOrganizationException;
import es.udc.OpenHope.exception.DuplicatedCampaignException;
import es.udc.OpenHope.exception.MaxCategoriesExceededException;
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
  public void createCampaignTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null,null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null);

    Optional<Campaign> campaign = campaignRepository.findById(campaignDto.getId());

    assertTrue(campaign.isPresent());
    assertEquals(CAMPAIGN_NAME, campaignDto.getName());
    assertEquals(CAMPAIGN_START_AT, campaignDto.getStartAt());
    assertEquals(CAMPAIGN_DATE_LIMIT, campaignDto.getDateLimit());
    assertEquals(organizationDto.getId(), campaignDto.getOrganization().getId());
  }

  @Test
  public void createCampaignWithOptionalInfoTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException, IOException {
    utils.initCategories();
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null);

    List<String> categories = utils.getCategoryNames().subList(0,1);
    MockMultipartFile testImage = utils.getTestImg();

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, CAMPAIGN_DESCRIPTION, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, ECONOMIC_TARGET, MINIMUM_DONATION, categories, testImage);

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
            CAMPAIGN_DATE_LIMIT, null, null, null, null));
  }

  @Test
  public void createCampaignWithIncorrectOwnerTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null);

    assertThrows(SecurityException.class, () ->
        campaignService.create(organizationDto.getId(), "another-email@openhope.com", CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
            CAMPAIGN_DATE_LIMIT, null, null, null, null));
  }

  @Test
  public void createCampaignWithNameNullTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), null, null, CAMPAIGN_START_AT,
            CAMPAIGN_DATE_LIMIT, null, null, null, null));
  }

  @Test
  public void createCampaignWithNameEmptyTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "", null, CAMPAIGN_START_AT,
            CAMPAIGN_DATE_LIMIT, null, null, null, null));
  }

  @Test
  public void createCampaignWithNameBlankTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), " ", null, CAMPAIGN_START_AT,
            CAMPAIGN_DATE_LIMIT, null, null, null, null));
  }

  @Test
  public void createCampaignWithDuplicatedNameTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null);

    campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null);

    assertThrows(DuplicatedCampaignException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
            CAMPAIGN_DATE_LIMIT, null, null, null, null));
  }

  @Test
  public void createCampaignWithStartAtNullTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, null,
            CAMPAIGN_DATE_LIMIT, null, null, null, null));
  }

  @Test
  public void createCampaignWithStartAtBeforeTodayTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null);

    LocalDate StartAtBeforeToday = LocalDate.now().minusDays(1);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, StartAtBeforeToday,
            CAMPAIGN_DATE_LIMIT, null, null, null, null));
  }

  @Test
  public void createCampaignWithouthDateLimitAndEconomicTagetTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
            null, null, null, null, null));
  }

  @Test
  public void createCampaignWithhDateLimitEqualsStartAtTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null);

    LocalDate dateLimit = CAMPAIGN_START_AT;

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
            dateLimit, null, null, null, null));
  }

  @Test
  public void createCampaignWithDateLimitBeforeStartAtTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, utils.getCategoryNames(), null);

    LocalDate startAt = CAMPAIGN_START_AT.plusDays(5);

    LocalDate dateLimit = startAt.minusDays(1);

    assertThrows(IllegalArgumentException.class, () ->
        campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, startAt,
            dateLimit, null, null, null, null));
  }

  @Test
  public void getCampaignsByOrganizationTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null,null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null);

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
  public void getCampaignTest() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null,null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null);

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
  public void updateCampaignBankAccount() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null,null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null);

    AspspParamsDto aspspParamsDto = Utils.getAspspParams();
    BankAccountParams bankAccountParams = Utils.getBankAccountParams();
    bankAccountParams.setAspsp(aspspParamsDto);

    CampaignDto campaignDtoUpdated = campaignService.updateBankAccount(campaignDto.getId(), bankAccountParams, ORG_EMAIL);
    assertTrue(campaignDtoUpdated.isHasBankAccount());
  }

  @Test
  public void updateCampaignBankAccountThatDoesntExist() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null,null, null);

    AspspParamsDto aspspParamsDto = Utils.getAspspParams();
    BankAccountParams bankAccountParams = Utils.getBankAccountParams();
    bankAccountParams.setAspsp(aspspParamsDto);

    assertThrows(NoSuchElementException.class, () ->
        campaignService.updateBankAccount(-1L, bankAccountParams, ORG_EMAIL));
  }

  @Test
  public void updateCampaignBankAccountWithNoPermission() throws DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null);

    AspspParamsDto aspspParamsDto = Utils.getAspspParams();
    BankAccountParams bankAccountParams = Utils.getBankAccountParams();
    bankAccountParams.setAspsp(aspspParamsDto);

    assertThrows(SecurityException.class, () ->
        campaignService.updateBankAccount(campaignDto.getId(), bankAccountParams, "another_email@openhope.com"));
  }
}
