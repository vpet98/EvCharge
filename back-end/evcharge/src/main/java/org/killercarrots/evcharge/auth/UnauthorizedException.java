package org.killercarrots.evcharge.auth;

@SuppressWarnings("all")
public class UnauthorizedException extends Exception {
    public UnauthorizedException(String message){
        super(message);
    }
}
