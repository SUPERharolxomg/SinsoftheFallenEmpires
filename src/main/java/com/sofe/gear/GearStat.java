package com.sofe.gear;

import com.sofe.progression.CharacterAttribute;

import java.util.Locale;
import java.util.Optional;

/**
 * What an affix can raise (docs/Pociones.md, "Affixes that raise the character"). Values are in natural
 * units: attribute points, health points, resource points, armor points, or percent for the rest.
 */
public enum GearStat {
    STRENGTH(false), AGILITY(false), INTELLECT(false), WILL(false), CHARISMA(false), VITALITY(false),
    ALL_ATTRIBUTES(false),
    MAX_HEALTH(false), MAX_RESOURCE(false), RESOURCE_REGEN(true), LIFE_STEAL(true),
    PHYSICAL_DAMAGE(true), MAGIC_DAMAGE(true),
    FIRE_DAMAGE(false), FROST_DAMAGE(false), STORM_DAMAGE(false),
    CRIT_CHANCE(true), CRIT_DAMAGE(true),
    ARMOR(false), DODGE(true),
    FIRE_RESISTANCE(true), FROST_RESISTANCE(true), STORM_RESISTANCE(true), VOID_RESISTANCE(true),
    COOLDOWN_REDUCTION(true),
    /** +ranks to every skill of one class; the class id is the affix parameter. */
    CLASS_SKILL_RANKS(false),
    /** +ranks to one skill; the skill id is the affix parameter. */
    SKILL_RANKS(false);

    /** Resistances stop at 75% (docs/Pociones.md). */
    public static final double RESISTANCE_CAP = 75;
    /** Ranks from gear can take a skill up to rank 8. */
    public static final int MAX_RANK_WITH_GEAR = 8;

    private final boolean percent;

    GearStat(boolean percent) {
        this.percent = percent;
    }

    public boolean isPercent() {
        return percent;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** "+%s Strength", "+%s%% critical chance": the tooltip line of an affix with this stat. */
    public String translationKey() {
        return "gear.sofe.stat." + id();
    }

    /** The character attribute this stat raises, for the six attribute stats. */
    public Optional<CharacterAttribute> attribute() {
        return switch (this) {
            case STRENGTH -> Optional.of(CharacterAttribute.STRENGTH);
            case AGILITY -> Optional.of(CharacterAttribute.AGILITY);
            case INTELLECT -> Optional.of(CharacterAttribute.INTELLECT);
            case WILL -> Optional.of(CharacterAttribute.WILL);
            case CHARISMA -> Optional.of(CharacterAttribute.CHARISMA);
            case VITALITY -> Optional.of(CharacterAttribute.VITALITY);
            default -> Optional.empty();
        };
    }

    public static GearStat byId(String id) {
        return valueOf(id.toUpperCase(Locale.ROOT));
    }
}
