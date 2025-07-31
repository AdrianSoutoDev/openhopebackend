package es.udc.OpenHope.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.udc.OpenHope.dto.*;
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
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static es.udc.OpenHope.utils.Constants.PASSWORD;
import static es.udc.OpenHope.utils.Constants.USER_EMAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

  private final MockMvc mockMvc;
  private final UserService userService;
  private final ObjectMapper objectMapper;
  private final AspspRepository aspspRepository;
  private final BankAccountRepository bankAccountRepository;
  private final UserRepository userRepository;

  @Autowired
  public UserControllerTest(final MockMvc mockMvc, final UserService userService, final ObjectMapper objectMapper, AspspRepository aspspRepository, BankAccountRepository bankAccountRepository, UserRepository userRepository) {
    this.mockMvc = mockMvc;
    this.userService = userService;
    this.objectMapper = objectMapper;
    this.aspspRepository = aspspRepository;
    this.bankAccountRepository = bankAccountRepository;
    this.userRepository = userRepository;
  }

  private ResultActions registerUser(UserParamsDto params) throws Exception {
    LoginParamsDto loginParamsDto = new LoginParamsDto(params.getEmail(), params.getPassword());
    String jsonContent = objectMapper.writeValueAsString(loginParamsDto);

    return mockMvc.perform(post("/api/users")
        .content(jsonContent)
        .contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  void registerUserResponseWithCorrectDataTest() throws Exception {
    UserParamsDto userParamsDto = new UserParamsDto();
    userParamsDto.setEmail(USER_EMAIL);
    userParamsDto.setPassword(PASSWORD);

    ResultActions result = registerUser(userParamsDto);
    result.andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value(USER_EMAIL))
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.password").doesNotExist());
  }

  @Test
  void registerUserWithDuplicatedEmailTest() throws Exception {
    userService.create(USER_EMAIL, PASSWORD);

    UserParamsDto userParamsDto = new UserParamsDto();
    userParamsDto.setEmail(USER_EMAIL);
    userParamsDto.setPassword(PASSWORD);

    ResultActions result = registerUser(userParamsDto);
    result.andExpect(status().isConflict());
  }

  @Test
  void registerUserWithEmailNullTest() throws Exception {
    UserParamsDto userParamsDto = new UserParamsDto();
    userParamsDto.setPassword(PASSWORD);
    ResultActions result = registerUser(userParamsDto);
    result.andExpect(status().isBadRequest());
  }

  @Test
  void registerUserWithEmailEmptyTest() throws Exception {
    UserParamsDto userParamsDto = new UserParamsDto();
    userParamsDto.setEmail("");
    userParamsDto.setPassword(PASSWORD);
    ResultActions result = registerUser(userParamsDto);
    result.andExpect(status().isBadRequest());
  }

  @Test
  void registerUserWithBadFormedEmailWithoutAtSymbolTest() throws Exception {
    UserParamsDto userParamsDto = new UserParamsDto();
    userParamsDto.setEmail("email_OpenHope.com");
    userParamsDto.setPassword(PASSWORD);
    ResultActions result = registerUser(userParamsDto);
    result.andExpect(status().isBadRequest());
  }

  @Test
  void addBankAccount() throws Exception {
    userService.create(USER_EMAIL, PASSWORD);

    AspspParamsDto aspspParamsDto = Utils.getAspspParams();
    BankAccountParams bankAccountParams = Utils.getBankAccountParams();
    bankAccountParams.setAspsp(aspspParamsDto);

    String jsonContent = objectMapper.writeValueAsString(bankAccountParams);

    LoginDto loginDto = userService.authenticate(USER_EMAIL, PASSWORD);
    ResultActions result = mockMvc.perform(post("/api/users/bank-account")
        .header("Authorization", "Bearer " + loginDto.getToken())
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonContent));

    Page<BankAccountListDto> bankAccountListDtos = userService.getBankAccounts(USER_EMAIL, 0, 5);

    result.andExpect(status().isOk());
    assertFalse(bankAccountListDtos.getContent().isEmpty());
    assertEquals(1, bankAccountListDtos.getContent().size());
  }

  @Test
  void updateFavoriteBankAccount() throws Exception {
    UserDto userDto = userService.create(USER_EMAIL, PASSWORD);
    Optional<User> user = userRepository.findById(userDto.getId());

    AspspParamsDto aspspParamsDto = Utils.getAspspParams();
    BankAccountParams bankAccountParams = Utils.getBankAccountParams();
    bankAccountParams.setAspsp(aspspParamsDto);

    Aspsp aspsp = Utils.getAspsps();
    aspspRepository.save(aspsp);

    BankAccount bankAccount = Utils.getBankAccount(aspsp, user.get());
    bankAccountRepository.save(bankAccount);

    String jsonContent = objectMapper.writeValueAsString(bankAccountParams);

    LoginDto loginDto = userService.authenticate(USER_EMAIL, PASSWORD);
    ResultActions result = mockMvc.perform(put("/api/users/bank-account")
        .header("Authorization", "Bearer " + loginDto.getToken())
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonContent));

    user = userRepository.findById(userDto.getId());

    result.andExpect(status().isOk());
    assertEquals(bankAccount.getId(), user.get().getFavoriteAccount().getId());
  }
}
