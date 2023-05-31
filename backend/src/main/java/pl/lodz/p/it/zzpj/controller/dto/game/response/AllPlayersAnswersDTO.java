package pl.lodz.p.it.zzpj.controller.dto.game.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.zzpj.model.CheckedWord;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AllPlayersAnswersDTO implements Serializable {

    @NotNull
    private Map<String, List<CheckedWord>> answers;
}
