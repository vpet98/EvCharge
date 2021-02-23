package org.killercarrots.evcharge.repos;

import java.util.Optional;

import org.killercarrots.evcharge.models.Station;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StationRepository extends MongoRepository<Station, String> {
    Optional<Station> findById(String id);
}
