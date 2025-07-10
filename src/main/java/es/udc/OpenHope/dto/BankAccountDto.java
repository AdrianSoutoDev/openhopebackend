package es.udc.OpenHope.dto;

import lombok.Data;

@Data
public class BankAccountDto {
  private Long id;
  private String resourceId;
  private String iban;
  private String name;
  private String ownerName;
  private String originalName;
  private String bban;
  private String msisdn;
  private String currency;
  private AspspDto aspsp;
}
