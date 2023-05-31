package pl.lodz.p.it.zzpj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.lodz.p.it.zzpj.model.CheckedWord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log
public class DictionaryService {

    private final WebClient webClient;

    public List<CheckedWord> checkWords(List<String> words) {

        List<Mono<CheckedWord>> individualResults =
            words.stream()
                .map(
                    word ->
                        webClient
                            .get()
                            .uri("https://sjp.pl/" + word)
                            .exchangeToMono(
                                response -> Mono.just(
                                    new CheckedWord(word, response.statusCode().is2xxSuccessful()))))
                .toList();

        Flux<CheckedWord> mergedResults = Flux.merge(individualResults);

        return mergedResults
            .collectList()
            .block();
    }
}
