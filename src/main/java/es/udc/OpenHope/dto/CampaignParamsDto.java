package es.udc.OpenHope.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class CampaignParamsDto {

  @NotNull(message="{dto.common.name.NotNull}")
  @NotEmpty(message="{dto.common.name.NotEmpty}")
  private String name;

  @NotNull(message="{dto.common.name.organizationId.NotNull}")
  private Long organizationId;

  @NotNull(message="{dto.common.name.startAt.NotNull}")
  private LocalDate startAt;

  private LocalDate dateLimit;
  private Long economicTarget;
  private Float minimumDonation;

  private String description;
  private List<String> categories;


}
