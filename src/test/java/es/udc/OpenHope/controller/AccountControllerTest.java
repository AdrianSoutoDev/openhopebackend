package es.udc.OpenHope.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import es.udc.OpenHope.dto.LoginParamsDto;
import es.udc.OpenHope.service.AccountService;
import es.udc.OpenHope.service.OrganizationService;
import es.udc.OpenHope.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class AccountControllerTest {

  private static final String USER_EMAIL = "user@openhope.com";
  private static final String PASSWORD = "12345abc?";
  private static final String ORG_EMAIL = "org@openhope.com";
  private static final String ORG_NAME = "Apadan";

  @Value("${jwt.secret}")
  private String SECRET;

  private final MockMvc mockMvc;
  private final AccountService accountService;
  private final UserService userService;
  private final OrganizationService organizationService;
  private final ObjectMapper objectMapper;

  @Autowired
  public AccountControllerTest(final MockMvc mockMvc, final AccountService accountService, final UserService userService,
                               final OrganizationService organizationService, final ObjectMapper objectMapper) {
    this.mockMvc = mockMvc;
    this.accountService = accountService;
    this.userService = userService;
    this.organizationService = organizationService;
    this.objectMapper = objectMapper;
  }

  private ResultActions loginUser(String email, String pass) throws Exception {
    LoginParamsDto loginParamsDto = new LoginParamsDto(email, pass);
    String jsonContent = objectMapper.writeValueAsString(loginParamsDto);

    return mockMvc.perform(post("/api/accounts/login")
        .content(jsonContent)
        .contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  public void loginUserTest() throws Exception {
    userService.create(USER_EMAIL, PASSWORD);
    ResultActions result = loginUser(USER_EMAIL, PASSWORD);
    AtomicReference<String> emailExpected = new AtomicReference<>("");

    result.andExpect(status().isOk())
        .andDo(r ->{
          String response = r.getResponse().getContentAsString();
          String jwt = JsonPath.parse(response).read("$.token");
          SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
          String subject = Jwts.parser()
              .verifyWith(key)
              .build()
              .parseSignedClaims(jwt)
              .getPayload()
              .getSubject();
          emailExpected.set(subject);
        });

    assertEquals(USER_EMAIL, emailExpected.get());
  }

  @Test
  public void loginOrganizationTest() throws Exception {
    organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null);

    ResultActions result = loginUser(ORG_EMAIL, PASSWORD);

    AtomicReference<String> emailExpected = new AtomicReference<>("");

    result.andExpect(status().isOk())
        .andDo(r ->{
          String response = r.getResponse().getContentAsString();
          String jwt = JsonPath.parse(response).read("$.token");
          SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
          String subject = Jwts.parser()
              .verifyWith(key)
              .build()
              .parseSignedClaims(jwt)
              .getPayload()
              .getSubject();

          emailExpected.set(subject);
        });

    assertEquals(ORG_EMAIL, emailExpected.get());
  }

  @Test
  public void loginUserNullEmailTest() throws Exception {
    userService.create(USER_EMAIL, PASSWORD);
    ResultActions result = loginUser(null, PASSWORD);
    result.andExpect(status().isBadRequest());
  }

  @Test
  public void loginUserEmptyEmailTest() throws Exception {
    userService.create(USER_EMAIL, PASSWORD);
    ResultActions result = loginUser("", PASSWORD);
    result.andExpect(status().isBadRequest());
  }

  @Test
  public void loginUserNullPasswordTest() throws Exception {
    userService.create(USER_EMAIL, PASSWORD);
    ResultActions result = loginUser(USER_EMAIL, null);
    result.andExpect(status().isBadRequest());
  }

  @Test
  public void loginUserEmptyPasswordTest() throws Exception {
    userService.create(USER_EMAIL, PASSWORD);
    ResultActions result = loginUser(USER_EMAIL, "");
    result.andExpect(status().isBadRequest());
  }

  @Test
  public void loginUserInvalidEmailTest() throws Exception {
    userService.create(USER_EMAIL, PASSWORD);
    ResultActions result = loginUser("another_email@openhope.com", PASSWORD);
    result.andExpect(status().isUnauthorized());
  }

  @Test
  public void loginUserInvalidPasswordTest() throws Exception {
    userService.create(USER_EMAIL, PASSWORD);
    ResultActions result = loginUser(USER_EMAIL, "another_password");
    result.andExpect(status().isUnauthorized());
  }

  @Test
  public void loginUserMalformedEmailTest() throws Exception {
    userService.create(USER_EMAIL, PASSWORD);
    ResultActions result = loginUser("another_emailopenhope.com", PASSWORD);
    result.andExpect(status().isBadRequest());
  }

  @Test
  public void logoutLoggedAccountTest() throws Exception {
    userService.create(USER_EMAIL, PASSWORD);
    String jwt = accountService.authenticate(USER_EMAIL, PASSWORD);

    ResultActions result = mockMvc.perform(post("/api/accounts/logout")
        .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwt))
        .contentType(MediaType.APPLICATION_JSON));

    result.andExpect(status().isNoContent());
  }

  @Test
  public void logoutUnLoggedAccountTest() throws Exception {
    userService.create(USER_EMAIL, PASSWORD);
    ResultActions result = mockMvc.perform(post("/api/accounts/logout")
        .contentType(MediaType.APPLICATION_JSON));
    result.andExpect(status().isUnauthorized());
  }
}
