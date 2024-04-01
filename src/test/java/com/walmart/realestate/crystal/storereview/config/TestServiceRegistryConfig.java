package com.walmart.realestate.crystal.storereview.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile("test")
@PropertySource("classpath:signature-test.properties")
public class TestServiceRegistryConfig {
}
