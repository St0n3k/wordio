package pl.lodz.p.it.zzpj.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import pl.lodz.p.it.zzpj.entity.Account;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class MongoDBConfig {

    @Bean
    public BeforeConvertCallback<Account> beforeSaveCallback() {
        return (entity, collection) -> {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return entity;
        };
    }
}
