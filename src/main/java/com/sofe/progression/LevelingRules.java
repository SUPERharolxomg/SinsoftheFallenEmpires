package com.sofe.progression;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Experience curve and points per level, from data/sofe/leveling.json:
 * XP needed to go from level L to L+1 is round(xp_base * L ^ xp_exponent). Each act lets a Bearer grow only
 * so far (act_caps: 20 levels per act, 100 at the end); the next act opens the next twenty.
 */
public record LevelingRules(int maxLevel, double xpBase, double xpExponent, int skillPointsPerLevel, int attributePointsPerLevel,
                            java.util.List<Integer> actCaps) {

    public static final java.util.List<Integer> DEFAULT_ACT_CAPS = java.util.List.of(20, 40, 60, 80, 100);
    public static final LevelingRules DEFAULT = new LevelingRules(100, 60, 1.5, 1, 5, DEFAULT_ACT_CAPS);

    public LevelingRules(int maxLevel, double xpBase, double xpExponent, int skillPointsPerLevel, int attributePointsPerLevel) {
        this(maxLevel, xpBase, xpExponent, skillPointsPerLevel, attributePointsPerLevel, java.util.List.of());
    }

    /** The highest level a Bearer in this act (1-5) can reach; without act caps, the maximum level. */
    public int capFor(int act) {
        if (actCaps.isEmpty()) return maxLevel;
        return Math.min(maxLevel, actCaps.get(Math.max(1, Math.min(actCaps.size(), act)) - 1));
    }

    public LevelingRules {
        if (maxLevel < 2) throw new IllegalArgumentException("max_level must be at least 2");
        if (xpBase <= 0 || xpExponent < 0) throw new IllegalArgumentException("xp_base must be positive and xp_exponent not negative");
        if (skillPointsPerLevel < 0 || attributePointsPerLevel < 0) throw new IllegalArgumentException("points per level cannot be negative");
        actCaps = java.util.List.copyOf(actCaps);
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
                integer(json, "attribute_points_per_level", DEFAULT.attributePointsPerLevel),
                caps(json));
    }

    private static java.util.List<Integer> caps(JsonObject json) {
        if (!json.has("act_caps")) return DEFAULT_ACT_CAPS;
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (JsonElement e : json.getAsJsonArray("act_caps")) list.add(e.getAsInt());
        if (list.size() != 5) throw new IllegalArgumentException("act_caps needs one level per act (5)");
        return list;
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
