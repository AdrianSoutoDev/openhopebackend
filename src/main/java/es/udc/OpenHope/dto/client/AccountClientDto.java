package es.udc.OpenHope.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountClientDto {
  private String resourceId;
  private String iban;
  private String name;
  private String ownerName;
}
