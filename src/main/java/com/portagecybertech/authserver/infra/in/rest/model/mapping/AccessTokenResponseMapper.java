package com.portagecybertech.authserver.infra.in.rest.model.mapping;

import org.springframework.stereotype.Component;

import com.portagecybertech.authserver.infra.in.rest.model.response.AccessTokenResponseDTO;
import lombok.NonNull;

@Component
public class AccessTokenResponseMapper {
    public AccessTokenResponseDTO map(@NonNull final String token) {
        return AccessTokenResponseDTO.builder()
                .token(token).build();
    }
}
