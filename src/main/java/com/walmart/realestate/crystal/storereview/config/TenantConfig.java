package com.walmart.realestate.crystal.storereview.config;

import com.walmart.realestate.crystal.storereview.properties.TenantProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({TenantProperties.class})
public class TenantConfig {
}
