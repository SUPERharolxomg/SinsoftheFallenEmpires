package com.sofe.skill;

import com.google.gson.JsonParser;
import com.sofe.LangFilesTest;
import com.sofe.player.PlayerClass;
import com.sofe.skill.data.ClassSkillData;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SkillCatalogTest {

    @Test
    void everyClassHasTheTreeFromTheClassesDocument() {
        assertEquals(50, SkillCatalog.all().size());
        for (PlayerClass c : PlayerClass.values()) {
            List<SkillInfo> skills = SkillCatalog.forClass(c);
            assertEquals(10, skills.size(), c + " should have 10 skills");
            assertEquals(3, count(skills, 1, SkillType.ACTIVE), c + ": 3 actives at level 1");
            assertEquals(3, count(skills, 11, SkillType.ACTIVE), c + ": 3 actives at level 11");
            assertEquals(1, count(skills, 11, SkillType.PASSIVE), c + ": 1 passive at level 11");
            assertEquals(1, count(skills, 21, SkillType.ACTIVE), c + ": 1 active at level 21");
            assertEquals(1, count(skills, 21, SkillType.PASSIVE), c + ": 1 passive at level 21");
            assertEquals(1, count(skills, 30, SkillType.ULTIMATE), c + ": 1 ultimate at level 30");
        }
    }

    private static long count(List<SkillInfo> skills, int level, SkillType type) {
        return skills.stream().filter(s -> s.level() == level && s.type() == type).count();
    }

    @Test
    void idsAreUnique() {
        Set<String> seen = new HashSet<>();
        SkillCatalog.all().forEach(s -> assertTrue(seen.add(s.id()), "duplicate " + s.id()));
    }

    @Test
    void theSorceressStartsWithHerThreeSpells() {
        assertEquals(List.of("ember_verse", "frost_lance", "wandering_spark"),
                SkillCatalog.startingSkills(PlayerClass.SORCERESS).stream().map(SkillInfo::id).toList());
    }

    @Test
    void everySkillHasAnIconAndANameInBothLanguages() {
        for (SkillInfo skill : SkillCatalog.all()) {
            assertNotNull(getClass().getResource("/assets/sofe/" + skill.iconPath()), "missing icon for " + skill.id());
        }
        LangFilesTest.assertKeysPresent(SkillCatalog.all().stream().map(SkillInfo::translationKey).toList());
    }

    @Test
    void everySkillHasValuesInItsClassFile() {
        for (PlayerClass c : PlayerClass.values()) {
            ClassSkillData data = load(c);
            for (SkillInfo skill : SkillCatalog.forClass(c)) {
                var stats = data.skill(skill.id());
                assertTrue(stats.isPresent(), c.id() + ".json has no entry for " + skill.id());
                if (skill.type() != SkillType.PASSIVE) {
                    assertTrue(stats.get().cooldownTicks() > 0, skill.id() + " needs a cooldown");
                }
            }
        }
    }

    @Test
    void sorceressValuesMatchTheClassesDocument() {
        ClassSkillData sorceress = load(PlayerClass.SORCERESS);
        assertEquals(8, sorceress.skill("ember_verse").orElseThrow().cost());
        assertEquals(20, sorceress.skill("ember_verse").orElseThrow().cooldownTicks());
        assertEquals(12, sorceress.skill("wandering_spark").orElseThrow().cost());
        assertEquals(100, sorceress.resource().max());
        assertEquals(5, sorceress.constellations().size());
    }

    static ClassSkillData load(PlayerClass c) {
        String path = "/data/sofe/skills/" + c.id() + ".json";
        try (InputStream in = SkillCatalogTest.class.getResourceAsStream(path)) {
            assertNotNull(in, "missing " + path);
            return ClassSkillData.parse(JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject());
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }
}
