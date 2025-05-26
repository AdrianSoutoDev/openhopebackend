package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.TopicDto;
import es.udc.OpenHope.service.CampaignService;
import es.udc.OpenHope.service.OrganizationService;
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
  public ResponseEntity<List<TopicDto>> getTopics(@RequestParam(required = true) Long organization,
                                                  @RequestHeader(name="Authorization") String token) {

    String owner = tokenService.extractsubject(token);
    List<TopicDto> topicDtos = new ArrayList<>();
    if(organization != null) {
      topicDtos = topicService.getFromOrganization(organization, owner);
    }

    return ResponseEntity.ok(topicDtos);
  }
}
