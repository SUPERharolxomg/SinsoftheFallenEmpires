package com.sofe.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sofe.world.lock.RegionLocks;
import com.sofe.world.region.BiomeZones;
import com.sofe.world.region.Region;
import com.sofe.world.region.RegionMap;
import com.sofe.world.zone.ProtectedZone;
import com.sofe.world.zone.StructurePositions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class NordrathRulesTest {
    private static final Path DATA = Path.of("src/main/resources/data/sofe");
    private static final Path LANG = Path.of("src/main/resources/assets/sofe/lang");
    private final RegionMap map = RegionMap.defaultLayout();

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    @Test
    void theVeilStandsAtTheScaledBordersOfTheBurningDeep() {
        // Sulthari / Parsivan is at overworld x = 1500: in the Nether, column 188 covers x = 1504
        assertTrue(RegionLocks.isVeilColumn(map, 188, 0, 8));
        assertFalse(RegionLocks.isVeilColumn(map, 187, 0, 8));
        assertFalse(RegionLocks.isVeilColumn(map, 189, 0, 8));
        assertFalse(RegionLocks.veilPassable(map, 188, 0, 2, 8), "Parsivan is still sealed in Act II");
        assertTrue(RegionLocks.veilPassable(map, 188, 0, 3, 8));
        assertEquals(Region.PARSIVAN, RegionLocks.sealedBehind(map, 188, 0, 2, 8).orElseThrow());
    }

    @Test
    void biomeZonesAreRoundWithAWavyEdge() {
        assertTrue(BiomeZones.contains(0, -4500, 560, 0, -4500));
        assertTrue(BiomeZones.contains(0, -4500, 560, 300, -4500));
        assertFalse(BiomeZones.contains(0, -4500, 560, 700, -4500));
    }

    @Test
    void everyGateHasItsConditionHintAndStructure() throws IOException {
        StructurePositions.Layout layout = StructurePositions.parse(json(DATA.resolve("structure_positions.json")));
        assertFalse(layout.gates().isEmpty());
        JsonObject en = json(LANG.resolve("en_us.json")), es = json(LANG.resolve("es_es.json"));
        for (StructurePositions.Gate gate : layout.gates().values()) {
            String condition = gate.condition().substring(gate.condition().indexOf(':') + 1);
            assertTrue(Files.exists(DATA.resolve("conditions/" + condition + ".json")), gate.id() + " needs conditions/" + condition + ".json");
            assertTrue(en.has(gate.hint()) && es.has(gate.hint()), gate.id() + " needs the text " + gate.hint());
            boolean inside = layout.structures().values().stream().anyMatch(s ->
                    Math.abs(gate.x() - s.x()) <= s.sizeX() / 2 && Math.abs(gate.z() - s.z()) <= s.sizeZ() / 2 && s.zone() == ProtectedZone.Kind.DUNGEON);
            assertTrue(inside, gate.id() + " should stand in the wall of a dungeon");
        }
    }

    @Test
    void theDungeonsAndTheCitadelStandInNordrath() throws IOException {
        StructurePositions.Layout layout = StructurePositions.parse(json(DATA.resolve("structure_positions.json")));
        layout.structures().values().stream().filter(s -> s.id().startsWith("sofe:nordrath/"))
                .forEach(s -> assertEquals(Region.NORDRATH, map.regionAt(s.x(), s.z()), s.id()));
        assertTrue(layout.structure("sofe:nordrath/burning_citadel").isPresent());
        assertEquals(ProtectedZone.Kind.ARENA, layout.structure("sofe:nordrath/citadel_arena").orElseThrow().zone());
    }

    @Test
    void everyStoryAdvancementHasItsParentAndTexts() throws IOException {
        Path folder = DATA.resolve("advancements/story");
        JsonObject en = json(LANG.resolve("en_us.json")), es = json(LANG.resolve("es_es.json"));
        try (Stream<Path> files = Files.list(folder)) {
            for (Path file : files.toList()) {
                JsonObject advancement = json(file);
                if (advancement.has("parent")) {
                    String parent = advancement.get("parent").getAsString().replace("sofe:story/", "");
                    assertTrue(Files.exists(folder.resolve(parent + ".json")), file + " has a missing parent " + parent);
                }
                JsonObject display = advancement.getAsJsonObject("display");
                for (String part : new String[]{"title", "description"}) {
                    String key = display.getAsJsonObject(part).get("translate").getAsString();
                    assertTrue(en.has(key) && es.has(key), file + " needs the text " + key);
                }
                assertTrue(advancement.getAsJsonObject("criteria").has("done"), file + " must be granted only by the story");
            }
        }
    }
}
