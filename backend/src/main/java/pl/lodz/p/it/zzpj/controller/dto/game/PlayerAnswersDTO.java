package pl.lodz.p.it.zzpj.controller.dto.game;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAnswersDTO {

    @NotNull
    private String username;
    @NotNull
    private List<String> answers;
}
