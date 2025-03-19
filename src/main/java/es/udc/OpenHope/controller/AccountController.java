package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.LoginParamsDto;
import es.udc.OpenHope.exception.InvalidCredentialsException;
import es.udc.OpenHope.service.AccountService;
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

  @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> login(@Valid @RequestBody LoginParamsDto params) throws InvalidCredentialsException {
    String jwt = accountService.authenticate(params.getEmail(), params.getPassword());
    return ResponseEntity.ok().body(jwt);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().build();
  }
}
