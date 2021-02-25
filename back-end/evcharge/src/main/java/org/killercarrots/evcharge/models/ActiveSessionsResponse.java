package org.killercarrots.evcharge.models;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AvtiveSessionsResponse extends MyAbstractObj {

    private int sessionsNum;
    private HashMap<String, Double> sessions = new HashMap<>();

    public ActiveSessionsResponse(HashMap map) {
        this.sessionsNum = map.size();
        this.sessions = map;
    }

    @Override
    public String toCsv() {
        String ret = "ProviderID,StationID,SessionID,VehicleID,StartedOn,FinishedOn,EnergyDelivered,CostPerKWh,TotalCost";
        for(ChargeEvent i : this.sessions) {
            ret = ret + "\n"+i.getOperator()+","+i.getStationId()+","+i.getEventId()+","+i.getVehicleId()+","+i.getStartTime()+","+i.getEndTime()+
                        ","+String.valueOf(i.getKWhDelivered())+","+String.valueOf(i.getCostPerKWh())+","+String.valueOf(i.getSessionCost());
        }
        return ret;
    }

    @Override
    public String toJson() {
        String ret = "{\"NumberOfChargingSessions\":"+String.valueOf(this.sessionsNum)+","+
                        "\"ActiveSessionsList\":"+this.sessions.toString()+"}";
        return ret;
    }

}
