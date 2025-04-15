package es.udc.OpenHope.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.udc.OpenHope.dto.LoginParamsDto;
import es.udc.OpenHope.dto.UserParamsDto;
import es.udc.OpenHope.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static es.udc.OpenHope.utils.Constants.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

  private final MockMvc mockMvc;
  private final UserService userService;
  private final ObjectMapper objectMapper;

  @Autowired
  public UserControllerTest(final MockMvc mockMvc, final UserService userService, final ObjectMapper objectMapper) {
    this.mockMvc = mockMvc;
    this.userService = userService;
    this.objectMapper = objectMapper;
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
}
