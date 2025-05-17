package es.udc.OpenHope.service.providers;

import es.udc.OpenHope.dto.AspspDto;
import es.udc.OpenHope.dto.ProviderAuthDto;
import es.udc.OpenHope.dto.client.CredentialsDto;
import es.udc.OpenHope.exception.ProviderException;

import java.util.List;

public interface ProviderService {
  List<AspspDto> getAspsps() throws ProviderException;
  ProviderAuthDto getOAuthUri(String aspsp, Integer campaign) throws ProviderException;
  CredentialsDto authorize(String code, String aspsp) throws ProviderException;
}
