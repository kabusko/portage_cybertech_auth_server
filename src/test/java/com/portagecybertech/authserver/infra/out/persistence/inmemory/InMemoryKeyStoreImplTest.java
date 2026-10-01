package com.portagecybertech.authserver.infra.out.persistence.inmemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.portagecybertech.authserver.utils.KeyPairTestData;

class InMemoryKeyStoreImplTest {

    private final InMemoryKeyStoreImpl keyStore = new InMemoryKeyStoreImpl();

    @Test
    void thirdKeyEvictsTheOldestKeepingCapAtTwo() {
        final var kp1 = KeyPairTestData.generateRSAKeyPair();
        final var kp2 = KeyPairTestData.generateRSAKeyPair();
        final var kp3 = KeyPairTestData.generateRSAKeyPair();

        keyStore.storeKeyPair(kp1);
        keyStore.storeKeyPair(kp2);
        keyStore.storeKeyPair(kp3);

        final var keyPairs = keyStore.getKeyPairs();
        assertEquals(2, keyPairs.size());
        assertTrue(keyPairs.contains(kp2));
        assertTrue(keyPairs.contains(kp3));
        assertFalse(keyPairs.contains(kp1));
        assertEquals(kp3, keyStore.getCurrentKeyPair().orElseThrow());
    }

    @Test
    void getKeyPairsResultIsADefensiveCopy() {
        keyStore.storeKeyPair(KeyPairTestData.generateRSAKeyPair());

        final var snapshot = keyStore.getKeyPairs();
        keyStore.storeKeyPair(KeyPairTestData.generateRSAKeyPair());

        assertEquals(1, snapshot.size());
    }
}
