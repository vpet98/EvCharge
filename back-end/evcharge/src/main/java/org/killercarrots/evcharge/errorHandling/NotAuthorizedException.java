package org.killercarrots.evcharge.errorHandling;

@SuppressWarnings("serial")
public class NotAuthorizedException extends Exception {
    public NotAuthorizedException(String message) {
        super(message);
    }
}
