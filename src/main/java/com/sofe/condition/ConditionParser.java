package com.sofe.condition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/** Reads conditions from the JSON format in data/sofe/conditions/. */
public final class ConditionParser {

    private ConditionParser() {
    }

    public static Condition parse(JsonElement json) {
        return parse(json, "$");
    }

    private static Condition parse(JsonElement json, String path) {
        if (!json.isJsonObject()) {
            throw error(path, "a condition must be a JSON object");
        }
        JsonObject obj = json.getAsJsonObject();
        String type = string(obj, "type", path);
        return switch (type) {
            case "act_reached" -> {
                int act = integer(obj, "act", path);
                if (act < 1 || act > 5) throw error(path, "act must be between 1 and 5, got " + act);
                yield new Condition.ActReached(act);
            }
            case "boss_defeated" -> new Condition.BossDefeated(id(obj, "boss", path));
            case "quest_step" -> new Condition.QuestStep(id(obj, "quest", path), positive(obj, "step", path));
            case "item_owned" -> new Condition.ItemOwned(id(obj, "item", path),
                    obj.has("count") ? positive(obj, "count", path) : 1);
            case "fate_is" -> new Condition.FateIs(string(obj, "region", path), string(obj, "fate", path));
            case "class_is" -> new Condition.ClassIs(string(obj, "class", path));
            case "all_of" -> new Condition.AllOf(list(obj, path));
            case "any_of" -> new Condition.AnyOf(list(obj, path));
            case "not" -> {
                if (!obj.has("condition")) throw error(path, "missing \"condition\"");
                yield new Condition.Not(parse(obj.get("condition"), path + ".condition"));
            }
            default -> throw error(path, "unknown condition type \"" + type + "\"");
        };
    }

    private static List<Condition> list(JsonObject obj, String path) {
        if (!obj.has("conditions") || !obj.get("conditions").isJsonArray()) {
            throw error(path, "missing \"conditions\" array");
        }
        JsonArray array = obj.getAsJsonArray("conditions");
        if (array.isEmpty()) {
            throw error(path, "\"conditions\" cannot be empty");
        }
        List<Condition> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            result.add(parse(array.get(i), path + ".conditions[" + i + "]"));
        }
        return result;
    }

    private static String string(JsonObject obj, String key, String path) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            throw error(path, "missing \"" + key + "\"");
        }
        return obj.get(key).getAsString();
    }

    /** A namespaced ID such as "sofe:vorath". */
    private static String id(JsonObject obj, String key, String path) {
        String value = string(obj, key, path);
        if (!value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            throw error(path, "\"" + key + "\" must be a namespaced ID like sofe:vorath, got \"" + value + "\"");
        }
        return value;
    }

    private static int integer(JsonObject obj, String key, String path) {
        JsonElement value = obj.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw error(path, "\"" + key + "\" must be a number");
        }
        return value.getAsInt();
    }

    private static int positive(JsonObject obj, String key, String path) {
        int value = integer(obj, key, path);
        if (value < 1) throw error(path, "\"" + key + "\" must be at least 1");
        return value;
    }

    private static IllegalArgumentException error(String path, String message) {
        return new IllegalArgumentException(path + ": " + message);
    }
}
