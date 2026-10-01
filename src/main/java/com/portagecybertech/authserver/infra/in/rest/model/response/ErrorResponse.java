package com.portagecybertech.authserver.infra.in.rest.model.response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Builder;

/**
 * HTTP response containing JSON error details.
 */
public record ErrorResponse(int code, String message, String errorId,
                            List<FieldError> errors) {
    @Builder
    public ErrorResponse(final int code, final String message,
            final String errorId, final List<FieldError> errors) {
        this.code = code;
        this.message = message;
        this.errorId = errorId != null ?
                errorId : String.format("%s%s", Instant.now(), UUID.randomUUID());
        this.errors = errors == null ? new ArrayList<>() : new ArrayList<>(errors);
    }

}
