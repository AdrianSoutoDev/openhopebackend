package es.udc.OpenHope.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddressDto {
  private String country;
  private String townName;
}
