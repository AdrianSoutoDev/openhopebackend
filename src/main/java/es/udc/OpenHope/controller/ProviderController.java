package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.*;
import es.udc.OpenHope.dto.client.CredentialsDto;
import es.udc.OpenHope.dto.client.PostConsentClientDto;
import es.udc.OpenHope.exception.ProviderException;
import es.udc.OpenHope.exception.UnauthorizedException;
import es.udc.OpenHope.service.ConsentService;
import es.udc.OpenHope.service.TokenService;
import es.udc.OpenHope.service.providers.Provider;
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
                                              @RequestParam(required = false) Integer campaign) throws ProviderException {
    ProviderService providerService = providerManager.getProviderService(provider);
    ProviderAuthDto providerAuthDto = providerService.getOAuthUri(aspsp, campaign);
    return ResponseEntity.ok(providerAuthDto);
  }

  @GetMapping("/oauth/callback")
  public void callback(@RequestParam(value="code") String code, @RequestParam String state,
                       HttpServletResponse response) throws ProviderException {

    HashMap<String, String> stateParams = StateParams.getStateParams(state);
    Provider provider = Provider.valueOf(stateParams.get("provider"));
    String aspsp = stateParams.get("aspsp");
    String campaign = stateParams.get("campaign");

    ProviderService providerService = providerManager.getProviderService(provider);
    CredentialsDto credentialsDto = providerService.authorize(code, aspsp);

    Cookie tokenCookie = CookieUtils.getCookie("token_".concat(aspsp), credentialsDto.getAccess_token(), credentialsDto.getExpires_in());
    Cookie refreshCookie = CookieUtils.getCookie("refresh_".concat(aspsp), credentialsDto.getRefresh_token(), credentialsDto.getExpires_in());

    response.addCookie(tokenCookie);
    response.addCookie(refreshCookie);

    StringBuilder UriRedirection = new StringBuilder(frontendBaseUrl);
    if(campaign != null && !campaign.isBlank()) {
      UriRedirection.append("openbanking/bank-selection?aspsp=").append(aspsp)
          .append("&campaign=").append(campaign);
    }

    try {
      response.sendRedirect(UriRedirection.toString());
    } catch (Exception e){
      throw new ProviderException(e.getMessage());
    }
  }

  @GetMapping("/{provider}/{aspsp}/accounts")
  public ResponseEntity<AccountsResponseDto> getAccounts(@PathVariable Provider provider, @PathVariable String aspsp,
                                                         @RequestParam(required = false) Integer campaign,
                                                         @RequestHeader(name="Authorization") String token,
                                                      HttpServletRequest request, HttpServletResponse response) throws ProviderException, UnauthorizedException {

    String owner = tokenService.extractsubject(token);
    AccountsResponseDto accountsResponseDto = new AccountsResponseDto();

    Cookie tokenCookie = CookieUtils.getCookieFromRequest("token_".concat(aspsp), request);
    String tokenOauth = tokenCookie != null ? tokenCookie.getValue() : null;

    Cookie refreshCookie = CookieUtils.getCookieFromRequest("refresh_".concat(aspsp), request);
    String refresh = refreshCookie != null ? refreshCookie.getValue() : null;

    accountsResponseDto.setNotAllowed(token == null || refresh == null);

    try {
      if (!accountsResponseDto.isNotAllowed()) {
        ConsentDto consentDto = consentService.get(owner, aspsp, provider.toString());
        ProviderService providerService = providerManager.getProviderService(provider);
        String ipClient = getClientIp(request);

        if (consentDto != null) {

          try {
            List<AccountDto> accounts = providerService.getAccounts(aspsp, tokenOauth, ipClient, consentDto.getConsentId());
            accountsResponseDto.setAccounts(accounts);
          } catch (UnauthorizedException e) {
            tokenOauth = refreshToken(providerService, refresh, aspsp, response);
            List<AccountDto> accounts = providerService.getAccounts(aspsp, tokenOauth, ipClient, consentDto.getConsentId());
            accountsResponseDto.setAccounts(accounts);
          }

        } else {

          try {
            createConsent(providerService, owner, aspsp, tokenOauth, ipClient, campaign.toString(), accountsResponseDto);
          } catch (UnauthorizedException e) {
            tokenOauth = refreshToken(providerService, refresh, aspsp, response);
            createConsent(providerService, owner, aspsp, tokenOauth, ipClient, campaign.toString(), accountsResponseDto);
          }

        }
      }
    } catch(UnauthorizedException e) {
      restartSession(accountsResponseDto, owner, aspsp, provider.toString());
    }

    return ResponseEntity.ok(accountsResponseDto);
  }

  private void createConsent(ProviderService providerService, String owner, String aspsp, String tokenOauth, String ipClient,
                    String campaign, AccountsResponseDto accountsResponseDto) throws ProviderException, UnauthorizedException {
    PostConsentClientDto postConsentClientDto = providerService.createConsent(owner, aspsp, tokenOauth, ipClient, campaign);
    String redirection = postConsentClientDto.get_links().getScaRedirect().getHref();
    accountsResponseDto.setRedirection(redirection);
  }

  private String refreshToken(ProviderService providerService, String refresh, String aspsp, HttpServletResponse response) throws ProviderException, UnauthorizedException {
    CredentialsDto credentialsDto = providerService.refreshToken(refresh, aspsp);
    if(credentialsDto == null){
      throw new UnauthorizedException("");
    }
    CookieUtils.reNewCookies(credentialsDto.getAccess_token(), credentialsDto.getRefresh_token(), credentialsDto.getExpires_in(), aspsp, response);
    return credentialsDto.getAccess_token();
  }

  private void restartSession(AccountsResponseDto accountsResponseDto, String owner, String aspsp, String provider) {
    accountsResponseDto.setAccounts(new ArrayList<>());
    accountsResponseDto.setNotAllowed(true);
    accountsResponseDto.setRedirection(null);
    consentService.delete(owner, aspsp, provider);
  }
}
