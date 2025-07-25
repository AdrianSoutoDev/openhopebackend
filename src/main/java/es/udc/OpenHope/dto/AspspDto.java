package es.udc.OpenHope.dto;

import es.udc.OpenHope.enums.Provider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AspspDto {
  private String bic;
  private String name;
  private String code;
  private Provider provider;
}
