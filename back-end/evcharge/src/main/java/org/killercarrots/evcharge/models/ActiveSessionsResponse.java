package org.killercarrots.evcharge.models;

import java.util.HashMap;

import lombok.Getter;

@Getter
public class ActiveSessionsResponse extends MyAbstractObj {

    private int sessionsNum;
    private HashMap<String, Double> sessions = new HashMap<String, Double>();

    public ActiveSessionsResponse(HashMap<String, Double> map) {
        this.sessionsNum = map.size();
        this.sessions = map;
    }

    @Override
    public String toCsv() {
        String ret = "SessionID,CurrentCost\n";
        for(String k : this.sessions.keySet())
            ret += k + "," + Double.toString(this.sessions.get(k)) + "\n";
        ret = ret.substring(0, ret.length() - 1);
        return ret;
    }

    @Override
    public String toJson() {
        String ses = "";
        for (String k : this.sessions.keySet())
          ses += "{\"SessionID\":" +  "\"" + k + "\",\"CurrentCost\":" + Double.toString(this.sessions.get(k)) + "},";
        ses = ses.substring(0, ses.length() - 1);
        String ret = "{\"NumberOfActiveSessions\":"+String.valueOf(this.sessionsNum)+","+
                        "\"ActiveSessionsList\":["+ses+"]}";
        return ret;
    }

}
