package es.udc.OpenHope.dto;

import lombok.Data;

@Data
public class DonationResponseDto {
  private boolean notAllowed;
  private String redirection;

  public DonationResponseDto(){
    this.notAllowed = false;
  }

  public void restart() {
   this.notAllowed = true;
   this.redirection = null;
  }
}
