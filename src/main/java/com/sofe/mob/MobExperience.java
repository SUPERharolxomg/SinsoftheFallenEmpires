package com.sofe.mob;

/**
 * Experience for killing a mob: grows with its level and with how tough the mob is by nature
 * (its base health compared to a zombie's 20).
 */
public final class MobExperience {

    private MobExperience() {
    }

    public static long forKill(int mobLevel, double baseMaxHealth) {
        double toughness = Math.max(0.5, Math.min(5.0, baseMaxHealth / 20.0));
        return Math.max(1, Math.round((5 + 2 * mobLevel) * toughness));
    }
}
