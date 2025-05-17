package es.udc.OpenHope.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AccountsResponseDto {
  private List<AccountDto> accounts;
  private boolean notAllowed;

  public AccountsResponseDto(){
    this.accounts = new ArrayList<>();
    this.notAllowed = false;
  }
}
