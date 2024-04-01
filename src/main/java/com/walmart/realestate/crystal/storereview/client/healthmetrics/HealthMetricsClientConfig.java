package com.walmart.realestate.crystal.storereview.client.healthmetrics;

import com.walmart.realestate.crystal.storereview.util.ServiceRegistryUtil;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

@RequiredArgsConstructor
public class HealthMetricsClientConfig {

    private final ServiceRegistryUtil serviceRegistryUtil;

    private final HealthMetricsProperties healthMetricsProperties;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> getHeaders().forEach(template::header);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = serviceRegistryUtil.generateRoutingHeaders(healthMetricsProperties.getServiceProvider());
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

}
