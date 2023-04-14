package pl.lodz.p.it.zzpj;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

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
                .withExposedPorts(27017)
                .withReuse(true);
        }

        private static Integer getMongoMappedPort() {
            return mongoContainer.getMappedPort(27017);
        }
    }

    @BeforeAll
    public static void setup() {
        mongoContainer = new MongoDBContainer();
        mongoContainer.start();
    }

    @AfterAll
    static void clearContainers() {
        mongoContainer.stop();
    }
}
