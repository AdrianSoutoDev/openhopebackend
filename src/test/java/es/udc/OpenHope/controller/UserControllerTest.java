package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.OrganizationParamsDto;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

  private static final String USER_EMAIL = "user@openhope.com";
  private static final String PASSWORD = "12345abc?";

  private final MockMvc mockMvc;
  private final UserService userService;

  @Autowired
  public UserControllerTest(final MockMvc mockMvc, final UserService userService) {
    this.mockMvc = mockMvc;
    this.userService = userService;
  }

  private ResultActions registerUser(UserParamsDto params) throws Exception {
    MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/api/users")
        .param("email", params.getEmail())
        .param("password", params.getPassword())
        .contentType(MediaType.MULTIPART_FORM_DATA);

    return mockMvc.perform(builder);
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
    userParamsDto.setPassword(PASSWORD);;
    ResultActions result = registerUser(userParamsDto);
    result.andExpect(status().isBadRequest());
  }
}
