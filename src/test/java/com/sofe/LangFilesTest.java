package com.sofe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sofe.player.PlayerClass;
import com.sofe.player.ResourceType;
import com.sofe.story.Sin;
import com.sofe.world.region.Region;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/** Every player-facing text must exist in English and Spanish (docs/Arquitectura.md, section 4). */
public class LangFilesTest {

    public static JsonObject lang(String code) {
        String path = "/assets/sofe/lang/" + code + ".json";
        try (InputStream in = LangFilesTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "missing " + path);
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void englishAndSpanishHaveTheSameKeys() {
        Set<String> en = new TreeSet<>(lang("en_us").keySet());
        Set<String> es = new TreeSet<>(lang("es_es").keySet());

        Set<String> onlyEn = new TreeSet<>(en);
        onlyEn.removeAll(es);
        Set<String> onlyEs = new TreeSet<>(es);
        onlyEs.removeAll(en);

        assertTrue(onlyEn.isEmpty(), "missing in es_es: " + onlyEn);
        assertTrue(onlyEs.isEmpty(), "missing in en_us: " + onlyEs);
    }

    @Test
    void noEmptyTexts() {
        for (String code : List.of("en_us", "es_es")) {
            lang(code).entrySet().forEach(e ->
                    assertFalse(e.getValue().getAsString().isBlank(), code + " has an empty text for " + e.getKey()));
        }
    }

    @Test
    void coreEnumsAreTranslated() {
        List<String> keys = new ArrayList<>();
        for (Sin s : Sin.values()) keys.add(s.translationKey());
        for (ResourceType r : ResourceType.values()) keys.add(r.translationKey());
        for (Region r : Region.values()) keys.add(r.translationKey());
        for (PlayerClass c : PlayerClass.values()) {
            keys.addAll(List.of(c.translationKey(), c.heroKey(), c.roleKey(), c.quoteKey()));
        }
        assertKeysPresent(keys);
    }

    public static void assertKeysPresent(List<String> keys) {
        for (String code : List.of("en_us", "es_es")) {
            JsonObject lang = lang(code);
            List<String> missing = keys.stream().filter(k -> !lang.has(k)).toList();
            assertTrue(missing.isEmpty(), code + " is missing " + missing);
        }
    }
}
