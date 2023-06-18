package pl.lodz.p.it.zzpj.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.zzpj.common.Actions;
import pl.lodz.p.it.zzpj.controller.dto.PlayerVotesDTO;
import pl.lodz.p.it.zzpj.controller.dto.UuidDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.CreateGameDto;
import pl.lodz.p.it.zzpj.controller.dto.game.MessageDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.PlayerAnswersDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.AllPlayersAnswersDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.FinishedGameResponseDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.JoinGameResponseDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.StartRoundResponseDTO;
import pl.lodz.p.it.zzpj.exception.AppBaseException;
import pl.lodz.p.it.zzpj.exception.game.GameAlreadyStartedException;
import pl.lodz.p.it.zzpj.exception.game.GameNotFoundException;
import pl.lodz.p.it.zzpj.exception.game.GameNotStartedException;
import pl.lodz.p.it.zzpj.exception.game.NotAuthorStartGameException;
import pl.lodz.p.it.zzpj.exception.game.NotEnoughPlayersException;
import pl.lodz.p.it.zzpj.exception.game.PlayerAlreadySentAnswersException;
import pl.lodz.p.it.zzpj.exception.game.PlayerAlreadyVotedException;
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
@Log
public class GameService {
    private final SimpMessagingTemplate template;
    private final Map<UUID, Semaphore> semaphoreMap = new HashMap<>();
    private final Map<String, TimerTask> timers = new HashMap<>();
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
        throws AppBaseException {
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
                getActionsHeader(Actions.JOIN));
            semaphoreMap.get(gameID).release();
        } catch (NullPointerException npe) {
            throw new GameNotFoundException();
        }
    }

    public void startGame(UUID gameID, String playerName)
        throws AppBaseException {
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
        game.setStarted(true);
        gameRedisRepository.putGame(game);
        startRound(game);
    }

    public void startRound(Game game) {
        if (game.getRounds().isEmpty()) {
            template.convertAndSend("/topic/game/" + game.getId(),
                new FinishedGameResponseDTO(game), getActionsHeader(Actions.GAME_FINISH));
        } else {
            template.convertAndSend("/topic/game/" + game.getId(),
                new StartRoundResponseDTO(game.getCategories(),
                    game.getRounds().peek().getLetter()), getActionsHeader(Actions.START));
            createTimerTask(game.getId(), game.getMaxRoundLength(), Actions.FINISH);
        }
    }

    public void finishRound(UUID gameID) throws GameNotFoundException, GameNotStartedException {
        try {
            if (semaphoreMap.get(gameID).tryAcquire()) {
                Game game = gameRedisRepository.getGame(gameID);
                if (!game.isStarted()) {
                    throw new GameNotStartedException();
                }
                template.convertAndSend("/topic/game/" + gameID, new MessageDTO(Actions.FINISH_NOTIFY),
                    getActionsHeader(Actions.FINISH_NOTIFY));
                createTimerTask(gameID, game.getCountdownTime(), Actions.FINISH);
                semaphoreMap.get(gameID).release();
            }
        } catch (NullPointerException npe) {
            throw new GameNotFoundException();
        }
    }

    public void sendAnswers(PlayerAnswersDTO playerAnswersDTO, UUID gameID)
        throws AppBaseException {
        try {
            semaphoreMap.get(gameID).acquireUninterruptibly();
            String username = playerAnswersDTO.getUsername();

            Game game = gameRedisRepository.getGame(gameID);
            Round finalRound = game.getRounds().peek();
            if (!game.getPlayers().contains(username)) {
                throw new UserNotFoundInGameException();
            }
            if (!game.isStarted()) {
                throw new GameNotStartedException();
            }
            if (finalRound.getAnswers().containsKey(playerAnswersDTO.getUsername())) {
                throw new PlayerAlreadySentAnswersException();
            }

            List<String> answers = playerAnswersDTO.getAnswers();

            List<CheckedWord> checkedWords = dictionaryService.checkWords(answers).stream()
                .peek(word -> {
                    if (word.getWord().charAt(0) != finalRound.getLetter() || !word.isValid()) {
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
                    getActionsHeader(Actions.DISPLAY_ANSWERS));
                createTimerTaskForVoting(gameID, 20);
            }
            semaphoreMap.get(gameID).release();
        } catch (NullPointerException npe) {
            throw new GameNotFoundException();
        }
    }

    public void sendVotes(PlayerVotesDTO playerVotesDTO, UUID gameID)
        throws AppBaseException {
        try {
            semaphoreMap.get(gameID).acquireUninterruptibly();
            String username = playerVotesDTO.getUsername();

            Game game = gameRedisRepository.getGame(gameID);
            Round round = game.getRounds().peek();
            if (!game.getPlayers().contains(username)) {
                throw new UserNotFoundInGameException();
            }
            if (!game.isStarted()) {
                throw new GameNotStartedException();
            }
            if (round.getPlayersVoted().contains(playerVotesDTO.getUsername())) {
                throw new PlayerAlreadyVotedException();
            }

            Map<String, List<Boolean>> votes = playerVotesDTO.getVotes();

            for (String player : game.getPlayers()) {
                List<CheckedWord> words = round.getAnswers().get(player);
                for (int i = 0; i < Math.min(round.getAnswers().get(player).size(), votes.get(player).size()); i++) {
                    if (votes.get(player).get(i)) {
                        words.get(i).incrementPositiveVotes();
                    } else {
                        words.get(i).decrementPositiveVotes();
                    }
                }
            }
            round.getPlayersVoted().add(playerVotesDTO.getUsername());

            gameRedisRepository.putGame(game);
            if (round.getPlayersVoted().size() == game.getPlayers().size()) {
                game = popRound(gameID);
                if (timers.containsKey(gameID + Actions.VOTING_FINISH)) {
                    timers.remove(gameID + Actions.VOTING_FINISH).cancel();
                }
                game.assignPointsForLastFinishedRound();
                gameRedisRepository.putGame(game);
                startRound(game);
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

    public Game popRound(UUID gameID) throws AppBaseException {
        Game game = gameRedisRepository.getGame(gameID);
        Round round = game.getRounds().pop();
        game.getPlayed().push(round);
        return gameRedisRepository.putGame(game);
    }

    public List<String> addPlayer(UUID gameID, String player) throws AppBaseException {
        Game game = gameRedisRepository.getGame(gameID);
        game.addPlayer(player);
        gameRedisRepository.putGame(game);
        return game.getPlayers();
    }

    public void createTimerTask(UUID gameID, int length, String message) {
        if (timers.containsKey(gameID + message)) {
            timers.remove(gameID + message).cancel();
        }
        timers.put(gameID + message, new TimerTask() {
            @Override
            public void run() {
                template.convertAndSend("/topic/game/" + gameID, new MessageDTO(message),
                    getActionsHeader(message));
            }
        });
        new Timer().schedule(timers.get(gameID + message), length * 1000L);
    }

    private void createTimerTaskForVoting(UUID gameID, int length) throws AppBaseException {
        if (timers.containsKey(gameID + Actions.VOTING_FINISH)) {
            timers.remove(gameID + Actions.VOTING_FINISH).cancel();
        }
        if (timers.containsKey(gameID + Actions.FINISH)) {
            timers.remove(gameID + Actions.FINISH).cancel();
        }
        timers.put(gameID + Actions.VOTING_FINISH, new TimerTask() {
            @Override
            public void run() {
                template.convertAndSend("/topic/game/" + gameID, new MessageDTO(Actions.VOTING_FINISH),
                    getActionsHeader(Actions.VOTING_FINISH));
                Game game = null;
                try {
                    game = popRound(gameID);
                } catch (AppBaseException e) {
                    log.severe("Unexpected exception during starting new round after voting");
                }
                startRound(game);
            }
        });
        new Timer().schedule(timers.get(gameID + Actions.VOTING_FINISH), length * 1000L);
    }
}
