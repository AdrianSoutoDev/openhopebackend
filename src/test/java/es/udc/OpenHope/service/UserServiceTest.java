package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.*;
import es.udc.OpenHope.exception.*;
import es.udc.OpenHope.model.*;
import es.udc.OpenHope.repository.*;
import es.udc.OpenHope.utils.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static es.udc.OpenHope.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class UserServiceTest {

    private final UserService userService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final BankAccountRepository bankAccountRepository;
    private final AspspRepository aspspRepository;
    private final OrganizationService organizationService;
    private final CampaignService campaignService;
    private final CampaignRepository campaignRepository;
    private final DonationRepository donationRepository;

    @Autowired
    public UserServiceTest(final UserService userService, final UserRepository userRepository,
                           final BCryptPasswordEncoder bCryptPasswordEncoder, final BankAccountRepository bankAccountRepository,
                           final AspspRepository aspspRepository, final OrganizationService organizationService,
                           final CampaignService campaignService, final CampaignRepository campaignRepository,
                           final DonationRepository donationRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.bankAccountRepository = bankAccountRepository;
        this.aspspRepository = aspspRepository;
        this.organizationService = organizationService;
        this.campaignService = campaignService;
        this.campaignRepository = campaignRepository;
        this.donationRepository = donationRepository;
    }

    @Test
    public void createUserTest() throws DuplicateEmailException {
        UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
        Optional<User> userFinded = userRepository.findById(userDto.getId());
        assertTrue(userFinded.isPresent());
        assertEquals(USER_EMAIL, userFinded.get().getEmail());
    }

    @Test
    public void createUserWithEncryptedPasswordTest() throws DuplicateEmailException {
        UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
        Optional<User> userFinded = userRepository.findById(userDto.getId());
        assertTrue(userFinded.isPresent());
        boolean passwordsAreEquals = bCryptPasswordEncoder.matches(PASSWORD, userFinded.get().getEncryptedPassword());
        assertTrue(passwordsAreEquals);
    }

    @Test
    public void createUserDuplicatedEmailTest() throws DuplicateEmailException {
        UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
        assertThrows(DuplicateEmailException.class, () ->
                userService.create(USER_EMAIL, "anotherPassword"));
    }

    @Test
    public void createOrganizationDuplicatedEmailIgnoringCaseTest() throws DuplicateEmailException {
        UserDto userDto = userService.create("user@openhope.com", PASSWORD);

        assertThrows(DuplicateEmailException.class, () ->
                userService.create("USER@OpenHope.com", "anotherPassword"));
    }

    @Test
    public void createUsersWithDiferentEmailTest() throws DuplicateEmailException {
        UserDto firstUserDto = userService.create(USER_EMAIL, PASSWORD);
        UserDto secondUserDto = userService.create("second_email@openHope.com", PASSWORD);

        List<User> users = userRepository.findAll();
        assertEquals(2, users.size());
    }

    @Test
    public void createUserWithEmailNullTest() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.create(null, PASSWORD));
    }

    @Test
    public void createUserWithPasswordNullTest() throws DuplicateEmailException {
        assertThrows(IllegalArgumentException.class, () ->
                userService.create(USER_EMAIL, null));
    }

    @Test
    public void getBankAccounts() throws DuplicateEmailException, InvalidCredentialsException {
        UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
        Optional<User> user = userRepository.findById(userDto.getId());

        AspspParamsDto aspspParamsDto = Utils.getAspspParams();
        BankAccountParams bankAccountParams = Utils.getBankAccountParams();
        bankAccountParams.setAspsp(aspspParamsDto);

        Aspsp aspsp = Utils.getAspsps();
        aspspRepository.save(aspsp);

        BankAccount bankAccount = Utils.getBankAccount(aspsp, user.get());
        bankAccountRepository.save(bankAccount);

        Page<BankAccountDto> bankAccountDtos = userService.getBankAccounts(userDto.getEmail(), 0, 5);

        assertFalse(bankAccountDtos.getContent().isEmpty());
        assertEquals(1, bankAccountDtos.getContent().size());
    }

    @Test
    public void getDonations() throws DuplicateEmailException, InvalidCredentialsException, MaxTopicsExceededException, DuplicateOrganizationException, MaxCategoriesExceededException, DuplicatedCampaignException {
        UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
        Optional<User> user = userRepository.findById(userDto.getId());

        OrganizationDto organizationDto = organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, ORG_DESCRIPTION, null, null, null);
        CampaignDto campaignDto = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), CAMPAIGN_NAME, null, CAMPAIGN_START_AT,
            CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

        CampaignDto campaignDto2 = campaignService.create(organizationDto.getId(), organizationDto.getEmail(), "another campaign", null, CAMPAIGN_START_AT,
            CAMPAIGN_DATE_LIMIT, null, null, null, null, null);

        Optional<Campaign> campaign = campaignRepository.findById(campaignDto.getId());
        Optional<Campaign> campaign2 = campaignRepository.findById(campaignDto2.getId());

        AspspParamsDto aspspParamsDto = Utils.getAspspParams();
        BankAccountParams bankAccountParams = Utils.getBankAccountParams();
        bankAccountParams.setAspsp(aspspParamsDto);

        Aspsp aspsp = Utils.getAspsps();
        aspspRepository.save(aspsp);

        BankAccount bankAccount = Utils.getBankAccount(aspsp, user.get());
        bankAccountRepository.save(bankAccount);

        Donation donation = Utils.getDonation(bankAccount, campaign.get(), AMOUNT_DONATION);
        Donation donation2 = Utils.getDonation(bankAccount, campaign2.get(), AMOUNT_DONATION);
        donationRepository.save(donation);
        donationRepository.save(donation2);

        Page<DonationDto> donationDtos = userService.getDonations(userDto.getEmail(), 0, 5);

        assertFalse(donationDtos.getContent().isEmpty());
        assertEquals(2, donationDtos.getContent().size());
    }

    @Test
    public void addBankAccount() throws DuplicateEmailException {
        UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
        Optional<User> user = userRepository.findById(userDto.getId());

        AspspParamsDto aspspParamsDto = Utils.getAspspParams();
        BankAccountParams bankAccountParams = Utils.getBankAccountParams();
        bankAccountParams.setAspsp(aspspParamsDto);

        BankAccountDto bankAccountDto = userService.addBankAccount(user.get().getEmail(), bankAccountParams);
        Optional<BankAccount> bankAccount = bankAccountRepository.findByIbanAndAccount(bankAccountParams.getIban(), user.get());

        assertNotNull(bankAccountDto);
        assertTrue(bankAccount.isPresent());
        assertEquals(bankAccountParams.getIban(), bankAccountDto.getIban());
        assertEquals(bankAccount.get().getIban(), bankAccountDto.getIban());
    }

    @Test
    public void updateFavoriteAccount() throws DuplicateEmailException {
        UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
        Optional<User> user = userRepository.findById(userDto.getId());

        AspspParamsDto aspspParamsDto = Utils.getAspspParams();
        BankAccountParams bankAccountParams = Utils.getBankAccountParams();
        bankAccountParams.setAspsp(aspspParamsDto);

        Aspsp aspsp = Utils.getAspsps();
        aspspRepository.save(aspsp);

        BankAccount bankAccount = Utils.getBankAccount(aspsp, user.get());
        bankAccountRepository.save(bankAccount);

        userService.updateFavoriteAccount(user.get().getEmail(), bankAccountParams);
        user = userRepository.findById(userDto.getId());

        assertNotNull(user.get().getFavoriteAccount());
        assertEquals(bankAccountParams.getIban(), user.get().getFavoriteAccount().getIban());
        assertEquals(bankAccount.getId(), user.get().getFavoriteAccount().getId());
    }

    @Test
    public void updateFavoriteAccountUserDoesntExist() throws DuplicateEmailException {
        UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
        Optional<User> user = userRepository.findById(userDto.getId());

        AspspParamsDto aspspParamsDto = Utils.getAspspParams();
        BankAccountParams bankAccountParams = Utils.getBankAccountParams();
        bankAccountParams.setAspsp(aspspParamsDto);

        Aspsp aspsp = Utils.getAspsps();
        aspspRepository.save(aspsp);

        BankAccount bankAccount = Utils.getBankAccount(aspsp, user.get());
        bankAccountRepository.save(bankAccount);

        assertThrows(NoSuchElementException.class, () ->
            userService.updateFavoriteAccount("another_email@openhope.com", bankAccountParams));
    }

    @Test
    public void updateFavoriteAccountBankAccountDoesntExist() throws DuplicateEmailException {
        UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
        Optional<User> user = userRepository.findById(userDto.getId());

        AspspParamsDto aspspParamsDto = Utils.getAspspParams();
        BankAccountParams bankAccountParams = Utils.getBankAccountParams();
        bankAccountParams.setAspsp(aspspParamsDto);
        bankAccountParams.setIban("ES2500000000000000000000");

        Aspsp aspsp = Utils.getAspsps();
        aspspRepository.save(aspsp);

        BankAccount bankAccount = Utils.getBankAccount(aspsp, user.get());
        bankAccountRepository.save(bankAccount);

        assertThrows(NoSuchElementException.class, () ->
            userService.updateFavoriteAccount(user.get().getEmail(), bankAccountParams));
    }
}
