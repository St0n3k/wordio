package pl.lodz.p.it.zzpj.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.IntStream;

@Getter
@NoArgsConstructor
@RedisHash("Game")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
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

    // username -> score
    private final Map<String, Integer> scores = new HashMap<>();

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

    public void addPlayer(String username) {
        this.getPlayers().add(username);
        this.scores.put(username, 0);
    }

    public void assignPointsForLastFinishedRound() {
        Round lastRound;
        try {
            lastRound = played.peek();
        } catch (EmptyStackException esx) {
            return;
        }

        var answers = lastRound.getAnswers();

        // Create an ordered collection
        var playerNames = new ArrayList<>(answers.keySet());

        IntStream.range(0, this.categories.size()) // for each category
            .mapToObj(i -> playerNames.stream()
                .map(answers::get)                 // get answers for player
                .map(list -> list.get(i))          // get word for category
                .toList())                         // collect to list
            .parallel()                            // parallel to speed up the process
            .map(checkedWords -> assignPointsInCategory(checkedWords, lastRound))
            .forEach(pointsInCategoryForUsers -> {
                for (int i = 0; i < playerNames.size(); i++) {
                    String name = playerNames.get(i);
                    int currentScore = scores.get(name);

                    int pointsGained = pointsInCategoryForUsers.get(i);
                    scores.put(name, currentScore + pointsGained);
                }
            });
    }

    private List<Integer> assignPointsInCategory(List<CheckedWord> checkedWords, Round lastRound) {
        List<Integer> points = new ArrayList<>(checkedWords.size());
        var words = checkedWords.stream()
            .map(CheckedWord::getWord)
            .map(String::toLowerCase)
            .toList();

        Set<String> processed = new HashSet<>();

        checkedWords.forEach(cw -> {
            String word = cw.getWord().toLowerCase();
            if (word.isBlank() || word.charAt(0) != lastRound.getLetter()) {
                cw.setValid(false);
            } else {
                cw.setValid(cw.getPositiveVotes() > 0);
            }
        });

        for (int i = 0; i < checkedWords.size(); i++) {
            CheckedWord checkedWord = checkedWords.get(i);
            String wordLowerCase = checkedWord.getWord().toLowerCase();

            if (!checkedWord.isValid()) {
                points.add(0);
            } else if (processed.contains(wordLowerCase)) {
                points.add(5);
            } else if (words.lastIndexOf(wordLowerCase) > i) {
                points.add(5);
                processed.add(wordLowerCase);
            } else if (checkedWords.stream()
                .filter(cw -> cw != checkedWord)
                .anyMatch(CheckedWord::isValid)) {
                processed.add(wordLowerCase);
                points.add(10);
            } else {
                processed.add(wordLowerCase);
                points.add(15);
            }
        }
        return points;
    }
}
