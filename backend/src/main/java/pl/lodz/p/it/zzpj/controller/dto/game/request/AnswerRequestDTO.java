package pl.lodz.p.it.zzpj.controller.dto.game.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UUID;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequestDTO {
    @Setter
    @UUID
    private String id;

    @NotNull
    private Map<String, List<String>> answers;
}
