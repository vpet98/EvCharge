package org.killercarrots.evcharge.models;

import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Point {
    @Id
    private int localId;
    private double power;
    private String type;
    private String port;

    public Point() {
        /***initialization_is_done_through_setters***/
    }
}
