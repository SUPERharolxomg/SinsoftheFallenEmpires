package com.sofe.world.lock;

import com.sofe.world.region.Region;
import com.sofe.world.region.RegionMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegionLocksTest {
    private final RegionMap map = RegionMap.defaultLayout();

    @Test
    void actOneOnlyOpensSulthari() {
        assertTrue(RegionLocks.canBeAt(map, 0, 0, 1));
        assertFalse(RegionLocks.canBeAt(map, 0, -4500, 1), "Nordrath is sealed in Act I");
        assertTrue(RegionLocks.canBeAt(map, 0, -4500, 2));
        assertFalse(RegionLocks.canBeAt(map, -4000, 0, 3), "Aureum opens in Act IV");
        assertEquals(Region.NORDRATH, RegionLocks.blockingRegion(map, 0, -4500, 1).orElseThrow());
    }

    @Test
    void theOceanIsOnlyAllowedNearAnOpenCoast() {
        // Sulthari's south coast is z = 1500 between x = -1500 and 800 (Khemet starts at x = 800)
        assertTrue(RegionLocks.canBeAt(map, 0, 1500 + 299, 1));
        assertFalse(RegionLocks.canBeAt(map, 0, 1500 + 301, 1));
        // near sealed Aureum's coast only: not allowed in Act I
        assertFalse(RegionLocks.canBeAt(map, -5000, 3600, 1));
        assertTrue(RegionLocks.canBeAt(map, -5000, 3600, 4));
    }

    @Test
    void theVeilStandsOnSharedBordersOnly() {
        assertTrue(RegionLocks.isVeilColumn(map, 1500, 0), "Sulthari / Parsivan border");
        assertFalse(RegionLocks.isVeilColumn(map, 1499, 0));
        assertTrue(RegionLocks.isVeilColumn(map, 0, -1500), "Nordrath / Sulthari border");
        assertFalse(RegionLocks.isVeilColumn(map, 0, 0));
        assertFalse(RegionLocks.isVeilColumn(map, 0, 1500), "the coast has no Veil");
    }

    @Test
    void theVeilOpensWhenBothSidesAreOpen() {
        assertFalse(RegionLocks.veilPassable(map, 0, -1500, 1));
        assertEquals(Region.NORDRATH, RegionLocks.sealedBehind(map, 0, -1500, 1).orElseThrow());
        assertTrue(RegionLocks.veilPassable(map, 0, -1500, 2));
        assertTrue(RegionLocks.sealedBehind(map, 0, -1500, 2).isEmpty());
        assertTrue(RegionLocks.veilPassable(map, 0, 0, 1), "a column without Veil never blocks");
    }
}
