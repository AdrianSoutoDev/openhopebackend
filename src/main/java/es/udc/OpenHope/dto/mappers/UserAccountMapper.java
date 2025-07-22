package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.UserAccountDto;
import es.udc.OpenHope.enums.AccountType;
import es.udc.OpenHope.model.Account;
import es.udc.OpenHope.model.User;

public abstract class UserAccountMapper {
  public static UserAccountDto toUserAccountDto(Account account){
    UserAccountDto userAccountDto = new UserAccountDto();
    userAccountDto.setId(account.getId());
    userAccountDto.setEmail(account.getEmail());
    AccountType accountType = account instanceof User ? AccountType.USER : AccountType.ORGANIZATION;
    userAccountDto.setAccountType(accountType);
    return userAccountDto;
  }
}
