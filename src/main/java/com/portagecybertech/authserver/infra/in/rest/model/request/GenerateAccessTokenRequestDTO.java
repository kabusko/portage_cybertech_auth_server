package com.portagecybertech.authserver.infra.in.rest.model.request;

import com.portagecybertech.authserver.core.domain.model.dto.GenerateAccessTokenCommandDTO;
import jakarta.validation.constraints.NotNull;

public record GenerateAccessTokenRequestDTO(@NotNull String sub) {
    public GenerateAccessTokenCommandDTO map() {
        return GenerateAccessTokenCommandDTO.builder()
                .sub(sub).build();
    }
}
