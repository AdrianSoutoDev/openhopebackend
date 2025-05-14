package es.udc.OpenHope.repository;

import es.udc.OpenHope.dto.client.AspspClientDto;
import es.udc.OpenHope.dto.client.GetAspspResponseDto;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import java.util.List;

@Repository
public class RedSysProviderRepositoryImpl implements RedSysProviderRepository {

  @Override
  public List<AspspClientDto> getAspsps(String digest, String signature, String certificate, String xRequestID, String uri, String clientId) {
    RestClient restClient = RestClient.create();

    GetAspspResponseDto response = restClient.get()
        .uri(uri)
          .header("accept", "application/json")
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
}
