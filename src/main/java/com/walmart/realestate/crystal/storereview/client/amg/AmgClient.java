package com.walmart.realestate.crystal.storereview.client.amg;

import com.netflix.graphql.dgs.client.DefaultGraphQLClient;
import com.netflix.graphql.dgs.client.GraphQLClient;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.HttpResponse;
import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.amg.model.AmgData;
import com.walmart.realestate.crystal.storereview.client.amg.model.AmgLocation;
import com.walmart.realestate.crystal.storereview.client.amg.model.AmgNote;
import com.walmart.realestate.crystal.storereview.util.ServiceRegistryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AmgClient {

    private static final String LOCATION_BY_STORE_NUMBER = "GET_LOCATION_BY_STORE_ID";
    private static final String DATASOURCE = "SERVICECHANNEL";
    private static final String DIVISION = "WM";
    private static final String NOTES_FOR_LOCATION_QUERY = "query($storeNumber: String!){\n" +
            "  locationByStoreId(storeId:$storeNumber){\n" +
            "    notes\n" +
            "  }\n" +
            "}";

    private final ServiceRegistryUtil serviceRegistryUtil;

    private final AmgProperties amgProperties;

    private final RestTemplate restTemplate;

    @Logger
    public Optional<AmgNote> getNote(Long storeNumber, String noteHeader) {

        AmgData data;

        try {
            data = getData(storeNumber);
        } catch (HttpClientErrorException e) {
            data = AmgData.builder()
                    .location(AmgLocation.builder()
                            .build())
                    .build();
        }

        return Optional.ofNullable(data)
                .map(AmgData::getLocation)
                .map(AmgLocation::getNotes)
                .orElseGet(Collections::emptyList).stream()
                .filter(note -> noteHeader.equalsIgnoreCase(note.getHeader()))
                .findAny();
    }

    private AmgData getData(Long storeNumber) {
        Map<String, ?> variables = Collections.singletonMap("storeNumber", storeNumber);

        GraphQLClient graphQLClient = new DefaultGraphQLClient(amgProperties.getUrl());

        GraphQLResponse response = graphQLClient.executeQuery(NOTES_FOR_LOCATION_QUERY, variables, (url, headers, body) -> {
            HttpHeaders requestHeaders = getHeaders();
            requestHeaders.putAll(headers);
            HttpEntity<String> requestEntity = new HttpEntity<>(body, requestHeaders);

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            return new HttpResponse(responseEntity.getStatusCodeValue(), responseEntity.getBody());
        });

        return response.dataAsObject(AmgData.class);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        serviceRegistryUtil.generateRoutingHeaders(amgProperties.getServiceProvider())
                .forEach(headers::set);
        headers.set("gql-datasource", DATASOURCE);
        headers.set("gql-division", DIVISION);
        headers.set("gql-request-type", LOCATION_BY_STORE_NUMBER);
        headers.set(HttpHeaders.CONTENT_TYPE, "application/graphql");
        return headers;
    }

}
