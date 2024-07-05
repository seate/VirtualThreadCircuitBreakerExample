package com.example.virtualThreadCircuitBreakerExample.circuitBreaker;

import com.example.virtualThreadCircuitBreakerExample.circuitBreaker.exception.RequestNotAllowedException;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class CircuitBreaker {

    private final TaskScheduler taskScheduler;
    private final int failureThreshold;
    private final long resetTimeout;

    private int failureCount = 0;
    private State state = State.CLOSED;
    private final ReentrantLock lock = new ReentrantLock();

    @Builder
    public CircuitBreaker(TaskScheduler taskScheduler, int failureThreshold, long resetTimeout) {
        this.taskScheduler = taskScheduler;
        this.failureThreshold = failureThreshold;
        this.resetTimeout = resetTimeout;
    }

    public void recordFailure(ProceedingJoinPoint joinPoint) {
        if (state == State.CLOSED) {
            log.error("{} is failed in 'CLOSED' state", joinPoint.toString());

            if ((++failureCount) < failureThreshold) return;
            log.error("Circuit breaker state changed into 'OPEN' state in {} process", joinPoint.toString());
        }
        else if (state == State.HALF_OPEN) {
            log.error("{} is failed in 'HALF_OPEN' state", joinPoint.toString());
        }

        state = State.OPEN;
        taskScheduler.schedule(() -> {
            state = State.HALF_OPEN;
            log.debug("Circuit breaker state changed into 'HALF_OPEN' state in {} process", joinPoint.toString());
        }, Instant.now().plusMillis(resetTimeout));
    }

    public void recordSuccess(ProceedingJoinPoint joinPoint) {
        if (state == State.HALF_OPEN) {
            state = State.CLOSED;
            failureCount = 0;
            log.debug("Circuit breaker state changed into 'CLOSED' state in {} process", joinPoint.toString());
        }
    }

    public boolean allowRequest() {
        return state != State.OPEN;
    }


    public Object processCircuitBreaker(ProceedingJoinPoint joinPoint) throws Throwable {
        lock.lock();
        if (!allowRequest()) {
            log.error("{} is not processable now", joinPoint.toString());
            lock.unlock();
            throw new RequestNotAllowedException(String.format("%s의 circuitBreaker가 현재 OPEN 상태입니다.", joinPoint.toString()));
        }

        try {
            Object proceed = joinPoint.proceed();
            recordSuccess(joinPoint);

            return proceed;
        } catch (Throwable e) {
            recordFailure(joinPoint);
            throw e;
        } finally {
            lock.unlock();
        }
    }
}
