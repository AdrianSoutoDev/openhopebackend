package es.udc.OpenHope.dto;

import es.udc.OpenHope.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateDto {
  private Long id;
  private String email;
  private AccountType accountType;
}
