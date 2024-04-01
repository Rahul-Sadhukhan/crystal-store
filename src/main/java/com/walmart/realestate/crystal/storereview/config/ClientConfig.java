package com.walmart.realestate.crystal.storereview.config;

import lombok.SneakyThrows;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@Configuration
@EnableFeignClients("com.walmart.realestate.crystal.storereview.client")
public class ClientConfig {

    private static final int MAXIMUM_TOTAL_CONNECTION = 50;
    private static final int MAXIMUM_CONNECTION_PER_ROUTE = 30;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.requestFactory(this::getTrustingRequestFactory)
                .build();
    }

    @SneakyThrows
    private HttpComponentsClientHttpRequestFactory getTrustingRequestFactory() {
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial((chain, authType) -> true)
                .build();

        HttpClient client = HttpClients.custom()
                .setMaxConnPerRoute(MAXIMUM_CONNECTION_PER_ROUTE)
                .setMaxConnTotal(MAXIMUM_TOTAL_CONNECTION)
                .setSSLContext(sslContext)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(client);
        return requestFactory;
    }

}
