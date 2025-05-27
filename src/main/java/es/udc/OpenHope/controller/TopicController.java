package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.TopicDto;
import es.udc.OpenHope.dto.TopicsResponseDto;
import es.udc.OpenHope.service.TokenService;
import es.udc.OpenHope.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/topics")
public class TopicController {

  private final TopicService topicService;
  private final TokenService tokenService;

  @GetMapping
  public ResponseEntity<TopicsResponseDto> getTopics(@RequestParam(required = true) Long organization,
                                                     @RequestHeader(name="Authorization") String token) {

    String owner = tokenService.extractsubject(token);
    TopicsResponseDto topicsResponseDto = new TopicsResponseDto();
    topicsResponseDto.setTopics(new ArrayList<>());

    if(organization != null) {
      List<TopicDto> topicDtos = topicService.getFromOrganization(organization, owner);
      topicsResponseDto.setTopics(topicDtos);
    }

    return ResponseEntity.ok(topicsResponseDto);
  }
}
