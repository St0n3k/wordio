package pl.lodz.p.it.zzpj.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@NoArgsConstructor
public class Round implements Serializable {

    private char letter;
    private Map<String, List<String>> answers = new HashMap<>();

    public Round(char letter) {
        this.letter = letter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Round round = (Round) o;

        return new EqualsBuilder().append(letter, round.letter)
            .append(answers, round.answers).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(letter).append(answers).toHashCode();
    }
}
