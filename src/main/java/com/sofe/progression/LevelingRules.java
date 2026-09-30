package com.sofe.progression;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Experience curve and points per level, from data/sofe/leveling.json:
 * XP needed to go from level L to L+1 is round(xp_base * L ^ xp_exponent).
 */
public record LevelingRules(int maxLevel, double xpBase, double xpExponent, int skillPointsPerLevel, int attributePointsPerLevel) {

    public static final LevelingRules DEFAULT = new LevelingRules(30, 60, 1.5, 1, 5);

    public LevelingRules {
        if (maxLevel < 2) throw new IllegalArgumentException("max_level must be at least 2");
        if (xpBase <= 0 || xpExponent < 0) throw new IllegalArgumentException("xp_base must be positive and xp_exponent not negative");
        if (skillPointsPerLevel < 0 || attributePointsPerLevel < 0) throw new IllegalArgumentException("points per level cannot be negative");
    }

    /** XP needed to reach the next level; 0 at the maximum level. */
    public long xpToNext(int level) {
        if (level >= maxLevel) return 0;
        return Math.max(1, Math.round(xpBase * Math.pow(level, xpExponent)));
    }

    public static LevelingRules parse(JsonObject json) {
        return new LevelingRules(
                integer(json, "max_level", DEFAULT.maxLevel),
                number(json, "xp_base", DEFAULT.xpBase),
                number(json, "xp_exponent", DEFAULT.xpExponent),
                integer(json, "skill_points_per_level", DEFAULT.skillPointsPerLevel),
                integer(json, "attribute_points_per_level", DEFAULT.attributePointsPerLevel));
    }

    private static double number(JsonObject json, String key, double fallback) {
        JsonElement e = json.get(key);
        if (e == null) return fallback;
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException("\"" + key + "\" must be a number");
        return e.getAsDouble();
    }

    private static int integer(JsonObject json, String key, int fallback) {
        return (int) Math.round(number(json, key, fallback));
    }
}
