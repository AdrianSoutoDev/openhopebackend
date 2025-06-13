package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.LoginDto;
import es.udc.OpenHope.dto.LoginParamsDto;
import es.udc.OpenHope.dto.ValidateDto;
import es.udc.OpenHope.exception.InvalidCredentialsException;
import es.udc.OpenHope.service.AccountService;
import es.udc.OpenHope.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
@Validated
public class AccountController {

  private final AccountService accountService;
  private final TokenService tokenService;

  @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LoginDto> login(@Valid @RequestBody LoginParamsDto params) throws InvalidCredentialsException {
    String jwt = accountService.authenticate(params.getEmail(), params.getPassword());
    return ResponseEntity.ok().body(new LoginDto(jwt, params.getEmail()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/validate")
  public ResponseEntity<ValidateDto> validate(@RequestHeader(name="Authorization") String token) {
    String owner = tokenService.extractsubject(token);
    ValidateDto validateDto = new ValidateDto(owner);
    return ResponseEntity.ok().body(validateDto);
  }
}
