package com.sofe.skill.data;

import java.util.Map;

/**
 * The numbers of one skill: resource cost, cooldown and any extra values the skill reads
 * (damage, range, duration...), all from data/sofe/skills/&lt;class&gt;.json.
 */
public record SkillStats(int cost, int cooldownTicks, Map<String, Double> params) {

    public SkillStats {
        params = Map.copyOf(params);
    }

    public double param(String name, double fallback) {
        return params.getOrDefault(name, fallback);
    }

    /** A value given in seconds in the JSON, as game ticks. */
    public int ticks(String name, double fallbackSeconds) {
        return (int) Math.round(param(name, fallbackSeconds) * 20);
    }
}
