package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.CampaignParamsDto;
import es.udc.OpenHope.exception.DuplicatedCampaignException;
import es.udc.OpenHope.service.CampaignService;
import es.udc.OpenHope.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/campaigns")
@Validated
public class CampaignController {

  private final CampaignService campaignService;
  private final TokenService tokenService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<CampaignDto> createCampaign(@Valid @ModelAttribute CampaignParamsDto params,
         @RequestParam(value = "file", required = false) MultipartFile file,  @RequestHeader(name="Authorization") String token) throws DuplicatedCampaignException {

    String owner = tokenService.extractsubject(token);
    CampaignDto campaignDto = campaignService.create(params.getOrganizationId(), owner, params.getName(),
        params.getDescription(), params.getStartAt(), params.getDateLimit(), params.getEconomicTarget(), params.getMinimumDonation(),
        params.getCategories(), file);

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(campaignDto.getId())
        .toUri();

    return ResponseEntity.created(location).body(campaignDto);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CampaignDto> getCampaign(@PathVariable long id) {
    CampaignDto campaignDto = campaignService.get(id);
    return ResponseEntity.ok(campaignDto);
  }
}
