package es.udc.OpenHope.service.providers;

import es.udc.OpenHope.dto.AccountDto;
import es.udc.OpenHope.dto.AspspDto;
import es.udc.OpenHope.dto.ProviderAuthDto;
import es.udc.OpenHope.dto.client.CredentialsDto;
import es.udc.OpenHope.dto.client.PostConsentClientDto;
import es.udc.OpenHope.exception.ProviderException;
import es.udc.OpenHope.exception.UnauthorizedException;

import java.util.List;

public interface ProviderService {
  List<AspspDto> getAspsps() throws ProviderException;
  ProviderAuthDto getOAuthUri(String aspsp, Integer campaign) throws ProviderException;
  CredentialsDto authorize(String code, String aspsp) throws ProviderException;
  List<AccountDto> getAccounts(String aspsp, String tokenOAuth, String token, String ipClient, String consentId) throws ProviderException, UnauthorizedException;
  PostConsentClientDto createConsent(String owner, String aspsp, String tokenOAuth, String ipClient, String campaignId) throws ProviderException, UnauthorizedException;
  CredentialsDto refreshToken(String refreshToken, String aspsps) throws ProviderException, UnauthorizedException;
}
