package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.*;
import es.udc.OpenHope.dto.client.CredentialsDto;
import es.udc.OpenHope.dto.client.PostConsentClientDto;
import es.udc.OpenHope.enums.Provider;
import es.udc.OpenHope.exception.*;
import es.udc.OpenHope.service.ConsentService;
import es.udc.OpenHope.service.TokenService;
import es.udc.OpenHope.service.providers.ProviderManager;
import es.udc.OpenHope.service.providers.ProviderService;
import es.udc.OpenHope.utils.CookieUtils;
import es.udc.OpenHope.utils.StateParams;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static es.udc.OpenHope.utils.HeaderUtils.getClientIp;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/providers")
public class ProviderController {

  private final ProviderManager providerManager;
  private final TokenService tokenService;
  private final ConsentService consentService;

  @Value("${frontend.base.url}")
  private String frontendBaseUrl;

  @GetMapping("/aspsp")
  public ResponseEntity<List<AspspDto>> getAspsps() throws ProviderException {
    List<AspspDto> aspsps = new ArrayList<>();

    for (Provider provider : Provider.values()) {
      ProviderService providerService = providerManager.getProviderService(provider);
      aspsps.addAll(providerService.getAspsps());
    }

    return ResponseEntity.ok(aspsps);
  }

  @GetMapping("/{provider}/{aspsp}/oauth")
  public ResponseEntity<ProviderAuthDto> auth(@PathVariable Provider provider, @PathVariable String aspsp,
                                              @RequestParam(required = false) Integer campaign,
                                              @RequestParam(required = false) Integer user) throws ProviderException {
    ProviderService providerService = providerManager.getProviderService(provider);
    ProviderAuthDto providerAuthDto = providerService.getOAuthUri(aspsp, campaign, user);
    return ResponseEntity.ok(providerAuthDto);
  }

  @GetMapping("/oauth/callback")
  public void callback(@RequestParam(value = "code") String code, @RequestParam String state,
                       HttpServletResponse response) throws ProviderException {

    HashMap<String, String> stateParams = StateParams.getStateParams(state);
    Provider provider = Provider.valueOf(stateParams.get("provider"));
    String aspsp = stateParams.get("aspsp");
    String campaign = stateParams.get("campaign");
    String userId = stateParams.get("user");

    ProviderService providerService = providerManager.getProviderService(provider);
    CredentialsDto credentialsDto = providerService.authorize(code, aspsp);

    Cookie tokenCookie = CookieUtils.getCookie("token_".concat(aspsp), credentialsDto.getAccess_token(), credentialsDto.getExpires_in() );
    Cookie refreshCookie = CookieUtils.getCookie("refresh_".concat(aspsp), credentialsDto.getRefresh_token(), 31536000);

    response.addCookie(tokenCookie);
    response.addCookie(refreshCookie);

    StringBuilder uriRedirection = new StringBuilder(frontendBaseUrl);
    if (campaign != null && !campaign.isBlank()) {
      uriRedirection.append("openbanking/bank-selection?aspsp=").append(aspsp)
          .append("&campaign=").append(campaign);
    } else if (userId != null && !userId.isBlank()) {
      uriRedirection.append("openbanking/bank-selection?aspsp=").append(aspsp)
          .append("&user=").append("me");
    }

    try {
      response.sendRedirect(uriRedirection.toString());
    } catch (Exception e) {
      throw new ProviderException(e.getMessage());
    }
  }

  @GetMapping("/{provider}/{aspsp}/accounts")
  public ResponseEntity<AccountsResponseDto> getAccounts(@PathVariable Provider provider, @PathVariable String aspsp,
                                                         @RequestParam(required = false) Integer campaign,
                                                         @RequestParam(required = false) Integer user,
                                                         @RequestHeader(name = "Authorization") String token,
                                                         HttpServletRequest request, HttpServletResponse response) throws ProviderException, UnauthorizedException {

    String owner = tokenService.extractsubject(token);

    AccountsResponseDto accountsResponseDto = new AccountsResponseDto();
    Cookie tokenCookie = CookieUtils.getCookieFromRequest("token_".concat(aspsp), request);
    String tokenOauth = tokenCookie != null ? tokenCookie.getValue() : null;
    Cookie refreshCookie = CookieUtils.getCookieFromRequest("refresh_".concat(aspsp), request);
    String refresh = refreshCookie != null ? refreshCookie.getValue() : null;

    accountsResponseDto.setNotAllowed(token == null || refresh == null);

    ProviderService providerService = providerManager.getProviderService(provider);
    String ipClient = getClientIp(request);

    try {
      if (!accountsResponseDto.isNotAllowed()) {
        //if we are allowed
        ConsentDto consentDto = consentService.get(owner, aspsp, provider.toString());

        if (consentDto != null) {
          try {
            List<BankAccountDto> accounts = providerService.getAccounts(aspsp, tokenOauth, ipClient, consentDto.getConsentId());
            accountsResponseDto.setAccounts(accounts);
          } catch (UnauthorizedException e) {

            tokenOauth = refreshToken(providerService, refresh, aspsp, response);

            try {
              List<BankAccountDto> accounts = providerService.getAccounts(aspsp, tokenOauth, ipClient, consentDto.getConsentId());
              accountsResponseDto.setAccounts(accounts);
            } catch (ConsentInvalidException ex) {
              consentService.delete(owner, aspsp, String.valueOf(provider));
              createConsent(providerService, owner, aspsp, tokenOauth, ipClient, campaign, user, accountsResponseDto);
            }

          } catch (ConsentInvalidException e) {
            consentService.delete(owner, aspsp, String.valueOf(provider));
            createConsent(providerService, owner, aspsp, tokenOauth, ipClient, campaign, user, accountsResponseDto);
          }

        } else {
          try {
            createConsent(providerService, owner, aspsp, tokenOauth, ipClient, campaign, user, accountsResponseDto);
          } catch (UnauthorizedException e) {
            tokenOauth = refreshToken(providerService, refresh, aspsp, response);
            createConsent(providerService, owner, aspsp, tokenOauth, ipClient, campaign, user, accountsResponseDto);
          }
        }
      }
    } catch (UnauthorizedException e) {
      accountsResponseDto.restart();
      restartSession(aspsp, response);
      consentService.delete(owner, aspsp, provider.toString());
    }

    return ResponseEntity.ok(accountsResponseDto);
  }

  @PostMapping("/donate")
  public ResponseEntity<DonationResponseDto> donate(@RequestBody DonateParamsDto params,
                                            @RequestHeader(name="Authorization") String token,
                                            HttpServletRequest request,
                                            HttpServletResponse response)
      throws CampaignFinalizedException, ProviderException, MissingBankAccountException {

    DonationResponseDto donationResponseDto = new DonationResponseDto();

    String owner = tokenService.extractsubject(token);
    String ipClient = getClientIp(request);

    Cookie tokenCookie = CookieUtils.getCookieFromRequest("token_".concat(params.getAspsp()), request);
    String tokenOauth = tokenCookie != null ? tokenCookie.getValue() : null;
    Cookie refreshCookie = CookieUtils.getCookieFromRequest("refresh_".concat(params.getAspsp()), request);
    String refresh = refreshCookie != null ? refreshCookie.getValue() : null;
    donationResponseDto.setNotAllowed(token == null || refresh == null);

    ProviderService providerService = providerManager.getProviderService(params.getProvider());

    try {
      if (!donationResponseDto.isNotAllowed()) {
        try {
          InitPaymentDto initPaymentDto = providerService.initPayment(tokenOauth, ipClient, params.getBankAccountId(), owner,
              params.getCampaignId(), params.getAmount());

          donationResponseDto.setRedirection(initPaymentDto.getRedirection());
        } catch (UnauthorizedException e) {
          //if fails tray to refresh the oauth token
          tokenOauth = refreshToken(providerService, refresh, params.getAspsp(), response);
          InitPaymentDto initPaymentDto = providerService.initPayment(tokenOauth, ipClient, params.getBankAccountId(), owner,
              params.getCampaignId(), params.getAmount());

          donationResponseDto.setRedirection(initPaymentDto.getRedirection());
        }
      }
    } catch(UnauthorizedException e) {
      //if fails after refresh token, we restart the session
      donationResponseDto.restart();
      restartSession(params.getAspsp(), response);
    }

    return ResponseEntity.ok(donationResponseDto);
  }

  private void createConsent(ProviderService providerService, String owner, String aspsp, String tokenOauth, String ipClient,
                    Integer campaign, Integer user, AccountsResponseDto accountsResponseDto) throws ProviderException, UnauthorizedException {
    PostConsentClientDto postConsentClientDto = providerService.createConsent(owner, aspsp, tokenOauth, ipClient, campaign, user);
    if(postConsentClientDto != null && postConsentClientDto.get_links() != null
            && postConsentClientDto.get_links().getScaRedirect() != null) {
      String redirection = postConsentClientDto.get_links().getScaRedirect().getHref();
      accountsResponseDto.setRedirection(redirection);
    }
  }

  private String refreshToken(ProviderService providerService, String refresh, String aspsp, HttpServletResponse response) throws ProviderException, UnauthorizedException {
    CredentialsDto credentialsDto = providerService.refreshToken(refresh, aspsp);
    if(credentialsDto == null){
      throw new UnauthorizedException("Refresh token response is null");
    }
    CookieUtils.reNewCookies(credentialsDto.getAccess_token(), credentialsDto.getRefresh_token(), credentialsDto.getExpires_in(), aspsp, response);
    return credentialsDto.getAccess_token();
  }

  private void restartSession(String aspsp, HttpServletResponse response) {
    CookieUtils.removeCookie("token_".concat(aspsp), response);
    CookieUtils.removeCookie("refresh_".concat(aspsp), response);
  }

}
