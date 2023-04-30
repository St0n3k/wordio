package pl.lodz.p.it.zzpj.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.UUID;

@Getter
@NoArgsConstructor
@RedisHash("Game")
public class Game implements Serializable {

    UUID id;
    int countdownTime;
    int maxRoundLenght;

    Stack<Round> rounds = new Stack<>();
    Stack<Round> played = new Stack<>();
    String authorName;
    List<String> categories = new ArrayList<>();
    List<String> players = new ArrayList<>();

    public Game(int numberOfRounds, int countdownTime, int maxRoundLenght, String authorName, List<String> categories) {
        this.id = UUID.randomUUID();
        this.countdownTime = countdownTime;
        this.maxRoundLenght = maxRoundLenght;
        this.authorName = authorName;
        this.categories = categories;
        String letters = "abcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        for (int i = 0; i < numberOfRounds; i++) {
            char letter = letters.charAt(random.nextInt(letters.length() - 1));
            rounds.push(new Round(letter));
            letters = letters.replace(String.valueOf(letter), "");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Game game = (Game) o;

        return new EqualsBuilder().append(id, game.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}