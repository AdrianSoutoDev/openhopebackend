package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.ConfirmDonationDto;

public interface DonationService {
  ConfirmDonationDto confirm(Long id, String status, String owner);
  void delete(Long id);
}
