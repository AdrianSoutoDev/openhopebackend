package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.ISearcheableDto;
import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.SearchParamsDto;
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
public class SearchController {

  private final OrganizationService organizationService;
  private final CampaignService campaignService;

  @PostMapping
  public ResponseEntity<Page<ISearcheableDto>> search(@RequestBody SearchParamsDto searchParamsDto,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {

    Page<ISearcheableDto> results = new PageImpl<>(Collections.emptyList());

    if(searchParamsDto.getShow() != null && searchParamsDto.getShow().equals(EntityType.ORGANIZATION)){
      results = organizationService.search(searchParamsDto, page, size)
          .map(organization -> (ISearcheableDto) organization);
    }

    if(searchParamsDto.getShow() != null && searchParamsDto.getShow().equals(EntityType.CAMPAING)){
       results = campaignService.search(searchParamsDto, page, size)
           .map(campaign -> (ISearcheableDto) campaign);
    }

    return ResponseEntity.ok(results);
  }
}
