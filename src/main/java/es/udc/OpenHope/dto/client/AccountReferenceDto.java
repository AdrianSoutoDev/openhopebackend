package es.udc.OpenHope.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountReferenceDto {
  private String iban;
  private String bban;
  private String msisdn;
  private String currency;
}
