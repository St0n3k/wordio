package pl.lodz.p.it.zzpj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.zzpj.controller.dto.UuidDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.CreateGameDto;
import pl.lodz.p.it.zzpj.controller.dto.game.request.AnswerRequestDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.AnswerResponseDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.FinishGameResponseDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.FinishNotifyOthersResponseDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.JoinGameResponseDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.StartGameResponseDTO;
import pl.lodz.p.it.zzpj.exception.game.GameAlreadyStartedException;
import pl.lodz.p.it.zzpj.exception.game.GameNotFoundException;
import pl.lodz.p.it.zzpj.exception.game.GameNotStartedException;
import pl.lodz.p.it.zzpj.exception.game.NotAuthorStartGameException;
import pl.lodz.p.it.zzpj.exception.game.NotEnoughPlayersException;
import pl.lodz.p.it.zzpj.model.Game;
import pl.lodz.p.it.zzpj.model.Round;
import pl.lodz.p.it.zzpj.repository.GameRedisRepository;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
public class GameRedisService {
    private final SimpMessagingTemplate template;
    private final Map<UUID, Semaphore> semaphoreMap = new HashMap<>();
    private final Map<UUID, TimerTask> timers = new HashMap<>();
    @Autowired
    private GameRedisRepository gameRedisRepository;

    public UuidDTO createGame(CreateGameDto createGameDto) {
        UUID id = gameRedisRepository.putGame(
            new Game(
                createGameDto.numberOfRounds(),
                createGameDto.countdownTime(),
                createGameDto.maxRoundDurationTime(),
                SecurityContextHolder.getContext().getAuthentication().getName(),
                createGameDto.categories())).getId();
        semaphoreMap.put(id, new Semaphore(1));
        return new UuidDTO(id);
    }

    public void joinGame(UUID gameID, String player) throws GameNotFoundException, GameAlreadyStartedException {
        try {
            semaphoreMap.get(gameID).acquireUninterruptibly();
            if (gameRedisRepository.getGame(gameID).isStarted()) {
                throw new GameAlreadyStartedException();
            }
            int counter = 1;
            while (gameRedisRepository.getGame(gameID).getPlayers().contains(player)) {
                player = player + "(" + counter + ")";
                counter++;
            }
            List<String> players = addPlayer(gameID, player);

            template.convertAndSend("/topic/game/" + gameID,
                new JoinGameResponseDTO(players),
                getActionsHeader("join"));
            semaphoreMap.get(gameID).release();
        } catch (NullPointerException npe) {
            throw new GameNotFoundException();
        }
    }

    public void startGame(UUID gameID, String playerName)
        throws GameNotFoundException, NotEnoughPlayersException, NotAuthorStartGameException,
        GameAlreadyStartedException {
        Game game = gameRedisRepository.getGame(gameID);
        if (!Objects.equals(game.getAuthorName(), playerName)) {
            throw new NotAuthorStartGameException();
        }
        if (game.isStarted()) {
            throw new GameAlreadyStartedException();
        }
        if (game.getPlayers().size() < 2) {
            throw new NotEnoughPlayersException();
        }
        template.convertAndSend("/topic/game/" + gameID,
            new StartGameResponseDTO(game.getCategories(),
                game.getRounds().peek().getLetter()), getActionsHeader("start"));
        game.setStarted(true);
        gameRedisRepository.putGame(game);
        createTimerTask(gameID, game.getMaxRoundLenght());
    }

    public void finishGame(UUID gameID) throws GameNotFoundException, GameNotStartedException {
        try {
            if (semaphoreMap.get(gameID).tryAcquire()) {
                Game game = gameRedisRepository.getGame(gameID);
                if (!game.isStarted()) {
                    throw new GameNotStartedException();
                }
                template.convertAndSend("/topic/game/" + gameID, new FinishNotifyOthersResponseDTO(),
                    getActionsHeader("finish-notify"));
                createTimerTask(gameID, game.getCountdownTime());
                semaphoreMap.get(gameID).release();
            }
        } catch (NullPointerException npe) {
            throw new GameNotFoundException();
        }
    }

    public void sendAnswers(AnswerRequestDTO answerRequestDTO, UUID gameID)
        throws GameNotFoundException, GameNotStartedException {
        try {
            semaphoreMap.get(gameID).acquireUninterruptibly();
            Game game = gameRedisRepository.getGame(gameID);
            if (!game.isStarted()) {
                throw new GameNotStartedException();
            }
            game.getRounds().peek().getAnswers().putAll(answerRequestDTO.getAnswers());
            game = gameRedisRepository.putGame(game);
            Round round = game.getRounds().peek();
            if (round.getAnswers().size() == game.getPlayers().size()) {
                template.convertAndSend("/topic/game/" + gameID,
                    new AnswerResponseDTO(round.getAnswers()),
                    getActionsHeader("display-answers"));
            }
            semaphoreMap.get(gameID).release();
        } catch (NullPointerException npe) {
            throw new GameNotFoundException();
        }
    }

    public void endRound(UUID gameID) throws GameNotFoundException {
        try {
            popRound(gameID);
            //TO SIE ZACZYNA KOLEJNA RUNDA
        } catch (EmptyStackException e) {
            //TO SIE GRA KONCZY
            throw new RuntimeException();
            //To to do wywalenia, PMD krzyczal ze nie moze byc puste
        }
    }

    private Map<String, Object> getActionsHeader(String value) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("action", value);
        return headers;
    }

    public void popRound(UUID gameID) throws GameNotFoundException {
        Game game = gameRedisRepository.getGame(gameID);
        Round round = game.getRounds().pop();
        game.getPlayed().push(round);
        gameRedisRepository.putGame(game);
    }

    public List<String> addPlayer(UUID gameID, String player) throws GameNotFoundException {
        Game game = gameRedisRepository.getGame(gameID);
        game.getPlayers().add(player);
        gameRedisRepository.putGame(game);
        return game.getPlayers();
    }

    public void createTimerTask(UUID gameID, int length) {
        if (timers.containsKey(gameID)) {
            timers.remove(gameID).cancel();
        }
        timers.put(gameID, new TimerTask() {
            @Override
            public void run() {
                template.convertAndSend("/topic/game/" + gameID, new FinishGameResponseDTO(),
                    getActionsHeader("finish"));
            }
        });
        new Timer().schedule(timers.get(gameID), length * 1000L);
    }
}
