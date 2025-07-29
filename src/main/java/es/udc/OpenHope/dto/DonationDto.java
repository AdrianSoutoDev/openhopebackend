package es.udc.OpenHope.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DonationDto {
  private Long id;
  private CampaignDto campaign;
  private BankAccountDto bankAccount;
  private Float amount;
  private LocalDateTime date;
}
