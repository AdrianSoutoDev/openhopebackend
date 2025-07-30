package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.ConsentDto;
import es.udc.OpenHope.model.Consent;

public class ConsentMapper {
  public static ConsentDto toConsentDto(Consent consent){
    ConsentDto consentDto = new ConsentDto();
    consentDto.setConsentId(consent.getConsentId());
    return consentDto;
  }
}
