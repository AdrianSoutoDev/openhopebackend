package es.udc.OpenHope.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static es.udc.OpenHope.utils.Constants.*;

@SpringBootTest
@ActiveProfiles("test")
public class OrganizationTest {

  @Test
  public void organizationInheritsFromRegistrationTest() {
    Organization organization = new Organization(ORG_EMAIL, ENCRYPTED_PASSWORD, ORG_NAME);
      assertInstanceOf(Account.class, organization);
  }

  @Test
  public void organizationConstructorTest() {
    Organization organization = new Organization(ORG_EMAIL, ENCRYPTED_PASSWORD, ORG_NAME);
    assertEquals(ORG_EMAIL, organization.getEmail());
    assertEquals(ENCRYPTED_PASSWORD, organization.getEncryptedPassword());
    assertEquals(ORG_NAME, organization.getName());
    assertNull(organization.getDescription());
    assertNull(organization.getImage());
  }

  @Test
  public void organizationConstructorWithDescriptionAndImageTest() {
    Organization organization = new Organization(ORG_EMAIL, ENCRYPTED_PASSWORD, ORG_NAME, ORG_DESCRIPTION, ORG_IMAGE);
    assertEquals(ORG_EMAIL, organization.getEmail());
    assertEquals(ENCRYPTED_PASSWORD, organization.getEncryptedPassword());
    assertEquals(ORG_NAME, organization.getName());
    assertEquals(ORG_DESCRIPTION, organization.getDescription());
    assertEquals(ORG_IMAGE, organization.getImage());
  }

}
