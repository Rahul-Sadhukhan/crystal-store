package com.walmart.realestate.crystal.storereview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

public class TestAsyncConfig {

    @Bean
    public TaskExecutor taskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

    @Bean
    public TaskExecutor noUserContextPrimaryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(12);
        executor.setMaxPoolSize(400);
        executor.setKeepAliveSeconds(80);
        executor.setQueueCapacity(5);
        executor.setAwaitTerminationSeconds(50);
        executor.afterPropertiesSet();
        return executor;

    }

    @Bean
    public TaskExecutor noUserContextSecondaryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(100);
        executor.setKeepAliveSeconds(80);
        executor.setQueueCapacity(5);
        executor.setAwaitTerminationSeconds(50);
        executor.afterPropertiesSet();
        return executor;

    }

    @Bean
    public DelegatingSecurityContextAsyncTaskExecutor taskExecutorSecondary() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(50);
        executor.setKeepAliveSeconds(80);
        executor.setQueueCapacity(5);
        executor.setAwaitTerminationSeconds(50);
        executor.afterPropertiesSet();
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }

}
