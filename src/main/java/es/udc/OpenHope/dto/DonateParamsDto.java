package es.udc.OpenHope.dto;

import es.udc.OpenHope.enums.Provider;
import lombok.Data;

@Data
public class DonateParamsDto {
  private Provider provider;
  private Long campaignId;
  private Long bankAccountId;
  private Float amount;
}
