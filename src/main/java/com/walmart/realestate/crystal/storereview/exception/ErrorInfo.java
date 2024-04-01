package com.walmart.realestate.crystal.storereview.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

@Builder
@Getter
public class ErrorInfo {

    private final String error;

    private final String message;

    private final List<String> codes;

    @Singular
    private final List<ErrorInfo> arguments;

    private final String defaultMessage;

    private final String objectName;

    private final String field;

    private final Object rejectedValue;

    private final Boolean bindingFailure;

    private final String code;

}

