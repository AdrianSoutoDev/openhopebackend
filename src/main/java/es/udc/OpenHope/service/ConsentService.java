package es.udc.OpenHope.service;


import es.udc.OpenHope.dto.ConsentDto;

public interface ConsentService {
  ConsentDto get(String owner, String aspsp, String provider);
  void delete(String owner, String aspsp, String provider);
}
