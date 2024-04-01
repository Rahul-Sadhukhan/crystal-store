package com.walmart.realestate.crystal.storereview.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.walmart.realestate.crystal.storereview.repository")
@Configuration
public class SpringDataConfig {
}
