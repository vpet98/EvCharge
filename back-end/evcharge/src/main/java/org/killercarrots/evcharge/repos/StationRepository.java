package org.killercarrots.evcharge.repos;

import java.util.List;
import java.util.HashSet;
import java.util.Optional;

import org.killercarrots.evcharge.models.Station;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface StationRepository extends MongoRepository<Station, String> {
    Optional<Station> findById(String id);
    HashSet<Station> findByOperator(String operator);

    @Query(value="{\"location.geo\":\n" +
    "       { $near :\n" +
    "          {\n" +
    "            $geometry : {\n" +
    "               type : \"Point\" ,\n" +
    "               coordinates : [ ?0, ?1] }\n" +
    "            $maxDistance : ?2" +
    "          }\n" +
    "       }}")
    public List<Station> nearByStations(double longitude, double latitude, int radius);
}
