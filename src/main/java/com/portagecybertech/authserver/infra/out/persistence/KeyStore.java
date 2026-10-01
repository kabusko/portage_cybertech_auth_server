package com.portagecybertech.authserver.infra.out.persistence;

import java.security.KeyPair;
import java.util.Collection;
import java.util.Optional;

public interface KeyStore {

    /**
     * Get the most current active signing key pair.
     * Implementation must ensure that the newest signing is returned by this
     * in case of key rotations.
     *
     * @return the newest signing key pair or empty if none exists
     */
    Optional<KeyPair> getCurrentKeyPair();

    /**
     * Get all current active key pairs
     *
     * @return Collection of current keys, maybe empty.
     */
    Collection<KeyPair> getKeyPairs();

    /**
     * Store the provided key pair.
     * Implementer is free about how many of the key pairs are stored.
     *
     * @param keyPair
     *         the key pair to store
     */
    void storeKeyPair(KeyPair keyPair);
}
