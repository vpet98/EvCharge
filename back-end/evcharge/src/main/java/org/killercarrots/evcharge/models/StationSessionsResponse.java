package org.killercarrots.evcharge.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.killercarrots.evcharge.GeneralController;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class StationSessionsResponse extends MyAbstractObj {

    private String stationId, operator, timestamp, fromDate, toDate;
    private double totalDelivered;
    private int sessionsNum, active;
    private List<PointSessionsSummary> sessions;

    public StationSessionsResponse(String stationId, String operator, String fromDate, String toDate) {
        this.stationId = stationId;
        this.operator = operator;
        Date date = new Date(System.currentTimeMillis());
        this.timestamp = GeneralController.formatter.format(date);
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.sessions = new ArrayList<PointSessionsSummary>();
    }

    public void buildList(List<ChargeEvent> ls) {
        for(ChargeEvent e : ls) {
            this.sessionsNum++;
            int index = this.sessions.indexOf(new PointSessionsSummary(e.getPointId()));
            if(index != -1) {
                this.sessions.get(index).increase(e.getKWhDelivered());
            } else {
                PointSessionsSummary p = new PointSessionsSummary(e.getPointId());
                p.increase(e.getKWhDelivered());
                this.sessions.add(p);
            }
        }
        for(PointSessionsSummary i : this.sessions) {
            this.totalDelivered = this.totalDelivered + i.getDelivered();
        }
        this.active = this.sessions.size();
    }

    @Override
    public String toCsv() {
        String ret = "StationID,Operator,RequestTimestamp,PeriodFrom,PeriodTo,TotalEnergyDelivered,NumberOfChargingSessions,NumberOfActivePoints\n"+
                        this.stationId+","+this.operator+","+this.timestamp+","+this.fromDate+","+this.toDate+","+String.valueOf(this.totalDelivered)+","+String.valueOf(this.sessionsNum)+","+String.valueOf(this.active)+"\n";
                ret = ret +"PointID,PointSessions,EnergyDelivered";
                for(PointSessionsSummary i : this.sessions) {
                    ret = ret + "\n"+i.getPointId()+","+String.valueOf(i.getSessions())+","+String.valueOf(i.getDelivered());
                }
        return ret;
    }

    @Override
    public String toJson() {
        String ret = "{\"StationID\":\""+this.stationId+"\","+
                        "\"Operator\":\""+this.operator+"\","+
                        "\"RequestTimestamp\":\""+this.timestamp+"\","+
                        "\"PeriodFrom\":\""+this.fromDate+"\","+
                        "\"PeriodTo\":\""+this.toDate+"\","+
                        "\"TotalEnergyDelivered\":"+this.totalDelivered+","+
                        "\"NumberOfChargingSessions\":"+this.sessionsNum+","+
                        "\"NumberOfActivePoints\":"+this.active+","+
                        "\"SessionsSummaryList\":"+this.sessions.toString()+"}";

        return ret;
    }
    
}
