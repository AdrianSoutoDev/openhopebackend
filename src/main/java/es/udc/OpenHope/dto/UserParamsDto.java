package es.udc.OpenHope.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserParamsDto {
    @NotNull(message="{dto.common.email.NotNull}")
    @NotEmpty(message="{dto.common.email.NotEmpty}")
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",flags = Pattern.Flag.CASE_INSENSITIVE, message = "{dto.common.email.Email}")
    private String email;

    @NotNull(message="{dto.common.password.NotNull}")
    @NotEmpty(message="{dto.common.password.NotEmpty}")
    private String password;
}
