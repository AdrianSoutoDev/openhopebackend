package es.udc.OpenHope.dto;

import lombok.Data;

@Data
public class BankAccountDto {
  private String resourceId;
  private String iban;
  private String name;
  private String ownerName;
  private String originalName;
}
