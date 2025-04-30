package es.udc.OpenHope.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrganizationTest {

  private static final String ORG_EMAIL = "org@openhope.com";
  private static final String ORG_NAME = "apadan";
  private static final String ORG_DESCRIPTION = "Asociación Protectora de Animales Domésticos Abandonados del Noroeste";
  private static final String ORG_IMAGE = "c:\\openhope\\images\\organizations\\apadan.png";
  private static final String ENCRYPTED_PASSWORD = "$2a$16$dUrZyai4SLzT.w3NMXjfC.SgYQMyRcKyK0miEopks5RULJfl8n38G";

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
