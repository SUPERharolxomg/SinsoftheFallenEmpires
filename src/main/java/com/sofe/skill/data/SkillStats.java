package com.sofe.skill.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The numbers of one skill: resource cost, cooldown and any extra values the skill reads
 * (damage, range, duration...), all from data/sofe/skills/&lt;class&gt;.json. These are the rank-1
 * values; {@link #withRank} applies the growth per rank (docs/Clases.md, "Skill points and ranks").
 */
public record SkillStats(int cost, int cooldownTicks, Map<String, Double> params, Map<String, Double> perRank) {

    /** Main values: +25% per rank by default. */
    private static final Set<String> POWER = Set.of("damage", "heal", "healing", "absorb", "shield");
    /** Durations, areas and counts: +15% per rank by default. */
    private static final Set<String> REACH = Set.of("radius", "bounce_range", "width", "targets", "bounces", "stars", "soldiers", "shots", "marks", "max_wardens");
    /** Counts are whole numbers: rounded down after growing. */
    private static final Set<String> COUNTS = Set.of("targets", "bounces", "stars", "soldiers", "shots", "marks", "max_wardens");
    private static final double POWER_GROWTH = 0.25, REACH_GROWTH = 0.15, COST_GROWTH = 0.08, COOLDOWN_GROWTH = -0.05;

    public SkillStats {
        params = Map.copyOf(params);
        perRank = Map.copyOf(perRank);
    }

    public SkillStats(int cost, int cooldownTicks, Map<String, Double> params) {
        this(cost, cooldownTicks, params, Map.of());
    }

    public double param(String name, double fallback) {
        return params.getOrDefault(name, fallback);
    }

    /** A value given in seconds in the JSON, as game ticks. */
    public int ticks(String name, double fallbackSeconds) {
        return (int) Math.round(param(name, fallbackSeconds) * 20);
    }

    /** Growth per rank of a value: the skill's own "per_rank" entry, or the default for that kind of value. */
    public double growth(String name) {
        if (perRank.containsKey(name)) return perRank.get(name);
        if (name.equals("cost")) return COST_GROWTH;
        if (name.equals("cooldown_s")) return COOLDOWN_GROWTH;
        if (POWER.contains(name)) return POWER_GROWTH;
        if (REACH.contains(name) || name.endsWith("_s")) return REACH_GROWTH;
        return 0;
    }

    /** The values at a given rank (rank 1 is this record unchanged). */
    public SkillStats withRank(int rank) {
        int steps = Math.max(0, rank - 1);
        if (steps == 0) return this;
        Map<String, Double> grown = new HashMap<>();
        params.forEach((name, value) -> {
            double v = value * (1 + growth(name) * steps);
            grown.put(name, COUNTS.contains(name) ? Math.floor(v) : v);
        });
        int newCost = (int) Math.round(cost * (1 + growth("cost") * steps));
        int newCooldown = (int) Math.round(cooldownTicks * Math.max(0.1, 1 + growth("cooldown_s") * steps));
        return new SkillStats(newCost, newCooldown, grown, perRank);
    }
}
