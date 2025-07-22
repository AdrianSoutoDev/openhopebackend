package es.udc.OpenHope.dto;

import es.udc.OpenHope.model.Donation;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ConfirmDonationDto extends DonationDto {
  private boolean isConfirmed;
}
