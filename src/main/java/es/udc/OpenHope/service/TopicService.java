package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.TopicDto;
import es.udc.OpenHope.model.Campaign;
import es.udc.OpenHope.model.Organization;

import java.util.List;

public interface TopicService {
  void saveTopics(List<String> topics, Organization organization);
  void saveTopics(List<String> topics, Campaign campaign);
  List<TopicDto> getFromOrganization(Long organizationId, String owner);
  void updateTopics(List<String> topics, Organization organization);
}
