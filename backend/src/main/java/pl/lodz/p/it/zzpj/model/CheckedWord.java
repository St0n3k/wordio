package pl.lodz.p.it.zzpj.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CheckedWord implements Serializable {
    private String word;
    private boolean valid;
    private int positiveVotes;

    public CheckedWord(String word, boolean valid) {
        this.word = word;
        this.valid = valid;
    }

    public void incrementPositiveVotes() {
        this.positiveVotes++;
    }

    public void decrementPositiveVotes() {
        this.positiveVotes--;
    }
}
