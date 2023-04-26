package pl.lodz.p.it.zzpj;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.time.Duration;

@SpringBootTest
@Testcontainers
public abstract class TestContainersSetup {
    private static GenericContainer<MongoDBContainer> mongoContainer;

    @DynamicPropertySource
    static void registerMongoDBProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.port", MongoDBContainer::getMongoMappedPort);
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
    }
}
