package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.BankAccountDto;
import es.udc.OpenHope.dto.BankAccountParams;
import es.udc.OpenHope.dto.DonationDto;
import es.udc.OpenHope.dto.UserDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService extends AccountService {
    UserDto create(String email, String password) throws DuplicateEmailException;
    Page<BankAccountDto> getBankAccounts(String owner, int page, int size);
    Page<DonationDto> getDonations(String owner, int page, int size);
    BankAccountDto addBankAccount(String owner, BankAccountParams bankAccountParams);
    UserDto updateFavoriteAccount(String owner, BankAccountParams bankAccountParams);
}
