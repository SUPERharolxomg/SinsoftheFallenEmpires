package com.sofe.skill;

import com.sofe.player.PlayerClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sofe.player.PlayerClass.*;
import static com.sofe.skill.SkillType.*;

/**
 * The 50 skills of docs/Clases.md, 10 per class, as a Diablo II style tree: three columns of
 * actives that grow down from the level-1 skills, and a column of passives (their own tab).
 */
public final class SkillCatalog {
    private static final List<SkillInfo> ALL = new ArrayList<>();
    private static final int PASSIVE_COLUMN = 3;

    static {
        // Knight: strike and charge / shield and protect / banner and verdict
        add(KNIGHT, 1, ACTIVE, "scale_strike", null, 0);
        add(KNIGHT, 1, ACTIVE, "shield_wall", null, 1);
        add(KNIGHT, 1, ACTIVE, "banner_cry", null, 2);
        add(KNIGHT, 11, ACTIVE, "legionary_charge", "scale_strike", 0);
        add(KNIGHT, 11, ACTIVE, "protectors_oath", "shield_wall", 1);
        add(KNIGHT, 11, ACTIVE, "verdict", "banner_cry", 2);
        add(KNIGHT, 11, PASSIVE, "iron_discipline", "shield_wall", PASSIVE_COLUMN);
        add(KNIGHT, 21, ACTIVE, "living_rampart", "protectors_oath", 1);
        add(KNIGHT, 21, PASSIVE, "contained_wrath", "iron_discipline", PASSIVE_COLUMN);
        add(KNIGHT, 30, ULTIMATE, "last_one_standing", "living_rampart", 1);

        // Necromancer: judgment / wardens / wraps and plagues
        add(NECROMANCER, 1, ACTIVE, "threshold_touch", null, 0);
        add(NECROMANCER, 1, ACTIVE, "clay_warden", null, 1);
        add(NECROMANCER, 1, ACTIVE, "burial_wraps", null, 2);
        add(NECROMANCER, 11, ACTIVE, "scales_of_anubet", "threshold_touch", 0);
        add(NECROMANCER, 11, ACTIVE, "canopic_jars", "clay_warden", 1);
        add(NECROMANCER, 11, ACTIVE, "scarab_plague", "burial_wraps", 2);
        add(NECROMANCER, 11, PASSIVE, "rite_of_passage", "clay_warden", PASSIVE_COLUMN);
        add(NECROMANCER, 21, ACTIVE, "boat_of_the_dead", "scarab_plague", 2);
        add(NECROMANCER, 21, PASSIVE, "heavy_heart", "rite_of_passage", PASSIVE_COLUMN);
        add(NECROMANCER, 30, ULTIMATE, "the_great_judgment", "boat_of_the_dead", 2);

        // Sorceress: fire / frost / storm and stars
        add(SORCERESS, 1, ACTIVE, "ember_verse", null, 0);
        add(SORCERESS, 1, ACTIVE, "frost_lance", null, 1);
        add(SORCERESS, 1, ACTIVE, "wandering_spark", null, 2);
        add(SORCERESS, 11, ACTIVE, "burning_calligraphy", "ember_verse", 0);
        add(SORCERESS, 11, ACTIVE, "water_mirror", "frost_lance", 1);
        add(SORCERESS, 11, ACTIVE, "petal_tempest", "wandering_spark", 2);
        add(SORCERESS, 11, PASSIVE, "sky_map", "wandering_spark", PASSIVE_COLUMN);
        add(SORCERESS, 21, ACTIVE, "starfall", "petal_tempest", 2);
        add(SORCERESS, 21, PASSIVE, "arcane_poetry", "sky_map", PASSIVE_COLUMN);
        add(SORCERESS, 30, ULTIMATE, "written_eclipse", "starfall", 2);

        // Thief: blades / thieving and throwing / smoke and traps
        add(THIEF, 1, ACTIVE, "double_edge", null, 0);
        add(THIEF, 1, ACTIVE, "light_fingers", null, 1);
        add(THIEF, 1, ACTIVE, "smoke_step", null, 2);
        add(THIEF, 11, ACTIVE, "cutthroat", "double_edge", 0);
        add(THIEF, 11, ACTIVE, "throwing_axe", "light_fingers", 1);
        add(THIEF, 11, ACTIVE, "fjord_snare", "smoke_step", 2);
        add(THIEF, 11, PASSIVE, "deep_pockets", "light_fingers", PASSIVE_COLUMN);
        add(THIEF, 21, ACTIVE, "thousand_cuts", "cutthroat", 0);
        add(THIEF, 21, PASSIVE, "scavengers_instinct", "deep_pockets", PASSIVE_COLUMN);
        add(THIEF, 30, ULTIMATE, "the_great_heist", "thousand_cuts", 0);

        // King: command / decrees / guard and treasury
        add(KING, 1, ACTIVE, "scepter_slash", null, 0);
        add(KING, 1, ACTIVE, "decree_of_steadfastness", null, 1);
        add(KING, 1, ACTIVE, "janissary_guard", null, 2);
        add(KING, 11, ACTIVE, "command", "scepter_slash", 0);
        add(KING, 11, ACTIVE, "siege_decree", "decree_of_steadfastness", 1);
        add(KING, 11, ACTIVE, "royal_treasury", "janissary_guard", 2);
        add(KING, 11, PASSIVE, "imperial_lineage", "decree_of_steadfastness", PASSIVE_COLUMN);
        add(KING, 21, ACTIVE, "bronze_cannon", "siege_decree", 1);
        add(KING, 21, PASSIVE, "voice_of_the_throne", "imperial_lineage", PASSIVE_COLUMN);
        add(KING, 30, ULTIMATE, "crown_of_the_five_lands", "bronze_cannon", 1);
    }

    private SkillCatalog() {
    }

    private static void add(PlayerClass owner, int level, SkillType type, String id, String parent, int column) {
        ALL.add(new SkillInfo(id, owner, level, type, parent, column));
    }

    public static List<SkillInfo> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static List<SkillInfo> forClass(PlayerClass owner) {
        return ALL.stream().filter(s -> s.owner() == owner).toList();
    }

    public static Optional<SkillInfo> byId(String id) {
        return ALL.stream().filter(s -> s.id().equals(id)).findFirst();
    }

    /** The three level-1 skills of a class, in column order. */
    public static List<SkillInfo> startingSkills(PlayerClass owner) {
        return forClass(owner).stream().filter(s -> s.level() == 1 && s.type() == ACTIVE).toList();
    }
}
