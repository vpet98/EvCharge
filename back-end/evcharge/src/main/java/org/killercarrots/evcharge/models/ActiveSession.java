package org.killercarrots.evcharge.models;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Document(collection = "activeSessions")
public class ActiveSession {
    @MongoId
    private String id;
    private String stationId;
    private String pointId;
    private String vehicleId;
    private String operator;
    private String startTime;
    private String user;
    private String protocol;
    private double kWhRequested;
    private double costPerKWh;

    public ActiveSession() {
        /***initialization_is_done_through_setters***/
    }
}
