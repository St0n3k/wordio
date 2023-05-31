package pl.lodz.p.it.zzpj.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerVotesDTO {

    @NotNull
    private String username;

    @NotNull
    private Map<String, List<Boolean>> votes;
}
