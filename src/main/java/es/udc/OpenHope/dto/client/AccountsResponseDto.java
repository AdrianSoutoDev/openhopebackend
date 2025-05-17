package es.udc.OpenHope.dto.client;

import lombok.Data;

import java.util.List;

@Data
public class AccountsResponseDto {
  private List<AccountsDto> accounts;
}
