package com.walmart.realestate.crystal.storereview.config;

import com.walmart.realestate.crystal.storereview.tenant.TenantContext;
import io.sixhours.memcached.cache.MemcachedCacheManager;
import io.sixhours.memcached.cache.MemcachedCacheProperties;
import io.sixhours.memcached.cache.XMemcachedCacheManagerFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.util.Arrays;

@Configuration
@EnableCaching
@EnableConfigurationProperties({MemcachedCacheProperties.class})
@Profile("!test")
public class CacheConfig extends CachingConfigurerSupport {

    @Primary
    @Bean
    public MemcachedCacheManager cloudCacheManager(MemcachedCacheProperties properties) throws IOException {
        return new XMemcachedCacheManagerFactory(properties).create();
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            var hashableString = Arrays.stream(params)
                    .map(Object::toString)
                    .reduce(String::concat).orElse("");
            return DigestUtils.sha256Hex(TenantContext.getCurrentTenant() + "_" + target.getClass().getSimpleName() + "." + method.getName() + "(" + hashableString + ")");
        };
    }

}
