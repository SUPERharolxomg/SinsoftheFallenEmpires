package com.sofe.skill;

import com.google.gson.JsonParser;
import com.sofe.skill.data.ClassSkillData;
import com.sofe.skill.data.SkillStats;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Higher rank, stronger skill (docs/Clases.md): the default growth table and per-skill overrides. */
class SkillRankGrowthTest {
    private final SkillStats emberRank1 = new SkillStats(8, 20, Map.of("damage", 5.0, "range", 18.0, "burn_s", 3.0));

    @Test
    void rankOneIsTheBaseValue() {
        assertSame(emberRank1, emberRank1.withRank(1));
    }

    @Test
    void rankFiveFollowsTheDefaultTable() {
        SkillStats r5 = emberRank1.withRank(5);
        assertEquals(10.0, r5.param("damage", 0), 1e-9, "damage doubles at rank 5");
        assertEquals(4.8, r5.param("burn_s", 0), 1e-9, "durations grow 15% per rank");
        assertEquals(18.0, r5.param("range", 0), 1e-9, "range does not grow");
        assertEquals(11, r5.cost(), "cost grows 8% per rank (8 -> 10.56, rounded)");
        assertEquals(16, r5.cooldownTicks(), "cooldown shrinks 5% per rank");
    }

    @Test
    void countsAreRoundedDown() {
        SkillStats spark = new SkillStats(12, 60, Map.of("bounces", 2.0));
        assertEquals(2.0, spark.withRank(3).param("bounces", 0), "2 * 1.3 = 2.6 -> 2 bounces");
        assertEquals(3.0, spark.withRank(5).param("bounces", 0), "2 * 1.6 = 3.2 -> 3 bounces");
    }

    @Test
    void aSkillCanOverrideItsGrowth() {
        ClassSkillData data = ClassSkillData.parse(JsonParser.parseString("""
                {"resource":{"max":100,"start":100,"regen_per_second":1},
                 "skills":{"frost_lance":{"cost":10,"cooldown_s":2,"damage":6,"slow_s":3,
                           "per_rank":{"damage":0.5,"cost":0}}}}""").getAsJsonObject());
        SkillStats r3 = data.skill("frost_lance").orElseThrow().withRank(3);
        assertEquals(12.0, r3.param("damage", 0), 1e-9, "+50% per rank from per_rank");
        assertEquals(10, r3.cost(), "per_rank cost 0 keeps the cost flat");
        assertFalse(r3.params().containsKey("per_rank"), "per_rank is not a parameter");
    }
}
