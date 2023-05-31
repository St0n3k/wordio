package pl.lodz.p.it.zzpj.controller.dto.game.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.zzpj.model.Game;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinishedGameResponseDTO {
    private Game game;
}
