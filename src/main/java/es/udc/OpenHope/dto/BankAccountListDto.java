package es.udc.OpenHope.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BankAccountListDto extends BankAccountDto {
  private boolean favorite;
}
