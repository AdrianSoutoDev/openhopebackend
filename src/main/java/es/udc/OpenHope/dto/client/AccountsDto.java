package es.udc.OpenHope.dto.client;

import lombok.Data;

@Data
public class AccountsDto {
  private String resourceId;
  private String iban;
  private String name;
}
