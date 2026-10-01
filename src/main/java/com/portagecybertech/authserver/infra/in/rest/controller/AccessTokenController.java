package com.portagecybertech.authserver.infra.in.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portagecybertech.authserver.core.domain.service.AccessTokenService;
import com.portagecybertech.authserver.infra.in.rest.model.mapping.AccessTokenResponseMapper;
import com.portagecybertech.authserver.infra.in.rest.model.request.GenerateAccessTokenRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AccessTokenController {

    private final AccessTokenService accessTokenService;
    private final AccessTokenResponseMapper accessTokenResponseMapper;

    @PostMapping("/tokens")
    public ResponseEntity<Object> generateAccessToken(
            @Valid @RequestBody final GenerateAccessTokenRequestDTO request) {
        log.info("Request to generate new access token for {}",
                request.sub());
        final var token = accessTokenService.generateAccessToken(
                request.map());
        final var accessToken = accessTokenResponseMapper.map(token);
        return ResponseEntity.ok(accessToken);
    }
}
