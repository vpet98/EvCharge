package org.killercarrots.evcharge.models;

import java.util.List;
import java.util.ArrayList;

import lombok.Getter;

@Getter
public class UserVehiclesResponse extends MyAbstractObj {

    private int vehiclesNum;
    private List<String> vehicles = new ArrayList<String>();

    public UserVehiclesResponse(List<String> vehicles) {
        this.vehiclesNum = vehicles.size();
        this.vehicles = vehicles;
    }

    @Override
    public String toCsv() {
        String ret = "VehicleID\n";
        for(String id : this.vehicles)
            ret += id + "\n";
        ret = ret.substring(0, ret.length() - 1);
        return ret;
    }

    @Override
    public String toJson() {
        String veh = "";
        for (String id : this.vehicles)
          veh += "\"" + id + "\",";
        veh = veh.substring(0, veh.length() - 1);
        String ret = "{\"NumberOfVehicles\":"+String.valueOf(this.vehiclesNum)+","+
                        "\"VehiclesList\":["+veh+"]}";
        return ret;
    }

}
