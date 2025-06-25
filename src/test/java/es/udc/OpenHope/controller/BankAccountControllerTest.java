package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.AspspParamsDto;
import es.udc.OpenHope.dto.BankAccountParams;
import es.udc.OpenHope.dto.LoginDto;
import es.udc.OpenHope.dto.UserDto;
import es.udc.OpenHope.model.Aspsp;
import es.udc.OpenHope.model.BankAccount;
import es.udc.OpenHope.model.User;
import es.udc.OpenHope.repository.AspspRepository;
import es.udc.OpenHope.repository.BankAccountRepository;
import es.udc.OpenHope.repository.UserRepository;
import es.udc.OpenHope.service.UserService;
import es.udc.OpenHope.utils.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static es.udc.OpenHope.utils.Constants.PASSWORD;
import static es.udc.OpenHope.utils.Constants.USER_EMAIL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BankAccountControllerTest {

  private final MockMvc mockMvc;
  private final UserService userService;
  private final AspspRepository aspspRepository;
  private final BankAccountRepository bankAccountRepository;
  private final UserRepository userRepository;

  @Autowired
  public BankAccountControllerTest(MockMvc mockMvc, UserService userService, AspspRepository aspspRepository, BankAccountRepository bankAccountRepository, UserRepository userRepository) {
    this.mockMvc = mockMvc;
    this.userService = userService;
    this.aspspRepository = aspspRepository;
    this.bankAccountRepository = bankAccountRepository;
    this.userRepository = userRepository;
  }

  @Test
  void getBankAccountsTest() throws Exception {
    UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
    Optional<User> user = userRepository.findById(userDto.getId());

    AspspParamsDto aspspParamsDto = Utils.getAspspParams();
    BankAccountParams bankAccountParams = Utils.getBankAccountParams();
    bankAccountParams.setAspsp(aspspParamsDto);

    Aspsp aspsp = Utils.getAspsps();
    aspspRepository.save(aspsp);

    BankAccount bankAccount = Utils.getBankAccount(aspsp, user.get());
    bankAccountRepository.save(bankAccount);

    LoginDto loginDto = userService.authenticate(USER_EMAIL, PASSWORD);
    ResultActions result = mockMvc.perform(get("/api/bank-accounts")
        .header("Authorization", "Bearer " + loginDto.getToken()));

    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content").isNotEmpty())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].iban").value(bankAccount.getIban()));
  }
}
