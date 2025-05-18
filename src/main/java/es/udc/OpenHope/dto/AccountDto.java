package es.udc.OpenHope.dto;

import lombok.Data;

@Data
public class AccountDto {
  private String resourceId;
  private String iban;
  private String name;
  private String ownerName;
}
