package pl.lodz.p.it.zzpj.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import pl.lodz.p.it.zzpj.model.CheckedWord;

import java.util.List;

public class DictionaryServiceTest {


    private final DictionaryService dictionaryService = new DictionaryService(WebClient.builder().build());

    @Test
    void shouldPositivelyValidateWords() {
        List<String> words =
            List.of("liść", "parówka", "afryka", "polska", "rzym", "tomasz", "sława", "frytki", "zombie", "hasło");
        List<CheckedWord> checkedWords = dictionaryService.checkWords(words);

        checkedWords.forEach(checkedWord -> Assertions.assertTrue(checkedWord.isValid()));
    }

    @Test
    void shouldNegativelyValidateWords() {
        List<String> words =
            List.of("liśćć", "parufka", "afrykaaaa", "grózja", "żeżucha", "chejka", "hasłord", "cma");
        List<CheckedWord> checkedWords = dictionaryService.checkWords(words);

        checkedWords.forEach(checkedWord -> Assertions.assertFalse(checkedWord.isValid()));
    }
}
