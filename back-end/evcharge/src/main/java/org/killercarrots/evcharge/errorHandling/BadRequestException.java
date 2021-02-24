package org.killercarrots.evcharge.errorHandling;

@SuppressWarnings("serial")
public class BadRequestException extends Exception {
    public BadRequestException(String message) {
        super(message);
    }
}
