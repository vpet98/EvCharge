package org.killercarrots.evcharge.models;

import org.killercarrots.evcharge.models.Location;
import org.killercarrots.evcharge.models.Point;

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
@Document(collection = "stations")
public class Station {
    @MongoId
    private String id;
    private String operator;
    private double cost;
    private Location location;
    // Station points
    private Set<Point> points = new HashSet<>();

    public Station() {
        /***initialization_is_done_through_setters***/
    }
}
