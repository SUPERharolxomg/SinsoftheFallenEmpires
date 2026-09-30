package com.sofe.mob;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sofe.world.region.Region;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * How strong a mob is (docs/Jugabilidad.md, "Difficulty rises with each act"), from
 * data/sofe/mob_scaling.json. Its level is the higher of a roll in its region's range and the
 * act floor of the nearest player; each level above 1 adds health, damage and armor.
 */
public record MobScalingRules(Map<Region, int[]> regionLevels, List<Integer> actFloors,
                              double healthPerLevel, double damagePerLevel, double armorPerLevel,
                              Set<String> excludedMods, int maxLevel) {

    public MobScalingRules {
        regionLevels = Map.copyOf(regionLevels);
        actFloors = List.copyOf(actFloors);
        excludedMods = Set.copyOf(excludedMods);
        for (Region r : Region.values()) {
            int[] range = regionLevels.get(r);
            if (range == null || range.length != 2 || range[0] < 1 || range[1] < range[0]) {
                throw new IllegalArgumentException("region " + r.id() + " needs a [min, max] level range");
            }
        }
        if (actFloors.size() != 5) throw new IllegalArgumentException("act_floors needs one value per act (5)");
    }

    public static MobScalingRules defaults() {
        Map<Region, int[]> levels = new EnumMap<>(Region.class);
        levels.put(Region.SULTHARI, new int[]{1, 5});
        levels.put(Region.NORDRATH, new int[]{5, 12});
        levels.put(Region.PARSIVAN, new int[]{12, 16});
        levels.put(Region.KHEMET, new int[]{16, 20});
        levels.put(Region.AUREUM, new int[]{20, 27});
        levels.put(Region.OCEAN, new int[]{1, 5});
        return new MobScalingRules(levels, List.of(1, 5, 12, 20, 27), 0.08, 0.06, 0.3, Set.of(), 30);
    }

    /** The act floor for a player in act 1-5; acts outside that range are clamped. */
    public int actFloor(int act) {
        return actFloors.get(Math.max(1, Math.min(5, act)) - 1);
    }

    /**
     * A new mob's level: a roll in the region's range, raised to the act floor.
     *
     * @param nearestPlayerAct the act of the nearest player, or 0 when no player is near
     */
    public int levelFor(Region region, int nearestPlayerAct, RandomGenerator random) {
        int[] range = regionLevels.get(region);
        int rolled = range[0] + random.nextInt(range[1] - range[0] + 1);
        int floor = nearestPlayerAct > 0 ? actFloor(nearestPlayerAct) : 1;
        return Math.min(maxLevel, Math.max(rolled, floor));
    }

    /** Health multiplier: level 1 is x1, each level above adds healthPerLevel. */
    public double healthMultiplier(int level) {
        return 1 + healthPerLevel * (level - 1);
    }

    public double damageMultiplier(int level) {
        return 1 + damagePerLevel * (level - 1);
    }

    public double armorBonus(int level) {
        return armorPerLevel * (level - 1);
    }

    public static MobScalingRules parse(JsonObject json) {
        MobScalingRules d = defaults();
        Map<Region, int[]> levels = new EnumMap<>(d.regionLevels);
        if (json.has("regions")) {
            for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("regions").entrySet()) {
                Region region = Region.byId(e.getKey()).orElseThrow(() -> new IllegalArgumentException("unknown region " + e.getKey()));
                JsonArray range = e.getValue().getAsJsonArray();
                levels.put(region, new int[]{range.get(0).getAsInt(), range.get(1).getAsInt()});
            }
        }
        List<Integer> floors = d.actFloors;
        if (json.has("act_floors")) {
            floors = json.getAsJsonArray("act_floors").asList().stream().map(JsonElement::getAsInt).toList();
        }
        JsonObject per = json.has("per_level") ? json.getAsJsonObject("per_level") : new JsonObject();
        Set<String> excluded = new HashSet<>();
        if (json.has("excluded_mods")) {
            json.getAsJsonArray("excluded_mods").forEach(e -> excluded.add(e.getAsString()));
        }
        return new MobScalingRules(levels, floors,
                per.has("health") ? per.get("health").getAsDouble() : d.healthPerLevel,
                per.has("damage") ? per.get("damage").getAsDouble() : d.damagePerLevel,
                per.has("armor") ? per.get("armor").getAsDouble() : d.armorPerLevel,
                excluded,
                json.has("max_level") ? json.get("max_level").getAsInt() : d.maxLevel);
    }
}
