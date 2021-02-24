package org.killercarrots.evcharge.models;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Document(collection = "chargeEvents")
public class ChargeEvent {
    @MongoId
    private String eventId;
    private String stationId, pointId, vehicleId, operator, startTime, endTime;
    private double kWhDelivered, costPerKWh, sessionCost;
    private String protocol, user;

    public ChargeEvent() {
        /* nothing */
    }

    @Override
    public String toString(){
        String ret = "{\"StationID\":\""+this.stationId+"\","+
                        "\"SessionID\":\""+this.eventId+"\","+
                        "\"VehicleID\":\""+this.vehicleId+"\","+
                        "\"StartedOn\":\""+this.startTime+"\","+
                        "\"FinishedOn\":\""+this.endTime+"\","+
                        "\"EnergyDelivered\":"+this.kWhDelivered+","+
                        "\"CostPerKWh\":"+this.costPerKWh+","+
                        "\"TotalCost\":\""+this.sessionCost+"\"}";
        return ret;
    }
}
