package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.BankAccountDto;
import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.DonationDto;
import es.udc.OpenHope.model.Donation;

public abstract class DonationMapper {

  public static DonationDto toDonationDto(Donation donation){
    DonationDto donationDto = new DonationDto();

    donationDto.setAmount(donation.getAmount());
    donationDto.setDate(donation.getDate().toLocalDate());

    CampaignDto campaignDto = CampaignMapper.toCampaignDto((donation.getCampaign()));
    donationDto.setCampaign(campaignDto);

    BankAccountDto bankAccountDto = BankAccountMapper.toBankAccountDto(donation.getBankAccount());
    donationDto.setBankAccount(bankAccountDto);

    return donationDto;
  }
}
