package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.*;
import es.udc.OpenHope.dto.client.AccountClientDto;
import es.udc.OpenHope.dto.client.CredentialsDto;
import es.udc.OpenHope.dto.client.PostConsentClientDto;
import es.udc.OpenHope.exception.ProviderException;
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

import java.io.IOException;
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

    Cookie tokenCookie = CookieUtils.getCookie("token_".concat(aspsp), credentialsDto.getAccess_token());
    Cookie refreshCookie = CookieUtils.getCookie("refresh_".concat(aspsp), credentialsDto.getRefresh_token());

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
                                                      HttpServletRequest request, HttpServletResponse response) throws ProviderException, IOException {

    String owner = tokenService.extractsubject(token);
    AccountsResponseDto accountsResponseDto = new AccountsResponseDto();
    List<AccountDto> accounts = new ArrayList<>();

    Cookie tokenCookie = CookieUtils.getCookieFromRequest("token_".concat(aspsp), request);
    String tokenOauth = tokenCookie != null ? tokenCookie.getValue() : null;

    Cookie refreshCookie = CookieUtils.getCookieFromRequest("refresh_".concat(aspsp), request);
    String refresh = refreshCookie != null ? refreshCookie.getValue() : null;

    accountsResponseDto.setNotAllowed(token == null || refresh == null);

    if(!accountsResponseDto.isNotAllowed()){
      ConsentDto consentDto = consentService.getConsent(owner, aspsp, provider.toString());
      ProviderService providerService = providerManager.getProviderService(provider);
      String ipClient = getClientIp(request);

      if(consentDto != null) {
        accounts = providerService.getAccounts(aspsp, tokenOauth, refresh, owner, ipClient, consentDto.getConsentId());
        accountsResponseDto.setAccounts(accounts);
      } else {
        PostConsentClientDto postConsentClientDto = providerService.createConsent(owner, aspsp, tokenOauth, refresh, ipClient, campaign.toString());
        String redirection = postConsentClientDto.get_links().getScaRedirect().getHref();
        accountsResponseDto.setRedirection(redirection);
      }
    }

    return ResponseEntity.ok(accountsResponseDto);
  }
}
