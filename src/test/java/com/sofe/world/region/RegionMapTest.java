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
        assertEquals(Region.PARSIVAN, map.regionAt(9000, -5000));     // Enchanted Gardens, in the north-east
        assertEquals(Region.PARSIVAN, map.regionAt(3600, -500));      // Parsivan Baths
        assertEquals(Region.KHEMET, map.regionAt(8000, 8500));        // Stagnant Marsh
        assertEquals(Region.AUREUM, map.regionAt(-8000, 500));        // Golden Vaults
        assertEquals(Region.AUREUM, map.regionAt(-9000, -8000));      // Shadow Throne, in the north-west
        assertEquals(Region.AUREUM, map.regionAt(-6000, 7000));       // Colosseum, in the south-west
        assertEquals(Region.OCEAN, map.regionAt(0, 8000));            // the Southern Sea
        assertEquals(Region.OCEAN, map.regionAt(12_100, 0));          // beyond the edge
    }

    @Test
    void theWorldIsTwentyFiveThousandAcross() {
        int minX = map.bounds().stream().mapToInt(RegionBounds::minX).min().orElseThrow();
        int maxX = map.bounds().stream().mapToInt(RegionBounds::maxX).max().orElseThrow();
        int minZ = map.bounds().stream().mapToInt(RegionBounds::minZ).min().orElseThrow();
        int maxZ = map.bounds().stream().mapToInt(RegionBounds::maxZ).max().orElseThrow();
        assertEquals(-RegionMap.EDGE, minX);
        assertEquals(RegionMap.EDGE, maxX);
        assertEquals(-RegionMap.EDGE, minZ);
        assertEquals(RegionMap.EDGE, maxZ);
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
