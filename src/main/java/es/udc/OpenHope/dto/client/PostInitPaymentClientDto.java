package es.udc.OpenHope.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostInitPaymentClientDto {
  private String transactionStatus;
  private String paymentId;
}
