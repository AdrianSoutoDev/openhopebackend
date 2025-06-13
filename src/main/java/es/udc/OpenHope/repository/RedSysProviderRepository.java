package es.udc.OpenHope.repository;

import es.udc.OpenHope.dto.CommonHeadersDto;
import es.udc.OpenHope.dto.client.AccountClientDto;
import es.udc.OpenHope.dto.client.AspspClientDto;
import es.udc.OpenHope.dto.client.CredentialsDto;
import es.udc.OpenHope.dto.client.PostConsentClientDto;
import es.udc.OpenHope.exception.ConsentInvalidException;
import es.udc.OpenHope.exception.UnauthorizedException;

import java.util.List;

public interface RedSysProviderRepository {
  List<AspspClientDto> getAspsps(CommonHeadersDto commonHeaders, String uri);
  CredentialsDto authorize(String redSysClientId, String code, String oauthCallback, String oauthCodeVerifier, String uri);
  CredentialsDto refreshToken(String redSysClientId, String refreshToken, String uri);
  PostConsentClientDto postConsent(CommonHeadersDto commonHeaders, String uri, String body, String aspsp,
                                   String PsuIpAddress, String authorization, String redirectionUri) throws UnauthorizedException;
  List<AccountClientDto> getAccounts(CommonHeadersDto commonHeaders, String uri, String consentId, String authorization) throws UnauthorizedException, ConsentInvalidException;
}
