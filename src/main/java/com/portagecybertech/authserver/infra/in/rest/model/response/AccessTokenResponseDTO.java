package com.portagecybertech.authserver.infra.in.rest.model.response;

import lombok.Builder;

@Builder
public record AccessTokenResponseDTO(String token) {
}
