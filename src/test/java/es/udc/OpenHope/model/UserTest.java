package es.udc.OpenHope.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserTest {

  private static final String USER_EMAIL = "user@openhope.com";
  private static final String ENCRYPTED_PASSWORD = "$2a$16$dUrZyai4SLzT.w3NMXjfC.SgYQMyRcKyK0miEopks5RULJfl8n38G";

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
