package es.udc.OpenHope.dto;

import es.udc.OpenHope.service.providers.ProviderService;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AspspDto {
  private String name;
  private String code;
  private ProviderService.Provider provider;
}
