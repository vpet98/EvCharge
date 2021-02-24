package org.killercarrots.evcharge.errorHandling;

@SuppressWarnings("serial")
public class NoDataException extends Exception {
    public NoDataException(String message) {
        super(message);
    }
}
