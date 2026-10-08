package com.sofe.world.zone;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZoneRulesTest {
    private static final ProtectedZone CITY = new ProtectedZone("sofe:city", ProtectedZone.Kind.CITY, -160, -64, -160, 159, 319, 159);
    private static final ProtectedZone PLOT = new ProtectedZone("sofe:plot", ProtectedZone.Kind.HOMESTEAD, 100, -64, 100, 140, 319, 140);
    private static final List<ProtectedZone> ZONES = List.of(CITY, PLOT);

    @Test
    void theCityCannotBeChangedButOutsideItCan() {
        assertFalse(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.BREAK));
        assertTrue(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.PLACE)); // a player may set blocks, the city's own stay
        assertFalse(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.USE_ITEM_ON_BLOCK));
        assertFalse(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.EXPLOSION));
        assertFalse(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.MOB_GRIEFING));
        assertFalse(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.FIRE));
        assertTrue(ZoneRules.allowed(ZONES, 500, 70, 0, ZoneAction.BREAK));
    }

    @Test
    void theHomesteadAllowsEverythingEvenInsideTheCity() {
        assertTrue(ZoneRules.allowed(ZONES, 120, 70, 120, ZoneAction.BREAK));
        assertTrue(ZoneRules.allowed(ZONES, 120, 70, 120, ZoneAction.PLACE));
        assertTrue(ZoneRules.allowed(ZONES, 120, 70, 120, ZoneAction.PISTON));
        assertTrue(ZoneRules.allowed(ZONES, 120, 70, 120, ZoneAction.FIRE));
    }

    @Test
    void nothingCanBeTakenFromTheCityButTheHomesteadIsTheBearers() {
        assertFalse(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.TAKE));
        assertFalse(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.USE_ITEM_ON_BLOCK));
        assertTrue(ZoneRules.allowed(ZONES, 120, 70, 120, ZoneAction.TAKE));
        // a dungeon's and an arena's chests are their loot; their walls stay
        assertTrue(ProtectedZone.Kind.DUNGEON.allows(ZoneAction.TAKE) && ProtectedZone.Kind.ARENA.allows(ZoneAction.TAKE));
        assertFalse(ProtectedZone.Kind.DUNGEON.allows(ZoneAction.BREAK));
    }

    @Test
    void theRealStructurePositionsFileMakesTheCityAndTheHomestead() throws IOException {
        StructurePositions.Layout layout = StructurePositions.parse(JsonParser.parseString(
                Files.readString(Path.of("src/main/resources/data/sofe/structure_positions.json"))).getAsJsonObject());
        List<ProtectedZone> zones = layout.zones();
        ProtectedZone city = zones.stream().filter(z -> z.kind() == ProtectedZone.Kind.CITY && z.id().equals("sofe:sulthari/city")).findFirst().orElseThrow();
        ProtectedZone homestead = zones.stream().filter(z -> z.kind() == ProtectedZone.Kind.HOMESTEAD).findFirst().orElseThrow();
        assertTrue(city.contains(layout.spawnX(), 70, layout.spawnZ()), "new players appear inside the city");
        assertFalse(city.contains(homestead.minX(), 70, homestead.minZ()), "the Homestead is outside the walls");
        assertFalse(layout.npcs().isEmpty());
        // Sulthari's people stand in the city; a liberated camp's merchants stand in their camp
        layout.npcs().forEach(npc -> assertTrue(city.contains(npc.x(), 70, npc.z()) || zones.stream().anyMatch(z ->
                z.kind() == ProtectedZone.Kind.CAMP && z.contains(npc.x(), 70, npc.z())), npc.npc() + " should stand in the city or a camp"));
        layout.waystones().forEach((id, w) -> {
            boolean dungeon = layout.structure(id).map(st -> st.zone() == ProtectedZone.Kind.DUNGEON).orElse(false); // a crypt's, a tomb's: at its door
            if (id.startsWith("sofe:sulthari/") && !dungeon) assertTrue(city.contains(w.getX(), 70, w.getZ()), id + " should stand in the city");
        });
        assertTrue(layout.structure("sofe:sulthari/bank").isPresent(), "the bank holds the Personal Vault");
    }
}
