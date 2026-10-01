package com.portagecybertech.authserver.infra.in.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portagecybertech.authserver.core.domain.AccessTokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/portage_auth_server/.well-known")
@RequiredArgsConstructor
@Slf4j
public class JwksController {

    private final AccessTokenGenerator accessTokenGenerator;

    @GetMapping("/jwks.json")
    public ResponseEntity<Object> getJwks() {
        log.info("Request to get current public signing keys");
        final var keys = accessTokenGenerator.getJwks();
        return ResponseEntity.ok(keys);
    }
}
