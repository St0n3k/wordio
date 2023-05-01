package pl.lodz.p.it.zzpj.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class Round implements Serializable {

    private final Map<String, List<String>> answers = new HashMap<>();
    private char letter;

    public Round(char letter) {
        this.letter = letter;
    }
}
