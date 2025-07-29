package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.DonationDto;

public interface DonationService {
  void delete(Long id);
  DonationDto updatePaymentId(Long id, String paymentId);
}
