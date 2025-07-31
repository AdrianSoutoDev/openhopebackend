package es.udc.OpenHope.controller;

import es.udc.OpenHope.service.ResourceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ResourceControllerTest {

  @Value("${upload.dir}")
  private String uploadDir;

  private String createdFileName = null;

  private final MockMvc mockMvc;
  private final ResourceService resourceService;

  @Autowired
  public ResourceControllerTest(final MockMvc mockMvc, final ResourceService resourceService) {
    this.mockMvc = mockMvc;
    this.resourceService = resourceService;
  }

  @AfterEach
  public void cleanUp() throws IOException {
    if (createdFileName != null) {
      resourceService.remove(createdFileName);
    }
  }

  private MockMultipartFile getTestImg() throws IOException {
    ClassPathResource resource = new ClassPathResource("test-images/test-image.png");
    byte[] fileContent = Files.readAllBytes(resource.getFile().toPath());
    return new MockMultipartFile(
        "file",
        "test-image.png",
        "image/png",
        fileContent
    );
  }

  @Test
  public void getImageTest() throws Exception {
    MockMultipartFile testImage = getTestImg();
    createdFileName = resourceService.save(testImage);

    ResultActions result = mockMvc.perform(get("/api/resources/".concat(createdFileName)));

    result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_PNG));
  }

  @Test
  public void getImageNotFoundTest() throws Exception {
    ResultActions result = mockMvc.perform(get("/api/resources/some-image-name.jpg"));
    result.andExpect(status().isNotFound());
  }

}
