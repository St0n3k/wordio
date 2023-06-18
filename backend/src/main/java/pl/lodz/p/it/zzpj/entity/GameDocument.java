package pl.lodz.p.it.zzpj.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.lodz.p.it.zzpj.model.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString
@Document("game")
@NoArgsConstructor
public class GameDocument {
    @Id
    @NotNull
    private UUID id;

    @NotNull
    @NotNull
    @Size(min = 2)
    private List<String> players = new ArrayList<>();

    @NotNull
    @Size(min = 2)
    private List<@NotBlank String> categories = new ArrayList<>();

    @NotNull
    @NotBlank
    private String authorName;

    @NotNull
    private Map<@NotBlank String, @PositiveOrZero Integer> scores = new HashMap<>();

    public GameDocument(
        @NotNull UUID id,
        @NotNull List<String> players,
        @NotNull List<@NotBlank String> categories,
        @NotNull String authorName,
        @NotNull Map<@NotBlank String, @PositiveOrZero Integer> scores) {
        this.players.addAll(players);
        this.categories.addAll(categories);
        this.authorName = authorName;
        this.scores.putAll(scores);
        this.id = id;
    }

    public static GameDocument of(Game game) {
        return new GameDocument(
            game.getId(),
            game.getPlayers(),
            game.getCategories(),
            game.getAuthorName(),
            game.getScores());
    }
}
