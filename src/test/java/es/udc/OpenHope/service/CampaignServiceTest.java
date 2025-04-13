package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.exception.DuplicateOrganizationException;
import es.udc.OpenHope.exception.DuplicatedCampaignException;
import es.udc.OpenHope.exception.MaxCategoriesExceededException;
import es.udc.OpenHope.model.Campaign;
import es.udc.OpenHope.repository.CampaignRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class CampaignServiceTest {

  private static final String ORG_EMAIL = "org@openhope.com";
  private static final String ORG_NAME = "Apadan";
  private static final String PASSWORD = "12345abc?";

  private static final String CAMPAIGN_NAME = "Campaña de esterilización";
  private static final LocalDate CAMPAIGN_START_AT = LocalDate.now();
  private static final LocalDate CAMPAIGN_DATE_LIMIT = LocalDate.now().plusMonths(1);

  private static final int PAGE_SIZE = 10;

  private final OrganizationService organizationService;
  private final CampaignService campaignService;
  private final CampaignRepository campaignRepository;

  @Autowired
  public CampaignServiceTest(final OrganizationService organizationService, final CampaignService campaignService,
                             final CampaignRepository campaignRepository) {
    this.organizationService = organizationService;
    this.campaignService = campaignService;
    this.campaignRepository = campaignRepository;
  }

  @Test
  public void createCampaignTest() throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null,null, null);

    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null);

    Optional<Campaign> campaign = campaignRepository.findById(campaignDto.getId());

    assertTrue(campaign.isPresent());
    assertEquals(CAMPAIGN_NAME, campaignDto.getName());
    assertTrue(campaignDto.getStartAt().isEqual(CAMPAIGN_START_AT));
    assertTrue(campaignDto.getDateLimit().isEqual(CAMPAIGN_DATE_LIMIT));
    assertEquals(organizationDto.getId(), campaignDto.getOrganization().getId());
  }

  //TODO testear resto de posibles casos de creación de campaña

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

  //TODO testear resto de casos de get Campaigns by organization
}
