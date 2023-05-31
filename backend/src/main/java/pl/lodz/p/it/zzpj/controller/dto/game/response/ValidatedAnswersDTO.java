package pl.lodz.p.it.zzpj.controller.dto.game.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.zzpj.model.CheckedWord;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ValidatedAnswersDTO {

    @NotNull
    private Character letter;
    @NotNull
    private Map<String, Map<String, CheckedWord>> answers;
}
