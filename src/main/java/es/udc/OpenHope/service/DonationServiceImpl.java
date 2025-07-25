package es.udc.OpenHope.service;

import es.udc.OpenHope.dto.ConfirmDonationDto;
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
  @Transactional
  public ConfirmDonationDto confirm(Long id, String status, String owner) {
    ConfirmDonationDto confirmDonationDto = new ConfirmDonationDto();
    Optional<Donation> donationOptional = donationRepository.findById(id);

    if(donationOptional.isEmpty()) {
      throw new NoSuchElementException(Messages.get("validation.donation.not.exists"));
    }

    String donator = donationOptional.get().getBankAccount().getAccount().getEmail();

    if(!owner.equals(donator)) {
      throw new SecurityException(Messages.get("validation.donate.confirm.not.allowed"));
    }

    confirmDonationDto.setConfirmed("OK".equals(status));

    if(confirmDonationDto.isConfirmed()) {
      Donation donation = donationOptional.get();
      DonationMapper.toConfirmDonationDto(confirmDonationDto, donation);
      donation.setConfirmed(true);
      donationRepository.save(donation);
    }

    return confirmDonationDto;
  }

  @Override
  public void delete(Long id) {
    donationRepository.deleteById(id);
  }
}
