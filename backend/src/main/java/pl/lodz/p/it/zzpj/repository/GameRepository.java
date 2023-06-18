package pl.lodz.p.it.zzpj.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.zzpj.entity.GameDocument;

import java.util.UUID;

@Repository
public interface GameRepository extends MongoRepository<GameDocument, UUID> {
}
