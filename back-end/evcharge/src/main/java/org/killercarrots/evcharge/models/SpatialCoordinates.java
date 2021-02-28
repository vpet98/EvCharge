package org.killercarrots.evcharge.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpatialCoordinates {
    private String type;
    private float[] coordinates;
}
