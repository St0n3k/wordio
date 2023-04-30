package pl.lodz.p.it.zzpj.controller.dto.game;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Dto for creating new game.
 *
 * @param numberOfRounds       Number of rounds in a game.
 * @param maxRoundDurationTime Maximum allowed time for a single round in seconds.
 * @param countdownTime        Time for submitting answers after first player finished (in seconds).
 * @param categories           List of categories used in the game.
 */
public record CreateGameDto(
    @Positive
    @Max(10)
    int numberOfRounds,

    @Positive
    @Max(300)
    int maxRoundDurationTime,

    @PositiveOrZero
    @Max(30)
    int countdownTime,

    @NotNull
    @Size(min = 2) List<@NotBlank String> categories
) {
    public CreateGameDto(int numberOfRounds, int maxRoundDurationTime, int countdownTime) {
        this(numberOfRounds, maxRoundDurationTime, countdownTime, new ArrayList<>());
    }
}
