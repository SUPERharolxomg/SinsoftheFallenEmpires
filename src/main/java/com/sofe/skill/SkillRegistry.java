package com.sofe.skill;

import com.sofe.skill.sorceress.SorceressSkills;

import java.util.Map;
import java.util.Optional;

/** The skills that have an effect in code. The other catalog skills are data only until their sprint. */
public final class SkillRegistry {
    private static final Map<String, Skill> SKILLS = Map.of(
            "ember_verse", SorceressSkills::emberVerse,
            "frost_lance", SorceressSkills::frostLance,
            "wandering_spark", SorceressSkills::wanderingSpark
    );

    private SkillRegistry() {
    }

    public static Optional<Skill> get(String id) {
        return Optional.ofNullable(SKILLS.get(id));
    }

    public static boolean isImplemented(String id) {
        return SKILLS.containsKey(id);
    }
}
