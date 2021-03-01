package org.killercarrots.evcharge.repos;

import java.util.List;

import org.killercarrots.evcharge.models.ChargeEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChargeEventsRepository extends MongoRepository<ChargeEvent, String> {
    List<ChargeEvent> findByPointIdAndStartTimeBetweenOrderByStartTimeAsc(String pointId, String start, String end);
    List<ChargeEvent> findByStationIdAndStartTimeBetweenOrderByStartTimeAsc(String stationId, String start, String end);
    List<ChargeEvent> findByVehicleIdAndStartTimeBetweenOrderByStartTimeAsc(String vehicleId, String start, String end);
    List<ChargeEvent> findByOperatorAndStartTimeBetweenOrderByStartTimeAsc(String operator, String start, String end);
    List<ChargeEvent> findByUser(String username);
}
