package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.DonationDto;
import es.udc.OpenHope.dto.mappers.DonationMapper;
import es.udc.OpenHope.model.Donation;
import es.udc.OpenHope.repository.DonationRepository;
import es.udc.OpenHope.utils.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

  private final DonationRepository donationRepository;

  @Override
  public void delete(Long id) {
    donationRepository.deleteById(id);
  }

  @Override
  @Transactional
  public DonationDto updatePaymentId(Long id, String paymentId) {
    Optional<Donation> donation = donationRepository.findById(id);
    if(donation.isEmpty()) {
      throw new NoSuchElementException(Messages.get("validation.donation.not.exists"));
    }

    donation.get().setPaymentId(paymentId);
    donationRepository.save(donation.get());

    return DonationMapper.toDonationDto(donation.get());
  }
}
