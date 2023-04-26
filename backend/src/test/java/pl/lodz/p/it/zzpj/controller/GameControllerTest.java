package pl.lodz.p.it.zzpj.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.springframework.messaging.converter.StringMessageConverter;
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
import pl.lodz.p.it.zzpj.controller.dto.CreateGameDto;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
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
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    class WebSocketConnectionTest {
        private static final WebSocketStompClient stompClient =
            new WebSocketStompClient(
                new SockJsClient(List.of(
                    new WebSocketTransport(new StandardWebSocketClient())
                ))
            );

        @LocalServerPort
        private int port;

        private String url;
        private StompSession session;

        @BeforeAll
        static void setupStompClient() {
            stompClient.setMessageConverter(new StringMessageConverter());
        }

        @AfterEach
        void disconnect() {
            session.disconnect();
        }

        @BeforeEach
        void initSession() {
            url = "ws://localhost:" + port + "/wordio";
        }

        @Test
        void shouldConnectTest() throws InterruptedException, ExecutionException {
            session = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {
                })
                .get();

            assertNotNull(session);
            assertNotNull(session.getSessionId());

            var blockingQueue = new ArrayBlockingQueue<>(1);

            var sub = session.subscribe("/topic/game/1234", new StompFrameHandler() {
                @Override
                @NotNull
                public Type getPayloadType(@NotNull StompHeaders headers) {
                    return String.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    // save payload in blockingQueue so that we can retrieve it later
                    blockingQueue.add(payload);
                }
            });

            session.send("/game/1234", "some payload");

            Object payload = blockingQueue.poll(2, TimeUnit.SECONDS);
            assertNotNull(payload);

            String message = (String) payload;
            assertNotNull(message);
            assertEquals("SubscriptionMessage(gameID=1234, payload='some payload', principal=null)", message);

            sub.unsubscribe();
        }
    }
}
