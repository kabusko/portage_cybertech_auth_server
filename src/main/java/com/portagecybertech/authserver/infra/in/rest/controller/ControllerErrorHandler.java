package com.portagecybertech.authserver.infra.in.rest.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.portagecybertech.authserver.infra.in.rest.model.response.ErrorResponse;
import com.portagecybertech.authserver.infra.in.rest.model.response.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class ControllerErrorHandler {

    protected static final String INVALID_REQUEST_ERROR_MSG = "Invalid request. Please ensure your request complies with contract requirements and try again.";
    protected static final String ERROR_MSG = "Something seriously went wrong. Use errorId to investigate.";
    public static final String BINDING_ERROR = "Binding Error";

    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handle(final MethodArgumentNotValidException ex, final HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        final var errorResponse = ErrorResponse.builder()
                .code(BAD_REQUEST.value())
                .message(BAD_REQUEST.getReasonPhrase())
                .errors(FieldError.fromSpringFieldErrors(ex.getFieldErrors()))
                .build();
        log.warn(errorResponse.toString(), ex);
        return errorResponse;
    }

    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorResponse handleHttpMessageNotReadableException(final HttpMessageNotReadableException ex,
            final HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        final var errorResponse = ErrorResponse.builder()
                .code(BAD_REQUEST.value())
                .message(INVALID_REQUEST_ERROR_MSG)
                .build();
        log.warn("Invalid request, errorId: {}", errorResponse.errorId(), ex);
        return errorResponse;
    }

    @ResponseBody
    @ResponseStatus(UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ErrorResponse handle(final HttpMediaTypeNotSupportedException ex, final HttpServletResponse response,
            final HttpServletRequest request) {
        log.warn("Unsupported media type: {}", ex.getMessage(), ex);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return ErrorResponse.builder()
                .code(UNSUPPORTED_MEDIA_TYPE.value())
                .message(UNSUPPORTED_MEDIA_TYPE.getReasonPhrase())
                .errors(List.of(FieldError.builder()
                        .code(UNSUPPORTED_MEDIA_TYPE.value())
                        .field(HttpHeaders.CONTENT_TYPE)
                        .objectName("headers")
                        .message(UNSUPPORTED_MEDIA_TYPE.getReasonPhrase())
                        .rejectedValue(request.getHeader(HttpHeaders.CONTENT_TYPE))
                        .build()))
                .build();
    }

    @ResponseBody
    @ResponseStatus(METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponse handle(final HttpRequestMethodNotSupportedException ex, final HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        final var errorResponse = ErrorResponse.builder()
                .code(METHOD_NOT_ALLOWED.value())
                .message(METHOD_NOT_ALLOWED.getReasonPhrase())
                .build();
        log.warn(errorResponse.toString(), ex);
        return errorResponse;
    }

    @ResponseBody
    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ErrorResponse handle(final NoResourceFoundException ex, final HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        final var errorResponse = ErrorResponse.builder()
                .code(NOT_FOUND.value())
                .message(NOT_FOUND.getReasonPhrase())
                .build();
        log.warn(errorResponse.toString(), ex);
        return errorResponse;
    }

    /**
     * Catch all handler
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handle(final Exception ex, final HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        final var errorResponse = ErrorResponse.builder()
                .code(INTERNAL_SERVER_ERROR.value())
                .message(ERROR_MSG)
                .build();
        log.error("Catch all REST error handler called: {}", errorResponse, ex);
        return errorResponse;
    }
}
