package es.udc.OpenHope.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static es.udc.OpenHope.utils.Constants.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserTest {

  @Test
  public void userInheritsFromRegistrationTest() {
    User user = new User(USER_EMAIL, ENCRYPTED_PASSWORD);
      assertInstanceOf(Account.class, user);
  }

  @Test
  public void userConstructorTest() {
    User user = new User(USER_EMAIL, ENCRYPTED_PASSWORD);
    assertEquals(USER_EMAIL, user.getEmail());
    assertEquals(ENCRYPTED_PASSWORD, user.getEncryptedPassword());
  }

}
