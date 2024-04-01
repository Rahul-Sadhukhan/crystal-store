package com.walmart.realestate.crystal.storereview.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
public class TestCacheConfig {

    @Primary
    @Bean
    public CacheManager cloudCacheManager() {
        return new ConcurrentMapCacheManager();
    }

}
