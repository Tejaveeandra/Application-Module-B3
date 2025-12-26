package com.application.exception;
 
public class ZoneIdRequiredException extends RuntimeException {
    public ZoneIdRequiredException(String message) {
        super(message);
    }
}
