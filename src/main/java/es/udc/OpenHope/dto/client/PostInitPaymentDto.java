package es.udc.OpenHope.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostInitPaymentDto {
  private AccountReferenceDto debtorAccount;
  private AmountDto instructedAmount;
  private AccountReferenceDto creditorAccount;
  private String creditorName;
  private String remittanceInformationUnstructured;
}
