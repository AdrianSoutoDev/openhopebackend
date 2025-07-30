package es.udc.OpenHope.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TopicsResponseDto {
  private List<TopicDto> topics;
}
