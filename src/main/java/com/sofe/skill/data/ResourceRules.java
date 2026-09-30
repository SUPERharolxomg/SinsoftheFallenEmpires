package com.sofe.skill.data;

/** How a class's resource behaves: its maximum, the value after joining or respawning, and passive regeneration. */
public record ResourceRules(int max, int start, float regenPerSecond) {

    public ResourceRules {
        if (max <= 0) throw new IllegalArgumentException("max must be positive, got " + max);
        if (start < 0 || start > max) throw new IllegalArgumentException("start must be between 0 and max, got " + start);
        if (regenPerSecond < 0) throw new IllegalArgumentException("regen_per_second cannot be negative");
    }
}
