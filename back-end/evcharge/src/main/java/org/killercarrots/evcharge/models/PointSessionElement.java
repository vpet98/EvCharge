package org.killercarrots.evcharge.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PointSessionElement {
    private int index;
    private String id, startTime, endTime, protocol;
    private double delivered;

    public PointSessionElement() {
        /* nothing */
    }

    public PointSessionElement(int index, String id, String startTime, String endTime, String protocol, Double delivered){
        this.index = index;
        this.id = id;
        this. startTime = startTime;
        this.endTime = endTime;
        this.protocol = protocol;
        this.delivered = delivered;
    }

    @Override
    public String toString() {
        String ret = "{\"SessionIndex\":"+String.valueOf(this.index)+","+
                        "\"SessionID\":\""+this.id+"\","+
                        "\"StartedOn\":\""+this.startTime+"\","+
                        "\"FinishedOn\":\""+this.endTime+"\","+
                        "\"Protocol\":\""+this.protocol+"\","+
                        "\"EnergyDelivered\":"+String.valueOf(delivered)+"}";
        return ret;
    }
}
