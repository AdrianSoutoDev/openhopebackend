package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.AccountDto;
import es.udc.OpenHope.dto.client.AccountClientDto;

import java.util.ArrayList;
import java.util.List;

public class BankAccountMapper {

  public static AccountDto toAccountDto(AccountClientDto accountClientDtos){
    AccountDto accountDto = new AccountDto();

    accountDto.setResourceId(accountClientDtos.getResourceId());
    accountDto.setIban(accountClientDtos.getIban());

    StringBuilder sb = new StringBuilder();
    if(accountClientDtos.getName() != null && !accountClientDtos.getName().isBlank()){
      sb.append(accountClientDtos.getName()).append(" - ");
    }

    sb.append(accountClientDtos.getIban().substring(0, 4))
        .append(" **** **** **** **** ").append(accountClientDtos.getIban().substring(accountClientDtos.getIban().length() - 4));

    accountDto.setName(sb.toString());
    accountDto.setOwnerName(accountClientDtos.getOwnerName());
    accountDto.setOriginalName(accountClientDtos.getName());

    return accountDto;
  }

  public static List<AccountDto> toAccountDto(List<AccountClientDto> accountClientDtos) {
    List<AccountDto> accountDtos = new ArrayList<>();

    accountClientDtos.forEach(c -> {
      AccountDto accountDto = toAccountDto(c);
      accountDtos.add(accountDto);
    });

    return accountDtos;
  }
}
