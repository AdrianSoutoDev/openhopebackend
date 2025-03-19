package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.OrganizationParamsDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.exception.DuplicateOrganizationException;
import es.udc.OpenHope.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
  public ResponseEntity<OrganizationDto> register(@Valid @ModelAttribute OrganizationParamsDto params) throws DuplicateEmailException, DuplicateOrganizationException {
    OrganizationDto organizationDto = organizationService.create(params.getEmail(), params.getPassword(), params.getName(),
        params.getDescription(), params.getFile());

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
}
