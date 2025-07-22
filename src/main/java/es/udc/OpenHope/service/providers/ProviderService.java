package es.udc.OpenHope.service.providers;

import es.udc.OpenHope.dto.*;
import es.udc.OpenHope.dto.client.CredentialsDto;
import es.udc.OpenHope.dto.client.PostConsentClientDto;
import es.udc.OpenHope.exception.*;

import java.util.List;

public interface ProviderService {
  List<AspspDto> getAspsps() throws ProviderException;
  ProviderAuthDto getOAuthUri(String aspsp, Integer campaign, Integer userId, Boolean isDonation, Float amount, Long bankAccount) throws ProviderException;
  CredentialsDto authorize(String code, String aspsp) throws ProviderException;
  List<BankAccountDto> getAccounts(String aspsp, String tokenOAuth, String ipClient, String consentId) throws ProviderException, UnauthorizedException, ConsentInvalidException;
  PostConsentClientDto createConsent(String owner, String aspsp, String tokenOAuth, String ipClient, Integer campaignId, Integer userId) throws ProviderException, UnauthorizedException;
  CredentialsDto refreshToken(String refreshToken, String aspsp) throws ProviderException, UnauthorizedException;
  InitPaymentDto initPayment(String tokenOAuth, String ipClient, Long bankAccountId, String owner, Long campaignId, Float amount)
      throws ProviderException, UnauthorizedException, MissingBankAccountException, CampaignFinalizedException;
}
