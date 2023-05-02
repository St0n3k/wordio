package pl.lodz.p.it.zzpj.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Game implements Serializable {

    private final Stack<Round> rounds = new Stack<>();
    private final Stack<Round> played = new Stack<>();
    private final List<String> players = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    @EqualsAndHashCode.Include
    private UUID id;
    private int countdownTime;
    private int maxRoundLength;
    private String authorName;

    @Setter
    private boolean started = false;

    public Game(int numberOfRounds, int countdownTime, int maxRoundLength, String authorName, List<String> categories) {
        this.id = UUID.randomUUID();
        this.countdownTime = countdownTime;
        this.maxRoundLength = maxRoundLength;
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
}