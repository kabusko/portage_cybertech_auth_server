package com.portagecybertech.authserver.core.domain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.portagecybertech.authserver.infra.out.persistence.KeyStore;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccessTokenGenerator {
    private final KeyStore keyStore;

    private final Object rotationLock = new Object();
    private Instant lastRotationAt = Instant.EPOCH; // Rotates on first call
    private static final Duration KEY_ROTATION_INTERVAL = Duration.ofMinutes(5);
    private static final long ACCESS_TOKEN_TTL_SECONDS = 120;

    public Map<String, Object> getJwks() {
        return new JWKSet(keyStore.getKeyPairs().stream()
                    .map(kp -> toJwk((RSAPublicKey) kp.getPublic()))
                    .collect(Collectors.toList()))
                .toJSONObject();
    }

    private RSAKey toJwk(final RSAPublicKey publicKey) {
        return new RSAKey.Builder(publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID(computeKid(publicKey)).build();
    }

    public String generateSignedAccessToken(@NonNull final String sub) {
        final KeyPair keyPair;
        synchronized (rotationLock) {
            rotateKeyIfNecessary();
            keyPair = keyStore.getCurrentKeyPair().orElseGet(() -> {
                final var kp = generateRSAKeyPair();
                keyStore.storeKeyPair(kp);
                return kp;
            });
        }
        final var kid = computeKid((RSAPublicKey) keyPair.getPublic());
        final var claims = getExpectedClaims(sub);
        final var signingKey = (RSAPrivateKey) keyPair.getPrivate();
        return signToken(kid, claims, signingKey);
    }

    private void rotateKeyIfNecessary() {
        final var now = Instant.now();

        if (Duration.between(lastRotationAt, now).compareTo(KEY_ROTATION_INTERVAL) >= 0) {
            keyStore.storeKeyPair(generateRSAKeyPair());
            lastRotationAt = now;
        }
    }

    private KeyPair generateRSAKeyPair() {
        try {
            final var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to generate RSA key pair");
        }
    }

    private Map<String, Object> getExpectedClaims(@NonNull final String sub) {
        final var now = Instant.now();
        final var claims = new HashMap<String, Object>();
        claims.put("iss", "http://localhost:8082/portage_cybertech_auth_server");
        claims.put("token_use", "access");
        claims.put("aud", "portage_cybertech_resource_server");
        claims.put("sub", sub);
        claims.put("scope", "scope_read");
        claims.put("iat", now.getEpochSecond());
        claims.put("nbf", now.getEpochSecond());
        claims.put("exp", now.plusSeconds(ACCESS_TOKEN_TTL_SECONDS).getEpochSecond());
        return claims;
    }

    private String signToken(final String kid, final Map<String, Object> claims,
            final RSAPrivateKey signingKey) {
        try {
            final var header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(kid).build();
            final var signedJWT = new SignedJWT(header, toClaimsSet(claims));
            signedJWT.sign(new RSASSASigner(signingKey));
            return signedJWT.serialize();
        } catch (final JOSEException e) {
            throw new IllegalStateException("Unable to sign the token");
        }
    }

    private String computeKid(final RSAPublicKey publicKey) {
        try {
            return new RSAKey.Builder(publicKey).build().computeThumbprint().toString();
        } catch (final JOSEException e) {
            throw new IllegalStateException("Unable to compute key id");
        }
    }

    private JWTClaimsSet toClaimsSet(final Map<String, Object> claims) {
        final var builder = new JWTClaimsSet.Builder();
        claims.forEach(builder::claim);
        return builder.build();
    }
}
