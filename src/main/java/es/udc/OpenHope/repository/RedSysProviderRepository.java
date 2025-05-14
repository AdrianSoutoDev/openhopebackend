package es.udc.OpenHope.repository;

import es.udc.OpenHope.dto.client.AspspClientDto;

import java.util.List;

public interface RedSysProviderRepository {
  List<AspspClientDto> getAspsps(String digest, String signature, String certificate, String xRequestID, String uri, String clientId);
}
