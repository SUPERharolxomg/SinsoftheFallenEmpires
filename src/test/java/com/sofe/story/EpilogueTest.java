package com.sofe.story;

import com.google.gson.JsonObject;
import com.sofe.LangFilesTest;
import com.sofe.player.PlayerClass;
import com.sofe.world.region.Region;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** The ending's slides and epilogues (UC-31): picked from the story, and every one written in English and Spanish. */
class EpilogueTest {

    @Test
    void aSlideShowsTheChosenFateOrTheUnsettledOne() {
        Map<String, String> fates = Map.of("nordrath", "peace", "aureum", "not_a_fate");
        assertEquals("ending.sofe.nordrath.peace", Epilogue.slideKey(Region.NORDRATH, fates));
        assertEquals("ending.sofe.khemet.unsettled", Epilogue.slideKey(Region.KHEMET, fates));
        assertEquals("ending.sofe.aureum.unsettled", Epilogue.slideKey(Region.AUREUM, fates), "an unknown fate shows as unsettled");
    }

    @Test
    void theFullEpilogueNeedsEveryBearerQuest() {
        Set<String> three = Set.of("sofe:bearer/shirin_act1", "sofe:bearer/shirin_act2", "sofe:bearer/shirin_act3");
        assertFalse(Epilogue.full(PlayerClass.SORCERESS, three::contains));
        Set<String> four = new java.util.HashSet<>(three);
        four.add("sofe:bearer/shirin_act4");
        assertTrue(Epilogue.full(PlayerClass.SORCERESS, four::contains));
        assertFalse(Epilogue.full(PlayerClass.KNIGHT, four::contains), "another hero's quests do not count");
        assertEquals("ending.sofe.bearer.shirin.full", Epilogue.epilogueKey(PlayerClass.SORCERESS, true));
    }

    @Test
    void everySlideEpilogueAndCodexLineIsWrittenInBothLanguages() {
        for (String code : new String[]{"en_us", "es_es"}) {
            JsonObject lang = LangFilesTest.lang(code);
            for (Region region : Epilogue.REGIONS) {
                for (String fate : RegionFates.of(region)) {
                    assertTrue(lang.has(Epilogue.slideKey(region, Map.of(region.id(), fate))), code + ": " + region + " " + fate);
                }
                assertTrue(lang.has(Epilogue.slideKey(region, Map.of())), code + ": " + region + " unsettled");
            }
            for (PlayerClass bearer : PlayerClass.values()) {
                assertTrue(lang.has(Epilogue.epilogueKey(bearer, true)), code + ": " + bearer + " full");
                assertTrue(lang.has(Epilogue.epilogueKey(bearer, false)), code + ": " + bearer + " unfinished");
                assertTrue(lang.has("ending.sofe.codex.sin." + bearer.name().toLowerCase(java.util.Locale.ROOT)), code + ": " + bearer + " sin");
                for (int i = 1; i <= 4; i++) {
                    assertTrue(lang.has("scene.sofe.crowned_in_ash." + bearer.name().toLowerCase(java.util.Locale.ROOT) + "." + i), code + ": crowned " + bearer);
                }
            }
        }
    }
}
