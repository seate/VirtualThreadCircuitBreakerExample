package com.example.virtualThreadCircuitBreakerExample.circuitBreaker;

import com.example.virtualThreadCircuitBreakerExample.circuitBreaker.exception.RequestNotAllowedException;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class CircuitBreaker {

    private int failureCount = 0;
    private State state = State.CLOSED;
    private final ReentrantLock lock = new ReentrantLock();

    private final TaskScheduler taskScheduler;
    private final Integer failureThreshold;
    private final Long resetTimeout;
    private final Boolean isTimeout;
    private final Long timeout;


    @Builder
    public CircuitBreaker(TaskScheduler taskScheduler, Integer failureThreshold, Long resetTimeout, Boolean isTimeout, Long timeout) {
        this.taskScheduler = taskScheduler;
        this.failureThreshold = failureThreshold;
        this.resetTimeout = resetTimeout;
        this.isTimeout = isTimeout;
        this.timeout = timeout;
    }


    private void recordFailure(ProceedingJoinPoint joinPoint) {
        lock.lock();
        if (state == State.OPEN) {
            log.error("{} is failed in 'OPEN' state", joinPoint.toString());
            lock.unlock();
            return;
        }
        else if (state == State.HALF_OPEN) {
            log.error("{} is failed in 'HALF_OPEN' state", joinPoint.toString());
        }
        else if (state == State.CLOSED) {
            log.error("{} is failed in 'CLOSED' state", joinPoint.toString());

            if ((++failureCount) < failureThreshold) return;
            log.error("Circuit breaker state changed into 'OPEN' state in {} process", joinPoint.toString());
        }

        state = State.OPEN;
        taskScheduler.schedule(() -> {
            state = State.HALF_OPEN;
            log.debug("Circuit breaker state changed into 'HALF_OPEN' state in {} process", joinPoint.toString());
        }, Instant.now().plusMillis(resetTimeout));

        lock.unlock();
    }

    private void recordSuccess(ProceedingJoinPoint joinPoint) {
        if (state == State.HALF_OPEN) {
            lock.lock();
            state = State.CLOSED;
            failureCount = 0;
            lock.unlock();
            log.debug("Circuit breaker state changed into 'CLOSED' state in {} process", joinPoint.toString());
        }
    }

    private boolean allowRequest() {
        return state != State.OPEN;
    }


    private Object processCircuitBreakerWithTimeout(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!allowRequest()) {
            log.error("{} is not processable now", joinPoint.toString());
            throw new RequestNotAllowedException(String.format("%s의 circuitBreaker가 현재 OPEN 상태입니다.", joinPoint.toString()));
        }

        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });

        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future.cancel(true);
            recordFailure(joinPoint);

            throw e;
        }
    }

    private Object processCircuitBreakerWithoutTimeout(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!allowRequest()) {
            log.error("{} is not processable now", joinPoint.toString());
            throw new RequestNotAllowedException(String.format("%s의 circuitBreaker가 현재 OPEN 상태입니다.", joinPoint.toString()));
        }

        try {
            Object proceed = joinPoint.proceed();
            recordSuccess(joinPoint);

            return proceed;
        } catch (Throwable e) {
            recordFailure(joinPoint);
            throw e;
        }
    }

    public Object processCircuitBreaker(ProceedingJoinPoint joinPoint) throws Throwable {
        return (isTimeout) ? processCircuitBreakerWithTimeout(joinPoint) : processCircuitBreakerWithoutTimeout(joinPoint);
    }
}
