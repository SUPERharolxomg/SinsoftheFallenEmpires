package com.sofe.world.region;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegionMapTest {
    private final RegionMap map = RegionMap.defaultLayout();

    @Test
    void defaultLayoutHasNoOverlaps() {
        assertTrue(RegionMap.problems(map.bounds()).isEmpty());
    }

    @Test
    void fixedLocationsFromTheWorldDocument() {
        assertEquals(Region.SULTHARI, map.regionAt(0, 0));            // Sulthari city
        assertEquals(Region.NORDRATH, map.regionAt(0, -4500));        // Burning Citadel
        assertEquals(Region.PARSIVAN, map.regionAt(4200, -1000));     // Enchanted Gardens
        assertEquals(Region.KHEMET, map.regionAt(3500, 4200));        // Stagnant Marsh
        assertEquals(Region.AUREUM, map.regionAt(-4000, 0));          // Golden Vaults
        assertEquals(Region.OCEAN, map.regionAt(5900, -5900));
    }

    @Test
    void sharedBordersBelongToExactlyOneRegion() {
        // x = 1500 is the Sulthari / Parsivan border: the east side belongs to Parsivan
        assertEquals(Region.SULTHARI, map.regionAt(1499, 0));
        assertEquals(Region.PARSIVAN, map.regionAt(1500, 0));
        // z = -1500 is the Sulthari / Nordrath border: north (negative z) is Nordrath
        assertEquals(Region.NORDRATH, map.regionAt(0, -1501));
        assertEquals(Region.SULTHARI, map.regionAt(0, -1500));
    }

    @Test
    void everyEmpireIsReachable() {
        for (Region r : Region.values()) {
            if (r == Region.OCEAN) continue;
            assertTrue(map.bounds().stream().anyMatch(b -> b.region() == r), r + " has no bounds");
        }
    }

    @Test
    void rejectsOverlappingLayouts() {
        List<RegionBounds> bad = List.of(
                new RegionBounds(Region.SULTHARI, -100, 100, -100, 100),
                new RegionBounds(Region.NORDRATH, 50, 200, 50, 200));
        assertThrows(IllegalArgumentException.class, () -> new RegionMap(bad));
    }

    @Test
    void rejectsEmptyBounds() {
        assertThrows(IllegalArgumentException.class, () -> new RegionBounds(Region.AUREUM, 10, 10, 0, 5));
    }
}
