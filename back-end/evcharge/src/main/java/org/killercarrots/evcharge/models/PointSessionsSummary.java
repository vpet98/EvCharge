package org.killercarrots.evcharge.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PointSessionsSummary {
    private String pointId;
    private int sessions;
    private double delivered;

    public PointSessionsSummary(String pointId) {
        this.pointId = pointId;
        this.sessions = 0;
        this.delivered = 0;
    }

    public void increase(double amount) {
        this.delivered = this.delivered + amount;
        this.sessions++;
    }

    @Override
    public String toString(){
        String ret = "{\"PointID\":\""+this.pointId+"\","+
                        "\"PointSessions\":"+this.sessions+","+
                        "\"EnergyDelivered\":"+this.delivered+"}";
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        try {
            PointSessionsSummary id = (PointSessionsSummary) o;
            if(this.pointId.equals(id.getPointId())){
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
