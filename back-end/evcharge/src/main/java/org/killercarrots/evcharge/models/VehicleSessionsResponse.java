package org.killercarrots.evcharge.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.killercarrots.evcharge.GeneralController;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class VehicleSessionsResponse extends MyAbstractObj {

    private String vehicleId, timestamp, fromDate, toDate;
    private double totalConsumed;
    private int totalPoints, totalSessions;
    private List<VehicleSession> sessions;

    public VehicleSessionsResponse(String id, String fromDate, String toDate) {
        this.vehicleId = id;
        Date date = new Date(System.currentTimeMillis());
        this.timestamp = GeneralController.formatter.format(date);
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.sessions = new ArrayList<VehicleSession>();
        this.totalConsumed = 0;
    }

    public void buildList(List<ChargeEvent> ls) {
        int index = 0;
        HashSet<String> points = new HashSet<String>();
        for(ChargeEvent e : ls) {
            index++;
            points.add(e.getPointId());
            this.totalConsumed = this.totalConsumed + e.getKWhDelivered();
            VehicleSession s = new VehicleSession(index, e.getEventId(), e.getOperator(), e.getStartTime(), e.getEndTime(), e.getKWhDelivered(), e.getCostPerKWh(), e.getSessionCost());
            this.sessions.add(s);
        }
        this.totalPoints = points.size();
        this.totalSessions = index;
    }

    @Override
    public String toCsv() {
        String ret = "VehicleID,RequestTimestamp,PeriodFrom,PeriodTo,TotalEnergyConsumed,TotalEnergyConsumed,NumberOfVehicleChargingSessions\n"+
                        this.vehicleId+","+this.timestamp+","+this.fromDate+","+this.toDate+","+String.valueOf(this.totalConsumed)+","+String.valueOf(this.totalPoints)+","+String.valueOf(this.totalSessions)+"\n"+
                        "SessionIndex,SessionID,EnergyProvider,StartedOn,FinishedOn,ΕnergyDelivered,CostPerKWh,SessionCost";
        for(VehicleSession i : this.sessions) {
            ret = ret + "\n"+String.valueOf(i.getIndex())+","+i.getId()+","+i.getOperator()+","+i.getStart()+","+i.getEnd()+","+String.valueOf(i.getEnergy())+","+String.valueOf(i.getCostPerKW())+","+String.valueOf(i.getTotalCost());
        }
        return ret;
    }

    @Override
    public String toJson() {
        String ret = "{\"VehicleID\":\""+this.vehicleId+"\","+
                        "\"RequestTimestamp\":\""+this.timestamp+"\","+
                        "\"PeriodFrom\":\""+this.fromDate+"\","+
                        "\"PeriodTo\":\""+this.toDate+"\","+
                        "\"TotalEnergyConsumed\":"+String.valueOf(this.totalConsumed)+","+
                        "\"NumberOfVisitedPoints\":"+String.valueOf(this.totalPoints)+","+
                        "\"NumberOfVehicleChargingSessions\":"+String.valueOf(this.totalSessions)+","+
                        "\"VehicleChargingSessionsList\":"+this.sessions.toString()+"}";
                        
        return ret;
    }
    
}
