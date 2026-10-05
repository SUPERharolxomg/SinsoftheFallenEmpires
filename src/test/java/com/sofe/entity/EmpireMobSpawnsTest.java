package com.sofe.entity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Each creature of an empire spawns only in the biomes of its own region, so each act has its own enemies (the Void's kin are everywhere). */
class EmpireMobSpawnsTest {
    private static final Path FOLDER = Path.of("src", "main", "resources", "data", "sofe", "forge", "biome_modifier");
    private static final Map<String, Set<String>> REGION_BIOMES = Map.of(
            "sulthari", Set.of("sofe:sulthari_desert", "sofe:ashen_wastes"),
            "nordrath", Set.of("sofe:nordrath_tundra", "sofe:nordrath_ice_fields", "sofe:nordrath_volcanic_forges"),
            "parsivan", Set.of("sofe:parsivan_gardens"),
            "khemet", Set.of("sofe:khemet_valley"),
            "aureum", Set.of("sofe:aureum_hills"));

    @Test
    void everyCreatureSpawnsInOneRegionOnly() throws IOException {
        Set<String> seen = new HashSet<>();
        try (var files = Files.list(FOLDER)) {
            for (Path p : files.filter(f -> f.getFileName().toString().startsWith("spawn_") && !f.getFileName().toString().startsWith("spawn_void_")).toList()) {
                JsonObject json = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
                Set<String> biomes = new HashSet<>();
                json.getAsJsonArray("biomes").forEach(b -> biomes.add(b.getAsString()));
                long regions = REGION_BIOMES.values().stream().filter(r -> r.containsAll(biomes)).count();
                assertEquals(1, regions, p.getFileName() + " spawns across regions: " + biomes);
                seen.add(json.getAsJsonArray("spawners").get(0).getAsJsonObject().get("type").getAsString());
            }
        }
        assertTrue(seen.size() >= 8, "every creature of the empires should spawn somewhere: " + seen);
    }
}
