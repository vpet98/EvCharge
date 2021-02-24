package org.killercarrots.evcharge.models;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Document(collection = "chargeEvents")
public class ChargeEvent {
    @MongoId
    private String eventId;
    private String stationId, pointId, vehicleId, operatorId, startTime, endTime;
    private double kWhDelivered, costPerKWh, sessionCost;
    private String protocol, user;

    public ChargeEvent() {
        /* nothing */
    }
}
