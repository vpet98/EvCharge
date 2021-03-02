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
        String ret = "StationID,Operator,Address,Country,Latitude,Longitude,CostPerKWh,PointsList(\"id_power_currentType_port\")\n";
        for(Station s : this.stations) {
            ret += s.getId()+","+s.getOperator()+","+s.getLocation().getAddress()+","+s.getLocation().getCountry()+","+
                   s.getLocation().getGeo().getCoordinates()[1]+","+s.getLocation().getGeo().getCoordinates()[0]+","+
                   s.getCost()+",[";
            for (Point p : s.getPoints())
              ret += "\""+p.getLocalId()+"_"+p.getPower()+"_"+p.getType()+"_"+p.getPort()+"\",";
            ret = ret.substring(0, ret.length() - 1) + "]\n";
        }
        ret = ret.substring(0, ret.length() - 1);
        return ret;
    }

    @Override
    public String toJson() {
        String stationPrint = "";
        for (Station s : this.stations) {
          stationPrint += "{\"StationId\":\""+s.getId()+"\","+
                             "\"Operator\":\""+s.getOperator()+"\","+
                             "\"Address\":\""+s.getLocation().getAddress()+"\","+
                             "\"Country\":\""+s.getLocation().getCountry()+"\","+
                             "\"Latitude\":"+s.getLocation().getGeo().getCoordinates()[1]+","+
                             "\"Longitude\":"+s.getLocation().getGeo().getCoordinates()[0]+","+
                             "\"CostPerKWh\":"+s.getCost()+","+
                             "\"PointsList\":[";

          for (Point p : s.getPoints())
            stationPrint += "{\"PointId\":"+p.getLocalId()+","+
                               "\"Power\":"+p.getPower()+","+
                               "\"CurrentType\":\""+p.getType()+"\","+
                               "\"Port\":\""+p.getPort()+"\""+"},";
          stationPrint = stationPrint.substring(0, stationPrint.length() - 1);
          stationPrint += "]},";
        }
        stationPrint = stationPrint.substring(0, stationPrint.length() - 1);
        String ret = "{\"NumberOfStations\":"+String.valueOf(this.stationsNum)+","+
                        "\"StationsList\":["+stationPrint+"]}";
        return ret;
    }

}
