package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.LoginDto;
import es.udc.OpenHope.exception.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;

import static es.udc.OpenHope.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AccountServiceTest {

  @Value("${jwt.secret}")
  private String SECRET;

  private final AccountService accountService;
  private final UserService userService;
  private final OrganizationService organizationService;

  @Autowired
  public AccountServiceTest(final AccountService accountService, final UserService userService, final OrganizationService organizationService) {
    this.accountService = accountService;
    this.userService = userService;
    this.organizationService = organizationService;
  }

  @Test
  public void LoginUserTest() throws DuplicateEmailException, InvalidCredentialsException {
    userService.create(USER_EMAIL, PASSWORD);
    LoginDto loginDto = accountService.authenticate(USER_EMAIL, PASSWORD);

    SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
    String subject = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(loginDto.getToken())
        .getPayload()
        .getSubject();

    assertEquals(USER_EMAIL, subject);
  }

  @Test
  public void LoginOrganizationTest() throws DuplicateEmailException, InvalidCredentialsException, DuplicateOrganizationException, MaxCategoriesExceededException, MaxTopicsExceededException {
    organizationService.create(ORG_EMAIL, PASSWORD, ORG_NAME, null, null,null, null);
    LoginDto loginDto = accountService.authenticate(ORG_EMAIL, PASSWORD);

    SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
    String subject = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(loginDto.getToken())
        .getPayload()
        .getSubject();

    assertEquals(ORG_EMAIL, subject);
  }

  @Test
  public void LoginUserNullEmailTest() throws DuplicateEmailException {
    userService.create(USER_EMAIL, PASSWORD);
    assertThrows(IllegalArgumentException.class, () ->
        accountService.authenticate(null, PASSWORD) );
  }

  @Test
  public void LoginUserNullPasswordTest() throws DuplicateEmailException {
    userService.create(USER_EMAIL, PASSWORD);
    assertThrows(IllegalArgumentException.class, () ->
        accountService.authenticate(USER_EMAIL, null) );
  }

  @Test
  public void LoginUserInvalidEmailTest() throws DuplicateEmailException {
    userService.create(USER_EMAIL, PASSWORD);
    assertThrows(InvalidCredentialsException.class, () ->
        accountService.authenticate("another_email@openhope.com", PASSWORD) );
  }

  @Test
  public void LoginUserInvalidPasswordTest() throws DuplicateEmailException {
    userService.create(USER_EMAIL, PASSWORD);
    assertThrows(InvalidCredentialsException.class, () ->
        accountService.authenticate(USER_EMAIL, "another_password") );
  }
}
