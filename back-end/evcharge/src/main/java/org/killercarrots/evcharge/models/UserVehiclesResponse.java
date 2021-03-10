package org.killercarrots.evcharge.models;

import java.util.List;
import java.util.ArrayList;

import lombok.Getter;

@Getter
public class UserVehiclesResponse extends MyAbstractObj {

    private int vehiclesNum;
    private List<Vehicle> vehicles = new ArrayList<Vehicle>();

    public UserVehiclesResponse(List<Vehicle> vehicles) {
        this.vehiclesNum = vehicles.size();
        this.vehicles = vehicles;
    }

    @Override
    public String toCsv() {
        String ret = "VehicleID,Brand,Model,Variant\n";
        for(Vehicle v : this.vehicles)
          if (v.getVariant().equals(""))
            ret += v.getId()+","+v.getBrand()+","+v.getModel()+",(not_any)\n";
          else
            ret += v.getId()+","+v.getBrand()+","+v.getModel()+","+v.getVariant()+"\n";
        ret = ret.substring(0, ret.length() - 1);
        return ret;
    }

    @Override
    public String toJson() {
        String veh = "";
        for (Vehicle v : this.vehicles)
          veh += "{\"VehicleId\":\""+v.getId()+"\","+
                    "\"Brand\":\""+v.getBrand()+"\","+
                    "\"Model\":\""+v.getModel()+"\","+
                    "\"Variant\":\""+v.getVariant()+"\""+"},";
        veh = veh.substring(0, veh.length() - 1);
        String ret = "{\"NumberOfVehicles\":"+String.valueOf(this.vehiclesNum)+","+
                        "\"VehiclesList\":["+veh+"]}";
        return ret;
    }

}
