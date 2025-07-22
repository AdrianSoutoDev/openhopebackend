package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.ConfirmDonationDto;
import es.udc.OpenHope.dto.DonationDto;
import es.udc.OpenHope.exception.ProviderException;
import es.udc.OpenHope.service.DonationService;
import es.udc.OpenHope.service.TokenService;
import es.udc.OpenHope.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/donations")
@Validated
public class DonationController {

  private final TokenService tokenService;
  private final UserService userService;
  private final DonationService donationService;

  @Value("${frontend.base.url}")
  private String frontendBaseUrl;

  @GetMapping()
  public ResponseEntity<Page<DonationDto>> getDonations(@RequestHeader(name="Authorization") String token,
                                                           @RequestParam(defaultValue="0") int page,
                                                           @RequestParam(defaultValue="5") int size) {
    String owner = tokenService.extractsubject(token);
    Page<DonationDto> donations = userService.getDonations(owner, page, size);

    return ResponseEntity.ok(donations);
  }

  @GetMapping("/payment/callback")
  public ResponseEntity<ConfirmDonationDto> paymentCallback(@RequestParam(value = "status") String status,
                       @RequestParam(value = "donation") Long donation,
                       @RequestHeader(name="Authorization") String token) {

    String owner = tokenService.extractsubject(token);
    ConfirmDonationDto confirmDonationDto = donationService.confirm(donation, status, owner);

    return ResponseEntity.ok(confirmDonationDto);
  }
}
