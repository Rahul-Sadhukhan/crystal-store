package com.walmart.realestate.crystal.storereview.client.storeinfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.realestate.crystal.storereview.exception.ErrorResponse;
import com.walmart.realestate.crystal.storereview.exception.StoreNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class StoreErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() <= 499 && response.body() != null) {
            ErrorResponse body = objectMapper.readValue(response.body().asInputStream(), ErrorResponse.class);
            if ("store-not-found".equals(body.getError())) return new StoreNotFoundException(body.getMessage());
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }

}
