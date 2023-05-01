package pl.lodz.p.it.zzpj.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.lodz.p.it.zzpj.TestContainersSetup;
import pl.lodz.p.it.zzpj.controller.dto.game.CreateGameDto;
import pl.lodz.p.it.zzpj.controller.dto.game.request.AnswerRequestDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.AnswerResponseDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.ExceptionResponseDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.FinishGameResponseDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.FinishNotifyOthersResponseDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.JoinGameResponseDTO;
import pl.lodz.p.it.zzpj.controller.dto.game.response.StartGameResponseDTO;
import pl.lodz.p.it.zzpj.service.GameRedisService;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class GameControllerTest extends TestContainersSetup {

    private static final String USERNAME = "kamillo";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @WithUserDetails(USERNAME)
    class CreateNewGameTest {
        private CreateGameDto createGameDto;

        @Test
        void shouldCreateGameAndReturnUuidWithStatusCode201Test() throws Exception {
            createGameDto = new CreateGameDto(1, 90, 10, List.of("a", "b"));

            mockMvc.perform(
                    post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isString());
        }

        @Test
        @WithAnonymousUser
        void shouldFailCreatingNewGameAsUnauthenticatedUserTest() throws Exception {
            createGameDto = new CreateGameDto(1, 90, 10, List.of("a", "b"));

            mockMvc.perform(
                    post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameDto)))
                .andExpect(status().isForbidden()); // TODO consider changing to 401
        }

        @ParameterizedTest
        @ValueSource(ints = {-3, 0, 11})
        void shouldFailCreatingNewGameWithStatusCode400DueToInvalidNumberOfRoundsTest(int rounds)
            throws Exception {
            createGameDto = new CreateGameDto(rounds, 90, 10, List.of("a", "b"));

            mockMvc.perform(
                    post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameDto)))
                .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(ints = {-90, 0, 301})
        void shouldFailCreatingNewGameWithStatusCode400DueToInvalidRoundDurationTest(int roundTime)
            throws Exception {
            createGameDto = new CreateGameDto(2, roundTime, 10, List.of("a", "b"));

            mockMvc.perform(
                    post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameDto)))
                .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(ints = {-5, 31})
        void shouldFailCreatingNewGameWithStatusCode400DueToInvalidCountdownTimeTest(int countdownTime)
            throws Exception {
            createGameDto = new CreateGameDto(2, 120, countdownTime, List.of("a", "b"));

            mockMvc.perform(
                    post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameDto)))
                .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ;", "; ", " ; ; ", "a;b; "})
        void shouldFailCreatingNewGameWithStatusCode400DueToInvalidCategoriesTest(String categories) throws Exception {
            List<String> categoriesList = Arrays.asList(categories.split(";", -1));

            createGameDto = new CreateGameDto(2, 120, 10, categoriesList);

            mockMvc.perform(
                    post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGameDto)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @WithUserDetails(USERNAME)
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    class GameLogicTest {
        private static final WebSocketStompClient stompClient =
            new WebSocketStompClient(
                new SockJsClient(List.of(
                    new WebSocketTransport(new StandardWebSocketClient())
                ))
            );
        private static UUID gameID;
        private final ArrayBlockingQueue<Object> blockingQueue1 = new ArrayBlockingQueue<>(1);
        private final ArrayBlockingQueue<Object> blockingQueue2 = new ArrayBlockingQueue<>(1);
        @Autowired
        GameRedisService gameRedisService;
        @LocalServerPort
        private int port;
        private String url;
        private StompSession session1;
        private StompSession session2;

        @BeforeAll
        static void setupStompClient() {
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        }

        @AfterEach
        void disconnect() {
            session1.disconnect();
            session2.disconnect();
        }

        @BeforeEach
        void initializeSessionAndSubscribeTopicAndHandleMessage()
            throws Exception {
            blockingQueue1.clear();
            blockingQueue2.clear();
            url = "ws://localhost:" + port + "/wordio";
            CreateGameDto createGameDto = new CreateGameDto(1, 4, 2, List.of("a", "b"));
            gameID = gameRedisService.createGame(createGameDto).getId();
            session1 = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {
                })
                .get();

            session2 = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {
                })
                .get();

            assertNotNull(session1);
            assertNotNull(session1.getSessionId());
            assertNotNull(session2);
            assertNotNull(session2.getSessionId());

            session1.subscribe("/topic/game/" + gameID, new StompFrameHandler() {
                @Override
                @NotNull
                public Type getPayloadType(@NotNull StompHeaders headers) {
                    return switch (headers.get("action").get(0)) {
                        case "join" -> JoinGameResponseDTO.class;
                        case "start" -> StartGameResponseDTO.class;
                        case "display-answers" -> AnswerResponseDTO.class;
                        case "finish" -> FinishGameResponseDTO.class;
                        default -> FinishNotifyOthersResponseDTO.class;
                    };
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    blockingQueue1.add(payload);
                }
            });

            session1.subscribe("/user/topic/game/error", new StompFrameHandler() {
                @Override
                public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                    return ExceptionResponseDTO.class;
                }

                @Override
                public void handleFrame(@NotNull StompHeaders headers, Object payload) {
                    blockingQueue1.add(payload);
                }
            });

            session2.subscribe("/topic/game/" + gameID, new StompFrameHandler() {
                @Override
                @NotNull
                public Type getPayloadType(@NotNull StompHeaders headers) {
                    return switch (headers.get("action").get(0)) {
                        case "join" -> JoinGameResponseDTO.class;
                        case "start" -> StartGameResponseDTO.class;
                        case "display-answers" -> AnswerResponseDTO.class;
                        case "finish" -> FinishGameResponseDTO.class;
                        default -> FinishNotifyOthersResponseDTO.class;
                    };
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    blockingQueue2.add(payload);
                }
            });

            session2.subscribe("/user/topic/game/error", new StompFrameHandler() {
                @Override
                public @NotNull Type getPayloadType(@NotNull StompHeaders headers) {
                    return ExceptionResponseDTO.class;
                }

                @Override
                public void handleFrame(@NotNull StompHeaders headers, Object payload) {
                    blockingQueue2.add(payload);
                }
            });
        }

        @Test
        void shouldJoinTwoPlayersWithDifferentNameAndReturnActualState()
            throws InterruptedException {
            session1.send("/game/" + gameID + "/join", "kamillo");
            session2.send("/game/" + gameID + "/join", "msocha19");

            Object objectSession1 = blockingQueue1.poll(2, TimeUnit.SECONDS);
            Object objectSession2 = blockingQueue2.poll(2, TimeUnit.SECONDS);

            assertTrue(objectSession1 instanceof JoinGameResponseDTO);
            assertTrue(objectSession2 instanceof JoinGameResponseDTO);
            assertEquals(((JoinGameResponseDTO) objectSession1).getPlayers().size(), 1);
            assertEquals(((JoinGameResponseDTO) objectSession2).getPlayers().size(), 1);

            objectSession1 = blockingQueue1.poll(2, TimeUnit.SECONDS);
            objectSession2 = blockingQueue2.poll(2, TimeUnit.SECONDS);

            assertTrue(objectSession1 instanceof JoinGameResponseDTO);
            assertTrue(objectSession2 instanceof JoinGameResponseDTO);
            assertEquals(((JoinGameResponseDTO) objectSession1).getPlayers().size(), 2);
            assertEquals(((JoinGameResponseDTO) objectSession2).getPlayers().size(), 2);
            assertTrue(((JoinGameResponseDTO) objectSession1).getPlayers().contains("kamillo")
                && ((JoinGameResponseDTO) objectSession1).getPlayers().contains("msocha19"));
            assertTrue(((JoinGameResponseDTO) objectSession2).getPlayers().contains("kamillo")
                && ((JoinGameResponseDTO) objectSession2).getPlayers().contains("msocha19"));
        }

        @Test
        void shouldThrowUsernameInUseExceptionWhenJoinTwoPlayersWithTheSameName()
            throws InterruptedException {
            session1.send("/game/" + gameID + "/join", "player");

            Object objectSession1 = blockingQueue1.poll(2, TimeUnit.SECONDS);
            Object objectSession2 = blockingQueue2.poll(2, TimeUnit.SECONDS);
            assertTrue(objectSession1 instanceof JoinGameResponseDTO);
            assertTrue(objectSession2 instanceof JoinGameResponseDTO);
            assertEquals(((JoinGameResponseDTO) objectSession1).getPlayers().size(), 1);
            assertEquals(((JoinGameResponseDTO) objectSession2).getPlayers().size(), 1);
            assertTrue(((JoinGameResponseDTO) objectSession1).getPlayers().contains("player"));
            assertTrue(((JoinGameResponseDTO) objectSession2).getPlayers().contains("player"));

            session2.send("/game/" + gameID + "/join", "player");

            objectSession1 = blockingQueue1.poll(2, TimeUnit.SECONDS);
            objectSession2 = blockingQueue2.poll(2, TimeUnit.SECONDS);

            assertTrue(objectSession2 instanceof ExceptionResponseDTO);
            assertNull(objectSession1);
            assertEquals(((ExceptionResponseDTO) objectSession2).getExceptionMessage(), "username.in.use");
        }

        @Test
        void shouldThrowGameNotFoundExceptionWhenJoiningNotExistingGame() throws InterruptedException {
            session1.send("/game/" + UUID.randomUUID() + "/join", "player");
            Object object = blockingQueue1.poll(2, TimeUnit.SECONDS);
            assertTrue(object instanceof ExceptionResponseDTO);
            assertEquals(((ExceptionResponseDTO) object).getExceptionMessage(), "game.not.found");
        }

        @Test
        void shouldThrowGameAlreadyStartedExceptionWhenJoiningOngoingGame() throws InterruptedException {
            session1.send("/game/" + gameID + "/join", "kamillo");
            session2.send("/game/" + gameID + "/join", "msocha");
            blockingQueue1.poll(2, TimeUnit.SECONDS);
            blockingQueue2.poll(2, TimeUnit.SECONDS);
            blockingQueue1.poll(2, TimeUnit.SECONDS);
            blockingQueue2.poll(2, TimeUnit.SECONDS);
            session1.send("/game/" + gameID + "/start", "kamillo");
            blockingQueue1.poll(2, TimeUnit.SECONDS);
            blockingQueue2.poll(2, TimeUnit.SECONDS);
            session1.send("/game/" + gameID + "/join", "player2");
            Object object = blockingQueue1.poll(2, TimeUnit.SECONDS);
            assertTrue(object instanceof ExceptionResponseDTO);
            assertEquals(((ExceptionResponseDTO) object).getExceptionMessage(), "game.already.started");
            object = blockingQueue2.poll(2, TimeUnit.SECONDS);
            assertNull(object);
        }

        @Test
        void shouldStartGameWithTwoPlayersAndSendNotificationMessageAndEndAfterMaxRoundTime()
            throws InterruptedException {
            session1.send("/game/" + gameID + "/join", "player");
            session2.send("/game/" + gameID + "/join", "kamillo");

            assertNotNull(blockingQueue1.poll(2, TimeUnit.SECONDS));
            assertNotNull(blockingQueue2.poll(2, TimeUnit.SECONDS));
            assertNotNull(blockingQueue1.poll(2, TimeUnit.SECONDS));
            assertNotNull(blockingQueue2.poll(2, TimeUnit.SECONDS));

            session1.send("/game/" + gameID + "/start", "kamillo");

            Object objectSession1 = blockingQueue1.poll(2, TimeUnit.SECONDS);
            Object objectSession2 = blockingQueue2.poll(2, TimeUnit.SECONDS);

            assertTrue(objectSession1 instanceof StartGameResponseDTO);
            assertTrue(objectSession2 instanceof StartGameResponseDTO);
            assertEquals(((StartGameResponseDTO) objectSession1).getCategories().size(), 2);
            assertEquals(((StartGameResponseDTO) objectSession2).getCategories().size(), 2);
            assertTrue(Character.isAlphabetic(((StartGameResponseDTO) objectSession1).getLetter()));
            assertTrue(Character.isAlphabetic(((StartGameResponseDTO) objectSession2).getLetter()));
            assertEquals(((StartGameResponseDTO) objectSession2).getLetter(),
                ((StartGameResponseDTO) objectSession1).getLetter());

            objectSession1 = blockingQueue1.poll(5, TimeUnit.SECONDS);
            objectSession2 = blockingQueue2.poll(5, TimeUnit.SECONDS);

            assertEquals("finish", ((FinishGameResponseDTO) objectSession1).getMessage());
            assertEquals("finish", ((FinishGameResponseDTO) objectSession2).getMessage());
        }

        @Test
        void shouldThrowGameNotFoundExceptionWhenStartingNonExistentGame() throws InterruptedException {
            session1.send("/game/" + UUID.randomUUID() + "/start", "kamillo");
            Object object = blockingQueue1.poll(2, TimeUnit.SECONDS);
            assertTrue(object instanceof ExceptionResponseDTO);
            assertEquals(((ExceptionResponseDTO) object).getExceptionMessage(), "game.not.found");
            Object object2 = blockingQueue2.poll(2, TimeUnit.SECONDS);
            assertNull(object2);
        }

        @Test
        void shouldThrowNotAuthorStartGameExceptionWhenStartInvokedNotByOwner() throws InterruptedException {
            session1.send("/game/" + gameID + "/join", "msocha");
            blockingQueue1.poll(2, TimeUnit.SECONDS);
            blockingQueue2.poll(2, TimeUnit.SECONDS);
            session1.send("/game/" + gameID + "/start", "msocha");
            Object object = blockingQueue1.poll(2, TimeUnit.SECONDS);
            assertTrue(object instanceof ExceptionResponseDTO);
            assertEquals(((ExceptionResponseDTO) object).getExceptionMessage(), "not.author.started");
            Object object2 = blockingQueue2.poll(2, TimeUnit.SECONDS);
            assertNull(object2);
        }

        @Test
        void shouldThrowNotEnoughPlayersExceptionWhenGameIsBeingStartedByOwer() throws InterruptedException {
            session1.send("/game/" + gameID + "/join", "kamillo");
            blockingQueue1.poll(2, TimeUnit.SECONDS);
            blockingQueue2.poll(2, TimeUnit.SECONDS);
            session1.send("/game/" + gameID + "/start", "kamillo");
            Object object = blockingQueue1.poll(2, TimeUnit.SECONDS);
            assertTrue(object instanceof ExceptionResponseDTO);
            assertEquals(((ExceptionResponseDTO) object).getExceptionMessage(), "not.enough.players");
            Object object2 = blockingQueue2.poll(2, TimeUnit.SECONDS);
            assertNull(object2);
        }

        @Test
        void shouldThrowGameAlreadyStartedExceptionWhenStartingStartedGame() throws InterruptedException {
            session1.send("/game/" + gameID + "/join", "msocha");
            session2.send("/game/" + gameID + "/join", "kamillo");
            blockingQueue1.poll(2, TimeUnit.SECONDS);
            blockingQueue2.poll(2, TimeUnit.SECONDS);
            blockingQueue1.poll(2, TimeUnit.SECONDS);
            blockingQueue2.poll(2, TimeUnit.SECONDS);
            session1.send("/game/" + gameID + "/start", "kamillo");
            blockingQueue1.poll(2, TimeUnit.SECONDS);
            blockingQueue2.poll(2, TimeUnit.SECONDS);
            session1.send("/game/" + gameID + "/start", "kamillo");
            Object object = blockingQueue1.poll(1, TimeUnit.SECONDS);
            assertTrue(object instanceof ExceptionResponseDTO);
            assertEquals(((ExceptionResponseDTO) object).getExceptionMessage(), "game.already.started");
            object = blockingQueue2.poll(2, TimeUnit.SECONDS);
            assertNull(object);
        }

        @Test
        void shouldThrowUserNotFoundWhenStartedByUserNotInGame() throws InterruptedException {
            session1.send("/game/" + gameID + "/start", "kamillo");
            Object object1 = blockingQueue1.poll(2, TimeUnit.SECONDS);
            Object object2 = blockingQueue2.poll(2, TimeUnit.SECONDS);
            assertNull(object2);
            assertTrue(object1 instanceof ExceptionResponseDTO);
            assertEquals(((ExceptionResponseDTO) object1).getExceptionMessage(), "user.not.found.in.game");
        }

        @Test
        void shouldFinishRoundAndEndAfterCountdownTime() throws InterruptedException {
            session1.send("/game/" + gameID + "/join", "player");
            session2.send("/game/" + gameID + "/join", "kamillo");

            assertNotNull(blockingQueue1.poll(2, TimeUnit.SECONDS));
            assertNotNull(blockingQueue2.poll(2, TimeUnit.SECONDS));
            assertNotNull(blockingQueue1.poll(2, TimeUnit.SECONDS));
            assertNotNull(blockingQueue2.poll(2, TimeUnit.SECONDS));

            session1.send("/game/" + gameID + "/start", "kamillo");

            assertTrue(blockingQueue1.poll(2, TimeUnit.SECONDS) instanceof StartGameResponseDTO);
            assertTrue(blockingQueue2.poll(2, TimeUnit.SECONDS) instanceof StartGameResponseDTO);

            session1.send("/game/" + gameID + "/finish", "");
            session2.send("/game/" + gameID + "/finish", "");

            Object objectSession1 = blockingQueue1.poll(2, TimeUnit.SECONDS);
            Object objectSession2 = blockingQueue2.poll(2, TimeUnit.SECONDS);

            assertTrue(objectSession1 instanceof FinishNotifyOthersResponseDTO);
            assertTrue(objectSession2 instanceof FinishNotifyOthersResponseDTO);
            assertEquals(((FinishNotifyOthersResponseDTO) objectSession1).getMessage(), "finish-notify");
            assertEquals(((FinishNotifyOthersResponseDTO) objectSession2).getMessage(), "finish-notify");

            objectSession1 = blockingQueue1.poll(3, TimeUnit.SECONDS);
            objectSession2 = blockingQueue2.poll(3, TimeUnit.SECONDS);

            assertTrue(objectSession1 instanceof FinishGameResponseDTO);
            assertTrue(objectSession2 instanceof FinishGameResponseDTO);
            assertEquals(((FinishGameResponseDTO) objectSession1).getMessage(), "finish");
            assertEquals(((FinishGameResponseDTO) objectSession2).getMessage(), "finish");

            objectSession1 = blockingQueue1.poll(4, TimeUnit.SECONDS);
            objectSession2 = blockingQueue2.poll(4, TimeUnit.SECONDS);

            assertNull(objectSession1);
            assertNull(objectSession2);
        }

        @Test
        void shouldThrowGameNotFoundExceptionWhenFinishOnNonExistentGame() throws InterruptedException {
            session2.send("/game/" + UUID.randomUUID() + "/finish", "");
            Object object = blockingQueue2.poll(3, TimeUnit.SECONDS);
            assertTrue(object instanceof ExceptionResponseDTO);
            assertEquals(((ExceptionResponseDTO) object).getExceptionMessage(), "game.not.found");
            Object object1 = blockingQueue1.poll(3, TimeUnit.SECONDS);
            assertNull(object1);
        }

        @Test
        void shouldThrowGameNotStartedExceptionWhenFinishNotStartedGame() throws InterruptedException {
            session2.send("/game/" + gameID + "/finish", "");
            Object object = blockingQueue2.poll(2, TimeUnit.SECONDS);
            assertTrue(object instanceof ExceptionResponseDTO);
            assertEquals(((ExceptionResponseDTO) object).getExceptionMessage(), "game.not.started");
            Object object1 = blockingQueue1.poll(3, TimeUnit.SECONDS);
            assertNull(object1);
        }

        @Test
        void shouldSendBothAnswersButInvokeDisplayAnswersAfterEveryPlayerSend() throws InterruptedException {
            session1.send("/game/" + gameID + "/join", "msocha19");
            session2.send("/game/" + gameID + "/join", "kamillo");

            assertNotNull(blockingQueue1.poll(2, TimeUnit.SECONDS));
            assertNotNull(blockingQueue2.poll(2, TimeUnit.SECONDS));
            assertNotNull(blockingQueue1.poll(2, TimeUnit.SECONDS));
            assertNotNull(blockingQueue2.poll(2, TimeUnit.SECONDS));


            AnswerRequestDTO answerRequestDTO1 =
                new AnswerRequestDTO(Map.of("kamillo", List.of("a", "b")));
            AnswerRequestDTO answerRequestDTO2 =
                new AnswerRequestDTO(Map.of("msocha19", List.of("a", "b")));

            session1.send("/game/" + gameID + "/start", "kamillo");
            blockingQueue1.poll(2, TimeUnit.SECONDS);
            blockingQueue2.poll(2, TimeUnit.SECONDS);

            session1.send("/game/" + gameID + "/answers", answerRequestDTO1);
            session2.send("/game/" + gameID + "/answers", answerRequestDTO2);

            Object objectSession1 = blockingQueue1.poll(2, TimeUnit.SECONDS);
            Object objectSession2 = blockingQueue2.poll(2, TimeUnit.SECONDS);

            assertTrue(objectSession1 instanceof AnswerResponseDTO);
            assertTrue(objectSession2 instanceof AnswerResponseDTO);
            assertEquals(((AnswerResponseDTO) objectSession1).getAnswers().size(), 2);
            assertEquals(((AnswerResponseDTO) objectSession2).getAnswers().size(), 2);
        }

        @Test
        void shouldThrowGameNotFoundExceptionWhenSendingAnswersToNonExistentGame() throws InterruptedException {
            AnswerRequestDTO answerRequestDTO2 =
                new AnswerRequestDTO(Map.of("msocha19", List.of("a", "b")));
            session2.send("/game/" + UUID.randomUUID() + "/answers", answerRequestDTO2);
            Object object = blockingQueue2.poll(2, TimeUnit.SECONDS);
            assertTrue(object instanceof ExceptionResponseDTO);
            assertEquals(((ExceptionResponseDTO) object).getExceptionMessage(), "game.not.found");
            Object object1 = blockingQueue1.poll(3, TimeUnit.SECONDS);
            assertNull(object1);
        }

        @Test
        void shouldThrowGameNotStartedExceptionWhenSendingAnswersToNotStartedGame() throws InterruptedException {
            session2.send("/game/" + gameID + "/join", "msocha19");
            blockingQueue1.poll(2, TimeUnit.SECONDS);
            blockingQueue2.poll(2, TimeUnit.SECONDS);
            AnswerRequestDTO answerRequestDTO2 =
                new AnswerRequestDTO(Map.of("msocha19", List.of("a", "b")));
            session2.send("/game/" + gameID + "/answers", answerRequestDTO2);
            Object object = blockingQueue2.poll(2, TimeUnit.SECONDS);
            assertTrue(object instanceof ExceptionResponseDTO);
            assertEquals(((ExceptionResponseDTO) object).getExceptionMessage(), "game.not.started");
            Object object1 = blockingQueue1.poll(3, TimeUnit.SECONDS);
            assertNull(object1);
        }

        @Test
        void shouldThrowUserNotFoundInGameExceptionWhenSendingAnswerByUserNotInGame() throws InterruptedException {
            AnswerRequestDTO answerRequestDTO2 =
                new AnswerRequestDTO(Map.of("msocha19", List.of("a", "b")));
            session2.send("/game/" + gameID + "/answers", answerRequestDTO2);
            Object object = blockingQueue2.poll(2, TimeUnit.SECONDS);
            assertTrue(object instanceof ExceptionResponseDTO);
            assertEquals(((ExceptionResponseDTO) object).getExceptionMessage(), "user.not.found.in.game");
            Object object1 = blockingQueue1.poll(3, TimeUnit.SECONDS);
            assertNull(object1);
        }
    }
}
