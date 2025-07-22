package es.udc.OpenHope.dto;

import es.udc.OpenHope.enums.AccountType;
import lombok.Data;

@Data
public class UserAccountDto {
  private Long id;
  private String email;
  private AccountType accountType;
}
