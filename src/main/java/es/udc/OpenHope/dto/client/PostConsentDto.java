package es.udc.OpenHope.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostConsentDto {
  private AccessDto access;
  private boolean recurringIndicator;
  private String validUntil;
  private Integer frequencyPerDay;
  private boolean combinedServiceIndicator;
}
