package com.example.virtualThreadCircuitBreakerExample.circuitBreaker.exception;


public class RequestNotAllowedException extends RuntimeException {
    public RequestNotAllowedException(String message) {
        super(message);
    }
}
