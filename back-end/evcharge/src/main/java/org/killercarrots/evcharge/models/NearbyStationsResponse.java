package org.killercarrots.evcharge.models;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NearbyStationsResponse extends MyAbstractObj {

    @Setter
    @Getter
    private class StationDetails{
        private String stationId, operator;
        private double cost;
        private String address;
        private double lat, lon;

        @Override
        public String toString() {
            String ret = "{\"StationId\":\""+this.stationId+"\","+
                            "\"Operator\":\""+this.operator+"\","+
                            "\"Address\":\""+this.address+"\","+
                            "\"CostPerKWh\":"+String.valueOf(this.cost)+","+
                            "\"Latitude\":"+String.valueOf(this.lat)+","+
                            "\"Longitude\":"+String.valueOf(this.lon)+"}";
            return ret;
        }
    }

    private double lat, lon;
    private int radius, found;
    private List<StationDetails> stations;

    public NearbyStationsResponse(double lat, double lon, int radius) {
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.found = 0;
        this.stations = new ArrayList<StationDetails>();
    }

    public void buildList(List<Station> ls) {
        for(Station i : ls) {
            this.found++;
            StationDetails st = new StationDetails();
            st.setStationId(i.getId());
            st.setOperator(i.getOperator());
            st.setAddress(i.getLocation().getAddress());
            st.setCost(i.getCost());
            st.setLat(i.getLocation().getGeo().getCoordinates()[1]);
            st.setLon(i.getLocation().getGeo().getCoordinates()[0]);
            this.stations.add(st);
        }
    }

    @Override
    public String toCsv() {
        String ret = "StationId,Operator,Address,CostPerKWh,Latitude,Longitude";
        for(StationDetails i : this.stations) {
            ret = ret + "\n"+i.getStationId()+","+i.getOperator()+","+i.getAddress()+","+String.valueOf(i.getCost())+","+String.valueOf(i.getLat())+","+String.valueOf(i.getLon());
        }
        return ret;
    }

    @Override
    public String toJson() {
        String ret = "{\"StationsFound:\":"+String.valueOf(this.found)+","+
                        "\"SearchLatitude\":"+String.valueOf(this.lat)+","+
                        "\"SearchLongitude\":"+String.valueOf(this.lon)+","+
                        "\"SearchRadius\":"+String.valueOf(this.radius)+","+
                        "\"Stations\":"+this.stations.toString()+"}";
        return ret;
    }
    
}
