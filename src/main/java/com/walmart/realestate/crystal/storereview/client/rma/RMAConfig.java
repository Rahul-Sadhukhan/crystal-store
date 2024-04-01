package com.walmart.realestate.crystal.storereview.client.rma;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "file:/etc/secrets/crystal-rma-application.properties", ignoreResourceNotFound = true)
public class RMAConfig {
}
