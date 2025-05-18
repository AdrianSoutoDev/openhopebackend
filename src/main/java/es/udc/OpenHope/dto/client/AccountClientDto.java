package es.udc.OpenHope.dto.client;

import lombok.Data;

@Data
public class AccountClientDto {
  private String resourceId;
  private String iban;
  private String name;
  private String ownerName;
}
