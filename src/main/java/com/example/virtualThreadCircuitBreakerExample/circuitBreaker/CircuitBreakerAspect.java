package com.example.virtualThreadCircuitBreakerExample.circuitBreaker;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Aspect
@Component
public class CircuitBreakerAspect {

    private final Map<String, CircuitBreaker> circuitBreakerMap = new ConcurrentHashMap<>();

    private final TaskScheduler taskScheduler;

    @Autowired
    public CircuitBreakerAspect(@Qualifier("taskScheduler") TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    private CircuitBreaker getCircuitBreaker(ProceedingJoinPoint joinPoint) {
        CircuitBreakerAnnotation circuitBreakerAnnotation = ((MethodSignature) joinPoint.getSignature())
                .getMethod().getAnnotation(CircuitBreakerAnnotation.class);

        String name = (circuitBreakerAnnotation.name().isEmpty()) ? joinPoint.toString() : circuitBreakerAnnotation.name();

        return circuitBreakerMap.computeIfAbsent(name, (key) -> CircuitBreaker.builder()
                .isTimeout(circuitBreakerAnnotation.isTimeout())
                .timeout(circuitBreakerAnnotation.timeout())
                .taskScheduler(taskScheduler)
                .failureThreshold(circuitBreakerAnnotation.failureThreshold())
                .resetTimeout(circuitBreakerAnnotation.resetTimeout())
                .build());
    }


    @Around("@annotation(CircuitBreakerAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        return getCircuitBreaker(joinPoint).processCircuitBreaker(joinPoint);
    }

}
