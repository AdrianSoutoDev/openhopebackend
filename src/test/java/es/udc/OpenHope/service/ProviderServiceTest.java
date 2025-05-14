package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.AspspDto;
import es.udc.OpenHope.exception.ProviderException;
import es.udc.OpenHope.service.providers.ProviderService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ProviderServiceTest {

  private final ProviderService providerService;

  @Autowired
  public ProviderServiceTest(final ProviderService providerService) {
    this.providerService = providerService;
  }

  @Test
  @Disabled
  public void getAspspsTest() throws ProviderException {
    List<AspspDto> aspspDtos = providerService.getAspsps();
    assertFalse(aspspDtos.isEmpty());
  }

}
