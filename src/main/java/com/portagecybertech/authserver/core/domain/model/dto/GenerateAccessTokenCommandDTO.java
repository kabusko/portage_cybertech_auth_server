package com.portagecybertech.authserver.core.domain.model.dto;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record GenerateAccessTokenCommandDTO(@NonNull String sub) {

}
