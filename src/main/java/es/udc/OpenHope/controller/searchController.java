package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.searcher.SearchParamsDto;
import es.udc.OpenHope.dto.searcher.SearchResultDto;
import es.udc.OpenHope.enums.EntityType;
import es.udc.OpenHope.service.CampaignService;
import es.udc.OpenHope.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class searchController {

  private final OrganizationService organizationService;
  private final CampaignService campaignService;

  @PostMapping
  public ResponseEntity<Page<SearchResultDto>> search(@RequestBody SearchParamsDto searchParamsDto,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {

    Page<SearchResultDto> results = new PageImpl<>(Collections.emptyList());

    if(searchParamsDto.getShow() != null && searchParamsDto.getShow().equals(EntityType.ORGANIZATION)){
      results = organizationService.search(searchParamsDto, page, size);
    }

    if(searchParamsDto.getShow() != null && searchParamsDto.getShow().equals(EntityType.ORGANIZATION)){
       results = campaignService.search(searchParamsDto, page, size);
    }

    return ResponseEntity.ok(results);
  }
}
