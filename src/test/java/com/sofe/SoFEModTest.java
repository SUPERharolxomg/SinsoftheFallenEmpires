package com.sofe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SoFEModTest {

    @Test
    void modIdFollowsForgeRules() {
        // Forge requires [a-z][a-z0-9_]{1,63}; the ID is also the namespace of every registry key
        assertTrue(SoFEMod.MOD_ID.matches("[a-z][a-z0-9_]{1,63}"));
    }
}
