package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.TopicDto;
import es.udc.OpenHope.dto.TopicsResponseDto;
import es.udc.OpenHope.dto.UserDto;
import es.udc.OpenHope.dto.UserParamsDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Validated
public class UserController {

  private final UserService userService;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserDto> register(@Valid @RequestBody UserParamsDto params) throws DuplicateEmailException {
    UserDto userDto = userService.create(params.getEmail(), params.getPassword());

    URI location = ServletUriComponentsBuilder
        .fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(userDto.getId())
        .toUri();

    return ResponseEntity.created(location).body(userDto);
  }
}
