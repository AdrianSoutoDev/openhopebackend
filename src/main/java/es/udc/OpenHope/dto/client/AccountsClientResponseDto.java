package es.udc.OpenHope.dto.client;

import lombok.Data;

import java.util.List;

@Data
public class AccountsClientResponseDto {
  private List<AccountClientDto> accounts;
}
