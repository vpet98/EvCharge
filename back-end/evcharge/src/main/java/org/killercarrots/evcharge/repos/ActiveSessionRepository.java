package org.killercarrots.evcharge.repos;

import java.util.Optional;

import org.killercarrots.evcharge.models.ActiveSession;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ActiveSessionRepository extends MongoRepository<ActiveSession, String> {
    Optional<ActiveSession> findById(String id);
    List<ActiveSession> findByUser(String user);
}
