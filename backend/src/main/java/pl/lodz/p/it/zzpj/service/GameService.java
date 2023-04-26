package pl.lodz.p.it.zzpj.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import pl.lodz.p.it.zzpj.controller.dto.CreateGameDto;
import pl.lodz.p.it.zzpj.controller.dto.UuidDTO;
import pl.lodz.p.it.zzpj.model.Game;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ApplicationScope
public class GameService {
    private static final ConcurrentHashMap<String, Game> ongoingGames = new ConcurrentHashMap<>();

    public UuidDTO createGame(CreateGameDto gameDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Game game = new Game(
            UUID.randomUUID(),
            gameDto.numberOfRounds(),
            gameDto.countdownTime(),
            gameDto.categories(),
            username);

        ongoingGames.put(username, game);

        return new UuidDTO(game.id());
    }
}
