package com.walmart.realestate.crystal.storereview.config;

import com.walmart.realestate.crystal.storereview.properties.WebProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final WebProperties webProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration registration = registry.addMapping("/**");

        if (!CollectionUtils.isEmpty(webProperties.getOrigins())) {
            registration.allowedOrigins(webProperties.getOrigins().toArray(new String[]{}));
        }

        if (!CollectionUtils.isEmpty(webProperties.getOriginPatterns())) {
            registration.allowedOriginPatterns(webProperties.getOriginPatterns().toArray(new String[]{}));
        }

        if (!CollectionUtils.isEmpty(webProperties.getAllowedMethods())) {
            registration.allowedMethods(webProperties.getAllowedMethods().toArray(new String[]{}));
        }
    }

}
