package com.walmart.realestate.crystal.storereview.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile("!test")
@PropertySource("file:/etc/secrets/crystal-store-review-api-cerberus.properties")
public class SecurityConfig {
}
