package com.sofe.world;

import com.sofe.mob.BossDifficulty;
import com.sofe.story.StoryProgress;
import com.sofe.travel.TravelRules;
import com.sofe.world.lock.RegionStatus;
import com.sofe.world.region.AshenWastes;
import com.sofe.world.region.Region;
import com.sofe.world.region.RegionBounds;
import com.sofe.world.region.RegionMap;
import net.minecraft.world.Difficulty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorldRulesTest {
    private final RegionMap map = RegionMap.defaultLayout();

    @Test
    void theAshenWastesRingSurroundsTheCity() {
        RegionBounds sulthari = map.bounds().stream().filter(b -> b.region() == Region.SULTHARI).findFirst().orElseThrow();
        assertFalse(AshenWastes.contains(sulthari, 800, 0, 0), "the city is not in the Wastes");
        assertFalse(AshenWastes.contains(sulthari, 800, 400, -300));
        assertTrue(AshenWastes.contains(sulthari, 800, 1400, 0), "the edge of the region is");
        assertTrue(AshenWastes.contains(sulthari, 800, -1300, 1300));
    }

    @Test
    void bossesFollowTheDifficulty() {
        assertEquals(0.75, BossDifficulty.multiplier(Difficulty.EASY));
        assertEquals(0.75, BossDifficulty.multiplier(Difficulty.PEACEFUL));
        assertEquals(1.0, BossDifficulty.multiplier(Difficulty.NORMAL));
        assertEquals(1.25, BossDifficulty.multiplier(Difficulty.HARD));
        assertTrue(BossDifficulty.extraMechanics(Difficulty.HARD));
        assertFalse(BossDifficulty.extraMechanics(Difficulty.NORMAL));
    }

    @Test
    void waystoneTravelRules() {
        assertEquals(TravelRules.Result.OK, TravelRules.check(true, 1000, false, map, 0, 0, 1, false));
        assertEquals(TravelRules.Result.NOT_ACTIVATED, TravelRules.check(false, 1000, false, map, 0, 0, 1, false));
        assertEquals(TravelRules.Result.IN_COMBAT, TravelRules.check(true, 50, false, map, 0, 0, 1, false));
        assertEquals(TravelRules.Result.IN_ARENA, TravelRules.check(true, 1000, true, map, 0, 0, 1, false));
        assertEquals(TravelRules.Result.SEALED_REGION, TravelRules.check(true, 1000, false, map, 0, -4500, 1, false));
        assertEquals(TravelRules.Result.OK, TravelRules.check(true, 50, true, map, 0, -4500, 1, true), "operators bypass");
    }

    @Test
    void regionTitlesShowSealedAndLiberated() {
        StoryProgress story = new StoryProgress();
        assertEquals(RegionStatus.NONE, RegionStatus.of(Region.SULTHARI, story));
        assertEquals(RegionStatus.SEALED, RegionStatus.of(Region.NORDRATH, story));
        story.advanceTo(2);
        assertEquals(RegionStatus.NONE, RegionStatus.of(Region.NORDRATH, story));
        story.defeat("sofe:vorath");
        assertEquals(RegionStatus.LIBERATED, RegionStatus.of(Region.NORDRATH, story));
        assertEquals(RegionStatus.NONE, RegionStatus.of(Region.OCEAN, story));
    }
}
