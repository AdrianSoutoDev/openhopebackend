package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.AspspDto;
import es.udc.OpenHope.dto.BankAccountDto;
import es.udc.OpenHope.dto.BankAccountListDto;
import es.udc.OpenHope.dto.BankAccountParams;
import es.udc.OpenHope.dto.client.AccountClientDto;
import es.udc.OpenHope.model.BankAccount;

import java.util.ArrayList;
import java.util.List;

public class BankAccountMapper {

  public static BankAccountDto toBankAccountDto(AccountClientDto accountClientDtos){
    BankAccountDto bankAccountDto = new BankAccountDto();

    bankAccountDto.setResourceId(accountClientDtos.getResourceId());
    bankAccountDto.setIban(accountClientDtos.getIban());

    StringBuilder sb = new StringBuilder();
    if(accountClientDtos.getName() != null && !accountClientDtos.getName().isBlank()){
      sb.append(accountClientDtos.getName()).append(" - ");
    }

    sb.append(accountClientDtos.getIban().substring(0, 4))
        .append(" **** **** **** **** ").append(accountClientDtos.getIban().substring(accountClientDtos.getIban().length() - 4));

    bankAccountDto.setName(sb.toString());
    bankAccountDto.setOwnerName(accountClientDtos.getOwnerName());
    bankAccountDto.setOriginalName(accountClientDtos.getName());

    return bankAccountDto;
  }

  public static BankAccountDto toBankAccountDto(BankAccount bankAccount){
    BankAccountDto bankAccountDto = new BankAccountDto();

    bankAccountDto.setResourceId(bankAccount.getResourceId());
    bankAccountDto.setIban(bankAccount.getIban());

    StringBuilder sb = new StringBuilder();
    if(bankAccount.getName() != null && !bankAccount.getName().isBlank()){
      sb.append(bankAccount.getName()).append(" - ");
    }

    sb.append(bankAccount.getIban().substring(0, 4))
        .append(" **** **** **** **** ").append(bankAccount.getIban().substring(bankAccount.getIban().length() - 4));

    bankAccountDto.setName(sb.toString());
    bankAccountDto.setOwnerName(bankAccount.getOwnerName());
    bankAccountDto.setOriginalName(bankAccount.getName());

    AspspDto aspspDto = AspspMapper.toAspspDto(bankAccount.getAspsp());
    bankAccountDto.setAspsp(aspspDto);

    return bankAccountDto;
  }

  public static List<BankAccountDto> toBankAccountDto(List<AccountClientDto> accountClientDtos) {
    List<BankAccountDto> bankAccountDtos = new ArrayList<>();

    accountClientDtos.forEach(c -> {
      BankAccountDto bankAccountDto = toBankAccountDto(c);
      bankAccountDtos.add(bankAccountDto);
    });

    return bankAccountDtos;
  }

  public static BankAccount toBankAccount(BankAccountParams bankAccountParams) {
    BankAccount bankAccount = new BankAccount();
    bankAccount.setResourceId(bankAccountParams.getResourceId());
    bankAccount.setIban(bankAccountParams.getIban());
    bankAccount.setName(bankAccountParams.getOriginalName());
    bankAccount.setOwnerName(bankAccountParams.getOwnerName());
    return bankAccount;
  }

  public static BankAccountListDto toBankAccountListDto(BankAccount bankAccount){
    BankAccountListDto bankAccountListDto = new BankAccountListDto();

    bankAccountListDto.setResourceId(bankAccount.getResourceId());
    bankAccountListDto.setIban(bankAccount.getIban());

    StringBuilder sb = new StringBuilder();
    if(bankAccount.getName() != null && !bankAccount.getName().isBlank()){
      sb.append(bankAccount.getName()).append(" - ");
    }

    sb.append(bankAccount.getIban().substring(0, 4))
        .append(" **** **** **** **** ").append(bankAccount.getIban().substring(bankAccount.getIban().length() - 4));

    bankAccountListDto.setName(sb.toString());
    bankAccountListDto.setOwnerName(bankAccount.getOwnerName());
    bankAccountListDto.setOriginalName(bankAccount.getName());

    AspspDto aspspDto = AspspMapper.toAspspDto(bankAccount.getAspsp());
    bankAccountListDto.setAspsp(aspspDto);

    return bankAccountListDto;
  }
}
