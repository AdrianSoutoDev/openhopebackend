package es.udc.OpenHope.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ValidateDonationDto extends DonationDto {
  private boolean validated;
}
