package com.walmart.realestate.crystal.storereview.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.Collection;

@Slf4j
@Aspect
@Configuration
public class LoggingConfig {

    @Around("publicMethod() && hasLogger()")
    public Object invokeInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        final var joinPoints = Arrays.toString(joinPoint.getArgs());
        final var stopWatch = new StopWatch();
        log.info("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(), joinPoints);
        stopWatch.start();
        final Object result = joinPoint.proceed();
        stopWatch.stop();
        if (result instanceof Collection) {
            log.info("Exit: {}.{}() for argument[s] {} with result count= {} | execution time [{}]ms", joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), joinPoints, CollectionUtils.size(result), stopWatch.getTotalTimeMillis());
        } else {
            log.info("Exit: {}.{}() for argument[s] {} with result = {} | execution time [{}]ms", joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), joinPoints, result, stopWatch.getTotalTimeMillis());
        }
        return result;
    }

    @Around("privateMethod() && hasLogger()")
    public Object invokeDebug(ProceedingJoinPoint joinPoint) throws Throwable {
        final var joinPoints = Arrays.toString(joinPoint.getArgs());
        final var stopWatch = new StopWatch();
        log.info("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(), joinPoints);
        stopWatch.start();
        final Object result = joinPoint.proceed();
        stopWatch.stop();
        if (result instanceof Collection) {
            log.info("Exit: {}.{}() for argument[s] {} with result count= {} | execution time [{}]ms", joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), joinPoints, CollectionUtils.size(result), stopWatch.getTotalTimeMillis());
        } else {
            log.info("Exit: {}.{}() for argument[s] {} with result = {} | execution time [{}]ms", joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), joinPoints, result, stopWatch.getTotalTimeMillis());
        }
        return result;
    }

    @AfterThrowing(pointcut = "hasLogger()", throwing = "e")
    public void logException(JoinPoint joinPoint, Throwable e) {
        final var joinPoints = Arrays.toString(joinPoint.getArgs());
        log.info("Exception in {}.{}() for argument[s] {} with cause = {}", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(), joinPoints, e.getMessage());
    }

    @Pointcut("execution(public * *(..))")
    public void publicMethod() {
        // annotation method
    }

    @Pointcut("execution(private * *(..))")
    public void privateMethod() {
        // annotation method
    }

    @Pointcut("@annotation(com.walmart.realestate.crystal.annotation.Logger)")
    public void hasLogger() {
        // annotation method
    }

}
