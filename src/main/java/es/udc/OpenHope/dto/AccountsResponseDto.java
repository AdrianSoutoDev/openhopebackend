package es.udc.OpenHope.dto;

import es.udc.OpenHope.dto.client.AccountClientDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AccountsResponseDto {
  private List<AccountDto> accounts;
  private boolean notAllowed;
  private String redirection;

  public AccountsResponseDto(){
    this.accounts = new ArrayList<>();
    this.notAllowed = false;
  }
}
