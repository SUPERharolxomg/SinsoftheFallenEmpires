package com.sofe.skill;

import com.sofe.player.PlayerClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sofe.player.PlayerClass.*;
import static com.sofe.skill.SkillType.*;

/** The 50 skills of docs/Clases.md, 10 per class, in unlock order. */
public final class SkillCatalog {
    private static final List<SkillInfo> ALL = new ArrayList<>();

    static {
        add(KNIGHT, 1, ACTIVE, "scale_strike", "shield_wall", "banner_cry");
        add(KNIGHT, 11, ACTIVE, "legionary_charge", "verdict", "protectors_oath");
        add(KNIGHT, 11, PASSIVE, "iron_discipline");
        add(KNIGHT, 21, ACTIVE, "living_rampart");
        add(KNIGHT, 21, PASSIVE, "contained_wrath");
        add(KNIGHT, 30, ULTIMATE, "last_one_standing");

        add(NECROMANCER, 1, ACTIVE, "threshold_touch", "clay_warden", "burial_wraps");
        add(NECROMANCER, 11, ACTIVE, "scales_of_anubet", "scarab_plague", "canopic_jars");
        add(NECROMANCER, 11, PASSIVE, "rite_of_passage");
        add(NECROMANCER, 21, ACTIVE, "boat_of_the_dead");
        add(NECROMANCER, 21, PASSIVE, "heavy_heart");
        add(NECROMANCER, 30, ULTIMATE, "the_great_judgment");

        add(SORCERESS, 1, ACTIVE, "ember_verse", "frost_lance", "wandering_spark");
        add(SORCERESS, 11, ACTIVE, "burning_calligraphy", "water_mirror", "petal_tempest");
        add(SORCERESS, 11, PASSIVE, "sky_map");
        add(SORCERESS, 21, ACTIVE, "starfall");
        add(SORCERESS, 21, PASSIVE, "arcane_poetry");
        add(SORCERESS, 30, ULTIMATE, "written_eclipse");

        add(THIEF, 1, ACTIVE, "double_edge", "light_fingers", "smoke_step");
        add(THIEF, 11, ACTIVE, "cutthroat", "fjord_snare", "throwing_axe");
        add(THIEF, 11, PASSIVE, "deep_pockets");
        add(THIEF, 21, ACTIVE, "thousand_cuts");
        add(THIEF, 21, PASSIVE, "scavengers_instinct");
        add(THIEF, 30, ULTIMATE, "the_great_heist");

        add(KING, 1, ACTIVE, "scepter_slash", "decree_of_steadfastness", "janissary_guard");
        add(KING, 11, ACTIVE, "siege_decree", "command", "royal_treasury");
        add(KING, 11, PASSIVE, "imperial_lineage");
        add(KING, 21, ACTIVE, "bronze_cannon");
        add(KING, 21, PASSIVE, "voice_of_the_throne");
        add(KING, 30, ULTIMATE, "crown_of_the_five_lands");
    }

    private SkillCatalog() {
    }

    private static void add(PlayerClass owner, int level, SkillType type, String... ids) {
        for (String id : ids) {
            ALL.add(new SkillInfo(id, owner, level, type));
        }
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

    /**
     * The skills in the Combat Bar slots (Left Alt + 1..5). Until the skill tree arrives
     * (Sprint 3), the slots hold the three level-1 skills of the class, in order.
     */
    public static List<SkillInfo> defaultLoadout(PlayerClass owner) {
        return forClass(owner).stream().filter(s -> s.level() == 1 && s.type() == ACTIVE).toList();
    }
}
