package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.*;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.service.TokenService;
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
  private final TokenService tokenService;

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

  @PostMapping("/bank-account")
  public ResponseEntity<BankAccountDto> addBankAccount(@RequestHeader(name="Authorization") String token,
                                                       @RequestBody BankAccountParams bankAccountParams) {

    String owner = tokenService.extractsubject(token);
    BankAccountDto bankAccountDto = userService.addBankAccount(owner, bankAccountParams);

    return ResponseEntity.ok(bankAccountDto);
  }

  @PutMapping("/bank-account")
  public ResponseEntity<UserDto> updateBankAccount(@RequestHeader(name="Authorization") String token,
                                                       @RequestBody BankAccountParams bankAccountParams) {

    String owner = tokenService.extractsubject(token);
    UserDto userDto = userService.updateFavoriteAccount(owner, bankAccountParams);

    return ResponseEntity.ok(userDto);
  }
}
