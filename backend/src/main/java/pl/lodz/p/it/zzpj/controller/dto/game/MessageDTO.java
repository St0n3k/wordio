package pl.lodz.p.it.zzpj.controller.dto.game;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    @NotBlank
    private String message;
}
