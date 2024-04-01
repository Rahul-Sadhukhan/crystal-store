package com.walmart.realestate.crystal.storereview.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final Instant timestamp;

    private final int status;

    private final String error;

    private final String message;

    private final String exception;

    @Singular
    private final List<ErrorInfo> errors;

    private final String path;

    private final String correlationId;

}