package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.TopicDto;
import es.udc.OpenHope.model.Topic;

import java.util.ArrayList;
import java.util.List;

public abstract class TopicMapper {
  public static TopicDto toTopicDto(Topic topic){
    TopicDto topicDto = new TopicDto();
    topicDto.setName(topic.getName());
    return topicDto;
  }

  public static List<TopicDto> toTopicsDto(List<Topic> topic) {
    List<TopicDto> topicDtos = new ArrayList<>();

    topic.forEach(t -> {
      TopicDto topicDto = toTopicDto(t);
      topicDtos.add(topicDto);
    });

    return topicDtos;
  }
}
