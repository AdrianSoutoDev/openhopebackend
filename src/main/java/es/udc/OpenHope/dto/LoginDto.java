package es.udc.OpenHope.dto;

import es.udc.OpenHope.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginDto {
  String token;
  Long id;
  String email;
  AccountType accountType;
}
