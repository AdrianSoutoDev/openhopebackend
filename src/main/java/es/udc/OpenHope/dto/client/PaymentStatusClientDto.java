package es.udc.OpenHope.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentStatusClientDto {
  private String transactionStatus;
}
