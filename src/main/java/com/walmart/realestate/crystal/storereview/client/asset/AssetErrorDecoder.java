package com.walmart.realestate.crystal.storereview.client.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.realestate.crystal.storereview.exception.AssetMDMException;
import com.walmart.realestate.crystal.storereview.exception.ErrorResponse;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class AssetErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() <= 499) {
            ErrorResponse body = objectMapper.readValue(response.body().asInputStream(), ErrorResponse.class);
            if ("asset-mdm-exception".equals(body.getError())) return new AssetMDMException(body);
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }

}
