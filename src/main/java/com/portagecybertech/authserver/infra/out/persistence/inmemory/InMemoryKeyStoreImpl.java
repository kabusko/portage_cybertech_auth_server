package com.portagecybertech.authserver.infra.out.persistence.inmemory;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.portagecybertech.authserver.infra.out.persistence.KeyStore;
import lombok.NonNull;

@Repository
public class InMemoryKeyStoreImpl implements KeyStore {
    // Maintains 2 keys only
    private static final int MAX_STORAGE_CAP = 2;
    private final ArrayList<KeyPair> currentKeys = new ArrayList<>();

    @Override
    public synchronized Optional<KeyPair> getCurrentKeyPair() {
        return currentKeys.isEmpty() ?
                Optional.empty() :
                Optional.of(currentKeys.get(currentKeys.size() - 1));
    }

    @Override
    public synchronized Collection<KeyPair> getKeyPairs() {
        return List.copyOf(currentKeys);
    }

    @Override
    public synchronized void storeKeyPair(@NonNull final KeyPair keyPair) {
        if (currentKeys.size() == MAX_STORAGE_CAP) {
            currentKeys.remove(0);
        }

        currentKeys.add(keyPair);
    }
}
