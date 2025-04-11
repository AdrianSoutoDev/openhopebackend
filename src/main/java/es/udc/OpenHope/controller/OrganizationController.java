package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.OrganizationParamsDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.exception.DuplicateOrganizationException;
import es.udc.OpenHope.exception.MaxCategoriesExceededException;
import es.udc.OpenHope.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organizations")
@Validated
public class OrganizationController {

  private final OrganizationService organizationService;

  @Value("${server.port}")
  private String serverPort;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<OrganizationDto> register(@Valid @ModelAttribute OrganizationParamsDto params, @RequestParam(value = "file", required = false) MultipartFile file) throws DuplicateEmailException, DuplicateOrganizationException, MaxCategoriesExceededException {
    OrganizationDto organizationDto = organizationService.create(params.getEmail(), params.getPassword(), params.getName(),
        params.getDescription(), params.getCategories(), file);

    if(organizationDto.getImage() != null) {
      String imgUrl = ServletUriComponentsBuilder
              .fromCurrentContextPath()
              .port(serverPort)
              .path("/api/resources/")
              .path(organizationDto.getImage())
              .toUriString();

      organizationDto.setImage(imgUrl);
    }

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(organizationDto.getId())
        .toUri();

    return ResponseEntity.created(location).body(organizationDto);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrganizationDto> getOrganization(@PathVariable long id) {
    OrganizationDto organizationDto = organizationService.getOrganizationById(id);
    return ResponseEntity.ok(organizationDto);
  }

}
