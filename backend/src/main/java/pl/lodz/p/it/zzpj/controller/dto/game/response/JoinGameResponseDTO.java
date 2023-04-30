package pl.lodz.p.it.zzpj.controller.dto.game.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinGameResponseDTO {
    private List<String> players;
}