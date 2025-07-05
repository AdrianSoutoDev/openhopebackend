package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.*;
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

import java.util.List;
import java.util.NoSuchElementException;
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
    public Page<BankAccountListDto> getBankAccounts(String owner, int page, int size) {
        User user = userRepository.getUserByEmailIgnoreCase(owner);
        Pageable pageable = PageRequest.of(page, size);
        Page<BankAccount> bankAccounts = bankAccountRepository.findByAccount(user, pageable);

        return bankAccounts.map(BankAccountMapper::toBankAccountListDto)
            .map(b -> {
                b.setFavorite(user.getFavoriteAccount() != null && user.getFavoriteAccount().getIban().equals(b.getIban()));
                return b;
            });
    }

    @Override
    public List<BankAccountListDto> getAllBankAccounts(String owner) {
        User user = userRepository.getUserByEmailIgnoreCase(owner);
        List<BankAccount> bankAccounts = bankAccountRepository.findByAccount(user);

        return bankAccounts.stream().map(BankAccountMapper::toBankAccountListDto)
            .peek(b -> b.setFavorite(user.getFavoriteAccount() != null && user.getFavoriteAccount().getIban().equals(b.getIban()))).toList();
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
        User user = userRepository.getUserByEmailIgnoreCase(owner);
        Optional<BankAccount> bankAccount = bankAccountRepository.findByIbanAndAccount(bankAccountParams.getIban(), user);
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
            newBankAccount.setAccount(user);
            bankAccountRepository.save(newBankAccount);

            if(user.getFavoriteAccount() == null) {
                saveUserFavoriteAccount(user, newBankAccount);
            }

            return BankAccountMapper.toBankAccountDto(newBankAccount);
        }

        if(user.getFavoriteAccount() == null) {
            saveUserFavoriteAccount(user, bankAccount.get());
        }

        return BankAccountMapper.toBankAccountDto(bankAccount.get());
    }

    @Override
    public UserDto updateFavoriteAccount(String owner, BankAccountParams bankAccountParams) {
        User user = userRepository.getUserByEmailIgnoreCase(owner);
        if(user == null) throw new NoSuchElementException(Messages.get("validation.user.not.exists"));

        Optional<BankAccount> bankAccount = bankAccountRepository.findByIbanAndAccount(bankAccountParams.getIban(), user);
        if(bankAccount.isEmpty()) throw new NoSuchElementException(Messages.get("validation.bank.account.not.exists"));

        user.setFavoriteAccount(bankAccount.get());
        userRepository.save(user);

        return UserMapper.toUserDto(user);
    }

    private void saveUserFavoriteAccount(User user, BankAccount bankAccount) {
            user.setFavoriteAccount(bankAccount);
            userRepository.save(user);
    }

    private void validateParamsCreate(String email, String password) throws DuplicateEmailException {
        if(email == null || email.isBlank()) throw new IllegalArgumentException(  Messages.get("validation.email.null") );
        if(password == null || password.isBlank()) throw new IllegalArgumentException( Messages.get("validation.password.null") );

        if(accountExists(email)) {
            throw new DuplicateEmailException( Messages.get("validation.email.duplicated") );
        }
    }
}
