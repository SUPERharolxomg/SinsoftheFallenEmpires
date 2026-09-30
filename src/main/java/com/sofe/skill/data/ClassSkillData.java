package com.sofe.skill.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * One class's balance file, data/sofe/skills/&lt;class&gt;.json:
 *
 * <pre>
 * {
 *   "resource": { "max": 100, "start": 100, "regen_per_second": 2 },
 *   "skills": { "ember_verse": { "cost": 8, "cooldown_s": 1, "damage": 5 } },
 *   "constellations": { "steam_burst": { "damage": 4, "radius": 3 } }
 * }
 * </pre>
 *
 * Any number besides cost and cooldown_s becomes a skill parameter; "per_rank" overrides the default
 * growth per rank of any value (docs/Clases.md). "constellations" is optional.
 */
public record ClassSkillData(ResourceRules resource, Map<String, SkillStats> skills, Map<String, SkillStats> constellations) {

    public ClassSkillData {
        skills = Map.copyOf(skills);
        constellations = Map.copyOf(constellations);
    }

    public Optional<SkillStats> skill(String id) {
        return Optional.ofNullable(skills.get(id));
    }

    public Optional<SkillStats> constellation(String id) {
        return Optional.ofNullable(constellations.get(id));
    }

    public static ClassSkillData parse(JsonObject json) {
        JsonObject res = object(json, "resource");
        ResourceRules rules = new ResourceRules(
                number(res, "max").intValue(),
                number(res, "start").intValue(),
                number(res, "regen_per_second").floatValue());
        return new ClassSkillData(rules, entries(object(json, "skills")),
                json.has("constellations") ? entries(object(json, "constellations")) : Map.of());
    }

    private static Map<String, SkillStats> entries(JsonObject section) {
        Map<String, SkillStats> result = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : section.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                throw new IllegalArgumentException(entry.getKey() + ": must be an object");
            }
            JsonObject values = entry.getValue().getAsJsonObject();
            int cost = values.has("cost") ? number(values, "cost").intValue() : 0;
            double cooldown = values.has("cooldown_s") ? number(values, "cooldown_s").doubleValue() : 0;
            if (cost < 0 || cooldown < 0) {
                throw new IllegalArgumentException(entry.getKey() + ": cost and cooldown_s cannot be negative");
            }
            Map<String, Double> params = new HashMap<>();
            Map<String, Double> perRank = new HashMap<>();
            values.entrySet().forEach(v -> {
                String key = v.getKey();
                if (key.equals("per_rank")) {
                    object(values, key).entrySet().forEach(g -> perRank.put(g.getKey(), number(object(values, key), g.getKey()).doubleValue()));
                } else if (!key.equals("cost") && !key.equals("cooldown_s")) {
                    params.put(key, number(values, key).doubleValue());
                }
            });
            result.put(entry.getKey(), new SkillStats(cost, (int) Math.round(cooldown * 20), params, perRank));
        }
        return result;
    }

    private static JsonObject object(JsonObject parent, String key) {
        if (!parent.has(key) || !parent.get(key).isJsonObject()) {
            throw new IllegalArgumentException("missing \"" + key + "\" object");
        }
        return parent.getAsJsonObject(key);
    }

    private static Number number(JsonObject parent, String key) {
        JsonElement value = parent.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("\"" + key + "\" must be a number");
        }
        return value.getAsNumber();
    }
}
