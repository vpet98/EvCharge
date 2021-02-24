package org.killercarrots.evcharge.models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VehicleSession {
    private int index;
    private String id, operator, start, end;
    private double energy, costPerKW, totalCost;

    public VehicleSession(int index, String id, String operator, String start, String end, double energy, double costPerKW, double totalCost) {
        this.index = index;
        this.id = id;
        this.operator = operator;
        this.start = start;
        this.end = end;
        this.energy = energy;
        this.costPerKW = costPerKW;
        this. totalCost = totalCost;
    }

    @Override
    public String toString(){
        String ret = "{\"SessionIndex\":"+String.valueOf(this.index)+","+
                        "\"SessionID\":\""+this.id+"\","+
                        "\"EnergyProvider\":\""+this.operator+"\","+
                        "\"StartedOn\":\""+this.start+"\","+
                        "\"FinishedOn\":\""+this.end+"\","+
                        "\"EnergyDelivered\":"+String.valueOf(this.energy)+","+
                        "\"CostPerKWh\":"+String.valueOf(this.costPerKW)+","+
                        "\"SessionCost\":"+String.valueOf(this.totalCost)+"}";
                        
        return ret;
    }

}
