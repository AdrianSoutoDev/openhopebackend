package es.udc.OpenHope.utils;

import es.udc.OpenHope.dto.AspspParamsDto;
import es.udc.OpenHope.dto.BankAccountParams;
import es.udc.OpenHope.dto.UserDto;
import es.udc.OpenHope.exception.DuplicateEmailException;
import es.udc.OpenHope.model.*;
import es.udc.OpenHope.repository.*;
import es.udc.OpenHope.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static es.udc.OpenHope.utils.Constants.*;

@Component
public class Utils {

  private final CategoryRepository categoryRepository;
  private final DonationRepository donationRepository;
  private final AspspRepository aspspRepository;
  private final BankAccountRepository bankAccountRepository;
  private final UserService userService;
  private final UserRepository userRepository;


  @Autowired
  public Utils(CategoryRepository categoryRepository, DonationRepository donationRepository, AspspRepository aspspRepository, BankAccountRepository bankAccountRepository, UserService userService, UserRepository userRepository) {
    this.categoryRepository = categoryRepository;
    this.donationRepository = donationRepository;
    this.aspspRepository = aspspRepository;
    this.bankAccountRepository = bankAccountRepository;
    this.userService = userService;
    this.userRepository = userRepository;
  }

  public void initCategories() {
    List<String> categoryNames = new ArrayList<>(Arrays.asList(CATEGORY_1, CATEGORY_2, CATEGORY_3));
    List<Category> categories = getCategories(categoryNames);
    categoryRepository.saveAll(categories);
  }

  public List<String> getCategoryNames() {
    return new ArrayList<>(Arrays.asList(CATEGORY_1, CATEGORY_2, CATEGORY_3));
  }

  public MockMultipartFile getTestImg(String fileName) throws IOException {
    ClassPathResource resource = new ClassPathResource("test-images/" + fileName);
    byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());
    return new MockMultipartFile(
        "file",
        "test-image.png",
        "image/png",
        fileContent
    );
  }

  public MockMultipartFile getTestImg() throws IOException {
    return getTestImg("test-image.png");
  }

  private List<Category> getCategories(List<String> categoryNames){
    List<Category> categories = new ArrayList<>();
    categoryNames.forEach(c -> categories.add(new Category(c)));
    return categories;
  }

  public static AspspParamsDto getAspspParams(){
    AspspParamsDto aspspParamsDto = new AspspParamsDto();
    aspspParamsDto.setCode(ASPSP_CODE);
    aspspParamsDto.setName(ASPSP_NAME);
    aspspParamsDto.setProvider(ASPSP_PROVIDER);

    return aspspParamsDto;
  }

  public static BankAccountParams getBankAccountParams() {
    BankAccountParams bankAccountParams = new BankAccountParams();
    bankAccountParams.setIban(BANK_IBAN);
    bankAccountParams.setResourceId(BANK_RESOURCE_ID);
    bankAccountParams.setOwnerName(BANK_OWNER_NAME);
    bankAccountParams.setOriginalName(BANK_ORIGINAL_NAME);
    return bankAccountParams;
  }

  public static List<String> getTopics() {
    List<String> topics = new ArrayList<>();
    topics.add("topic1");
    topics.add("topic2");
    topics.add("topic3");
    topics.add("topic4");
    topics.add("topic5");
    return topics;
  }

  public static List<String> getAnotherTopics() {
    List<String> topics = new ArrayList<>();
    topics.add("topic7");
    topics.add("topic6");
    topics.add("topic4");
    topics.add("topic5");
    return topics;
  }

  public void createDonation(Campaign campaign, Float amount, User user) {
    Aspsp aspsp = getAspsps();
    aspspRepository.save(aspsp);
    BankAccount bankAccount = getBankAccount(aspsp, user);
    bankAccountRepository.save(bankAccount);
    Donation donation = getDonation(bankAccount, campaign, amount);
    donationRepository.save(donation);
  }

  public User getUser(String email, String password) throws DuplicateEmailException {
    UserDto userDto = userService.create(email, password);
    Optional<User> userFinded = userRepository.findById(userDto.getId());
    return userFinded.get();
  }

  public static Aspsp getAspsps(){
    Aspsp aspsp = new Aspsp();
    aspsp.setCode(ASPSP_CODE);
    aspsp.setName(ASPSP_NAME);
    aspsp.setProvider(ASPSP_PROVIDER);
    return aspsp;
  }

  public static BankAccount getBankAccount(Aspsp aspsp, User user) {
    BankAccount bankAccount = new BankAccount();
    bankAccount.setResourceId(BANK_RESOURCE_ID);
    bankAccount.setIban(BANK_IBAN);
    bankAccount.setOwnerName(BANK_OWNER_NAME);
    bankAccount.setAspsp(aspsp);
    bankAccount.setAccount(user);
    return bankAccount;
  }

  public static Donation getDonation(BankAccount bankAccount, Campaign campaign, Float amount) {
    Donation donation = new Donation();
    donation.setCampaign(campaign);
    donation.setBankAccount(bankAccount);
    donation.setDate(Date.valueOf(LocalDate.now()));
    donation.setAmount(amount);
    return donation;
  }

}
