package es.udc.OpenHope.service.providers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProviderManagerImpl implements ProviderManager {

  @Qualifier("redSysProviderService")
  private final ProviderService redSysProviderService;

  @Override
  public ProviderService getProviderService(Provider provider) {
    ProviderService providerService = null;
    if(provider.equals(Provider.REDSYS)) providerService = redSysProviderService;
    return providerService;
  }
}
