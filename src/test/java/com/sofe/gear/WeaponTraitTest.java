package com.sofe.gear;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeaponTraitTest {

    @Test
    void piercingGrowsWithArmorAndStopsAtHalfAgain() {
        assertEquals(10f, WeaponTrait.pierce(10f, 0), 1e-4);
        assertEquals(13f, WeaponTrait.pierce(10f, 10), 1e-4);
        assertEquals(15f, WeaponTrait.pierce(10f, 30), 1e-4);
        assertEquals(10f, WeaponTrait.pierce(10f, -4), 1e-4);
    }

    @Test
    void theBloodPriceNeverKillsTheWielder() {
        assertEquals(1f, WeaponTrait.sacrificeCost(20f), 1e-4);
        assertEquals(0.5f, WeaponTrait.sacrificeCost(1.5f), 1e-4);
        assertEquals(0f, WeaponTrait.sacrificeCost(1f), 1e-4);
    }

    @Test
    void everyTraitHasAKeyForItsTooltip() {
        for (WeaponTrait trait : WeaponTrait.values()) {
            assertEquals("trait.sofe." + trait.name().toLowerCase(java.util.Locale.ROOT), trait.translationKey());
        }
    }
}
