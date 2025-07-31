package es.udc.OpenHope.service.providers;

import es.udc.OpenHope.enums.Provider;

public interface ProviderManager {
  ProviderService getProviderService(Provider provider);
}
