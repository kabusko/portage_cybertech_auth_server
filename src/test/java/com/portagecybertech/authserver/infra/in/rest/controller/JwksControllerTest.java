package com.portagecybertech.authserver.infra.in.rest.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.portagecybertech.authserver.core.domain.AccessTokenGenerator;

@WebMvcTest(JwksController.class)
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class JwksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccessTokenGenerator accessTokenGenerator;

    @Test
    void returnsJwksBodyFromGenerator() throws Exception {
        when(accessTokenGenerator.getJwks())
                .thenReturn(Map.of("keys", List.of(Map.of("kid", "abc", "kty", "RSA"))));

        mockMvc.perform(get("/portage_auth_server/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].kid").value("abc"));
    }

    @Test
    void noKeysYetReturnsEmptyKeysArray() throws Exception {
        when(accessTokenGenerator.getJwks()).thenReturn(Map.of("keys", List.of()));

        mockMvc.perform(get("/portage_auth_server/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").isEmpty());
    }

    @Test
    void generatorFailurePropagatesAsGeneric500() throws Exception {
        when(accessTokenGenerator.getJwks()).thenThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/portage_auth_server/.well-known/jwks.json"))
                .andExpect(status().isInternalServerError());
    }
}
