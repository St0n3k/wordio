package pl.lodz.p.it.zzpj.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record Game(
    UUID id,
    int numberOfRounds,
    int countdownTime,
    List<String> categories,
    String authorName,
    List<String> players,
    Map<String, List<String>> answers
) {
    public Game(UUID id, int numberOfRounds, int countdownTime, List<String> categories, String authorName) {
        this(id, numberOfRounds, countdownTime,
            Collections.unmodifiableList(categories),
            authorName,
            new ArrayList<>(),
            new HashMap<>());
    }
}
