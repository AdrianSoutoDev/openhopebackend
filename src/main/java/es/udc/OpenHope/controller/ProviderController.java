package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.AspspDto;
import es.udc.OpenHope.exception.ProviderException;
import es.udc.OpenHope.service.providers.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/providers")
public class ProviderController {

  private final ProviderService providerService;

  @GetMapping("/aspsp")
  public ResponseEntity<List<AspspDto>> getAspsps() throws ProviderException {
    List<AspspDto> aspsps = providerService.getAspsps();
    return ResponseEntity.ok(aspsps);
  }
}
