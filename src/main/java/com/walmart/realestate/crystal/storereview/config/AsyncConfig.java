package com.walmart.realestate.crystal.storereview.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public DelegatingSecurityContextAsyncTaskExecutor taskExecutor(ThreadPoolTaskExecutor applicationTaskExecutor) {
        return new DelegatingSecurityContextAsyncTaskExecutor(applicationTaskExecutor);
    }

    @Bean
    public ThreadPoolTaskExecutor applicationTaskExecutor(BeanFactory beanFactory, TaskExecutorBuilder builder) {
        return new LazyTraceThreadPoolTaskExecutor(beanFactory, builder.build());
    }

    @Bean
    public DelegatingSecurityContextAsyncTaskExecutor taskExecutorSecondary(BeanFactory beanFactory) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(50);
        executor.setKeepAliveSeconds(80);
        executor.setQueueCapacity(5);
        executor.setAwaitTerminationSeconds(50);
        executor.afterPropertiesSet();
        executor.setTaskDecorator(new MultiTenantTaskDecorator());
        return new DelegatingSecurityContextAsyncTaskExecutor(new LazyTraceThreadPoolTaskExecutor(beanFactory, executor));
    }


    @Bean
    public TaskExecutor noUserContextPrimaryTaskExecutor(BeanFactory beanFactory) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(12);
        executor.setMaxPoolSize(400);
        executor.setKeepAliveSeconds(80);
        executor.setQueueCapacity(5);
        executor.setAwaitTerminationSeconds(50);
        executor.afterPropertiesSet();
        executor.setTaskDecorator(new MultiTenantTaskDecorator());
        return new LazyTraceThreadPoolTaskExecutor(beanFactory, executor);

    }

    @Bean
    public TaskExecutor noUserContextSecondaryTaskExecutor(BeanFactory beanFactory) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setKeepAliveSeconds(80);
        executor.setQueueCapacity(5);
        executor.setAwaitTerminationSeconds(50);
        executor.afterPropertiesSet();
        executor.setTaskDecorator(new MultiTenantTaskDecorator());
        return new LazyTraceThreadPoolTaskExecutor(beanFactory, executor);

    }


}
