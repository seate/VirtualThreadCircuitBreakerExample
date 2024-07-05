package com.example.virtualThreadCircuitBreakerExample.virtualThread.service;

import com.example.virtualThreadCircuitBreakerExample.circuitBreaker.CircuitBreakerAnnotation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VirtualThreadService {

    @CircuitBreakerAnnotation(failureThreshold = 3, resetTimeout = 30000)
    public void virtualThreadService1(Boolean isFail) {
        System.out.println("Virtual Thread Service 1 executed");
        System.out.println("isFail: " + isFail);
        if (isFail) {
            throw new RuntimeException("Virtual Thread Service 1 failed");
        }
        else {
            System.out.println("Virtual Thread Service 1 success");
        }
    }
}
