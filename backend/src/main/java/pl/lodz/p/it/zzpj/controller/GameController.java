package pl.lodz.p.it.zzpj.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.zzpj.controller.dto.game.CreateGameDto;
import pl.lodz.p.it.zzpj.controller.dto.game.request.AnswerRequestDTO;
import pl.lodz.p.it.zzpj.exception.game.GameNotFoundException;
import pl.lodz.p.it.zzpj.service.GameRedisService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GameController {

    private final GameRedisService gameService;

    @Secured({"ROLE_PLAYER", "ROLE_ANONYMOUS"})
    @PostMapping("/games")
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createNewGame(@Valid @RequestBody CreateGameDto createGameDto) {
        return gameService.createGame(createGameDto);
    }

    @MessageMapping("/{gameID}/join")
    public void join(@DestinationVariable UUID gameID, @NotBlank @Size(min = 2) @Payload String player)
        throws GameNotFoundException {
        gameService.joinGame(gameID, player);
    }

    @MessageMapping("/{gameID}/start")
    public void startGame(@DestinationVariable UUID gameID) throws GameNotFoundException {
        gameService.startGame(gameID);
    }

    @MessageMapping("/{gameID}/finish")
    public void finishRound(@DestinationVariable UUID gameID) throws GameNotFoundException {
        gameService.finishGame(gameID);
    }

    @MessageMapping("/{gameID}/answers")
    public void sendAnswer(@Valid @Payload AnswerRequestDTO answerDTO) throws GameNotFoundException {
        gameService.sendAnswers(answerDTO);
    }
}
