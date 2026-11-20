package com.dispatch.loadbalancer.exception;

/** Thrown when a dispatch plan is requested before orders and/or vehicles were submitted. */
public class InsufficientDataException extends RuntimeException {
    public InsufficientDataException(String message) {
        super(message);
    }
}
