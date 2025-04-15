package es.udc.OpenHope.service;

import es.udc.OpenHope.utils.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ResouceServiceTest {

  @Value("${upload.dir}")
  private String uploadDir;

  private String createdFileName = null;

  private final ResourceService resourceService;
  private final Utils utils;

  @Autowired
  public ResouceServiceTest(final ResourceService resourceService, final Utils utils) {
    this.resourceService = resourceService;
    this.utils = utils;
  }

  @AfterEach
  public void cleanUp() throws IOException {
    if (createdFileName != null) {
      resourceService.remove(createdFileName);
    }
  }

  @Test
  public void saveImageTest() throws IOException {
    MockMultipartFile testImage = utils.getTestImg();
    createdFileName = resourceService.save(testImage);
    Path filePath = Path.of(uploadDir, createdFileName);
    assertTrue(Files.exists(filePath));
  }

  @Test
  public void removeImageTest() throws IOException {
    MockMultipartFile testImage = utils.getTestImg();
    String fileName = resourceService.save(testImage);
    resourceService.remove(fileName);
    Path filePath = Path.of(uploadDir, fileName);
    assertFalse(Files.exists(filePath));
  }

  @Test
  public void getImageTest() throws IOException, NoSuchAlgorithmException {
    MockMultipartFile testImage = utils.getTestImg();
    createdFileName = resourceService.save(testImage);
    Resource resource = resourceService.get(createdFileName);

    byte[] resourceBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
    byte[] mockFileBytes = testImage.getBytes();

    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] resourceHash = digest.digest(resourceBytes);
    byte[] mockFileHash = digest.digest(mockFileBytes);

    assertArrayEquals(resourceHash, mockFileHash);
  }
}
