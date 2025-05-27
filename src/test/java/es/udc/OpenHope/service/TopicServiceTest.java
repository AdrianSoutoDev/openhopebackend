package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.TopicDto;
import es.udc.OpenHope.exception.*;
import es.udc.OpenHope.model.Campaign;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.model.Topic;
import es.udc.OpenHope.repository.*;
import es.udc.OpenHope.utils.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static es.udc.OpenHope.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class TopicServiceTest {

  private final OrganizationService organizationService;
  private final OrganizationRepository organizationRepository;
  private final CampaignService campaignService;
  private final CampaignRepository campaignRepository;
  private final TopicService topicService;
  private final TopicRepository topicRepository;

  @Autowired
  public TopicServiceTest(final OrganizationService organizationService, final OrganizationRepository organizationRepository,
                          final CampaignService campaignService, final CampaignRepository campaignRepository,
                          final TopicService topicService, final TopicRepository topicRepository) {
    this.organizationService = organizationService;
    this.organizationRepository = organizationRepository;
    this.campaignService = campaignService;
    this.campaignRepository = campaignRepository;
    this.topicRepository = topicRepository;
    this.topicService = topicService;
  }

  @Test
  public void saveOrganizationTopicsTest() throws MaxTopicsExceededException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    List<String> topics = Utils.getTopics();
    Optional<Organization> organization = organizationRepository.findById(organizationDto.getId());
    topicService.saveTopics(topics, organization.get(), organizationDto.getEmail());
    List<Topic> topicsFinded = topicRepository.findByOrganization(organization.get());
    assertEquals(topicsFinded.size(), topics.size());
  }

  @Test
  public void saveOrganizationTopicsWithNoPermissionTest() throws MaxTopicsExceededException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    List<String> topics = Utils.getTopics();
    Optional<Organization> organization = organizationRepository.findById(organizationDto.getId());
    assertThrows(SecurityException.class, () ->
        topicService.saveTopics(topics, organization.get(), "another_email@openhope.com"));
  }

  @Test
  public void saveOrganizationTopicsWithExceedMaxTest() throws MaxTopicsExceededException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);

    List<String> topics = Utils.getTopics();
    topics.add("one_more_topic");

    Optional<Organization> organization = organizationRepository.findById(organizationDto.getId());
    assertThrows(MaxTopicsExceededException.class, () ->
        topicService.saveTopics(topics, organization.get(), organizationDto.getEmail()));
  }

  @Test
  public void saveCampaignTopicsTest() throws MaxTopicsExceededException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    List<String> topics = Utils.getTopics();

    Optional<Campaign> campaign = campaignRepository.findById(campaignDto.getId());
    topicService.saveTopics(topics, campaign.get(), organizationDto.getEmail());

    List<Topic> topicsFinded = topicRepository.findByCampaign(campaign.get());
    assertEquals(topicsFinded.size(), topics.size());
  }

  @Test
  public void saveCampaignTopicsWithNoPermissionTest() throws MaxTopicsExceededException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    List<String> topics = Utils.getTopics();
    Optional<Campaign> campaign = campaignRepository.findById(campaignDto.getId());

    assertThrows(SecurityException.class, () ->
        topicService.saveTopics(topics, campaign.get(), "another_email@openhope.com"));
  }

  @Test
  public void saveCampaignTopicsWithExceedMaxTest() throws MaxTopicsExceededException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException, DuplicatedCampaignException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
        CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

    List<String> topics = Utils.getTopics();
    topics.add("one_more_topic");

    Optional<Campaign> campaign = campaignRepository.findById(campaignDto.getId());
    assertThrows(MaxTopicsExceededException.class, () ->
        topicService.saveTopics(topics, campaign.get(), organizationDto.getEmail()));
  }

  @Test
  public void updateOrganizationTopicsTest() throws MaxTopicsExceededException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    List<String> topics = Utils.getTopics();
    Optional<Organization> organization = organizationRepository.findById(organizationDto.getId());
    topicService.saveTopics(topics, organization.get(), organizationDto.getEmail());

    List<String> newTopics = Utils.getAnotherTopics();
    topicService.updateTopics(newTopics, organization.get(), organizationDto.getEmail());

    List<Topic> topicsFinded = topicRepository.findByOrganization(organization.get());
    assertEquals(topicsFinded.size(), newTopics.size());
  }

  @Test
  public void updateOrganizationTopicsWithNoPermissionTest() throws MaxTopicsExceededException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    Optional<Organization> organization = organizationRepository.findById(organizationDto.getId());

    List<String> newTopics = Utils.getAnotherTopics();

    assertThrows(SecurityException.class, () ->
        topicService.updateTopics(newTopics, organization.get(), "another_email@openhope.com"));
  }

  @Test
  public void updateOrganizationTopicsWithExceedMaxTest() throws MaxTopicsExceededException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    List<String> topics = Utils.getTopics();
    Optional<Organization> organization = organizationRepository.findById(organizationDto.getId());
    topicService.saveTopics(topics, organization.get(), organizationDto.getEmail());

    List<String> newTopics = Utils.getAnotherTopics();
    newTopics.add("one_more");
    newTopics.add("two_more");

    assertThrows(MaxTopicsExceededException.class, () ->
        topicService.updateTopics(newTopics, organization.get(), organizationDto.getEmail()));
  }

  @Test
  public void getTopicsFromOrganization() throws MaxTopicsExceededException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    List<String> topics = Utils.getTopics();
    Optional<Organization> organization = organizationRepository.findById(organizationDto.getId());
    topicService.saveTopics(topics, organization.get(), organizationDto.getEmail());

    List<TopicDto> topicsFinded = topicService.getFromOrganization(organizationDto.getId(), organizationDto.getEmail());
    assertEquals(topics.size(), topicsFinded.size());
  }

  @Test
  public void getTopicsFromOrganizationTheDoesntExist() throws MaxTopicsExceededException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    List<TopicDto> topicsFinded = topicService.getFromOrganization(-1L, ORG_EMAIL);
    assertTrue(topicsFinded.isEmpty());
  }

  @Test
  public void getTopicsFromOrganizationWithNoPermission() throws MaxTopicsExceededException, DuplicateOrganizationException, DuplicateEmailException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null, null, null);
    List<String> topics = Utils.getTopics();
    Optional<Organization> organization = organizationRepository.findById(organizationDto.getId());
    topicService.saveTopics(topics, organization.get(), organizationDto.getEmail());
    List<TopicDto> topicsFinded = topicService.getFromOrganization(organizationDto.getId(), "another_email@openHope.com");
    assertTrue(topicsFinded.isEmpty());
  }
}
