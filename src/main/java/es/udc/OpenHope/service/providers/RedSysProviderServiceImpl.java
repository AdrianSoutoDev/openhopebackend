package es.udc.OpenHope.service.providers;

import es.udc.OpenHope.dto.AspspDto;
import es.udc.OpenHope.dto.client.AspspClientDto;
import es.udc.OpenHope.exception.ProviderException;
import es.udc.OpenHope.repository.RedSysProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service("redSysProviderService")
@RequiredArgsConstructor
public class RedSysProviderServiceImpl implements ProviderService {

  private final RedSysProviderRepository redSysProviderRepository;

  private static final String PRIVATE_KEY_HEADER = "-----BEGIN RSA PRIVATE KEY-----";
  private static final String PRIVATE_KEY_FOOTER = "-----END RSA PRIVATE KEY-----";
  private static final String CERTIFICATE_HEADER = "-----BEGIN CERTIFICATE-----";
  private static final String CERTIFICATE_FOOTER = "-----END CERTIFICATE-----";
  private static final String ALGORITHM_HEADER = "algorithm=\"SHA-256\"";
  private static final String HEADERS_HEADER = "headers=\"digest x-request-id\"";

  @Value("${redsys.rsa.privateKey.file.path}")
  private String privateKey;

  @Value("${redsys.rsa.certificate.file.path}")
  private String certificate;

  @Value("${redsys.client.id}")
  private String redSysClientId;

  @Value("${redsys.api.url}")
  private String redSysApiUrl;

  @Value("${redsys.api.aspsp.endpoint}")
  private String aspspEndpoint;

  @Override
  public List<AspspDto> getAspsps() throws ProviderException {

    try {
      String bodyHashed = hashSha256Base64("");
      String digest = "SHA-256=" + bodyHashed;
      String xRequestID = UUID.randomUUID().toString();

      String digestRequestId = "digest: " + digest + "\n" + "x-request-id: " + xRequestID;
      String digestRequestIdSigned = sign(digestRequestId, privateKey);

      String keyId = getKeyIdFromCertificate(certificate);
      String keyIdHeader = "keyId=\"" + keyId + "\"";

      String signatureForHeader = "signature=\"" + digestRequestIdSigned + "\"";

      String signature = keyIdHeader + "," + ALGORITHM_HEADER + "," + HEADERS_HEADER + "," + signatureForHeader;
      String certificateContent = getCertificateContent(certificate);

      String uri = redSysApiUrl + aspspEndpoint;

      List<AspspClientDto> response = redSysProviderRepository.getAspsps(digest, signature, certificateContent, xRequestID, uri, redSysClientId);

      return response.stream()
          .map(a -> new AspspDto(a.getName(), a.getApiName(), Provider.REDSSYS))
          .toList();
    } catch(Exception e) {
      throw new ProviderException("");
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

  private static String getKeyIdFromCertificate(String certificate) throws CertificateException, IOException {
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

  private static String getCertificateContent(String certificate)  throws IOException {
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
}
