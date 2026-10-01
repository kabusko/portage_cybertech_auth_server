package com.portagecybertech.authserver.infra.in.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portagecybertech.authserver.core.domain.service.AccessTokenService;
import com.portagecybertech.authserver.infra.in.rest.model.mapping.AccessTokenResponseMapper;
import com.portagecybertech.authserver.infra.in.rest.model.request.GenerateAccessTokenRequestDTO;
import com.portagecybertech.authserver.infra.in.rest.model.response.AccessTokenResponseDTO;

@WebMvcTest(AccessTokenController.class)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class AccessTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean
    private AccessTokenResponseMapper accessTokenResponseMapper;

    @Test
    void validRequestReturnsTokenInResponseBody() throws Exception {
        when(accessTokenService.generateAccessToken(any())).thenReturn("token-123");
        when(accessTokenResponseMapper.map("token-123"))
                .thenReturn(AccessTokenResponseDTO.builder().token("token-123").build());

        mockMvc.perform(post("/api/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GenerateAccessTokenRequestDTO("alice"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"));

        verify(accessTokenResponseMapper).map("token-123");
    }

    @Test
    void missingSubReturns400() throws Exception {
        mockMvc.perform(post("/api/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("sub"))
                .andExpect(jsonPath("$.errors[0].code").value(400))
                .andExpect(jsonPath("$.errors[0].rejectedValue").doesNotExist());

        verifyNoInteractions(accessTokenService, accessTokenResponseMapper);
    }

    @Test
    void serviceFailurePropagatesAsGeneric500() throws Exception {
        when(accessTokenService.generateAccessToken(any())).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(post("/api/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GenerateAccessTokenRequestDTO("alice"))))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(accessTokenResponseMapper);
    }
}
