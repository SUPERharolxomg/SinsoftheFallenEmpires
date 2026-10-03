package com.sofe.skill;

import com.sofe.skill.data.SkillDataManager;
import com.sofe.skill.data.SkillStats;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Applies a skill's upgrades to its values (Diablo II synergies): for each upgrade the player has learned, at
 * its rank r, every value the upgrade names is changed:
 * <ul>
 *   <li>{@code <value>_bonus: x}: the skill's value times (1 + x * r);</li>
 *   <li>{@code <value>_add: x}: the skill's value plus x * r (a value it did not have starts at 0);</li>
 *   <li>{@code cost_cut} and {@code cooldown_cut: x}: cost or cooldown times (1 - x * r), never below half.</li>
 * </ul>
 * Other values of an upgrade are read by the improved skill's own code through its rank
 * ({@link Skill.Context#upgrade}). Plain logic: the ranks come in as a function, so it is unit tested.
 */
public final class Upgrades {

    private Upgrades() {
    }

    /** The skill's values with every learned upgrade applied. */
    public static SkillStats apply(String skill, SkillStats stats, ToIntFunction<String> rankOf) {
        Map<String, Double> params = new HashMap<>(stats.params());
        double cost = stats.cost(), cooldown = stats.cooldownTicks();
        boolean changed = false;
        for (SkillInfo upgrade : SkillCatalog.upgradesOf(skill)) {
            int rank = rankOf.applyAsInt(upgrade.id());
            if (rank <= 0) continue;
            SkillStats u = SkillDataManager.forClass(upgrade.owner()).flatMap(d -> d.skill(upgrade.id())).orElse(null);
            if (u == null) continue;
            for (Map.Entry<String, Double> e : u.params().entrySet()) {
                String key = e.getKey();
                double x = e.getValue() * rank;
                if (key.equals("cost_cut")) {
                    cost *= Math.max(0.5, 1 - x);
                } else if (key.equals("cooldown_cut")) {
                    cooldown *= Math.max(0.5, 1 - x);
                } else if (key.endsWith("_bonus") && params.containsKey(key.substring(0, key.length() - 6))) {
                    String target = key.substring(0, key.length() - 6);
                    params.put(target, params.get(target) * (1 + x));
                } else if (key.endsWith("_add")) {
                    String target = key.substring(0, key.length() - 4);
                    params.merge(target, x, Double::sum);
                } else {
                    continue;
                }
                changed = true;
            }
        }
        if (!changed) return stats;
        return new SkillStats((int) Math.round(cost), (int) Math.round(cooldown), floorCounts(params), stats.perRank());
    }

    /** Counts (soldiers, stars, bounces...) are whole numbers after an upgrade too. */
    private static Map<String, Double> floorCounts(Map<String, Double> params) {
        for (String count : new String[]{"targets", "bounces", "stars", "soldiers", "shots", "marks", "max_wardens", "jumps", "coins", "count", "max_dead", "extra_snares", "chain", "extra_bolts", "pierce", "pellets"}) {
            params.computeIfPresent(count, (k, v) -> Math.floor(v));
        }
        return params;
    }
}
