package pl.lodz.p.it.zzpj.controller.dto.account;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessfulLoginDTO {
    @NotNull
    private String jwt;
}
