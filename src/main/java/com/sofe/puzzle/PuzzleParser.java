package com.sofe.puzzle;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Reads a data/sofe/puzzles/*.json file into a {@link PuzzleDefinition}. */
public final class PuzzleParser {

    private PuzzleParser() {
    }

    public static PuzzleDefinition parse(String id, JsonObject json) {
        PuzzleDefinition.Kind kind = PuzzleDefinition.Kind.valueOf(json.get("kind").getAsString().toUpperCase(Locale.ROOT));
        List<Boolean> start = new ArrayList<>();
        if (json.has("start")) json.getAsJsonArray("start").forEach(e -> start.add(e.getAsBoolean()));
        return new PuzzleDefinition(id, kind, runes(id, json.getAsJsonArray("runes")),
                json.has("solution") ? runes(id, json.getAsJsonArray("solution")) : List.of(), start,
                str(json, "gate"), str(json, "boss"), str(json, "anchor"),
                json.has("x") ? json.get("x").getAsInt() : null, json.has("z") ? json.get("z").getAsInt() : null,
                json.has("facing") ? json.get("facing").getAsString() : "south",
                json.has("riddle_lines") ? json.get("riddle_lines").getAsInt() : 1);
    }

    private static List<PuzzleDefinition.Rune> runes(String id, JsonArray array) {
        List<PuzzleDefinition.Rune> out = new ArrayList<>();
        array.forEach(e -> out.add(PuzzleDefinition.Rune.byId(e.getAsString())
                .orElseThrow(() -> new IllegalArgumentException(id + ": unknown rune " + e.getAsString()))));
        return out;
    }

    private static String str(JsonObject json, String key) {
        return json.has(key) ? json.get(key).getAsString() : null;
    }
}
