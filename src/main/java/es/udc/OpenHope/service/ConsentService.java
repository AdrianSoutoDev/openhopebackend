package es.udc.OpenHope.service;


import es.udc.OpenHope.dto.ConsentDto;

public interface ConsentService {
  ConsentDto getConsent(String owner, String aspsp, String provider);
}
