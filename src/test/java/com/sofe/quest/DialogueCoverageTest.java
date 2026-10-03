package com.sofe.quest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sofe.LangFilesTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every line of every conversation is written in English and Spanish, and every speaker has a face in the
 * dialogue box (a painted portrait or a placeholder; the player is drawn live). Every quest has its title
 * and the text of each step.
 */
class DialogueCoverageTest {
    private static final Path DATA = Path.of("src", "main", "resources", "data", "sofe");
    private static final Path PORTRAITS = Path.of("src", "main", "resources", "assets", "sofe", "textures", "gui", "portrait");

    private static List<Path> json(Path dir) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(p -> p.toString().endsWith(".json")).toList();
        }
    }

    private static JsonObject read(Path p) throws IOException {
        try (Reader r = Files.newBufferedReader(p)) {
            return JsonParser.parseReader(r).getAsJsonObject();
        }
    }

    @Test
    void everyLineIsTranslatedAndEverySpeakerHasAFace() throws IOException {
        List<String> keys = new ArrayList<>();
        List<String> faceless = new ArrayList<>();
        for (Path p : json(DATA.resolve("dialogue"))) {
            for (JsonElement e : read(p).getAsJsonArray("lines")) {
                JsonObject line = e.getAsJsonObject();
                keys.add(line.get("text").getAsString());
                if (line.has("answers")) for (JsonElement a : line.getAsJsonArray("answers")) keys.add(a.getAsJsonObject().get("text").getAsString());
                String speaker = line.has("speaker") ? line.get("speaker").getAsString() : "narrator";
                boolean face = speaker.equals("player") || Files.exists(PORTRAITS.resolve(speaker + ".png"))
                        || Files.exists(PORTRAITS.resolve("placeholder").resolve(speaker + ".png"));
                if (!face) faceless.add(speaker + " in " + p.getFileName());
            }
        }
        LangFilesTest.assertKeysPresent(keys);
        assertTrue(faceless.isEmpty(), "speakers without a portrait: " + faceless);
    }

    @Test
    void everyQuestHasItsTitleAndSteps() throws IOException {
        List<String> keys = new ArrayList<>();
        Path quests = DATA.resolve("quests");
        for (Path p : json(quests)) {
            String id = quests.relativize(p).toString().replace('\\', '/').replace(".json", "");
            String key = "quest.sofe." + id.replace('/', '.');
            keys.add(key);
            int steps = read(p).getAsJsonArray("steps").size();
            for (int i = 1; i <= steps; i++) keys.add(key + ".step" + i);
        }
        LangFilesTest.assertKeysPresent(keys);
    }
}
