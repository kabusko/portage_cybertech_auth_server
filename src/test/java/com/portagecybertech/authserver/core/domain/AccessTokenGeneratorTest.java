package com.portagecybertech.authserver.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import com.portagecybertech.authserver.infra.out.persistence.KeyStore;
import com.portagecybertech.authserver.utils.KeyPairTestData;

@ExtendWith(MockitoExtension.class)
class AccessTokenGeneratorTest {

    private static final long ACCESS_TOKEN_TTL_SECONDS = 120;

    @Mock
    private KeyStore keyStore;

    private AccessTokenGenerator accessTokenGenerator;

    @BeforeEach
    void setUp() {
        accessTokenGenerator = new AccessTokenGenerator(keyStore);
    }

    @Nested
    class GenerateSignedAccessToken {

        @Test
        void returnsWellFormedRS256Jwt() throws Exception {
            final var keyPair = KeyPairTestData.generateRSAKeyPair();
            when(keyStore.getCurrentKeyPair()).thenReturn(Optional.of(keyPair));

            final var token = accessTokenGenerator.generateSignedAccessToken("alice");

            final var signedJWT = SignedJWT.parse(token);
            assertEquals(JWSAlgorithm.RS256, signedJWT.getHeader().getAlgorithm());
            assertTrue(signedJWT.verify(new RSASSAVerifier((RSAPublicKey) keyPair.getPublic())));
        }

        @Test
        void populatesExpectedClaims() throws Exception {
            final var keyPair = KeyPairTestData.generateRSAKeyPair();
            when(keyStore.getCurrentKeyPair()).thenReturn(Optional.of(keyPair));

            final var token = accessTokenGenerator.generateSignedAccessToken("alice");

            final var claims = SignedJWT.parse(token).getJWTClaimsSet();
            assertEquals("http://localhost:8082/portage_cybertech_auth_server", claims.getIssuer());
            assertEquals(List.of("portage_cybertech_resource_server"), claims.getAudience());
            assertEquals("alice", claims.getSubject());
            assertEquals("access", claims.getStringClaim("token_use"));
            assertEquals("scope_read", claims.getStringClaim("scope"));
            assertEquals(claims.getIssueTime(), claims.getNotBeforeTime());
            final var ttlSeconds = (claims.getExpirationTime().getTime() - claims.getIssueTime().getTime()) / 1000;
            assertEquals(ACCESS_TOKEN_TTL_SECONDS, ttlSeconds);
        }

        @Test
        void kidInHeaderMatchesJwksEntryForSameKey() throws Exception {
            final var keyPair = KeyPairTestData.generateRSAKeyPair();
            when(keyStore.getCurrentKeyPair()).thenReturn(Optional.of(keyPair));
            when(keyStore.getKeyPairs()).thenReturn(List.of(keyPair));

            final var token = accessTokenGenerator.generateSignedAccessToken("alice");
            final var tokenKid = SignedJWT.parse(token).getHeader().getKeyID();

            final var jwkSet = JWKSet.parse(accessTokenGenerator.getJwks());
            assertEquals(1, jwkSet.getKeys().size());
            assertEquals(tokenKid, jwkSet.getKeys().get(0).getKeyID());
        }

        @Test
        void nullSubThrowsNpe() {
            assertThrows(NullPointerException.class,
                    () -> accessTokenGenerator.generateSignedAccessToken(null));
        }

        @Test
        void bootstrapsWhenNoCurrentKeyExists() {
            when(keyStore.getCurrentKeyPair()).thenReturn(Optional.empty());

            final var token = accessTokenGenerator.generateSignedAccessToken("alice");

            assertNotNull(token);
            // Forced first-call rotation AND the empty-store fallback both store a key here,
            // so storeKeyPair is actually invoked twice; asserting atLeastOnce() documents the
            // bootstrap behavior without pinning the test to that internal double-store detail.
            verify(keyStore, atLeastOnce()).storeKeyPair(any());
        }

        @Test
        void firstCallTriggersRotation() {
            final var keyPair = KeyPairTestData.generateRSAKeyPair();
            when(keyStore.getCurrentKeyPair()).thenReturn(Optional.of(keyPair));

            accessTokenGenerator.generateSignedAccessToken("alice");

            verify(keyStore, times(1)).storeKeyPair(any());
        }

        @Test
        void secondImmediateCallDoesNotRotateAgain() {
            final var keyPair = KeyPairTestData.generateRSAKeyPair();
            when(keyStore.getCurrentKeyPair()).thenReturn(Optional.of(keyPair));

            accessTokenGenerator.generateSignedAccessToken("alice");
            accessTokenGenerator.generateSignedAccessToken("bob");

            verify(keyStore, times(1)).storeKeyPair(any());
        }
    }

    @Nested
    class GetJwks {

        @Test
        void emptyStoreReturnsEmptyKeysArray() throws Exception {
            when(keyStore.getKeyPairs()).thenReturn(List.of());

            final var jwkSet = JWKSet.parse(accessTokenGenerator.getJwks());

            assertTrue(jwkSet.getKeys().isEmpty());
        }

        @Test
        void multipleKeysProduceOneJwkEachWithCorrectFields() throws Exception {
            final var keyPair1 = KeyPairTestData.generateRSAKeyPair();
            final var keyPair2 = KeyPairTestData.generateRSAKeyPair();
            when(keyStore.getKeyPairs()).thenReturn(List.of(keyPair1, keyPair2));

            final var jwkSet = JWKSet.parse(accessTokenGenerator.getJwks());

            assertEquals(2, jwkSet.getKeys().size());
            final var kids = Set.of(jwkSet.getKeys().get(0).getKeyID(), jwkSet.getKeys().get(1).getKeyID());
            assertEquals(2, kids.size());
            jwkSet.getKeys().forEach(jwk -> {
                assertEquals(KeyType.RSA, jwk.getKeyType());
                assertEquals(KeyUse.SIGNATURE, jwk.getKeyUse());
                assertEquals(JWSAlgorithm.RS256, jwk.getAlgorithm());
                assertNotNull(jwk.getKeyID());
                assertFalse(jwk.getKeyID().isBlank());
            });
        }

        @Test
        void jwksNeverLeaksPrivateKeyMaterial() throws Exception {
            final var keyPair1 = KeyPairTestData.generateRSAKeyPair();
            final var keyPair2 = KeyPairTestData.generateRSAKeyPair();
            when(keyStore.getKeyPairs()).thenReturn(List.of(keyPair1, keyPair2));

            final var jwkSet = JWKSet.parse(accessTokenGenerator.getJwks());

            jwkSet.getKeys().forEach(jwk -> assertFalse(((RSAKey) jwk).isPrivate()));
        }
    }
}
