package pl.lodz.p.it.zzpj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.zzpj.controller.dto.UuidDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.CreateGameDto;
import pl.lodz.p.it.zzpj.controller.dto.game.MessageDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.PlayerAnswersDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.AllPlayersAnswersDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.JoinGameResponseDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.StartGameResponseDTO;
import pl.lodz.p.it.zzpj.exception.game.GameAlreadyStartedException;
import pl.lodz.p.it.zzpj.exception.game.GameNotFoundException;
import pl.lodz.p.it.zzpj.exception.game.GameNotStartedException;
import pl.lodz.p.it.zzpj.exception.game.NotAuthorStartGameException;
import pl.lodz.p.it.zzpj.exception.game.NotEnoughPlayersException;
import pl.lodz.p.it.zzpj.exception.game.UserNotFoundInGameException;
import pl.lodz.p.it.zzpj.exception.game.UsernameInUseException;
import pl.lodz.p.it.zzpj.model.CheckedWord;
import pl.lodz.p.it.zzpj.model.Game;
import pl.lodz.p.it.zzpj.model.Round;
import pl.lodz.p.it.zzpj.repository.GameRedisRepository;

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
    private final GameRedisRepository gameRedisRepository;
    private final DictionaryService dictionaryService;

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

    public void joinGame(UUID gameID, String player)
        throws GameNotFoundException, GameAlreadyStartedException, UsernameInUseException {
        try {
            semaphoreMap.get(gameID).acquireUninterruptibly();
            Game game = gameRedisRepository.getGame(gameID);
            if (game.isStarted()) {
                throw new GameAlreadyStartedException();
            }
            if (game.getPlayers().contains(player)) {
                throw new UsernameInUseException();
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

    public void start(UUID gameID, String playerName)
        throws GameNotFoundException, NotEnoughPlayersException, NotAuthorStartGameException,
        GameAlreadyStartedException, UserNotFoundInGameException {
        Game game = gameRedisRepository.getGame(gameID);
        if (!game.getPlayers().contains(playerName)) {
            throw new UserNotFoundInGameException();
        }
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
        createTimerTask(gameID, game.getMaxRoundLength());
    }

    public void finishRound(UUID gameID) throws GameNotFoundException, GameNotStartedException {
        try {
            if (semaphoreMap.get(gameID).tryAcquire()) {
                Game game = gameRedisRepository.getGame(gameID);
                if (!game.isStarted()) {
                    throw new GameNotStartedException();
                }
                template.convertAndSend("/topic/game/" + gameID, new MessageDTO("finish-notify"),
                    getActionsHeader("finish-notify"));
                createTimerTask(gameID, game.getCountdownTime());
                semaphoreMap.get(gameID).release();
            }
        } catch (NullPointerException npe) {
            throw new GameNotFoundException();
        }
    }

    public void sendAnswers(PlayerAnswersDTO playerAnswersDTO, UUID gameID)
        throws GameNotFoundException, GameNotStartedException, UserNotFoundInGameException {
        try {
            semaphoreMap.get(gameID).acquireUninterruptibly();
            String username = playerAnswersDTO.getUsername();

            Game game = gameRedisRepository.getGame(gameID);
            if (!game.getPlayers().contains(username)) {
                throw new UserNotFoundInGameException();
            }
            if (!game.isStarted()) {
                throw new GameNotStartedException();
            }

            List<String> answers = playerAnswersDTO.getAnswers();

            Round finalRound = game.getRounds().peek();
            List<CheckedWord> checkedWords = dictionaryService.checkWords(answers).stream()
                .peek(word -> {
                    if (word.getWord().charAt(0) != finalRound.getLetter()) {
                        word.setValid(false);
                    }
                })
                .toList();

            game.getRounds().peek().getAnswers().put(username, checkedWords);
            game = gameRedisRepository.putGame(game);
            Round round = game.getRounds().peek();

            if (round.getAnswers().size() == game.getPlayers().size()) {
                Map<String, List<CheckedWord>> roundAnswers = round.getAnswers();

                template.convertAndSend("/topic/game/" + gameID,
                    new AllPlayersAnswersDTO(roundAnswers),
                    getActionsHeader("display-answers"));
            }
            semaphoreMap.get(gameID).release();
        } catch (NullPointerException npe) {
            throw new GameNotFoundException();
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
                template.convertAndSend("/topic/game/" + gameID, new MessageDTO("finish"),
                    getActionsHeader("finish"));
            }
        });
        new Timer().schedule(timers.get(gameID), length * 1000L);
    }
}
