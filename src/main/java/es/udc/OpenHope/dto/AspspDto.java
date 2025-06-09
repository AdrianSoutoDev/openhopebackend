package es.udc.OpenHope.dto;

import es.udc.OpenHope.enums.Provider;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AspspDto {
  private String name;
  private String code;
  private Provider provider;
}
