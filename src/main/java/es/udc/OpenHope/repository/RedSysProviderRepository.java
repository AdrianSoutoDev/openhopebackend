package es.udc.OpenHope.repository;

import es.udc.OpenHope.dto.CommonHeadersDto;
import es.udc.OpenHope.dto.client.AccountClientDto;
import es.udc.OpenHope.dto.client.AspspClientDto;
import es.udc.OpenHope.dto.client.PostConsentClientDto;

import java.util.List;

public interface RedSysProviderRepository {
  List<AspspClientDto> getAspsps(CommonHeadersDto commonHeaders, String uri);
  PostConsentClientDto postConsent(CommonHeadersDto commonHeaders, String uri, String body, String aspsp,
                                   String PsuIpAddress, String authorization, String redirectionUri);
  List<AccountClientDto> getAccounts(CommonHeadersDto commonHeaders, String uri, String consentId, String authorization);
}
