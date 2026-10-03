package com.sofe.skill;

import com.sofe.player.PlayerClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sofe.player.PlayerClass.*;
import static com.sofe.skill.SkillType.*;

/**
 * The skill trees, 30 per class as in Diablo II: three branches (tabs) of 10, each growing down from
 * one level-1 skill. The 10 base skills of docs/Clases.md and 20 upgrades per class (synergies and
 * changes to the base skills); written by scripts/make_skill_tree.py.
 */
public final class SkillCatalog {
    private static final List<SkillInfo> ALL = new ArrayList<>();
    private static final int PASSIVE_COLUMN = 3;

    static {
        // <generated-tree> by scripts/make_skill_tree.py from scripts/skill_tree_catalog.py

        // knight
        add(KNIGHT, 1, ACTIVE, "scale_strike", null, 1, 0, List.of());
        add(KNIGHT, 6, PASSIVE, "weighted_blade", "scale_strike", 0, 0, List.of("scale_strike"));
        add(KNIGHT, 6, PASSIVE, "rending_strike", "scale_strike", 2, 0, List.of("scale_strike"));
        add(KNIGHT, 11, ACTIVE, "legionary_charge", "scale_strike", 1, 0, List.of());
        add(KNIGHT, 16, PASSIVE, "cavalry_drill", "legionary_charge", 0, 0, List.of("legionary_charge"));
        add(KNIGHT, 16, PASSIVE, "trampling_charge", "legionary_charge", 2, 0, List.of("legionary_charge"));
        add(KNIGHT, 21, PASSIVE, "relentless_advance", "cavalry_drill", 0, 0, List.of("legionary_charge"));
        add(KNIGHT, 21, PASSIVE, "executioners_edge", "rending_strike", 2, 0, List.of("scale_strike"));
        add(KNIGHT, 26, PASSIVE, "charge_mastery", "trampling_charge", 1, 0, List.of());
        add(KNIGHT, 26, PASSIVE, "wrath_of_the_scale", "executioners_edge", 2, 0, List.of("scale_strike"));
        add(KNIGHT, 1, ACTIVE, "shield_wall", null, 1, 1, List.of());
        add(KNIGHT, 6, PASSIVE, "tower_shield", "shield_wall", 0, 1, List.of("shield_wall"));
        add(KNIGHT, 6, PASSIVE, "shield_bash", "shield_wall", 2, 1, List.of("shield_wall"));
        add(KNIGHT, 11, PASSIVE, "iron_discipline", "shield_wall", 0, 1, List.of());
        add(KNIGHT, 11, ACTIVE, "protectors_oath", "shield_wall", 1, 1, List.of());
        add(KNIGHT, 16, PASSIVE, "sworn_guardian", "protectors_oath", 2, 1, List.of("protectors_oath"));
        add(KNIGHT, 21, PASSIVE, "contained_wrath", "iron_discipline", 0, 1, List.of());
        add(KNIGHT, 21, ACTIVE, "living_rampart", "protectors_oath", 1, 1, List.of());
        add(KNIGHT, 26, PASSIVE, "bulwark_of_aureum", "living_rampart", 2, 1, List.of("living_rampart"));
        add(KNIGHT, 30, ULTIMATE, "last_one_standing", "living_rampart", 1, 1, List.of());
        add(KNIGHT, 1, ACTIVE, "banner_cry", null, 1, 2, List.of());
        add(KNIGHT, 6, PASSIVE, "rallying_call", "banner_cry", 0, 2, List.of("banner_cry"));
        add(KNIGHT, 6, PASSIVE, "banner_of_the_order", "banner_cry", 2, 2, List.of("banner_cry"));
        add(KNIGHT, 11, ACTIVE, "verdict", "banner_cry", 1, 2, List.of());
        add(KNIGHT, 16, PASSIVE, "wide_judgment", "verdict", 0, 2, List.of("verdict"));
        add(KNIGHT, 16, PASSIVE, "heavy_verdict", "verdict", 2, 2, List.of("verdict"));
        add(KNIGHT, 21, PASSIVE, "martyrs_resolve", "rallying_call", 0, 2, List.of("banner_cry"));
        add(KNIGHT, 21, PASSIVE, "sentence_of_the_scale", "heavy_verdict", 2, 2, List.of("verdict"));
        add(KNIGHT, 26, PASSIVE, "oathbound_voice", "martyrs_resolve", 0, 2, List.of("banner_cry"));
        add(KNIGHT, 26, PASSIVE, "war_banner", "sentence_of_the_scale", 2, 2, List.of("verdict"));

        // necromancer
        add(NECROMANCER, 1, ACTIVE, "threshold_touch", null, 1, 0, List.of());
        add(NECROMANCER, 6, PASSIVE, "soul_siphon", "threshold_touch", 0, 0, List.of("threshold_touch"));
        add(NECROMANCER, 6, PASSIVE, "lingering_mark", "threshold_touch", 2, 0, List.of("threshold_touch"));
        add(NECROMANCER, 11, ACTIVE, "scales_of_anubet", "threshold_touch", 1, 0, List.of());
        add(NECROMANCER, 16, PASSIVE, "final_rites", "scales_of_anubet", 0, 0, List.of("scales_of_anubet"));
        add(NECROMANCER, 16, PASSIVE, "weighed_heart", "scales_of_anubet", 2, 0, List.of("scales_of_anubet"));
        add(NECROMANCER, 21, PASSIVE, "devouring_beam", "soul_siphon", 0, 0, List.of("threshold_touch"));
        add(NECROMANCER, 21, PASSIVE, "judges_gaze", "lingering_mark", 2, 0, List.of("threshold_touch"));
        add(NECROMANCER, 26, PASSIVE, "soul_harvest", "final_rites", 0, 0, List.of());
        add(NECROMANCER, 26, PASSIVE, "verdict_of_anubet", "weighed_heart", 2, 0, List.of("scales_of_anubet"));
        add(NECROMANCER, 1, ACTIVE, "clay_warden", null, 1, 1, List.of());
        add(NECROMANCER, 6, PASSIVE, "baked_clay", "clay_warden", 0, 1, List.of("clay_warden"));
        add(NECROMANCER, 6, ACTIVE, "raise_the_embalmed", "clay_warden", 2, 1, List.of());
        add(NECROMANCER, 11, PASSIVE, "rite_of_passage", "clay_warden", 0, 1, List.of());
        add(NECROMANCER, 11, ACTIVE, "canopic_jars", "clay_warden", 1, 1, List.of());
        add(NECROMANCER, 16, PASSIVE, "embalming_salts", "raise_the_embalmed", 2, 1, List.of("raise_the_embalmed"));
        add(NECROMANCER, 21, PASSIVE, "heavy_heart", "rite_of_passage", 0, 1, List.of());
        add(NECROMANCER, 21, PASSIVE, "legion_of_clay", "canopic_jars", 1, 1, List.of("clay_warden"));
        add(NECROMANCER, 21, PASSIVE, "linen_of_eternity", "embalming_salts", 2, 1, List.of("raise_the_embalmed", "clay_warden"));
        add(NECROMANCER, 26, PASSIVE, "jackal_fury", "linen_of_eternity", 2, 1, List.of("raise_the_embalmed"));
        add(NECROMANCER, 1, ACTIVE, "burial_wraps", null, 1, 2, List.of());
        add(NECROMANCER, 6, PASSIVE, "tight_wraps", "burial_wraps", 0, 2, List.of("burial_wraps"));
        add(NECROMANCER, 6, PASSIVE, "mummify", "burial_wraps", 2, 2, List.of("burial_wraps"));
        add(NECROMANCER, 11, ACTIVE, "scarab_plague", "burial_wraps", 1, 2, List.of());
        add(NECROMANCER, 16, PASSIVE, "swarm_queen", "scarab_plague", 0, 2, List.of("scarab_plague"));
        add(NECROMANCER, 16, PASSIVE, "locust_cloud", "scarab_plague", 2, 2, List.of("scarab_plague"));
        add(NECROMANCER, 21, ACTIVE, "boat_of_the_dead", "scarab_plague", 1, 2, List.of());
        add(NECROMANCER, 26, PASSIVE, "the_ferrymans_toll", "boat_of_the_dead", 0, 2, List.of("boat_of_the_dead"));
        add(NECROMANCER, 26, PASSIVE, "tide_of_the_underworld", "boat_of_the_dead", 2, 2, List.of("the_great_judgment"));
        add(NECROMANCER, 30, ULTIMATE, "the_great_judgment", "boat_of_the_dead", 1, 2, List.of());

        // sorceress
        add(SORCERESS, 1, ACTIVE, "ember_verse", null, 1, 0, List.of());
        add(SORCERESS, 6, PASSIVE, "kindled_verse", "ember_verse", 0, 0, List.of("ember_verse"));
        add(SORCERESS, 6, PASSIVE, "cinder_burst", "ember_verse", 2, 0, List.of("ember_verse"));
        add(SORCERESS, 11, ACTIVE, "burning_calligraphy", "ember_verse", 1, 0, List.of());
        add(SORCERESS, 16, PASSIVE, "double_stroke", "burning_calligraphy", 0, 0, List.of("burning_calligraphy"));
        add(SORCERESS, 16, PASSIVE, "searing_ink", "burning_calligraphy", 2, 0, List.of("burning_calligraphy"));
        add(SORCERESS, 21, PASSIVE, "phoenix_verse", "kindled_verse", 0, 0, List.of("ember_verse"));
        add(SORCERESS, 21, PASSIVE, "wildfire", "cinder_burst", 2, 0, List.of("ember_verse"));
        add(SORCERESS, 26, PASSIVE, "fire_mastery", "double_stroke", 0, 0, List.of("ember_verse", "burning_calligraphy"));
        add(SORCERESS, 26, PASSIVE, "solar_script", "searing_ink", 2, 0, List.of("burning_calligraphy"));
        add(SORCERESS, 1, ACTIVE, "frost_lance", null, 1, 1, List.of());
        add(SORCERESS, 6, PASSIVE, "piercing_cold", "frost_lance", 0, 1, List.of("frost_lance"));
        add(SORCERESS, 6, PASSIVE, "shatter", "frost_lance", 2, 1, List.of("frost_lance"));
        add(SORCERESS, 11, ACTIVE, "water_mirror", "frost_lance", 1, 1, List.of());
        add(SORCERESS, 16, PASSIVE, "deep_freeze", "piercing_cold", 0, 1, List.of("frost_lance"));
        add(SORCERESS, 16, PASSIVE, "long_stride", "water_mirror", 1, 1, List.of("water_mirror"));
        add(SORCERESS, 16, PASSIVE, "mirror_shards", "water_mirror", 2, 1, List.of("water_mirror"));
        add(SORCERESS, 21, PASSIVE, "glacial_spike", "deep_freeze", 0, 1, List.of("frost_lance"));
        add(SORCERESS, 26, PASSIVE, "frost_mastery", "long_stride", 1, 1, List.of("frost_lance", "water_mirror"));
        add(SORCERESS, 26, PASSIVE, "lingering_reflection", "mirror_shards", 2, 1, List.of("water_mirror"));
        add(SORCERESS, 1, ACTIVE, "wandering_spark", null, 1, 2, List.of());
        add(SORCERESS, 6, PASSIVE, "forked_spark", "wandering_spark", 0, 2, List.of("wandering_spark"));
        add(SORCERESS, 6, PASSIVE, "static_field", "wandering_spark", 2, 2, List.of("wandering_spark"));
        add(SORCERESS, 11, PASSIVE, "sky_map", "wandering_spark", 0, 2, List.of());
        add(SORCERESS, 11, ACTIVE, "petal_tempest", "wandering_spark", 1, 2, List.of());
        add(SORCERESS, 16, PASSIVE, "thorned_petals", "petal_tempest", 2, 2, List.of("petal_tempest"));
        add(SORCERESS, 21, PASSIVE, "arcane_poetry", "sky_map", 0, 2, List.of());
        add(SORCERESS, 21, ACTIVE, "starfall", "petal_tempest", 1, 2, List.of());
        add(SORCERESS, 26, PASSIVE, "falling_heavens", "starfall", 2, 2, List.of("starfall"));
        add(SORCERESS, 30, ULTIMATE, "written_eclipse", "starfall", 1, 2, List.of());

        // thief
        add(THIEF, 1, ACTIVE, "double_edge", null, 1, 0, List.of());
        add(THIEF, 6, PASSIVE, "quick_hands", "double_edge", 0, 0, List.of("double_edge"));
        add(THIEF, 6, PASSIVE, "third_edge", "double_edge", 2, 0, List.of("double_edge"));
        add(THIEF, 11, ACTIVE, "cutthroat", "double_edge", 1, 0, List.of());
        add(THIEF, 16, PASSIVE, "butchers_arithmetic", "cutthroat", 0, 0, List.of("cutthroat"));
        add(THIEF, 16, PASSIVE, "open_artery", "cutthroat", 2, 0, List.of("cutthroat"));
        add(THIEF, 21, ACTIVE, "thousand_cuts", "cutthroat", 1, 0, List.of());
        add(THIEF, 26, PASSIVE, "whirling_steel", "thousand_cuts", 0, 0, List.of("thousand_cuts"));
        add(THIEF, 26, PASSIVE, "master_thief", "thousand_cuts", 2, 0, List.of("the_great_heist"));
        add(THIEF, 30, ULTIMATE, "the_great_heist", "thousand_cuts", 1, 0, List.of());
        add(THIEF, 1, ACTIVE, "light_fingers", null, 1, 1, List.of());
        add(THIEF, 6, PASSIVE, "pickpocket", "light_fingers", 0, 1, List.of("light_fingers"));
        add(THIEF, 6, PASSIVE, "sleight_of_hand", "light_fingers", 2, 1, List.of("light_fingers"));
        add(THIEF, 11, PASSIVE, "deep_pockets", "light_fingers", 0, 1, List.of());
        add(THIEF, 11, ACTIVE, "throwing_axe", "light_fingers", 1, 1, List.of());
        add(THIEF, 16, PASSIVE, "twin_axes", "throwing_axe", 1, 1, List.of("throwing_axe"));
        add(THIEF, 16, PASSIVE, "keen_axe", "throwing_axe", 2, 1, List.of("throwing_axe"));
        add(THIEF, 21, PASSIVE, "scavengers_instinct", "deep_pockets", 0, 1, List.of());
        add(THIEF, 26, PASSIVE, "the_fences_cut", "deep_pockets", 1, 1, List.of("deep_pockets"));
        add(THIEF, 26, PASSIVE, "ambusher", "scavengers_instinct", 0, 1, List.of("scavengers_instinct"));
        add(THIEF, 1, ACTIVE, "smoke_step", null, 1, 2, List.of());
        add(THIEF, 6, PASSIVE, "thick_smoke", "smoke_step", 0, 2, List.of("smoke_step"));
        add(THIEF, 6, PASSIVE, "choking_smoke", "smoke_step", 2, 2, List.of("smoke_step"));
        add(THIEF, 11, ACTIVE, "fjord_snare", "smoke_step", 1, 2, List.of());
        add(THIEF, 16, PASSIVE, "line_of_snares", "fjord_snare", 0, 2, List.of("fjord_snare"));
        add(THIEF, 16, PASSIVE, "barbed_rope", "fjord_snare", 2, 2, List.of("fjord_snare"));
        add(THIEF, 21, PASSIVE, "ash_cloak", "thick_smoke", 0, 2, List.of("smoke_step"));
        add(THIEF, 21, PASSIVE, "shadow_walker", "choking_smoke", 2, 2, List.of("smoke_step"));
        add(THIEF, 26, PASSIVE, "hunters_patience", "line_of_snares", 0, 2, List.of("fjord_snare"));
        add(THIEF, 26, PASSIVE, "master_of_the_fjords", "barbed_rope", 2, 2, List.of("fjord_snare"));

        // king
        add(KING, 1, ACTIVE, "scepter_slash", null, 1, 0, List.of());
        add(KING, 6, PASSIVE, "royal_edge", "scepter_slash", 0, 0, List.of("scepter_slash"));
        add(KING, 6, PASSIVE, "tribute", "scepter_slash", 2, 0, List.of("scepter_slash"));
        add(KING, 11, ACTIVE, "command", "scepter_slash", 1, 0, List.of());
        add(KING, 16, PASSIVE, "sweeping_scepter", "royal_edge", 0, 0, List.of("scepter_slash"));
        add(KING, 16, PASSIVE, "rally_the_court", "command", 1, 0, List.of("command"));
        add(KING, 16, PASSIVE, "marked_for_death", "command", 2, 0, List.of("command"));
        add(KING, 21, PASSIVE, "sultans_wrath", "sweeping_scepter", 0, 0, List.of("scepter_slash"));
        add(KING, 26, PASSIVE, "authority_of_the_crown", "rally_the_court", 1, 0, List.of());
        add(KING, 26, PASSIVE, "order_of_execution", "marked_for_death", 2, 0, List.of("command"));
        add(KING, 1, ACTIVE, "decree_of_steadfastness", null, 1, 1, List.of());
        add(KING, 6, PASSIVE, "iron_decree", "decree_of_steadfastness", 0, 1, List.of("decree_of_steadfastness"));
        add(KING, 11, PASSIVE, "imperial_lineage", "decree_of_steadfastness", 0, 1, List.of());
        add(KING, 11, ACTIVE, "siege_decree", "decree_of_steadfastness", 1, 1, List.of());
        add(KING, 16, PASSIVE, "unbreaking_walls", "siege_decree", 2, 1, List.of("siege_decree"));
        add(KING, 21, PASSIVE, "voice_of_the_throne", "imperial_lineage", 0, 1, List.of());
        add(KING, 21, ACTIVE, "bronze_cannon", "siege_decree", 1, 1, List.of());
        add(KING, 26, PASSIVE, "heavy_shot", "bronze_cannon", 0, 1, List.of("bronze_cannon"));
        add(KING, 26, PASSIVE, "grapeshot", "bronze_cannon", 2, 1, List.of("bronze_cannon"));
        add(KING, 30, ULTIMATE, "crown_of_the_five_lands", "bronze_cannon", 1, 1, List.of());
        add(KING, 1, ACTIVE, "janissary_guard", null, 1, 2, List.of());
        add(KING, 6, PASSIVE, "brass_armor", "janissary_guard", 0, 2, List.of("janissary_guard"));
        add(KING, 6, PASSIVE, "scimitar_drill", "janissary_guard", 2, 2, List.of("janissary_guard"));
        add(KING, 11, ACTIVE, "royal_treasury", "janissary_guard", 1, 2, List.of());
        add(KING, 16, PASSIVE, "veteran_guard", "brass_armor", 0, 2, List.of("janissary_guard"));
        add(KING, 16, PASSIVE, "golden_rain", "royal_treasury", 2, 2, List.of("royal_treasury"));
        add(KING, 21, PASSIVE, "standing_army", "veteran_guard", 0, 2, List.of("janissary_guard"));
        add(KING, 21, PASSIVE, "war_chest", "golden_rain", 2, 2, List.of("royal_treasury"));
        add(KING, 26, PASSIVE, "aetherium_guard", "standing_army", 0, 2, List.of("janissary_guard"));
        add(KING, 26, PASSIVE, "royal_mint", "war_chest", 1, 2, List.of("royal_treasury"));
        // </generated-tree>
    }

    private SkillCatalog() {
    }

    private static void add(PlayerClass owner, int level, SkillType type, String id, String parent, int column, int tab, List<String> upgrades) {
        ALL.add(new SkillInfo(id, owner, level, type, parent, column, tab, upgrades));
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

    /** The three level-1 skills of a class, one per branch, in branch order. */
    public static List<SkillInfo> startingSkills(PlayerClass owner) {
        return forClass(owner).stream().filter(s -> s.level() == 1 && s.type() == ACTIVE).toList();
    }

    /** The upgrades that improve this skill. */
    public static List<SkillInfo> upgradesOf(String skill) {
        return ALL.stream().filter(s -> s.upgrades().contains(skill)).toList();
    }
}
