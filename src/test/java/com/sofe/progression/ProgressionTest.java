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
    void atLevelOneHundredThereAreOneHundredSkillPointsAndFiveHundredAttributePoints() {
        ProgressionData p = new ProgressionData();
        assertTrue(p.grantStartingPoints(rules));
        assertFalse(p.grantStartingPoints(rules), "the level-1 points are given only once");
        p.addXp(Long.MAX_VALUE / 2, rules);
        assertEquals(100, p.level());
        assertEquals(100, p.skillPoints(), "1 at level 1 + 99 level-ups");
        assertEquals(500, p.attributePoints());
        assertEquals(0, p.addXp(1000, rules), "no levels past 100");
    }

    @Test
    void eachActLetsTheBearerGrowTwentyLevels() {
        assertEquals(20, rules.capFor(1));
        assertEquals(40, rules.capFor(2));
        assertEquals(60, rules.capFor(3));
        assertEquals(80, rules.capFor(4));
        assertEquals(100, rules.capFor(5));
        ProgressionData p = new ProgressionData();
        p.addXp(Long.MAX_VALUE / 2, rules, rules.capFor(1));
        assertEquals(20, p.level(), "Act I stops at 20");
        assertEquals(0, p.addXp(1_000_000, rules, rules.capFor(1)), "no XP past the cap of the act");
        assertTrue(p.addXp(1_000_000, rules, rules.capFor(2)) > 0, "Act II opens the next twenty levels");
    }

    @Test
    void theCurveGrows() {
        assertTrue(rules.xpToNext(10) > rules.xpToNext(2));
        assertEquals(0, rules.xpToNext(100));
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
            assertTrue(level >= 20 && level <= 40, "Nordrath level " + level);
        }
    }

    @Test
    void theActFloorRaisesMobsInEarlierRegions() {
        MobScalingRules scaling = MobScalingRules.defaults();
        Random random = new Random(7);
        for (int i = 0; i < 200; i++) {
            assertTrue(scaling.levelFor(Region.SULTHARI, 4, random) >= 60, "Act IV floor is 60");
        }
        assertEquals(80, scaling.actFloor(5));
    }

    @Test
    void mobsFollowTheBearersLevel() {
        MobScalingRules scaling = MobScalingRules.defaults();
        Random random = new Random(5);
        for (int i = 0; i < 200; i++) {
            int level = scaling.levelFor(Region.SULTHARI, 1, 40, false, random);
            assertTrue(level >= 38 && level <= 41, "a level-40 Bearer meets level " + level);
        }
        assertEquals(41, scaling.levelFor(Region.SULTHARI, 1, 40, true, random), "a boss stands one level above");
        assertTrue(scaling.levelFor(Region.AUREUM, 4, 5, false, random) >= 60, "the region is still the least a mob can be");
    }

    @Test
    void levelOneHundredScalesAsDocumented() {
        MobScalingRules scaling = MobScalingRules.defaults();
        assertEquals(1.0, scaling.healthMultiplier(1), 1e-9);
        assertEquals(5.95, scaling.healthMultiplier(100), 1e-9);
        assertEquals(4.465, scaling.damageMultiplier(100), 1e-9);
        assertEquals(14.85, scaling.armorBonus(100), 1e-9);
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
        assertEquals(5.95, fromFile.healthMultiplier(100), 1e-9);
    }

    private static com.google.gson.JsonObject json(String path) {
        try (InputStream in = ProgressionTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "missing " + path);
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void enemiesFollowTheBearersLevelNotTheRegionsTop() {
        var mobs = com.sofe.mob.MobScalingRules.defaults();
        var random = new java.util.Random(4);
        for (int i = 0; i < 200; i++) {
            int lowly = mobs.levelFor(com.sofe.world.region.Region.SULTHARI, 1, 1, false, random);
            assertTrue(lowly >= 1 && lowly <= 2, "a level-1 Bearer met a level-" + lowly + " enemy in Sulthari");
            int strong = mobs.levelFor(com.sofe.world.region.Region.SULTHARI, 2, 40, false, random);
            assertTrue(strong >= 38 && strong <= 41, "a level-40 Bearer met a level-" + strong + " enemy");
            int boss = mobs.levelFor(com.sofe.world.region.Region.NORDRATH, 2, 30, true, random);
            assertEquals(31, boss, "a boss stands one level above the strongest Bearer");
            int floored = mobs.levelFor(com.sofe.world.region.Region.NORDRATH, 2, 12, false, random);
            assertTrue(floored >= 20, "Nordrath's enemies are never under its least level, met " + floored);
        }
    }
}
