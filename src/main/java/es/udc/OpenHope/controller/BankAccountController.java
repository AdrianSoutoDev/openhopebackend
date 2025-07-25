package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.BankAccountListDto;
import es.udc.OpenHope.service.TokenService;
import es.udc.OpenHope.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bank-accounts")
@Validated
public class BankAccountController {

  private final TokenService tokenService;
  private final UserService userService;

  @GetMapping()
  public ResponseEntity<Page<BankAccountListDto>> getBankAccounts(@RequestHeader(name="Authorization") String token,
                                                                  @RequestParam(defaultValue="0") int page,
                                                                  @RequestParam(defaultValue="5") int size) {
    String owner = tokenService.extractsubject(token);
    Page<BankAccountListDto> bankAccounts = userService.getBankAccounts(owner, page, size);

    return ResponseEntity.ok(bankAccounts);
  }

  @GetMapping("/all")
  public ResponseEntity<List<BankAccountListDto>> getAllBankAccounts(@RequestHeader(name="Authorization") String token) {
    String owner = tokenService.extractsubject(token);
    List<BankAccountListDto> bankAccounts = userService.getAllBankAccounts(owner);

    return ResponseEntity.ok(bankAccounts);
  }
}
