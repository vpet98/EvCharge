package org.killercarrots.evcharge.repos;

import java.util.Optional;
import java.util.HashSet;

import org.killercarrots.evcharge.models.ActiveSession;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ActiveSessionRepository extends MongoRepository<ActiveSession, String> {
    Optional<ActiveSession> findById(String id);
    HashSet<ActiveSession> findByUser(String user);
}
