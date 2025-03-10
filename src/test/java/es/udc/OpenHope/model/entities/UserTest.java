package es.udc.OpenHope.model.entities;

import es.udc.OpenHope.model.Registration;
import es.udc.OpenHope.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class UserTest {

  private static final String USER_EMAIL = "user@openhope.com";
  private static final String ENCRYPTED_PASSWORD = "fa89sdfjasdpmcds9";

  @Test
  public void userInheritsFromRegistrationTest() {
    User user = new User(USER_EMAIL, ENCRYPTED_PASSWORD);
    assertTrue(user instanceof Registration);
  }

  @Test
  public void userConstructorTest() {
    User user = new User(USER_EMAIL, ENCRYPTED_PASSWORD);
    assertEquals(user.getEmail(), USER_EMAIL);
    assertEquals(user.getEncryptedPassword(), ENCRYPTED_PASSWORD);
  }

}
