package pl.lodz.p.it.zzpj.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.zzpj.TestContainersSetup;
import pl.lodz.p.it.zzpj.exception.game.GameNotFoundException;
import pl.lodz.p.it.zzpj.model.Game;

import java.util.List;
import java.util.UUID;

@Testcontainers
@SpringBootTest
public class RedisGameRepositoryTest extends TestContainersSetup {

    @Autowired
    GameRedisRepository gameRedisRepository;

    @Nested
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    class RedisCacheMethodsTest {

        private static Game game;

        @BeforeEach
        void init() {
            game = new Game(1, 90, 10, "kamillo", List.of("a", "b"));
            gameRedisRepository.putGame(game);
        }

        @Test
        public void shouldPutGameInRedisCache() throws GameNotFoundException {
            Game createGame = new Game(1, 90, 10, "kamillo", List.of("a", "b"));
            gameRedisRepository.putGame(createGame);
            assertDoesNotThrow(() -> gameRedisRepository.getGame(createGame.getId()));
            Game createdGame = gameRedisRepository.getGame(createGame.getId());
            assertNotNull(createdGame);
            assertEquals(createdGame, createGame);
        }

        @Test
        public void shouldGetGameFromRedisCache() throws GameNotFoundException {
            assertDoesNotThrow(() -> gameRedisRepository.getGame(game.getId()));
            Game createdGame = gameRedisRepository.getGame(game.getId());
            assertNotNull(createdGame);
            assertEquals(createdGame, game);
            assertEquals(createdGame.getId(), game.getId());
            assertEquals(createdGame.getRounds(), game.getRounds());
            assertEquals(createdGame.getPlayed(), game.getPlayed());
            assertEquals(createdGame.getPlayers(), game.getPlayers());
            assertEquals(createdGame.getCountdownTime(), game.getCountdownTime());
            assertEquals(createdGame.getMaxRoundLength(), game.getMaxRoundLength());
            assertEquals(createdGame.getCategories(), game.getCategories());
        }

        @Test
        public void shouldThrowGameNotFoundExceptionWhenGetGame() {
            assertThrows(GameNotFoundException.class,
                () -> gameRedisRepository.getGame(UUID.fromString("c54ef949-dc82-4ac8-854a-5c4f60283648")));
        }

        @Test
        public void shouldUpdateGamePlayers() throws GameNotFoundException {
            assertDoesNotThrow(() -> gameRedisRepository.getGame(game.getId()));
            Game gotGame = gameRedisRepository.getGame(game.getId());
            assertEquals(gotGame.getPlayers(), game.getPlayers());

            gotGame.getPlayers().add("Players");
            gameRedisRepository.putGame(gotGame);

            assertDoesNotThrow(() -> gameRedisRepository.getGame(game.getId()));
            gotGame = gameRedisRepository.getGame(game.getId());
            assertNotEquals(gotGame.getPlayers(), game.getPlayers());
            assertEquals(gotGame.getPlayers().size(), 1);
            assertEquals(gotGame.getPlayers().get(0), "Players");
        }

        @Test
        public void shouldDeleteGame() {
            assertDoesNotThrow(() -> gameRedisRepository.deleteGame(game.getId()));
            assertDoesNotThrow(() -> gameRedisRepository.deleteGame(game.getId()));
            assertThrows(GameNotFoundException.class, () -> gameRedisRepository.getGame(game.getId()));
        }
    }
}
