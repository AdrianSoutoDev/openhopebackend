package es.udc.OpenHope.dto.client;

import lombok.Data;

@Data
public class CredentialsDto {
  private String access_token;
  private String token_type;
  private String refresh_token;
  private String expires_in;
}
