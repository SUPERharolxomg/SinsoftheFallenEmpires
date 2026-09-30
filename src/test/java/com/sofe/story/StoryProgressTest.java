package com.sofe.story;

import com.sofe.world.region.Region;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StoryProgressTest {

    @Test
    void actsOnlyMoveForwardAndStayInRange() {
        StoryProgress story = new StoryProgress();
        assertEquals(1, story.act());
        assertTrue(story.advanceTo(3));
        assertFalse(story.advanceTo(2));
        assertEquals(3, story.act());
        assertTrue(story.advanceTo(9));
        assertEquals(StoryProgress.LAST_ACT, story.act());
    }

    @Test
    void regionsOpenWithTheirActAndTheOceanNever() {
        StoryProgress story = new StoryProgress();
        assertTrue(story.hasUnlocked(Region.SULTHARI));
        assertFalse(story.hasUnlocked(Region.NORDRATH));
        story.advanceTo(3);
        assertTrue(story.hasUnlocked(Region.KHEMET));
        assertFalse(story.hasUnlocked(Region.AUREUM));
        story.advanceTo(5);
        assertFalse(story.hasUnlocked(Region.OCEAN));
    }

    @Test
    void questStepCountsFromOneAndCompletedPassesEveryStep() {
        StoryProgress story = new StoryProgress();
        assertEquals(0, story.questStep("sofe:q"));
        story.start("sofe:q");
        assertEquals(1, story.questStep("sofe:q"));
        story.update("sofe:q", story.quest("sofe:q").orElseThrow().advance());
        assertEquals(2, story.questStep("sofe:q"));
        story.update("sofe:q", story.quest("sofe:q").orElseThrow().complete());
        assertEquals(Integer.MAX_VALUE, story.questStep("sofe:q"));
    }

    @Test
    void startingTwiceKeepsProgressAndTracksTheFirstQuest() {
        StoryProgress story = new StoryProgress();
        assertTrue(story.start("sofe:a"));
        story.update("sofe:a", story.quest("sofe:a").orElseThrow().withCount(3));
        assertFalse(story.start("sofe:a"));
        assertEquals(3, story.quest("sofe:a").orElseThrow().count());
        story.start("sofe:b");
        assertEquals("sofe:a", story.trackedQuest().orElseThrow());
    }

    @Test
    void completingTheTrackedQuestTracksAnotherActiveOne() {
        StoryProgress story = new StoryProgress();
        story.start("sofe:a");
        story.start("sofe:b");
        story.update("sofe:a", story.quest("sofe:a").orElseThrow().complete());
        assertEquals("sofe:b", story.trackedQuest().orElseThrow());
    }

    @Test
    void aFateIsSetOnlyOnce() {
        StoryProgress story = new StoryProgress();
        assertTrue(story.setFate(Region.SULTHARI, "bazaar"));
        assertFalse(story.setFate(Region.SULTHARI, "observatory"));
        assertEquals("bazaar", story.fate(Region.SULTHARI).orElseThrow());
    }

    @Test
    void openFatesAreUnlockedRegionsWithoutAChoice() {
        StoryProgress story = new StoryProgress();
        assertEquals(List.of(Region.SULTHARI), RegionFates.open(story));
        story.setFate(Region.SULTHARI, "bazaar");
        assertTrue(RegionFates.open(story).isEmpty());
        story.advanceTo(2);
        assertEquals(List.of(Region.NORDRATH), RegionFates.open(story));
        assertTrue(RegionFates.isValid(Region.NORDRATH, "peace"));
        assertFalse(RegionFates.isValid(Region.NORDRATH, "bazaar"));
    }

    @Test
    void loadRestoresEverythingAndDropsAnUnknownTrackedQuest() {
        StoryProgress story = new StoryProgress();
        story.load(4, Map.of("sofe:a", new StoryProgress.QuestState(1, 2, false)), Set.of("sofe:vorath"),
                Map.of(Region.NORDRATH, "peace"), "sofe:missing");
        assertEquals(4, story.act());
        assertEquals(2, story.questStep("sofe:a"));
        assertTrue(story.hasDefeated("sofe:vorath"));
        assertEquals("peace", story.fate(Region.NORDRATH).orElseThrow());
        assertTrue(story.trackedQuest().isEmpty());
    }
}
