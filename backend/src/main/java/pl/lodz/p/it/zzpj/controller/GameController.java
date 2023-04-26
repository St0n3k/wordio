package pl.lodz.p.it.zzpj.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.zzpj.controller.dto.CreateGameDto;
import pl.lodz.p.it.zzpj.controller.dto.UuidDTO;
import pl.lodz.p.it.zzpj.service.GameService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/games")
    @Secured("ROLE_PLAYER")
    public UuidDTO createNewGame(@Valid @RequestBody CreateGameDto createGameDto) {
        return gameService.createGame(createGameDto);
    }

    @MessageMapping("/{gameID}")
    @SendTo("/topic/game/{gameID}")
    public String sampleEndpoint(
        @DestinationVariable String gameID,
        @Payload String payload,
        Principal principal) {

        return "SubscriptionMessage(gameID=%s, payload='%s', principal=%s)".formatted(gameID, payload, principal);
    }
}
