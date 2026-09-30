package com.sofe.skill;

import com.sofe.player.PlayerClass;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.sofe.skill.SkillBook.LearnResult.*;
import static org.junit.jupiter.api.Assertions.*;

class SkillBookTest {
    private static SkillInfo skill(String id) {
        return SkillCatalog.byId(id).orElseThrow();
    }

    @Test
    void theFirstPointGoesIntoALevelOneSkillAndFillsTheFirstSlot() {
        SkillBook book = new SkillBook();
        assertEquals(LEARNED, book.learn(skill("frost_lance"), PlayerClass.SORCERESS, 1, 1));
        assertEquals(1, book.rank("frost_lance"));
        assertEquals("frost_lance", book.slot(0).orElseThrow());
    }

    @Test
    void eachRankNeedsOneMoreLevel() {
        SkillBook book = new SkillBook();
        book.learn(skill("ember_verse"), PlayerClass.SORCERESS, 1, 5);
        assertEquals(NEEDS_LEVEL, book.check(skill("ember_verse"), PlayerClass.SORCERESS, 1, 5), "rank 2 needs level 2");
        assertEquals(LEARNED, book.learn(skill("ember_verse"), PlayerClass.SORCERESS, 2, 5));
        assertEquals(2, book.rank("ember_verse"));
    }

    @Test
    void ranksStopAtFiveAndUltimatesAtOne() {
        SkillBook book = new SkillBook();
        for (int level = 1; level <= 5; level++) {
            assertEquals(LEARNED, book.learn(skill("ember_verse"), PlayerClass.SORCERESS, level, 10));
        }
        assertEquals(MAX_RANK, book.check(skill("ember_verse"), PlayerClass.SORCERESS, 30, 10));
        assertEquals(1, skill("written_eclipse").maxRank());
    }

    @Test
    void theTreeArrowsMustBeFollowed() {
        SkillBook book = new SkillBook();
        assertEquals(NEEDS_PARENT, book.check(skill("petal_tempest"), PlayerClass.SORCERESS, 11, 5), "Petal Tempest comes from Wandering Spark");
        book.learn(skill("wandering_spark"), PlayerClass.SORCERESS, 11, 5);
        assertEquals(LEARNED, book.learn(skill("petal_tempest"), PlayerClass.SORCERESS, 11, 5));
    }

    @Test
    void levelElevenSkillsNeedLevelEleven() {
        SkillBook book = new SkillBook();
        book.learn(skill("ember_verse"), PlayerClass.SORCERESS, 10, 5);
        assertEquals(NEEDS_LEVEL, book.check(skill("burning_calligraphy"), PlayerClass.SORCERESS, 10, 5));
    }

    @Test
    void noPointsNoOtherClassesSkills() {
        SkillBook book = new SkillBook();
        assertEquals(NO_POINTS, book.check(skill("ember_verse"), PlayerClass.SORCERESS, 1, 0));
        assertEquals(WRONG_CLASS, book.check(skill("shield_wall"), PlayerClass.SORCERESS, 1, 5));
    }

    @Test
    void passivesTakeNoSlotAndTheUltimateTakesTheLastOne() {
        SkillBook book = new SkillBook();
        book.learn(skill("wandering_spark"), PlayerClass.SORCERESS, 30, 99);
        book.learn(skill("sky_map"), PlayerClass.SORCERESS, 30, 99);
        book.learn(skill("petal_tempest"), PlayerClass.SORCERESS, 30, 99);
        book.learn(skill("starfall"), PlayerClass.SORCERESS, 30, 99);
        book.learn(skill("written_eclipse"), PlayerClass.SORCERESS, 30, 99);
        assertEquals(java.util.Arrays.asList("wandering_spark", "petal_tempest", "starfall", null, null, "written_eclipse"), book.slots());
    }

    @Test
    void respecGivesEveryPointBackAndSaveRoundTrips() {
        SkillBook book = new SkillBook();
        book.learn(skill("ember_verse"), PlayerClass.SORCERESS, 5, 9);
        book.learn(skill("ember_verse"), PlayerClass.SORCERESS, 5, 9);
        book.learn(skill("frost_lance"), PlayerClass.SORCERESS, 5, 9);

        SkillBook loaded = new SkillBook();
        loaded.load(book.ranks(), book.slots());
        assertEquals(book.ranks(), loaded.ranks());
        assertEquals(book.slots(), loaded.slots());

        assertEquals(3, loaded.reset());
        assertEquals(Map.of(), loaded.ranks());
    }

    @Test
    void everyTreeArrowPointsToAnEarlierSkillOfTheSameClass() {
        for (SkillInfo s : SkillCatalog.all()) {
            if (s.parent() == null) {
                assertEquals(1, s.level(), s.id() + " has no parent but is not a first-row skill");
                continue;
            }
            SkillInfo parent = SkillCatalog.byId(s.parent()).orElseThrow(() -> new AssertionError(s.id() + " -> missing " + s.parent()));
            assertEquals(s.owner(), parent.owner(), s.id());
            assertTrue(parent.level() < s.level(), s.id() + " must come from an earlier row");
        }
    }
}
