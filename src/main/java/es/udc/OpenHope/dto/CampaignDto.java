package es.udc.OpenHope.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CampaignDto {
  private Long id;
  private String name;
  private LocalDate startAt;
  private LocalDate dateLimit;
  private Long economicTarget;
  private String image;
  private Float minimumDonation;
  private String description;
  private OrganizationDto organization;
  private List<CategoryDto> categories;
  private Boolean isOnGoing;
  private Float amountCollected;
  private Float percentageCollected;

  public CampaignDto isOnGoing(Boolean isOnGoing) {
    this.isOnGoing = isOnGoing;
    return this;
  }

  public CampaignDto amountCollected(Float amountCollected) {
    this.amountCollected = amountCollected;
    return this;
  }

  public CampaignDto percentageCollected(Float percentageCollected) {
    this.percentageCollected = percentageCollected;
    return this;
  }
}
