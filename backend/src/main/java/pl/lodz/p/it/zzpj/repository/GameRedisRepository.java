package pl.lodz.p.it.zzpj.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.zzpj.exception.game.GameNotFoundException;
import pl.lodz.p.it.zzpj.model.Game;

import java.util.UUID;

@Repository
public class GameRedisRepository {
    @CachePut(value = "${spring.cache.cache-names}", key = "#game.id")
    public Game putGame(Game game) {
        return game;
    }

    @Cacheable(value = "${spring.cache.cache-names}", key = "#gameID")
    public Game getGame(UUID gameID) throws GameNotFoundException {
        throw new GameNotFoundException();
    }

    @CacheEvict(value = "${spring.cache.cache-names}", key = "#gameID")
    public void deleteGame(UUID gameID) {
    }
}


