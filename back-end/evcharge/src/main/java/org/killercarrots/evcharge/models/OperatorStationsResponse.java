package org.killercarrots.evcharge.models;

import java.util.HashSet;

import lombok.Getter;

@Getter
public class OperatorStationsResponse extends MyAbstractObj {

    private int stationsNum;
    private HashSet<Station> stations = new HashSet<Station>();

    public OperatorStationsResponse(HashSet<Station> set) {
        this.stationsNum = set.size();
        this.stations = set;
    }

    @Override
    public String toCsv() {
        String ret = "StationID\n";
        for(Station s : this.stations)
            ret += s.getId() + "\n";
        ret = ret.substring(0, ret.length() - 1);
        return ret;
    }

    @Override
    public String toJson() {
        String sids = "";
        for (Station s : this.stations)
          sids += "\"" + s.getId() + "\",";
        sids = sids.substring(0, sids.length() - 1);
        String ret = "{\"NumberOfStations\":"+String.valueOf(this.stationsNum)+","+
                        "\"StationIDsList\":["+sids+"]}";
        return ret;
    }

}
