package es.udc.OpenHope.repository;

import es.udc.OpenHope.dto.AccountsResponseDto;
import es.udc.OpenHope.dto.CommonHeadersDto;
import es.udc.OpenHope.dto.client.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import java.util.List;

@Repository
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
  public PostConsentClientDto postConsent(CommonHeadersDto commonHeaders, String uri,  String body, String aspsp,
                                          String PsuIpAddress, String authorization, String redirectionUri) {

    RestClient restClient = RestClient.create();

    PostConsentClientDto response = restClient.post()
        .uri(uri)
        .body(body)
        .header("accept", APPLICATION_JSON)
        .header("Content-Type", APPLICATION_JSON)
        .header("digest", commonHeaders.getDigest())
        .header("signature", commonHeaders.getSignature())
        .header("tpp-signature-certificate", commonHeaders.getClientId())
        .header("x-ibm-client-id", commonHeaders.getClientId())
        .header("x-request-id", commonHeaders.getXRequestID())
        .header("authorization", authorization)
        .header("psu-ip-address", PsuIpAddress)
        .header("TPP-Redirect-URI", redirectionUri)
        .retrieve()
        .body(PostConsentClientDto.class);

    System.out.println(response.toString());

    return response;
  }

  @Override
  public List<AccountClientDto> getAccounts(CommonHeadersDto commonHeaders, String uri, String consentId, String authorization) {
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
  }
}
