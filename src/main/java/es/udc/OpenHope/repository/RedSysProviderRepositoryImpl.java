package es.udc.OpenHope.repository;

import es.udc.OpenHope.dto.CommonHeadersDto;
import es.udc.OpenHope.dto.client.*;
import es.udc.OpenHope.exception.ConsentInvalidException;
import es.udc.OpenHope.exception.UnauthorizedException;
import lombok.Generated;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Repository
@Generated
public class RedSysProviderRepositoryImpl implements RedSysProviderRepository {

  private static final String APPLICATION_JSON = "application/json";

  @Override
  public List<AspspClientDto> getAspsps(CommonHeadersDto commonHeaders, String uri) {

    RestClient restClient = RestClient.create();

    GetAspspResponseDto response = restClient.get()
        .uri(uri)
          .header("accept", APPLICATION_JSON)
          .header("digest", commonHeaders.getDigest())
          .header("signature", commonHeaders.getSignature())
          .header("tpp-signature-certificate", commonHeaders.getCertificateContent())
          .header("x-ibm-client-id", commonHeaders.getClientId())
          .header("x-request-id", commonHeaders.getXRequestID())
          .retrieve()
          .body(GetAspspResponseDto.class);

    return response != null && response.getAspsps() != null
        ? response.getAspsps()
        : List.of();
  }

  @Override
  public CredentialsDto authorize(String redSysClientId, String code, String oauthCallback, String oauthCodeVerifier, String uri) {
    RestClient restClient = RestClient.create();

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "authorization_code");
    params.add("client_id", redSysClientId);
    params.add("code", code);
    params.add("redirect_uri", oauthCallback);
    params.add("code_verifier", oauthCodeVerifier);

    return restClient.post()
        .uri(uri)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(params)
        .retrieve()
        .body(CredentialsDto.class);
  }

  @Override
  public CredentialsDto refreshToken(String redSysClientId, String refreshToken, String uri) {
    RestClient restClient = RestClient.create();

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "refresh_token");
    params.add("client_id", redSysClientId);
    params.add("refresh_token", refreshToken);

    return restClient.post()
        .uri(uri)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(params)
        .retrieve()
        .body(CredentialsDto.class);
  }

  @Override
  public PostConsentClientDto postConsent(CommonHeadersDto commonHeaders, String uri,  String body, String aspsp,
                                          String PsuIpAddress, String authorization, String redirectionUri) throws UnauthorizedException {

    try {
      RestClient restClient = RestClient.create();

      return restClient.post()
          .uri(uri)
          .body(body)
          .header("accept", APPLICATION_JSON)
          .header("Content-Type", APPLICATION_JSON)
          .header("digest", commonHeaders.getDigest())
          .header("signature", commonHeaders.getSignature())
          .header("tpp-signature-certificate", commonHeaders.getCertificateContent())
          .header("x-ibm-client-id", commonHeaders.getClientId())
          .header("x-request-id", commonHeaders.getXRequestID())
          .header("authorization", authorization)
          .header("psu-ip-address", PsuIpAddress)
          .header("TPP-Redirect-URI", redirectionUri)
          .retrieve()
          .body(PostConsentClientDto.class);

    } catch (HttpClientErrorException e){
      if(e.getStatusCode().is4xxClientError()){
        throw new UnauthorizedException(e.getMessage());
      } else {
        throw e;
      }
    }
  }

  @Override
  public List<AccountClientDto> getAccounts(CommonHeadersDto commonHeaders, String uri, String consentId, String authorization) throws UnauthorizedException, ConsentInvalidException {

    try{
      RestClient restClient = RestClient.create();

      AccountsClientResponseDto response = restClient.get()
          .uri(uri)
          .header("accept", APPLICATION_JSON)
          .header("digest", commonHeaders.getDigest())
          .header("signature", commonHeaders.getSignature())
          .header("tpp-signature-certificate", commonHeaders.getCertificateContent())
          .header("x-ibm-client-id", commonHeaders.getClientId())
          .header("x-request-id", commonHeaders.getXRequestID())
          .header("consent-id", consentId)
          .header("authorization", authorization)
          .retrieve()
          .body(AccountsClientResponseDto.class);

      return response != null && response.getAccounts() != null
          ? response.getAccounts()
          : List.of();

    } catch (HttpClientErrorException e){
      if(e.getStatusCode().is4xxClientError()){
        if(e.getMessage().contains("\"code\":\"CONSENT_INVALID\"")) {
          throw new ConsentInvalidException(e.getMessage());
        } else {
          throw new UnauthorizedException(e.getMessage());
        }
      } else {
        throw e;
      }
    }
  }

  @Override
  public PostInitPaymentClientDto postInitPayment(CommonHeadersDto commonHeaders, String uri, String body, String PsuIpAddress, String authorization, String redirectUri) throws UnauthorizedException {
    try {
      RestClient restClient = RestClient.create();

      return restClient.post()
          .uri(uri)
          .body(body)
          .header("accept", APPLICATION_JSON)
          .header("Content-Type", APPLICATION_JSON)
          .header("digest", commonHeaders.getDigest())
          .header("signature", commonHeaders.getSignature())
          .header("tpp-signature-certificate", commonHeaders.getCertificateContent())
          .header("x-ibm-client-id", commonHeaders.getClientId())
          .header("x-request-id", commonHeaders.getXRequestID())
          .header("authorization", authorization)
          .header("psu-ip-address", PsuIpAddress)
          .header("TPP-Redirect-URI", redirectUri)
          .retrieve()
          .body(PostInitPaymentClientDto.class);

    } catch (HttpClientErrorException e){
      if(e.getStatusCode().is4xxClientError()){
        throw new UnauthorizedException(e.getMessage());
      } else {
        throw e;
      }
    }
  }
}
