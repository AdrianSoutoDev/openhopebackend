package es.udc.OpenHope.controller;

import es.udc.OpenHope.dto.DonationDto;
import es.udc.OpenHope.service.TokenService;
import es.udc.OpenHope.service.UserService;
import lombok.RequiredArgsConstructor;
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

  @GetMapping()
  public ResponseEntity<Page<DonationDto>> getDonations(@RequestHeader(name="Authorization") String token,
                                                           @RequestParam(defaultValue="0") int page,
                                                           @RequestParam(defaultValue="5") int size) {
    String owner = tokenService.extractsubject(token);
    Page<DonationDto> donations = userService.getDonations(owner, page, size);

    return ResponseEntity.ok(donations);
  }
}
