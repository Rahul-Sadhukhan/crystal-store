package com.walmart.realestate.crystal.settingchangelog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile("test")
@PropertySource("classpath:cerberus-test.properties")
public class TestSecurityConfigSCL {
}
