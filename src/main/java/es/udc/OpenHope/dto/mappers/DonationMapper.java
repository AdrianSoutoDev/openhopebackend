package es.udc.OpenHope.dto.mappers;

import es.udc.OpenHope.dto.BankAccountDto;
import es.udc.OpenHope.dto.CampaignDto;
import es.udc.OpenHope.dto.ConfirmDonationDto;
import es.udc.OpenHope.dto.DonationDto;
import es.udc.OpenHope.model.Donation;

public abstract class DonationMapper {

  public static DonationDto toDonationDto(Donation donation){
    DonationDto donationDto = new DonationDto();

    donationDto.setId(donation.getId());
    donationDto.setAmount(donation.getAmount());
    donationDto.setDate(donation.getDate().toLocalDate());

    CampaignDto campaignDto = CampaignMapper.toCampaignDto((donation.getCampaign()));
    donationDto.setCampaign(campaignDto);

    BankAccountDto bankAccountDto = BankAccountMapper.toBankAccountDto(donation.getBankAccount());
    donationDto.setBankAccount(bankAccountDto);

    return donationDto;
  }

  public static ConfirmDonationDto toConfirmDonationDto(ConfirmDonationDto confirmDonationDto, Donation donation){
    confirmDonationDto.setAmount(donation.getAmount());
    confirmDonationDto.setDate(donation.getDate().toLocalDate());

    CampaignDto campaignDto = CampaignMapper.toCampaignDto((donation.getCampaign()));
    confirmDonationDto.setCampaign(campaignDto);

    BankAccountDto bankAccountDto = BankAccountMapper.toBankAccountDto(donation.getBankAccount());
    confirmDonationDto.setBankAccount(bankAccountDto);

    return confirmDonationDto;
  }
}
