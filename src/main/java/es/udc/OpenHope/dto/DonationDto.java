package es.udc.OpenHope.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DonationDto {
  private Long id;
  private CampaignDto campaign;
  private BankAccountDto bankAccount;
  private Float amount;
  private LocalDate date;
}
