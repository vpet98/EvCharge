package org.killercarrots.evcharge.repos;

import java.util.List;

import org.killercarrots.evcharge.models.ChargeEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChargeEventsRepository extends MongoRepository<ChargeEvent, String> {
    List<ChargeEvent> findByPointIdAndStartTimeBetweenOrderByStartTimeAsc(String pointId, String start, String end);
}
