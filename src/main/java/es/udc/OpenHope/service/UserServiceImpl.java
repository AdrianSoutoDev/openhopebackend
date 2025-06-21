package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.BankAccountDto;
import es.udc.OpenHope.dto.UserDto;
import es.udc.OpenHope.dto.mappers.BankAccountMapper;
import es.udc.OpenHope.dto.mappers.UserMapper;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.model.Account;
import es.udc.OpenHope.model.BankAccount;
import es.udc.OpenHope.model.User;
import es.udc.OpenHope.repository.AccountRepository;
import es.udc.OpenHope.repository.BankAccountRepository;
import es.udc.OpenHope.repository.UserRepository;
import es.udc.OpenHope.utils.Messages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends AccountServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                           AccountRepository accountRepository, TokenService tokenService, BankAccountRepository bankAccountRepository) {
        super(bCryptPasswordEncoder, accountRepository, tokenService);
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public UserDto create(String email, String password) throws DuplicateEmailException {
        validateParamsCreate(email, password);
        String encryptedPassword = bCryptPasswordEncoder.encode(password);
        User user = new User(email, encryptedPassword);

        userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public Page<BankAccountDto> getBankAccounts(String owner, int page, int size) {
        Account account = accountRepository.getUserByEmailIgnoreCase(owner);
        Pageable pageable = PageRequest.of(page, size);
        Page<BankAccount> bankAccounts = bankAccountRepository.findByAccount(account, pageable);

        return bankAccounts.map(BankAccountMapper::toBankAccountDto);
    }

    private void validateParamsCreate(String email, String password) throws DuplicateEmailException {
        if(email == null || email.isBlank()) throw new IllegalArgumentException(  Messages.get("validation.email.null") );
        if(password == null || password.isBlank()) throw new IllegalArgumentException( Messages.get("validation.password.null") );

        if(accountExists(email)) {
            throw new DuplicateEmailException( Messages.get("validation.email.duplicated") );
        }
    }
}
