package org.killercarrots.evcharge.models;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Document(collection = "vehicles")
public class Vehicle {
    @MongoId
    private String id;
    private String brand;
    private String model;
    private String variant;
    private double consumption;
    private double batterySize;
    private Current ac;
    private Current dc;

    public class Current {
      public Set<String> ports = new HashSet<>();
      public double max_power;
    }

    public Vehicle() {
        /***initialization_is_done_through_setters***/
    }
}
