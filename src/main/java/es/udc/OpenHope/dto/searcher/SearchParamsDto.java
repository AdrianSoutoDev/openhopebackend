package es.udc.OpenHope.dto.searcher;

import es.udc.OpenHope.enums.CampaignFinalizeType;
import es.udc.OpenHope.enums.CampaignState;
import es.udc.OpenHope.enums.EntityType;
import es.udc.OpenHope.enums.SortCriteria;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SearchParamsDto {
  private String text;
  private EntityType show;
  private SortCriteria sortCriteria;
  private List<String> categories;
  private LocalDate startDateFrom;
  private LocalDate startDateTo;
  private CampaignState campaignState;
  private CampaignFinalizeType campaignFinalizeType;
  private boolean hasMinimumDonation;
  private boolean hasCampaignsOnGoing;
  private LocalDate finalizeDateFrom;
  private LocalDate finalizeDateTo;
  private Long economicTargetFrom;
  private Long economicTargetTo;
  private Long minimumDonationFrom;
  private Long minimumDonationTo;
}
