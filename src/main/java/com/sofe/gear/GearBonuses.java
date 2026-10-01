package com.sofe.gear;

import com.sofe.progression.CharacterAttribute;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Everything a Bearer's equipped gear adds up to (docs/Pociones.md): the sum of the affixes of every
 * piece whose requirements they meet. An item whose requirements are not met stays equipped but gives nothing.
 */
public final class GearBonuses {
    public static final GearBonuses NONE = new GearBonuses();

    private final Map<GearStat, Double> stats = new EnumMap<>(GearStat.class);
    private final Map<String, Integer> classRanks = new HashMap<>();
    private final Map<String, Integer> skillRanks = new HashMap<>();

    /**
     * Adds up the gear a player wears.
     *
     * @param level          the player's level
     * @param attributeValue the player's attribute values without gear (requirements never count gear itself)
     */
    public static GearBonuses of(Iterable<GearData> equipped, int level, ToIntFunction<CharacterAttribute> attributeValue) {
        GearBonuses bonuses = new GearBonuses();
        for (GearData gear : equipped) {
            if (meetsRequirements(gear, level, attributeValue)) bonuses.add(gear);
        }
        return bonuses;
    }

    public static boolean meetsRequirements(GearData gear, int level, ToIntFunction<CharacterAttribute> attributeValue) {
        if (level < gear.requiredLevel()) return false;
        return gear.requiredAttribute() == null || attributeValue.applyAsInt(gear.requiredAttribute()) >= gear.requiredValue();
    }

    void add(GearData gear) {
        for (GearData.Roll roll : gear.affixes()) {
            switch (roll.stat()) {
                case CLASS_SKILL_RANKS -> classRanks.merge(roll.parameter(), roll.value(), Integer::sum);
                case SKILL_RANKS -> skillRanks.merge(roll.parameter(), roll.value(), Integer::sum);
                default -> stats.merge(roll.stat(), (double) roll.value(), Double::sum);
            }
        }
    }

    public double get(GearStat stat) {
        return stats.getOrDefault(stat, 0.0);
    }

    /** Points added to an attribute: its own affixes plus "of the Five Crowns". */
    public int attribute(CharacterAttribute attribute) {
        GearStat own = GearStat.valueOf(attribute.name());
        return (int) (get(own) + get(GearStat.ALL_ATTRIBUTES));
    }

    public Map<CharacterAttribute, Integer> attributes() {
        Map<CharacterAttribute, Integer> result = new EnumMap<>(CharacterAttribute.class);
        for (CharacterAttribute a : CharacterAttribute.values()) {
            int value = attribute(a);
            if (value != 0) result.put(a, value);
        }
        return result;
    }

    /** A percent stat as a fraction: 12 (%) → 0.12. Resistances stop at 75%. */
    public double fraction(GearStat stat) {
        double value = get(stat);
        if (stat.name().endsWith("_RESISTANCE")) value = Math.min(GearStat.RESISTANCE_CAP, value);
        return value / 100.0;
    }

    /** Extra ranks gear gives one skill of a class. */
    public int bonusRanks(String skillId, String classId) {
        return skillRanks.getOrDefault(skillId, 0) + classRanks.getOrDefault(classId, 0);
    }

    /** Ranks from gear only raise learned skills, and never past rank 8. */
    public static int effectiveRank(int learnedRank, int bonusRanks) {
        if (learnedRank <= 0) return 0;
        return Math.min(GearStat.MAX_RANK_WITH_GEAR, learnedRank + Math.max(0, bonusRanks));
    }

    public boolean isEmpty() {
        return stats.isEmpty() && classRanks.isEmpty() && skillRanks.isEmpty();
    }
}
