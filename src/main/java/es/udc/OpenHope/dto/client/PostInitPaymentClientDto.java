package es.udc.OpenHope.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostInitPaymentClientDto {
  private String transactionStatus; //p.e. "RCVD"
  private String paymentId; //p.e. "1b3ab8e8-0fd5-43d2-946e-d75958b172e7"
  private LinksDto _links;
}
