package es.udc.OpenHope.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AccountsResponseDto {
  private List<BankAccountDto> accounts;
  private boolean notAllowed;
  private String redirection;

  public AccountsResponseDto(){
    this.accounts = new ArrayList<>();
    this.notAllowed = false;
  }

  public void restart() {
    this.accounts = new ArrayList<>();
    this.notAllowed = true;
    this.redirection = null;
  }
}
