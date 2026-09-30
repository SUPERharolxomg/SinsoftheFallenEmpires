package com.sofe.progression;

import com.google.gson.JsonParser;
import com.sofe.mob.MobExperience;
import com.sofe.mob.MobScalingRules;
import com.sofe.world.region.Region;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ProgressionTest {
    private final LevelingRules rules = LevelingRules.DEFAULT;

    @Test
    void eachLevelGivesOneSkillPointAndFiveAttributePoints() {
        ProgressionData p = new ProgressionData();
        assertEquals(1, p.addXp(rules.xpToNext(1), rules));
        assertEquals(2, p.level());
        assertEquals(1, p.skillPoints());
        assertEquals(5, p.attributePoints());
        assertEquals(0, p.xp());
    }

    @Test
    void oneBigGainCanGiveSeveralLevels() {
        ProgressionData p = new ProgressionData();
        long needed = rules.xpToNext(1) + rules.xpToNext(2) + rules.xpToNext(3);
        assertEquals(3, p.addXp(needed + 7, rules));
        assertEquals(4, p.level());
        assertEquals(7, p.xp());
    }

    @Test
    void atLevelThirtyThereAreThirtySkillPointsAndOneHundredFiftyAttributePoints() {
        ProgressionData p = new ProgressionData();
        assertTrue(p.grantStartingPoints(rules));
        assertFalse(p.grantStartingPoints(rules), "the level-1 points are given only once");
        p.addXp(Long.MAX_VALUE / 2, rules);
        assertEquals(30, p.level());
        assertEquals(30, p.skillPoints(), "1 at level 1 + 29 level-ups");
        assertEquals(150, p.attributePoints());
        assertEquals(0, p.addXp(1000, rules), "no levels past 30");
    }

    @Test
    void theCurveGrows() {
        assertTrue(rules.xpToNext(10) > rules.xpToNext(2));
        assertEquals(0, rules.xpToNext(30));
    }

    @Test
    void pointsCannotGoBelowZero() {
        ProgressionData p = new ProgressionData();
        assertFalse(p.spendSkillPoint());
        assertFalse(p.spendAttributePoint());
    }

    @Test
    void mobsRollInTheirRegionRange() {
        MobScalingRules scaling = MobScalingRules.defaults();
        Random random = new Random(42);
        for (int i = 0; i < 200; i++) {
            int level = scaling.levelFor(Region.NORDRATH, 0, random);
            assertTrue(level >= 5 && level <= 12, "Nordrath level " + level);
        }
    }

    @Test
    void theActFloorRaisesMobsInEarlierRegions() {
        MobScalingRules scaling = MobScalingRules.defaults();
        Random random = new Random(7);
        for (int i = 0; i < 200; i++) {
            assertTrue(scaling.levelFor(Region.SULTHARI, 4, random) >= 20, "Act IV floor is 20");
        }
        assertEquals(27, scaling.actFloor(5));
    }

    @Test
    void levelThirtyScalesAsDocumented() {
        MobScalingRules scaling = MobScalingRules.defaults();
        assertEquals(1.0, scaling.healthMultiplier(1), 1e-9);
        assertEquals(3.32, scaling.healthMultiplier(30), 1e-9);
        assertEquals(2.74, scaling.damageMultiplier(30), 1e-9);
        assertEquals(8.7, scaling.armorBonus(30), 1e-9);
    }

    @Test
    void experienceGrowsWithLevelAndToughness() {
        long zombieLevel1 = MobExperience.forKill(1, 20);
        assertTrue(MobExperience.forKill(10, 20) > zombieLevel1);
        assertTrue(MobExperience.forKill(1, 40) > zombieLevel1, "an enderman (40 health) is worth more");
    }

    @Test
    void dataFilesMatchTheDefaults() {
        assertEquals(LevelingRules.DEFAULT, LevelingRules.parse(json("/data/sofe/leveling.json")));
        MobScalingRules fromFile = MobScalingRules.parse(json("/data/sofe/mob_scaling.json"));
        assertEquals(MobScalingRules.defaults().actFloors(), fromFile.actFloors());
        assertEquals(3.32, fromFile.healthMultiplier(30), 1e-9);
    }

    private static com.google.gson.JsonObject json(String path) {
        try (InputStream in = ProgressionTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "missing " + path);
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }
}
