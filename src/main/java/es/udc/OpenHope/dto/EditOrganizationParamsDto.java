package es.udc.OpenHope.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class EditOrganizationParamsDto {

  private Long id;

  @NotNull(message="{OrganizationParamsDto.name.NotNull}")
  @NotEmpty(message="{OrganizationParamsDto.name.NotEmpty}")
  private String name;

  private String description;
  private List<String> categories;
  private List<String> topics;
}
