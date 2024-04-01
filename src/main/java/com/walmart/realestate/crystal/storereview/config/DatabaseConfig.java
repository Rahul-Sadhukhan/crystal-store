package com.walmart.realestate.crystal.storereview.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile("!test")
@PropertySource("file:/etc/secrets/crystal-store-review-api-database.properties")
@EntityScan("com.walmart.realestate.crystal.storereview.entity")
public class DatabaseConfig {
}
