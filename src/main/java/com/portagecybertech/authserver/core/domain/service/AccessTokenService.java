package com.portagecybertech.authserver.core.domain.service;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.portagecybertech.authserver.core.domain.AccessTokenGenerator;
import com.portagecybertech.authserver.core.domain.model.dto.GenerateAccessTokenCommandDTO;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final AccessTokenGenerator accessTokenGenerator;

    /**
     * Generate a new JWT signed access token.
     *
     * Note: To simulate token signing keys rotation, this method uses a new
     * signing key every three calls, leveraging a simple counter.
     *
     * @param command
     *         the request to generate a new access token for a subject
     * @return the generated new signed valid access token
     */
    public String generateAccessToken(final @NonNull GenerateAccessTokenCommandDTO command) {
        return accessTokenGenerator.generateSignedAccessToken(command.sub());
    }
}
