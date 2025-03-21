package es.udc.OpenHope.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class OrganizationParamsDto {
  @NotNull(message="{dto.common.email.NotNull}")
  @NotEmpty(message="{dto.common.email.NotEmpty}")
  @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",flags = Pattern.Flag.CASE_INSENSITIVE, message = "{dto.common.email.Email}")
  private String email;

  @NotNull(message="{dto.common.password.NotNull}")
  @NotEmpty(message="{dto.common.password.NotEmpty}")
  private String password;

  @NotNull(message="{OrganizationParamsDto.name.NotNull}")
  @NotEmpty(message="{OrganizationParamsDto.name.NotEmpty}")
  private String name;

  private String description;
  private MultipartFile file;
}