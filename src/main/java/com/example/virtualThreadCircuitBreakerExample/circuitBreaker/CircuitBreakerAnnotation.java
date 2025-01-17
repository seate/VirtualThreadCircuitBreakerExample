package com.example.virtualThreadCircuitBreakerExample.circuitBreaker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CircuitBreakerAnnotation {
    String name() default "";

    int failureThreshold() default 5;
    long resetTimeout() default 600000;

    boolean isTimeout() default false;
    long timeout() default 10000;
}
