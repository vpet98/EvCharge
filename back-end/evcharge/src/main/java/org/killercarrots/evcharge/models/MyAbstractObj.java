package org.killercarrots.evcharge.models;

// Just a dummy abstract class that defines
// the functions for the 2 response types
public abstract class MyAbstractObj {

    // Must implement both response types
    public abstract String toCsv();
    public abstract String toJson();
    
}
