package org.killercarrots.evcharge.models;

import java.util.Map;
import java.util.HashMap;

import lombok.Getter;

@Getter
public class PointInfoResponse extends MyAbstractObj {
    private Map<String, String> fields_messages = new HashMap<String, String>();

    public PointInfoResponse(HashMap<String, String> map){
        this.fields_messages = map;
    }

    @Override
    public String toCsv() {
      String out = "";
      for (String s : fields_messages.keySet())
        out += s + ",";
      out = out.substring(0, out.length() - 1);
      out += "\n";
      for (String s : fields_messages.keySet())
        out += fields_messages.get(s) + ",";
      out = out.substring(0, out.length() - 1);
      return out;
    }

    @Override
    public String toJson() {
      String out = "";
      for (String s : fields_messages.keySet())
        if (fields_messages.get(s).charAt(0) == '[' || s.equals("cost"))
          out += "\"" + s + "\": " + fields_messages.get(s) + ", ";
        else out += "\"" + s + "\": \"" + fields_messages.get(s) + "\", ";
      out = out.substring(0, out.length() - 2);
      return "{" + out + "}";
    }
}
