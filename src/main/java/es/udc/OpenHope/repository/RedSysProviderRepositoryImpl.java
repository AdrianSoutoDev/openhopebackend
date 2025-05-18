package es.udc.OpenHope.repository;

import es.udc.OpenHope.dto.client.AspspClientDto;
import es.udc.OpenHope.dto.client.PostConsentClientDto;
import es.udc.OpenHope.dto.client.GetAspspResponseDto;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import java.util.List;

@Repository
public class RedSysProviderRepositoryImpl implements RedSysProviderRepository {

  private static final String APPLICATION_JSON = "application/json";

  @Override
  public List<AspspClientDto> getAspsps(String digest, String signature, String certificate, String xRequestID, String uri, String clientId) {
    RestClient restClient = RestClient.create();

    GetAspspResponseDto response = restClient.get()
        .uri(uri)
          .header("accept", APPLICATION_JSON)
          .header("digest", digest)
          .header("signature", signature)
          .header("tpp-signature-certificate", certificate)
          .header("x-ibm-client-id", clientId)
          .header("x-request-id", xRequestID)
          .retrieve()
          .body(GetAspspResponseDto.class);

    return response != null && response.getAspsps() != null
        ? response.getAspsps()
        : List.of();
  }

  @Override
  public PostConsentClientDto postConsent(String digest, String signature, String certificate, String xRequestID,
                                          String uri, String clientId, String body, String aspsp, String PsuIpAddress,
                                          String authorization, String redirectionUri) {

    RestClient restClient = RestClient.create();

    PostConsentClientDto response = restClient.post()
        .uri(uri)
        .body(body)
        .header("accept", APPLICATION_JSON)
        .header("Content-Type", APPLICATION_JSON)
        .header("digest", digest)
        .header("signature", signature)
        .header("tpp-signature-certificate", certificate)
        .header("x-ibm-client-id", clientId)
        .header("x-request-id", xRequestID)
        .header("authorization", authorization)
        .header("psu-ip-address", PsuIpAddress)
        .header("TPP-Redirect-URI", redirectionUri)
        .retrieve()
        .body(PostConsentClientDto.class);

    System.out.println(response.toString());

    return response;
  }
}
