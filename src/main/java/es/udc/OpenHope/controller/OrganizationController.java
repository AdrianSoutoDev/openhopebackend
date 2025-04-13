package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.EditOrganizationParamsDto;
import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.OrganizationParamsDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.exception.DuplicateOrganizationException;
import es.udc.OpenHope.exception.MaxCategoriesExceededException;
import es.udc.OpenHope.service.CampaignService;
import es.udc.OpenHope.service.OrganizationService;
import es.udc.OpenHope.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organizations")
@Validated
public class OrganizationController {

  private final OrganizationService organizationService;
  private final TokenService tokenService;
  private final CampaignService campaignService;

  @Value("${server.port}")
  private String serverPort;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<OrganizationDto> register(@Valid @ModelAttribute OrganizationParamsDto params, @RequestParam(value = "file", required = false) MultipartFile file) throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(params.getEmail(), params.getPassword(), params.getName(),
        params.getDescription(), params.getCategories(), file);

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(organizationDto.getId())
        .toUri();

    return ResponseEntity.created(location).body(organizationDto);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrganizationDto> getOrganization(@PathVariable long id) {
    OrganizationDto organizationDto = organizationService.getById(id);
    return ResponseEntity.ok(organizationDto);
  }

  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<OrganizationDto> updateOrganization(@Valid @ModelAttribute EditOrganizationParamsDto params,
       @RequestParam(value = "file", required = false) MultipartFile file, @RequestHeader(name="Authorization") String token) throws DuplicateOrganizationException, MaxCategoriesExceededException, IOException {

    String owner = tokenService.extractsubject(token);
    OrganizationDto organizationDto = organizationService.update(params.getId(), params.getName(), params.getDescription(),
        params.getCategories(), file, owner);

    return ResponseEntity.ok(organizationDto);
  }

  @GetMapping(value = "/{id}/campaigns")
  public ResponseEntity<Page<CampaignDto>> getCampaigns(@PathVariable long id, @RequestParam(defaultValue="0") int page,
                                                        @RequestParam(defaultValue="10") int size) {

    Page<CampaignDto> campaigns = campaignService.getByOrganization(id, page, size);
    return ResponseEntity.ok(campaigns);
  }
}
