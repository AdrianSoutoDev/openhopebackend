package es.udc.OpenHope.dto;

import lombok.Data;

@Data
public class CommonHeadersDto {

  private String digest;
  private String signature;
  private String certificateContent;
  private String xRequestID;
  private String clientId;

}
