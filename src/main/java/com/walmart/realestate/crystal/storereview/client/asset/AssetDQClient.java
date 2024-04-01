package com.walmart.realestate.crystal.storereview.client.asset;


import com.walmart.realestate.crystal.storereview.client.asset.model.StoreDataQuality;
import com.walmart.realestate.crystal.storereview.client.asset.model.StoreDataQualityFilter;
import com.walmart.realestate.crystal.storereview.util.ServiceRegistryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class AssetDQClient {

    private static final ParameterizedTypeReference<List<StoreDataQuality>> summaryTypeRef = new ParameterizedTypeReference<List<StoreDataQuality>>() {
    };

    private static final String SUMMARY_URL = "/dataquality/store/summary";

    private final AssetDQProperties assetDQProperties;

    private final RestTemplate restTemplate;

    private final ServiceRegistryUtil serviceRegistryUtil;

    public List<StoreDataQuality> getStoreDataQuality(StoreDataQualityFilter storeDataQualityFilter) {
        return restTemplate.exchange(
                assetDQProperties.getUrl() + SUMMARY_URL,
                HttpMethod.POST,
                new HttpEntity<>(storeDataQualityFilter, getHeaders()),
                summaryTypeRef
        ).getBody();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        serviceRegistryUtil.generateRoutingHeaders(assetDQProperties.getServiceProvider())
                .forEach(headers::set);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

}
