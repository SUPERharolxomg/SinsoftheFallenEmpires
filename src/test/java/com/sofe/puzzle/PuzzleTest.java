package com.sofe.puzzle;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sofe.world.region.Region;
import com.sofe.world.region.RegionMap;
import com.sofe.world.zone.StructurePositions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** The rune puzzles: pressing the runes, and every real puzzle file (solvable, placed, with its texts). */
class PuzzleTest {
    private static final Path DATA = Path.of("src/main/resources/data/sofe");
    private static final Path LANG = Path.of("src/main/resources/assets/sofe/lang");

    private static PuzzleDefinition parse(String json) {
        return PuzzleParser.parse("sofe:test", JsonParser.parseString(json).getAsJsonObject());
    }

    @Test
    void anOrderPuzzleWantsItsRunesInTheRiddlesOrder() {
        PuzzleDefinition p = parse("""
                {"kind": "order", "runes": ["moon", "sun", "eye"], "solution": ["sun", "eye", "moon"], "x": 0, "z": 0}""");
        PuzzleLogic.State s = PuzzleLogic.start(p);
        assertEquals(PuzzleLogic.Result.LIT, PuzzleLogic.press(p, s, 1), "the sun comes first");
        assertEquals(PuzzleLogic.Result.NOTHING, PuzzleLogic.press(p, s, 1), "a burning rune does nothing");
        assertEquals(PuzzleLogic.Result.WRONG, PuzzleLogic.press(p, s, 0), "the moon is not next");
        assertFalse(s.lit[1], "a wrong rune puts them all out");
        assertEquals(0, s.progress);
        PuzzleLogic.press(p, s, 1);
        PuzzleLogic.press(p, s, 2);
        assertEquals(PuzzleLogic.Result.SOLVED, PuzzleLogic.press(p, s, 0));
    }

    @Test
    void aLightsPuzzleTurnsARuneAndItsNeighbours() {
        PuzzleDefinition p = parse("""
                {"kind": "lights", "runes": ["flame", "flame", "flame", "flame"], "start": [false, true, true, false], "x": 0, "z": 0}""");
        PuzzleLogic.State s = PuzzleLogic.start(p);
        assertEquals(PuzzleLogic.Result.LIT, PuzzleLogic.press(p, s, 0));
        assertArrayEquals(new boolean[]{true, false, true, false}, s.lit);
        assertEquals(2, PuzzleLogic.fewestPresses(p));
    }

    @Test
    void aBadPuzzleIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> parse("""
                {"kind": "order", "runes": ["moon", "sun"], "solution": ["sun"], "x": 0, "z": 0}"""), "the solution must use every rune");
        assertThrows(IllegalArgumentException.class, () -> parse("""
                {"kind": "order", "runes": ["moon", "sun"], "solution": ["sun", "moon"]}"""), "a puzzle needs a place");
        assertThrows(IllegalArgumentException.class, () -> parse("""
                {"kind": "order", "runes": ["moon", "comet"], "solution": ["moon", "comet"], "x": 0, "z": 0}"""), "no comet rune");
    }

    private static JsonObject read(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    @Test
    void everyRealPuzzleIsSolvablePlacedAndWritten() throws IOException {
        StructurePositions.Layout layout = StructurePositions.parse(read(DATA.resolve("structure_positions.json")));
        JsonObject en = read(LANG.resolve("en_us.json")), es = read(LANG.resolve("es_es.json"));
        Map<String, PuzzleDefinition> puzzles = new HashMap<>();
        try (Stream<Path> files = Files.list(DATA.resolve("puzzles"))) {
            for (Path f : files.toList()) {
                String name = f.getFileName().toString().replace(".json", "");
                puzzles.put(name, PuzzleParser.parse("sofe:" + name, read(f)));
            }
        }
        assertTrue(puzzles.size() >= 20, "the dungeons have their puzzles");
        for (PuzzleDefinition p : puzzles.values()) {
            int presses = PuzzleLogic.fewestPresses(p);
            assertTrue(presses >= 2, p.id() + " should need at least two presses, needs " + presses);
            assertTrue(PuzzleService.origin(p, layout).isPresent(), p.id() + ": its anchor " + p.anchor() + " is not a Waystone");
            if (p.gate() != null) assertTrue(layout.gates().containsKey(p.gate()), p.id() + ": no gate " + p.gate());
            for (JsonObject lang : List.of(en, es)) {
                assertTrue(lang.has(p.translationKey()), "no title for " + p.id());
                for (int i = 1; i <= p.riddleLines(); i++) assertTrue(lang.has(p.translationKey() + ".riddle." + i), "no riddle line " + i + " for " + p.id());
            }
        }
        assertEquals(puzzles.size(), puzzles.values().stream().map(p -> p.gate() == null ? p.id() : p.gate()).distinct().count(), "two puzzles seal one gate");
        for (PuzzleDefinition.Rune r : PuzzleDefinition.Rune.values()) assertTrue(en.has("rune.sofe." + r.id()) && es.has("rune.sofe." + r.id()));
    }

    @Test
    void everyCryptAndRuinStandsInItsRegionAndClearOfTheOthers() throws IOException {
        StructurePositions.Layout layout = StructurePositions.parse(read(DATA.resolve("structure_positions.json")));
        RegionMap map = RegionMap.defaultLayout();
        int found = 0;
        for (StructurePositions.Structure s : layout.structures().values()) {
            if (!s.id().endsWith("/crypt") && !s.id().endsWith("/ruin")) continue;
            found++;
            String region = s.id().substring(s.id().indexOf(':') + 1, s.id().indexOf('/'));
            if (region.equals("void")) region = "sulthari"; // the Void's dungeons of Act V lie round Sulthari
            assertEquals(Region.byId(region).orElseThrow(), map.regionAt(s.x(), s.z()), s.id() + " is not in " + region);
            for (StructurePositions.Structure other : layout.structures().values()) {
                if (other == s) continue;
                double gap = Math.max(Math.abs(s.x() - other.x()) - (s.sizeX() + other.sizeX()) / 2.0,
                        Math.abs(s.z() - other.z()) - (s.sizeZ() + other.sizeZ()) / 2.0);
                assertTrue(gap > 32, s.id() + " is too close to " + other.id());
            }
        }
        assertEquals(12, found, "a crypt and a ruin in each of the five regions and for the Void");
    }
}
