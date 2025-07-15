package es.udc.OpenHope.service.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.udc.OpenHope.dto.*;
import es.udc.OpenHope.dto.client.*;
import es.udc.OpenHope.dto.mappers.BankAccountMapper;
import es.udc.OpenHope.enums.Provider;
import es.udc.OpenHope.exception.*;
import es.udc.OpenHope.exception.ProviderException;
import es.udc.OpenHope.model.*;
import es.udc.OpenHope.repository.*;
import es.udc.OpenHope.service.CampaignService;
import es.udc.OpenHope.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service("redSysProviderService")
@RequiredArgsConstructor
public class RedSysProviderServiceImpl implements ProviderService {

  private final RedSysProviderRepository redSysProviderRepository;
  private final ConsentRepository consentRepository;
  private final AccountRepository accountRepository;
  private final CampaignRepository campaignRepository;
  private final BankAccountRepository bankAccountRepository;
  private final CampaignService campaignService;

  private static final String PRIVATE_KEY_HEADER = "-----BEGIN RSA PRIVATE KEY-----";
  private static final String PRIVATE_KEY_FOOTER = "-----END RSA PRIVATE KEY-----";
  private static final String CERTIFICATE_HEADER = "-----BEGIN CERTIFICATE-----";
  private static final String CERTIFICATE_FOOTER = "-----END CERTIFICATE-----";
  private static final String ALGORITHM_HEADER = "algorithm=\"SHA-256\"";
  private static final String HEADERS_HEADER = "headers=\"digest x-request-id\"";
  private static final String PAYMENT_TYPE = "instant-sepa-credit-transfers";

  @Value("${redsys.rsa.privateKey.file.path}")
  private String privateKey;

  @Value("${redsys.rsa.certificate.file.path}")
  private String certificate;

  @Value("${redsys.client.id}")
  private String redSysClientId;

  @Value("${redsys.api.url}")
  private String redSysApiUrl;

  @Value("${redsys.api.get.aspsp.endpoint}")
  private String aspspEndpoint;

  @Value("${redsys.api.get.accounts.endpoint}")
  private String accountsEndpoint;

  @Value("${redsys.oauth.uri}")
  private String oauthEndpoint;

  @Value("${redsys.oauth.callback.uri}")
  private String oauthCallback;

  @Value("${redsys.oauth.challenge}")
  private String oauthChallenge;

  @Value("${redsys.oauth.challenge.method}")
  private String oauthChallengeMethod;

  @Value("${redsys.oauth.code.verifier}")
  private String oauthCodeVerifier;

  @Value("${redsys.api.post.consent.endpoint}")
  private String createConsentEndpoint;

  @Value("${frontend.base.url}")
  private String frontendBaseUrl;

  @Value("${redsys.api.post.payment.endpoint}")
  private String initPaymentEndpoint;

  @Override
  public List<AspspDto> getAspsps() throws ProviderException {
    try {
      CommonHeadersDto commonHeadersDto = getCommonHeaders("");
      String uri = redSysApiUrl + aspspEndpoint;
      List<AspspClientDto> response = redSysProviderRepository.getAspsps(commonHeadersDto, uri);

      return response.stream()
          .map(a -> new AspspDto(a.getName(), a.getApiName(), Provider.REDSYS))
          .sorted(Comparator.comparing(AspspDto::getName))
          .toList();
    } catch(Exception e) {
      throw new ProviderException(e.getMessage());
    }
  }

  @Override
  public ProviderAuthDto getOAuthUri(String aspsp, Integer campaign, Integer userId) throws ProviderException {
    try {
      StringBuilder sb = new StringBuilder(redSysApiUrl)
          .append(oauthEndpoint)
          .append(aspsp)
          .append("/authorize")
          .append("?response_type=code")
          .append("&client_id=").append(redSysClientId)
          .append("&redirect_uri=").append(oauthCallback)
          .append("&scope=PIS%20AIS%20SVA")
          .append("&state=").append("provider=").append(Provider.REDSYS).append(",aspsp=").append(aspsp);

      if(campaign != null) {
        sb.append(",campaign=").append(campaign);
      } else if(userId != null) {
        sb.append(",user=").append(userId);
      }

      sb.append("&code_challenge=").append(oauthChallenge)
          .append("&code_challenge_method=").append(oauthChallengeMethod);

      ProviderAuthDto providerAuthDto = new ProviderAuthDto();
      providerAuthDto.setUri(sb.toString());
      return providerAuthDto;
    } catch(Exception e) {
      throw new ProviderException(e.getMessage());
    }
  }

  @Override
  public CredentialsDto authorize(String code, String aspsp) throws ProviderException {
    try {

      StringBuilder uri = new StringBuilder(redSysApiUrl)
          .append(oauthEndpoint)
          .append(aspsp)
          .append("/token");

      return redSysProviderRepository.authorize(redSysClientId, code, oauthCallback, oauthCodeVerifier, uri.toString());

    }catch(Exception e) {
      throw new ProviderException(e.getMessage());
    }
  }

  @Override
  public CredentialsDto refreshToken(String refreshToken, String aspsp) throws ProviderException, UnauthorizedException {
    try {
      StringBuilder uri = new StringBuilder(redSysApiUrl)
          .append(oauthEndpoint)
          .append(aspsp)
          .append("/token");

      return redSysProviderRepository.refreshToken(redSysClientId, refreshToken, uri.toString());

    } catch (HttpClientErrorException e){
      if(e.getStatusCode().is4xxClientError()){
        throw new UnauthorizedException(e.getMessage());
      } else {
        throw new ProviderException(e.getMessage());
      }
    }
  }

  @Override
  @Transactional
  public DonationDto initPayment(String tokenOAuth, String ipClient, Long bankAccountId, String owner, Long campaignId, Float amount) throws ProviderException, UnauthorizedException, MissingBankAccountException, CampaignFinalizedException {

    Optional<Campaign> campaignOptional = campaignRepository.findById(campaignId);
    if(campaignOptional.isEmpty()) {
      throw new NoSuchElementException(Messages.get("validation.campaign.not.exists"));
    }

    if(campaignOptional.get().getFinalizedDate() != null) {
      throw new CampaignFinalizedException(Messages.get("validation.campaign.finalized"));
    }

    if(campaignOptional.get().getBankAccount() == null) {
      throw new MissingBankAccountException(Messages.get("validation.bank.account.on.campaign"));
    }

    Account account = accountRepository.getUserByEmailIgnoreCase(owner);
    Optional<BankAccount> bankAccountOptional = bankAccountRepository.findById(bankAccountId);

    if(bankAccountOptional.isEmpty() || !bankAccountOptional.get().getAccount().equals(account) ){
      throw new SecurityException(Messages.get("validation.donate"));
    }

    try {
      BankAccount bankAccountDestiny = campaignOptional.get().getBankAccount();
      BankAccount bankAccountOrigin = bankAccountOptional.get();

      PostInitPaymentDto postInitPaymentDto = getPostInitPaymentDto(amount, bankAccountOrigin, bankAccountDestiny);

      String body = new ObjectMapper().writeValueAsString(postInitPaymentDto);

      CommonHeadersDto commonHeadersDto = getCommonHeaders(body);
      String aspsp = bankAccountOrigin.getAspsp().getCode();

      String uri = redSysApiUrl + initPaymentEndpoint
          .replace("{aspsp}", aspsp)
          .replace("{payment-product}", PAYMENT_TYPE);

      PostInitPaymentClientDto response = redSysProviderRepository.postInitPayment(commonHeadersDto, uri, body, ipClient,
          "Bearer ".concat(tokenOAuth));

      return campaignService.addDonation(campaignOptional.get(), bankAccountOrigin, amount, Date.valueOf(LocalDate.now()));

    } catch (UnauthorizedException e){
        throw e;
    } catch(Exception e) {
      throw new ProviderException(e.getMessage());
    }
  }

  private static PostInitPaymentDto getPostInitPaymentDto(Float amount, BankAccount bankAccountOrigin, BankAccount bankAccountDestiny) {
    AccountReferenceDto accountReferenceOriginDto = new AccountReferenceDto(bankAccountOrigin.getIban(), bankAccountOrigin.getCurrency());

    AccountReferenceDto accountReferenceDestinyDto = new AccountReferenceDto(bankAccountDestiny.getIban(), bankAccountDestiny.getCurrency());

    AmountDto amountDto = new AmountDto(bankAccountDestiny.getCurrency(), amount.toString());

    AddressDto addressDto = new AddressDto("ES", "");

    PostInitPaymentDto postInitPaymentDto = new PostInitPaymentDto(bankAccountOrigin.getOwnerName(), accountReferenceOriginDto, amountDto
    ,accountReferenceDestinyDto, bankAccountDestiny.getOwnerName(), addressDto);

    return postInitPaymentDto;
  }

  @Transactional
  public List<BankAccountDto> getAccounts(String aspsp, String tokenOAuth, String ipClient, String consentId) throws ProviderException, UnauthorizedException, ConsentInvalidException {
    try {
      CommonHeadersDto commonHeadersDto = getCommonHeaders("");
      String uri = redSysApiUrl + accountsEndpoint.replace("{aspsp}", aspsp);
      List<AccountClientDto> accountClientDtos = redSysProviderRepository.getAccounts(commonHeadersDto, uri, consentId, "Bearer ".concat(tokenOAuth));
      return BankAccountMapper.toBankAccountDto(accountClientDtos);
    } catch (UnauthorizedException | ConsentInvalidException e){
      throw e;
    } catch(Exception e) {
      throw new ProviderException(e.getMessage());
    }
  }

  @Transactional
  public PostConsentClientDto createConsent(String owner, String aspsp, String token, String ipClient, Integer campaignId, Integer userId) throws ProviderException, UnauthorizedException {
    try {
      LocalDate dateNowPlus60Days = LocalDate.now().plusDays(60);
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      String dateFormated = dateNowPlus60Days.format(formatter);

      AccessDto accessDto = new AccessDto("allAccounts");
      PostConsentDto postConsentDto = new PostConsentDto(accessDto, true, dateFormated, 5, false);
      String body = new ObjectMapper().writeValueAsString(postConsentDto);

      CommonHeadersDto commonHeadersDto = getCommonHeaders(body);

      String uri = redSysApiUrl + createConsentEndpoint.replace("{aspsp}", aspsp);

      StringBuilder sb = new StringBuilder(frontendBaseUrl)
          .append("openbanking/bank-selection")
          .append("?aspsp=").append(aspsp);

      if(campaignId != null) {
        sb.append("&campaign=").append(campaignId);
      } else if(userId != null) {
        sb.append("&user=").append("me");
      }

      PostConsentClientDto response = redSysProviderRepository.postConsent(commonHeadersDto, uri, body, aspsp, ipClient,
          "Bearer ".concat(token), sb.toString());

      Account account = accountRepository.getUserByEmailIgnoreCase(owner);
      Consent consent = new Consent();
      consent.setConsentId(response.getConsentId());
      consent.setAspsp(aspsp);
      consent.setProvider(Provider.REDSYS.toString());
      consent.setAccount(account);
      consentRepository.save(consent);

      return response;
    } catch (UnauthorizedException e){
      throw e;
    } catch(Exception e) {
      throw new ProviderException(e.getMessage());
    }
  }

  private String hashSha256Base64(String value) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = digest.digest(value.getBytes());
    return Base64.getEncoder().encodeToString(hashBytes);
  }

  private String sign(String value, String privKey) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    String key = readPrivateKeyFromFile(privKey);
    byte[] decodedKey = Base64.getDecoder().decode(key);

    //Get private Key
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

    //Sign
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initSign(privateKey);
    signature.update(value.getBytes());
    byte[] signedBytes = signature.sign();

    return Base64.getEncoder().encodeToString(signedBytes);
  }

  private String readPrivateKeyFromFile(String path) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(path);

    assert inputStream != null;
    String privateKeyPEM = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    privateKeyPEM = privateKeyPEM.replace(PRIVATE_KEY_HEADER, "")
        .replace(PRIVATE_KEY_FOOTER, "")
        .replaceAll("\\s", "");

    return privateKeyPEM;
  }

  private String getKeyIdFromCertificate(String certificate) throws CertificateException, IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(certificate);

    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509Certificate cert = (X509Certificate) cf.generateCertificate(inputStream);
    assert inputStream != null;
    inputStream.close();

    String serialNumber = cert.getSerialNumber().toString();
    String issuer = cert.getIssuerX500Principal().getName();

    return String.format("SN=%s,CA=%s", serialNumber, issuer).replace(" ", "");
  }

  private String getCertificateContent(String certificate)  throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    {
      InputStream inputStream = classLoader.getResourceAsStream(certificate);
      StringBuilder certContent = new StringBuilder();
      assert inputStream != null;
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
        String line;
        while ((line = reader.readLine()) != null) {
          certContent.append(line).append("\n");
        }
      }

      return  certContent.toString()
          .replace(CERTIFICATE_HEADER, "")
          .replace(CERTIFICATE_FOOTER, "")
          .replaceAll("\\s", "");
    }
  }

  private CommonHeadersDto getCommonHeaders(String body) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, SignatureException, InvalidKeyException, CertificateException {
    String bodyHashed = hashSha256Base64(body);
    String digest = "SHA-256=" + bodyHashed;
    String xRequestID = UUID.randomUUID().toString();

    String digestRequestId = "digest: " + digest + "\n" + "x-request-id: " + xRequestID;
    String digestRequestIdSigned = sign(digestRequestId, privateKey);

    String keyId = getKeyIdFromCertificate(certificate);
    String keyIdHeader = "keyId=\"" + keyId + "\"";

    String signatureForHeader = "signature=\"" + digestRequestIdSigned + "\"";

    String signature = keyIdHeader + "," + ALGORITHM_HEADER + "," + HEADERS_HEADER + "," + signatureForHeader;
    String certificateContent = getCertificateContent(certificate);

    CommonHeadersDto commonHeadersDto = new CommonHeadersDto();
    commonHeadersDto.setDigest(digest);
    commonHeadersDto.setSignature(signature);
    commonHeadersDto.setCertificateContent(certificateContent);
    commonHeadersDto.setXRequestID(xRequestID);
    commonHeadersDto.setClientId(redSysClientId);
    return commonHeadersDto;
  }
}
