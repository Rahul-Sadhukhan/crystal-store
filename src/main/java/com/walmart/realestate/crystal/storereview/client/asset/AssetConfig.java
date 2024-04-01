package com.walmart.realestate.crystal.storereview.client.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.realestate.crystal.storereview.util.ServiceRegistryUtil;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(AssetProperties.class)
public class AssetConfig {

    private final ServiceRegistryUtil serviceRegistryUtil;

    private final AssetProperties assetProperties;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> getHeaders().forEach(template::header);
    }

    @Bean
    public AssetErrorDecoder assetErrorDecoder(ObjectMapper objectMapper) {
        return new AssetErrorDecoder(objectMapper);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>(serviceRegistryUtil.generateRoutingHeaders(assetProperties.getServiceProvider()));
        headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

}
