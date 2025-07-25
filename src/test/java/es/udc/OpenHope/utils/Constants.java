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
  public static final String CAMPAIGN_DESCRIPTION = "La campaña sera llevada cabo con el fin de acotar las camadas de " +
      "animales domesticos que hacen vida fuera de sus hogares.";
  public static final Long ECONOMIC_TARGET = 2500L;
  public static final Float MINIMUM_DONATION = 0.50f;
  public static final LocalDate CAMPAIGN_START_AT = LocalDate.now();
  public static final LocalDate CAMPAIGN_DATE_LIMIT = LocalDate.now().plusMonths(1);

  //Aspsp
  public static final String ASPSP_CODE = "BBVA";
  public static final String ASPSP_NAME = "BANCO BILBAO VIZCAYA ARGENTARIA, S.A.";
  public static final String ASPSP_PROVIDER = "REDSYS";
  public static final String ASPSP_BIC = "BBVAESMMXXX";

  //Bank account
  public static final String BANK_IBAN = "ES2501822200160201933547";
  public static final String BANK_RESOURCE_ID = "ES018202000000000500000000361787589";
  public static final String BANK_OWNER_NAME = "Nombre Apellido1 Apellido2";
  public static final String BANK_ORIGINAL_NAME = "CUENTA PARA AHORROS";

  //donations
  public static final Float AMOUNT_DONATION = 25f;

}
