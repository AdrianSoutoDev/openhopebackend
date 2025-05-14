package es.udc.OpenHope.service.providers;

import es.udc.OpenHope.dto.AspspDto;
import es.udc.OpenHope.exception.ProviderException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@AllArgsConstructor
public class ProviderServiceImpl implements ProviderService {

  @Qualifier("redSysProviderService")
  private final ProviderService redSysProviderService;

  @Override
  public List<AspspDto> getAspsps() throws ProviderException {
    return redSysProviderService.getAspsps();
  }
}
