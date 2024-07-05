package com.example.virtualThreadCircuitBreakerExample.circuitBreaker.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CircuitBreakerExceptionHandler {

    @ExceptionHandler(RequestNotAllowedException.class)
    public ResponseEntity<String> handleException(RequestNotAllowedException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
