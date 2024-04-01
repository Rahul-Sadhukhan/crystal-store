package com.walmart.realestate.crystal.storereview.exception;

import brave.baggage.BaggageField;
import com.walmart.realestate.crystal.metadata.exception.MetadataItemInvalidException;
import com.walmart.realestate.crystal.metadata.exception.MetadataTypeInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.time.Instant;

@RequiredArgsConstructor
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleEntityNotFoundException(EntityNotFoundException exp, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .exception(EntityNotFoundException.class.getName())
                .error("Not found")
                .message(exp.getLocalizedMessage())
                .path(request.getServletPath())
                .correlationId(BaggageField.getByName("wm_qos.correlation_id").getValue())
                .build();
    }

    @ExceptionHandler(MetadataTypeInvalidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleMetadataTypeInvalidException(MetadataTypeInvalidException exp, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .exception(MetadataTypeInvalidException.class.getName())
                .message(exp.getLocalizedMessage())
                .error("MetadataType does not exist!")
                .path(request.getServletPath())
                .correlationId(BaggageField.getByName("wm_qos.correlation_id").getValue())
                .build();
    }

    @ExceptionHandler(MetadataItemInvalidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleMetadataItemInvalidException(MetadataItemInvalidException exp, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .exception(MetadataItemInvalidException.class.getName())
                .message(exp.getLocalizedMessage())
                .error("MetadataItem does not exist!")
                .path(request.getServletPath())
                .correlationId(BaggageField.getByName("wm_qos.correlation_id").getValue())
                .build();
    }

    @ExceptionHandler(AssetNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleAssetNotFoundException(AssetNotFoundException exp, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .exception(AssetNotFoundException.class.getName())
                .message(exp.getLocalizedMessage())
                .error("asset-not-found")
                .path(request.getServletPath())
                .correlationId(BaggageField.getByName("wm_qos.correlation_id").getValue())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException exp, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .exception(ConstraintViolationException.class.getName())
                .message(exp.getLocalizedMessage())
                .error("constraint-validation-failed")
                .path(request.getServletPath())
                .correlationId(BaggageField.getByName("wm_qos.correlation_id").getValue())
                .build();
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleValidationException(ValidationException exp, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .exception(ValidationException.class.getName())
                .message(exp.getLocalizedMessage())
                .error("validation-failed")
                .path(request.getServletPath())
                .correlationId(BaggageField.getByName("wm_qos.correlation_id").getValue())
                .build();
    }

    @ExceptionHandler(AssetMDMException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleAssetMDMException(AssetMDMException exp, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .exception(AssetMDMException.class.getName())
                .error(exp.getResponse().getError())
                .errors(exp.getResponse().getErrors())
                .message("Error received from Asset Core API")
                .path(request.getServletPath())
                .correlationId(BaggageField.getByName("wm_qos.correlation_id").getValue())
                .build();
    }

    @ExceptionHandler(StoreReviewRetryFailedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleRetryFailedException(StoreReviewRetryFailedException exp, HttpServletRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.NOT_FOUND.value())
                .exception(EntityNotFoundException.class.getName())
                .error("retry-failed")
                .message(exp.getLocalizedMessage())
                .path(request.getServletPath())
                .correlationId(BaggageField.getByName("wm_qos.correlation_id").getValue())
                .build();
    }

}
