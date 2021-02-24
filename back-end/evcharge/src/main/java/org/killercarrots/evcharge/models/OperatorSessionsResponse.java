package org.killercarrots.evcharge.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.killercarrots.evcharge.GeneralController;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OperatorSessionsResponse extends MyAbstractObj {

    private String operator, timestamp, fromDate, toDate;
    private int sessionsNum;
    private double totalKW;
    private List<ChargeEvent> sessions;

    public OperatorSessionsResponse(String operator, String fromDate, String toDate) {
        this.operator = operator;
        Date date = new Date(System.currentTimeMillis());
        this.timestamp = GeneralController.formatter.format(date);
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.totalKW = 0;
        this.sessions = new ArrayList<ChargeEvent>();
    }

    public void buildList(List<ChargeEvent> ls) {
        for(ChargeEvent e : ls) {
            this.totalKW = this.totalKW + e.getKWhDelivered();
        }
        this.sessions = ls;
        this.sessionsNum = ls.size();
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
        String ret = "{\"ProviderID\":\""+this.operator+"\","+
                        "\"RequestTimestamp\":\""+this.timestamp+"\","+
                        "\"PeriodFrom\":\""+this.fromDate+"\","+
                        "\"PeriodTo\":\""+this.toDate+"\","+
                        "\"NumberOfChargingSessions\":"+String.valueOf(this.sessionsNum)+","+
                        "\"TotalConsumption\":"+String.valueOf(this.totalKW)+","+
                        "\"ChargingSessionsList\":"+this.sessions.toString()+"}";
        return ret;
    }
    
}
