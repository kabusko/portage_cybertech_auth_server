package com.portagecybertech.authserver.utils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public final class KeyPairTestData {

    private KeyPairTestData() {
        throw new IllegalStateException("Utility Class");
    }

    public static KeyPair generateRSAKeyPair() {
        try {
            final var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to generate RSA key pair for tests", e);
        }
    }
}
