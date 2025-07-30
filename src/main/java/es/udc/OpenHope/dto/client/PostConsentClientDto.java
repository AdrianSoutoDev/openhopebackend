package es.udc.OpenHope.dto.client;

import lombok.Data;

@Data
public class PostConsentClientDto {
  private String consentStatus;
  private String consentId;
  private LinksDto _links;
}
