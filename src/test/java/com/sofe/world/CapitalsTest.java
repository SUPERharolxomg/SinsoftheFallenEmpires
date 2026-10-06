package com.sofe.world;

import com.google.gson.JsonParser;
import com.sofe.world.region.Region;
import com.sofe.world.region.RegionMap;
import com.sofe.world.zone.ProtectedZone;
import com.sofe.world.zone.StructurePositions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The capitals of Parsivan, Khemet and Aureum (CapitalCity): in their own region, protected, apart from every other place. */
class CapitalsTest {
    @Test
    void everyCapitalStandsWholeInItsRegionAwayFromTheOtherPlaces() throws Exception {
        StructurePositions.Layout layout = StructurePositions.parse(JsonParser.parseString(
                Files.readString(Path.of("src/main/resources/data/sofe/structure_positions.json"))).getAsJsonObject());
        RegionMap map = RegionMap.defaultLayout();
        for (Map.Entry<String, Region> capital : Map.of("sofe:parsivan/city", Region.PARSIVAN, "sofe:khemet/city", Region.KHEMET,
                "sofe:aureum/city", Region.AUREUM).entrySet()) {
            StructurePositions.Structure city = layout.structure(capital.getKey()).orElseThrow();
            int h = city.sizeX() / 2 + 90; // the walls, the shore and the roads out of the gates
            for (int[] c : new int[][]{{-h, -h}, {h, -h}, {-h, h}, {h, h}, {0, 0}}) {
                assertEquals(capital.getValue(), map.regionAt(city.x() + c[0], city.z() + c[1]), capital.getKey() + " leaves its region");
            }
            assertEquals(ProtectedZone.Kind.CITY, city.zone(), capital.getKey() + " is not a protected city");
            assertTrue(layout.waystones().containsKey(capital.getKey()), capital.getKey() + " has no Waystone");
            for (StructurePositions.Structure other : layout.structures().values()) {
                if (other == city) continue;
                boolean near = Math.abs(other.x() - city.x()) < h + other.sizeX() / 2 && Math.abs(other.z() - city.z()) < h + other.sizeZ() / 2;
                assertFalse(near, capital.getKey() + " is too close to " + other.id());
            }
        }
    }
}
