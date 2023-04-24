package pl.lodz.p.it.zzpj.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.zzpj.controller.dto.CreateGameDto;
import pl.lodz.p.it.zzpj.controller.dto.UuidDTO;
import pl.lodz.p.it.zzpj.service.GameService;

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
}
