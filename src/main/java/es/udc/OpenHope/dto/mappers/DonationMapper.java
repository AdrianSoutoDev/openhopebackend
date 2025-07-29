package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.BankAccountDto;
import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.ValidateDonationDto;
import es.udc.OpenHope.dto.DonationDto;
import es.udc.OpenHope.model.Donation;

public abstract class DonationMapper {

  public static DonationDto toDonationDto(Donation donation){
    DonationDto donationDto = new DonationDto();

    donationDto.setId(donation.getId());
    donationDto.setAmount(donation.getAmount());
    donationDto.setDate(donation.getDate().toLocalDateTime());

    CampaignDto campaignDto = CampaignMapper.toCampaignDto((donation.getCampaign()));
    donationDto.setCampaign(campaignDto);

    BankAccountDto bankAccountDto = BankAccountMapper.toBankAccountDto(donation.getBankAccount());
    donationDto.setBankAccount(bankAccountDto);

    return donationDto;
  }

  public static ValidateDonationDto toConfirmDonationDto(ValidateDonationDto validateDonationDto, Donation donation){
    validateDonationDto.setAmount(donation.getAmount());
    validateDonationDto.setDate(donation.getDate().toLocalDateTime());

    CampaignDto campaignDto = CampaignMapper.toCampaignDto((donation.getCampaign()));
    validateDonationDto.setCampaign(campaignDto);

    BankAccountDto bankAccountDto = BankAccountMapper.toBankAccountDto(donation.getBankAccount());
    validateDonationDto.setBankAccount(bankAccountDto);

    return validateDonationDto;
  }
}
