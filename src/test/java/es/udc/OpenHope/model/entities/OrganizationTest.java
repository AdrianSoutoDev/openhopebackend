package es.udc.OpenHope.model.entities;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrganizationTest {

  private static final String ORG_EMAIL = "org@openhope.com";
  private static final String ORG_NAME = "apadan";
  private static final String ORG_DESCRIPTION = "Asociación Protectora de Animales Domésticos Abandonados del Noroeste";
  private static final String ORG_IMAGE = "c:\\openhope\\images\\organizations\\apadan.png";
  private static final String ENCRYPTED_PASSWORD = "fa89sdfjasdpmcds9";

  @Test
  public void organizationInheritsFromRegistrationTest() {
    Organization organization = new Organization(ORG_EMAIL, ENCRYPTED_PASSWORD, ORG_NAME);
    assertTrue(organization instanceof Registration);
  }

  @Test
  public void organizationConstructorTest() {
    Organization organization = new Organization(ORG_EMAIL, ENCRYPTED_PASSWORD, ORG_NAME);
    assertEquals(organization.getEmail(), ORG_EMAIL);
    assertEquals(organization.getEncryptedPassword(), ENCRYPTED_PASSWORD);
    assertEquals(organization.getName(), ORG_NAME);
    assertNull(organization.getDescription());
    assertNull(organization.getImage());
  }

  @Test
  public void organizationConstructorWithDescriptionAndImageTest() {
    Organization organization = new Organization(ORG_EMAIL, ENCRYPTED_PASSWORD, ORG_NAME, ORG_DESCRIPTION, ORG_IMAGE);
    assertEquals(organization.getEmail(), ORG_EMAIL);
    assertEquals(organization.getEncryptedPassword(), ENCRYPTED_PASSWORD);
    assertEquals(organization.getName(), ORG_NAME);
    assertEquals(organization.getDescription(), ORG_DESCRIPTION);
    assertEquals(organization.getImage(), ORG_IMAGE);
  }

}
