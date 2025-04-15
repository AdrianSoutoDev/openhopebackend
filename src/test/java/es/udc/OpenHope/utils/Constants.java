package es.udc.OpenHope.utils;

import java.time.LocalDate;

public abstract class Constants {

  //common
  public static final String PASSWORD = "12345abc?";
  public static final String ENCRYPTED_PASSWORD = "$2a$16$dUrZyai4SLzT.w3NMXjfC.SgYQMyRcKyK0miEopks5RULJfl8n38G";

  public static final String CATEGORY_1 = "CATEGORY 1";
  public static final String CATEGORY_2 = "CATEGORY 2";
  public static final String CATEGORY_3 = "CATEGORY 3";
  public static final String CATEGORY_4 = "CATEGORY 4";

  //user
  public static final String USER_EMAIL = "user@openhope.com";

  //organization
  public static final String ORG_EMAIL = "org@openhope.com";
  public static final String ORG_NAME = "Apadan";
  public static final String ORG_DESCRIPTION = "Asociación Protectora de Animales Domésticos Abandonados del Noroeste";
  public static final String ORG_IMAGE = "c:\\openhope\\images\\organizations\\apadan.png";

  //campaign
  public static final String CAMPAIGN_NAME = "Campaña de esterilización";
  public static final LocalDate CAMPAIGN_START_AT = LocalDate.now();
  public static final LocalDate CAMPAIGN_DATE_LIMIT = LocalDate.now().plusMonths(1);

}
