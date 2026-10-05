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
        assertFalse(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.BREAK, false));
        assertFalse(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.PLACE, false));
        assertFalse(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.EXPLOSION, false));
        assertFalse(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.MOB_GRIEFING, false));
        assertFalse(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.FIRE, false));
        assertTrue(ZoneRules.allowed(ZONES, 500, 70, 0, ZoneAction.BREAK, false));
    }

    @Test
    void theHomesteadAllowsEverythingEvenInsideTheCity() {
        assertTrue(ZoneRules.allowed(ZONES, 120, 70, 120, ZoneAction.BREAK, false));
        assertTrue(ZoneRules.allowed(ZONES, 120, 70, 120, ZoneAction.PLACE, false));
        assertTrue(ZoneRules.allowed(ZONES, 120, 70, 120, ZoneAction.PISTON, false));
        assertTrue(ZoneRules.allowed(ZONES, 120, 70, 120, ZoneAction.FIRE, false));
    }

    @Test
    void bypassingPlayersMayBuildAnywhere() {
        assertTrue(ZoneRules.allowed(ZONES, 0, 70, 0, ZoneAction.BREAK, true));
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
            if (id.startsWith("sofe:sulthari/")) assertTrue(city.contains(w.getX(), 70, w.getZ()), id + " should stand in the city");
        });
        assertTrue(layout.structure("sofe:sulthari/bank").isPresent(), "the bank holds the Personal Vault");
    }
}
