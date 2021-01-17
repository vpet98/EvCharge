package org.killercarrots.evcharge.repos;

import java.util.Optional;

import org.killercarrots.evcharge.models.ERole;
import org.killercarrots.evcharge.models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);

}
