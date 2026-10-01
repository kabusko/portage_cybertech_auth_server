package com.portagecybertech.authserver.infra.in.rest.model.response;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.portagecybertech.authserver.infra.in.rest.controller.ControllerErrorHandler;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record FieldError(int code, String field, String objectName,
                         String message, String rejectedValue) implements Serializable {

    public static List<FieldError> fromSpringFieldErrors(
            @NotNull final List<org.springframework.validation.FieldError> springFieldErrors) {
        final var errors = new LinkedList<FieldError>();

        for (final org.springframework.validation.FieldError er : springFieldErrors) {
            errors.add(FieldError.builder()
                    .code(HttpStatus.BAD_REQUEST.value())
                    .message(er.isBindingFailure() ?
                            ControllerErrorHandler.BINDING_ERROR :
                            er.getDefaultMessage())
                    .objectName(er.getObjectName())
                    .field(er.getField())
                    .rejectedValue(er.getRejectedValue() != null ? er.getRejectedValue().toString() : null)
                    .build());
        }

        return errors;
    }

}
