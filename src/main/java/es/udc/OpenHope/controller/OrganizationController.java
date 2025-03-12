package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.OrganizationDto;
import es.udc.OpenHope.dto.OrganizationParamsDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organization")
@Validated
public class OrganizationController {

  private final OrganizationService organizationService;

  @PostMapping
  public ResponseEntity<OrganizationDto> register(@Valid @RequestBody OrganizationParamsDto params) throws DuplicateEmailException, IOException {
    OrganizationDto organizationDto = organizationService.create(params.getEmail(), params.getPassword(), params.getName(),
        params.getDescription(), params.getImage());

    if(organizationDto.getImage() != null) {
      String imgUrl = ServletUriComponentsBuilder
              .fromCurrentContextPath()
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
