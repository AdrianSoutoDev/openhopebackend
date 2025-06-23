package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.BankAccountDto;
import es.udc.OpenHope.dto.BankAccountParams;
import es.udc.OpenHope.dto.DonationDto;
import es.udc.OpenHope.dto.UserDto;
import es.udc.OpenHope.dto.mappers.AspspMapper;
import es.udc.OpenHope.dto.mappers.BankAccountMapper;
import es.udc.OpenHope.dto.mappers.DonationMapper;
import es.udc.OpenHope.dto.mappers.UserMapper;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.model.*;
import es.udc.OpenHope.repository.*;
import es.udc.OpenHope.utils.Messages;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl extends AccountServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final DonationRepository donationRepository;
    private final AspspRepository aspspRepository;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                           AccountRepository accountRepository, TokenService tokenService, BankAccountRepository bankAccountRepository, DonationRepository donationRepository, AspspRepository aspspRepository) {
        super(bCryptPasswordEncoder, accountRepository, tokenService);
        this.userRepository = userRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.donationRepository = donationRepository;
        this.aspspRepository = aspspRepository;
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

    @Override
    public Page<DonationDto> getDonations(String owner, int page, int size) {
        Account account = accountRepository.getUserByEmailIgnoreCase(owner);
        Pageable pageable = PageRequest.of(page, size);
        Page<Donation> donations = donationRepository.findByBankAccount_Account(account, pageable);

        return donations.map(DonationMapper::toDonationDto);
    }

    @Override
    @Transactional
    public BankAccountDto addBankAccount(String owner, BankAccountParams bankAccountParams) {
        Account account = accountRepository.getUserByEmailIgnoreCase(owner);
        Optional<BankAccount> bankAccount = bankAccountRepository.findByIbanAndAccount(bankAccountParams.getIban(), account);
        Optional<Aspsp> aspsp = aspspRepository.findByProviderAndCode(bankAccountParams.getAspsp().getProvider(),
            bankAccountParams.getAspsp().getCode());

        if(bankAccount.isEmpty()) {
            BankAccount newBankAccount = BankAccountMapper.toBankAccount(bankAccountParams);

            if(aspsp.isEmpty()){
                Aspsp newAspsp = AspspMapper.toAspsp(bankAccountParams.getAspsp());
                aspspRepository.save(newAspsp);
                aspsp = Optional.of(newAspsp);
            }

            newBankAccount.setAspsp(aspsp.get());
            newBankAccount.setAccount(account);
            bankAccountRepository.save(newBankAccount);
            return BankAccountMapper.toBankAccountDto(newBankAccount);
        }

        return BankAccountMapper.toBankAccountDto(bankAccount.get());
    }

    private void validateParamsCreate(String email, String password) throws DuplicateEmailException {
        if(email == null || email.isBlank()) throw new IllegalArgumentException(  Messages.get("validation.email.null") );
        if(password == null || password.isBlank()) throw new IllegalArgumentException( Messages.get("validation.password.null") );

        if(accountExists(email)) {
            throw new DuplicateEmailException( Messages.get("validation.email.duplicated") );
        }
    }
}
