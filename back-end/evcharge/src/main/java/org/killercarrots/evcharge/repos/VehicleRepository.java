package org.killercarrots.evcharge.repos;

import java.util.Optional;

import org.killercarrots.evcharge.models.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    Optional<Vehicle> findById(String id);
}
