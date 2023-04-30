package pl.lodz.p.it.zzpj;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

import java.io.IOException;
import java.time.Duration;

@SpringBootTest
@Testcontainers
public abstract class TestContainersSetup {
    private static GenericContainer<MongoDBContainer> mongoContainer;

    private static GenericContainer<RedisContainer> redisContainer;

    @DynamicPropertySource
    static void registerMongoDBAndRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.port", MongoDBContainer::getMongoMappedPort);
        registry.add("spring.data.redis.port", RedisContainer::getRedisMappedPort);
    }

    @BeforeAll
    public static void setup() throws IOException, InterruptedException {
        mongoContainer = new MongoDBContainer();
        mongoContainer.start();
        mongoContainer.execInContainer(
            "/usr/bin/mongoimport",
            "--collection=account",
            "--db=wordio",
            "--username=word-io",
            "--password=word-io-pwd",
            "--authenticationDatabase=admin",
            "./accounts.json");

        redisContainer = new RedisContainer();
        redisContainer.start();
    }

    private static class MongoDBContainer extends GenericContainer<MongoDBContainer> {
        public MongoDBContainer() {
            super(DockerImageName.parse("mongo:6.0.5"));
            this.withEnv("MONGO_INITDB_ROOT_USERNAME", "word-io")
                .withEnv("MONGO_INITDB_ROOT_PASSWORD", "word-io-pwd")
                .withCopyFileToContainer(MountableFile.forClasspathResource("accounts.json"), "/accounts.json")
                .waitingFor(new HttpWaitStrategy()
                    .forPort(27017)
                    .forStatusCodeMatching(response -> response == HTTP_OK || response == HTTP_UNAUTHORIZED)
                    .withStartupTimeout(Duration.ofMinutes(2)))
                .withExposedPorts(27017)
                .withReuse(true);
        }

        private static Integer getMongoMappedPort() {
            return mongoContainer.getMappedPort(27017);
        }
    }

    private static class RedisContainer extends GenericContainer<RedisContainer> {
        public RedisContainer() {
            super(DockerImageName.parse("redis/redis-stack:latest"));
            this.withExposedPorts(6379);
        }

        private static Integer getRedisMappedPort() {
            return redisContainer.getMappedPort(6379);
        }
    }
}
