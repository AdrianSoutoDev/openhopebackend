package es.udc.OpenHope.service.providers;

import es.udc.OpenHope.dto.AspspDto;
import es.udc.OpenHope.exception.ProviderException;

import java.util.List;

public interface ProviderService {

  enum Provider {
    REDSSYS
  }

  List<AspspDto> getAspsps() throws ProviderException;
}
