package com.portagecybertech.authserver.core.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.portagecybertech.authserver.core.domain.AccessTokenGenerator;
import com.portagecybertech.authserver.core.domain.model.dto.GenerateAccessTokenCommandDTO;

@ExtendWith(MockitoExtension.class)
class AccessTokenServiceTest {

    @Mock
    private AccessTokenGenerator accessTokenGenerator;

    private AccessTokenService accessTokenService;

    @BeforeEach
    void setUp() {
        accessTokenService = new AccessTokenService(accessTokenGenerator);
    }

    @Test
    void delegatesToGeneratorWithCommandSubject() {
        when(accessTokenGenerator.generateSignedAccessToken("alice")).thenReturn("token-123");
        final var command = GenerateAccessTokenCommandDTO.builder().sub("alice").build();

        final var result = accessTokenService.generateAccessToken(command);

        assertEquals("token-123", result);
        verify(accessTokenGenerator).generateSignedAccessToken("alice");
    }

    @Test
    void propagatesGeneratorExceptionUnchanged() {
        when(accessTokenGenerator.generateSignedAccessToken(any())).thenThrow(new IllegalStateException("boom"));
        final var command = GenerateAccessTokenCommandDTO.builder().sub("alice").build();

        final var exception = assertThrows(IllegalStateException.class,
                () -> accessTokenService.generateAccessToken(command));
        assertEquals("boom", exception.getMessage());
    }
}
