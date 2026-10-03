package com.sofe.skill;

import com.sofe.skill.king.KingSkills;
import com.sofe.skill.knight.KnightSkills;
import com.sofe.skill.necromancer.NecromancerSkills;
import com.sofe.skill.sorceress.SorceressSkills;
import com.sofe.skill.thief.ThiefSkills;

import java.util.Map;
import java.util.Optional;

/** The active skills and ultimates of the five Bearers; passives work through ClassMechanics and their callers. */
public final class SkillRegistry {
    private static final Map<String, Skill> SKILLS = Map.ofEntries(
            // Sorceress
            Map.entry("ember_verse", SorceressSkills::emberVerse),
            Map.entry("frost_lance", SorceressSkills::frostLance),
            Map.entry("wandering_spark", SorceressSkills::wanderingSpark),
            Map.entry("burning_calligraphy", SorceressSkills::burningCalligraphy),
            Map.entry("water_mirror", SorceressSkills::waterMirror),
            Map.entry("petal_tempest", SorceressSkills::petalTempest),
            Map.entry("starfall", SorceressSkills::starfall),
            Map.entry("written_eclipse", SorceressSkills::writtenEclipse),
            // Knight
            Map.entry("scale_strike", KnightSkills::scaleStrike),
            Map.entry("shield_wall", KnightSkills::shieldWall),
            Map.entry("banner_cry", KnightSkills::bannerCry),
            Map.entry("legionary_charge", KnightSkills::legionaryCharge),
            Map.entry("verdict", KnightSkills::verdict),
            Map.entry("protectors_oath", KnightSkills::protectorsOath),
            Map.entry("living_rampart", KnightSkills::livingRampart),
            Map.entry("last_one_standing", KnightSkills::lastOneStanding),
            // Necromancer
            Map.entry("threshold_touch", NecromancerSkills::thresholdTouch),
            Map.entry("clay_warden", NecromancerSkills::clayWarden),
            Map.entry("raise_the_embalmed", NecromancerSkills::raiseTheEmbalmed),
            Map.entry("burial_wraps", NecromancerSkills::burialWraps),
            Map.entry("scales_of_anubet", NecromancerSkills::scalesOfAnubet),
            Map.entry("scarab_plague", NecromancerSkills::scarabPlague),
            Map.entry("canopic_jars", NecromancerSkills::canopicJars),
            Map.entry("boat_of_the_dead", NecromancerSkills::boatOfTheDead),
            Map.entry("the_great_judgment", NecromancerSkills::greatJudgment),
            // Thief
            Map.entry("double_edge", ThiefSkills::doubleEdge),
            Map.entry("light_fingers", ThiefSkills::lightFingers),
            Map.entry("smoke_step", ThiefSkills::smokeStep),
            Map.entry("cutthroat", ThiefSkills::cutthroat),
            Map.entry("fjord_snare", ThiefSkills::fjordSnare),
            Map.entry("throwing_axe", ThiefSkills::throwingAxe),
            Map.entry("thousand_cuts", ThiefSkills::thousandCuts),
            Map.entry("the_great_heist", ThiefSkills::greatHeist),
            // King
            Map.entry("scepter_slash", KingSkills::scepterSlash),
            Map.entry("decree_of_steadfastness", KingSkills::decreeOfSteadfastness),
            Map.entry("janissary_guard", KingSkills::janissaryGuard),
            Map.entry("command", KingSkills::command),
            Map.entry("siege_decree", KingSkills::siegeDecree),
            Map.entry("royal_treasury", KingSkills::royalTreasury),
            Map.entry("bronze_cannon", KingSkills::bronzeCannon),
            Map.entry("crown_of_the_five_lands", KingSkills::crownOfTheFiveLands)
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
