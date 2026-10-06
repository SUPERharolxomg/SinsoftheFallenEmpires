package com.sofe.pact;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Pact's numbers (docs/Anexos.md, A5): alone nothing changes; together a little more for everyone. */
class PactRulesTest {
    @Test
    void aloneTheXpIsWhole() {
        assertEquals(100, PactRules.xpShare(100, 1, 0.10));
    }

    @Test
    void togetherTheXpIsSharedWithABonus() {
        assertEquals(55, PactRules.xpShare(100, 2, 0.10)); // 110 split in two
        assertEquals(28, PactRules.xpShare(100, 5, 0.10)); // 140 split in five
    }

    @Test
    void theLootBonusGrowsPerAllyUpToItsCap() {
        assertEquals(0, PactRules.findBonus(0, 0.05, 0.20), 1e-9);
        assertEquals(0.10, PactRules.findBonus(2, 0.05, 0.20), 1e-9);
        assertEquals(0.20, PactRules.findBonus(4, 0.05, 0.20), 1e-9);
        assertEquals(0.20, PactRules.findBonus(9, 0.05, 0.20), 1e-9);
    }

    @Test
    void aBossGains60PercentHealthPerExtraPlayer() {
        assertEquals(1.0, PactRules.bossHealthMultiplier(1, 0.6), 1e-9);
        assertEquals(1.6, PactRules.bossHealthMultiplier(2, 0.6), 1e-9);
        assertEquals(2.8, PactRules.bossHealthMultiplier(4, 0.6), 1e-9);
    }

    @Test
    void aFullPactOrAPlayerInAnotherCannotJoin() {
        assertTrue(PactRules.canJoin(4, false, 5));
        assertFalse(PactRules.canJoin(5, false, 5));
        assertFalse(PactRules.canJoin(2, true, 5));
    }
}
