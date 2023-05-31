package pl.lodz.p.it.zzpj.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Round implements Serializable {

    private final Map<String, List<CheckedWord>> answers = new HashMap<>();
    private char letter;
    private int playersVoted = 0;

    public Round(char letter) {
        this.letter = letter;
    }

    public void incrementPlayersVoted() {
        this.playersVoted++;
    }
}
