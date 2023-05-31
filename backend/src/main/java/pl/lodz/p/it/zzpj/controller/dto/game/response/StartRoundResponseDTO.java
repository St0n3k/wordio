package pl.lodz.p.it.zzpj.controller.dto.game.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartRoundResponseDTO {
    private List<String> categories;
    private char letter;
}
