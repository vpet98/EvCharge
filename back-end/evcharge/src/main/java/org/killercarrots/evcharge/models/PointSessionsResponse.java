package org.killercarrots.evcharge.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.killercarrots.evcharge.GeneralController;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointSessionsResponse extends MyAbstractObj {

    private String pointId, operator, timestamp, fromDate, toDate;
    private int sessionsNum;
    private List<PointSessionElement> sessions;

    public PointSessionsResponse(String pointId, String operator, String fromDate, String toDate) {
        this.pointId = pointId;
        this.operator = operator;
        Date date = new Date(System.currentTimeMillis());
        this.timestamp = GeneralController.formatter.format(date);
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.sessions = new ArrayList<PointSessionElement>();
    }

    public void buildList(List<ChargeEvent> ls) {
        int index = 0;
        PointSessionElement p;
        for(ChargeEvent e : ls) {
            index++;
            p = new PointSessionElement(index, e.getEventId(), e.getStartTime(), e.getEndTime(), e.getProtocol(), e.getKWhDelivered());
            this.sessions.add(p);
        }
        this.sessionsNum = index;
    }

    @Override
    public String toCsv() {
        String ret = "Point,PointOperator,RequestTimestamp,PeriodFrom,PeriodTo,NumberOfChargingSessions\n"+
                        String.valueOf(this.pointId)+","+this.operator+","+this.timestamp+","+this.fromDate+","+this.toDate+","+String.valueOf(this.sessionsNum)+"\n";
        ret = ret + "SessionIndex,SessionID,StartedOn,FinishedOn,Protocol,EnergyDelivered";
        for(PointSessionElement i : this.sessions) {
            ret = ret + "\n"+String.valueOf(i.getIndex())+","+i.getId()+","+i.getStartTime()+","+i.getEndTime()+","+i.getProtocol()+","+String.valueOf(i.getDelivered());
        }
        return ret;
    }

    @Override
    public String toJson() {
        String ret = "{\"Point\":\""+this.pointId+"\","+
                        "\"PointOperator\":\""+this.operator+"\","+
                        "\"RequestTimestamp\":\""+this.timestamp+"\","+
                        "\"PeriodFrom\":\""+this.fromDate+"\","+
                        "\"PeriodTo\":\""+this.toDate+"\","+
                        "\"NumberOfChargingSessions\":"+String.valueOf(this.sessionsNum)+","+
                        "\"ChargingSessionsList\":";
        ret = ret + sessions.toString() + "}";
        return ret;
    }

}
