package pl.lodz.p.it.zzpj.controller.dto.mok;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDTO {
    @NotNull
    private String oldPassword;

    @NotNull
    private String newPassword;
}
