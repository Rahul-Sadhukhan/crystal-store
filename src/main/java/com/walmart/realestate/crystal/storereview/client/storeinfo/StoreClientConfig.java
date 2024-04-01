package com.walmart.realestate.crystal.storereview.client.storeinfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.realestate.crystal.storereview.util.ServiceRegistryUtil;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

@RequiredArgsConstructor
public class StoreClientConfig {

    private final ServiceRegistryUtil serviceRegistryUtil;

    private final StoreProperties storeProperties;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> getHeaders().forEach(template::header);
    }

    @Bean
    public StoreErrorDecoder storeErrorDecoder(ObjectMapper objectMapper) {
        return new StoreErrorDecoder(objectMapper);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = serviceRegistryUtil.generateRoutingHeaders(storeProperties.getServiceProvider());
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

}
