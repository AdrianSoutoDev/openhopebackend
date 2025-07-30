package es.udc.OpenHope.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AmountDto {
  private String currency;
  private String amount;
}
