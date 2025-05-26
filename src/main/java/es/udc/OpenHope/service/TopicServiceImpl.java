package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.TopicDto;
import es.udc.OpenHope.dto.mappers.TopicMapper;
import es.udc.OpenHope.model.Campaign;
import es.udc.OpenHope.model.Organization;
import es.udc.OpenHope.model.Topic;
import es.udc.OpenHope.repository.CampaignRepository;
import es.udc.OpenHope.repository.OrganizationRepository;
import es.udc.OpenHope.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

  private final TopicRepository topicRepository;
  private final OrganizationRepository organizationRepository;
  private final CampaignRepository campaignRepository;

  @Override
  @Transactional
  public void saveTopics(List<String> topics, Organization organization) {
    List<Topic> topicsToSave = new ArrayList<>();
    topics.forEach(t ->  topicsToSave.add(new Topic(t, organization)));
    topicRepository.saveAll(topicsToSave);
  }

  @Override
  @Transactional
  public void saveTopics(List<String> topics, Campaign campaign) {
    List<Topic> topicsToSave = new ArrayList<>();
    topics.forEach(t ->  topicsToSave.add(new Topic(t, campaign)));
    topicRepository.saveAll(topicsToSave);
  }

  @Override
  @Transactional
  public List<TopicDto> getFromOrganization(Long organizationId, String owner) {
    Optional<Organization> organization = organizationRepository.findById(organizationId);
    if(organization.isEmpty() || !organization.get().getEmail().equals(owner)){
      return List.of();
    }
    List<Topic> topics = topicRepository.findByOrganization(organization.get());
    return TopicMapper.toTopicsDto(topics);
  }

  @Override
  @Transactional
  public void updateTopics(List<String> topics, Organization organization) {
    List<Topic> topicsFinded = topicRepository.findByOrganization(organization);

    Set<String> foundTopics = topicsFinded.stream().map(Topic::getName).collect(Collectors.toSet());
    Set<String> inputTopics = new HashSet<>(topics);

    Set<String> topicsToRemove = new HashSet<>(foundTopics);
    topicsToRemove.removeAll(inputTopics);

    Set<String> topicsToAdd = new HashSet<>(inputTopics);
    topicsToAdd.removeAll(foundTopics);

    topicsToRemove.forEach(topicName -> {
      Topic topic = topicRepository.findByNameAndOrganization(topicName, organization);
      if (topic != null) {
        topicRepository.delete(topic);
      }
    });

    topicsToAdd.forEach(topicName -> {
      Topic newTopic = new Topic(topicName, organization);
      topicRepository.save(newTopic);
    });
  }
}
