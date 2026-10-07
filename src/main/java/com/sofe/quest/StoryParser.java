package com.sofe.quest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sofe.condition.Condition;
import com.sofe.condition.ConditionParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Reads quests and dialogues from JSON. Plain Java; errors name the file part that is wrong. */
public final class StoryParser {

    private StoryParser() {
    }

    public static QuestDefinition quest(String id, JsonObject json) {
        QuestDefinition.Type type = QuestDefinition.Type.valueOf(str(json, "type", "main").toUpperCase(Locale.ROOT));
        int act = json.has("act") ? json.get("act").getAsInt() : 1;
        List<QuestDefinition.Step> steps = new ArrayList<>();
        for (JsonElement e : array(json, "steps")) {
            JsonObject step = e.getAsJsonObject();
            QuestDefinition.Target target = null;
            if (step.has("target")) {
                JsonObject t = step.getAsJsonObject("target");
                target = new QuestDefinition.Target(t.get("x").getAsInt(), t.get("z").getAsInt());
            }
            Objective objective = objective(step.getAsJsonObject("objective"));
            QuestDefinition.Invasion invasion = step.has("invasion") ? invasion(step.getAsJsonObject("invasion")) : null;
            if (invasion != null && (!(objective instanceof Objective.Kill kill) || kill.count() != invasion.total())) {
                throw new IllegalArgumentException("an invasion's waves must add up to its step's kill count (" + invasion.total() + ")");
            }
            QuestDefinition.Lair lair = null;
            if (step.has("lair")) {
                JsonObject l = step.getAsJsonObject("lair");
                if (!(objective instanceof Objective.Kill kill) || !kill.entity().startsWith(Objective.LORD)) {
                    throw new IllegalArgumentException("a lair's step must ask to kill its \"" + Objective.LORD + "\"");
                }
                lair = new QuestDefinition.Lair(l.get("x").getAsInt(), l.get("z").getAsInt(), l.has("depth") ? l.get("depth").getAsInt() : 12,
                        str(l, "name", null));
            }
            steps.add(new QuestDefinition.Step(objective, effects(step, "on_start"), target, invasion, lair));
        }
        Condition requires = json.has("requires") ? ConditionParser.parse(json.get("requires")) : null;
        QuestDefinition.Discovery discovery = null;
        if (json.has("starts_near")) {
            JsonObject near = json.getAsJsonObject("starts_near");
            discovery = new QuestDefinition.Discovery(near.get("x").getAsInt(), near.get("z").getAsInt(),
                    near.has("radius") ? near.get("radius").getAsInt() : 48);
        }
        return new QuestDefinition(id, type, act, steps, effects(json, "rewards"), requires, discovery);
    }

    static QuestDefinition.Invasion invasion(JsonObject json) {
        QuestDefinition.Target center = null;
        if (json.has("center")) {
            JsonObject c = json.getAsJsonObject("center");
            center = new QuestDefinition.Target(c.get("x").getAsInt(), c.get("z").getAsInt());
        }
        java.util.Map<String, QuestDefinition.Target> points = new java.util.HashMap<>();
        if (json.has("points")) {
            for (var e : json.getAsJsonObject("points").entrySet()) {
                if (!QuestDefinition.Invasion.SIDES.contains(e.getKey())) throw new IllegalArgumentException("unknown side \"" + e.getKey() + "\"");
                JsonObject p = e.getValue().getAsJsonObject();
                points.put(e.getKey(), new QuestDefinition.Target(p.get("x").getAsInt(), p.get("z").getAsInt()));
            }
        }
        List<String> mobs = new ArrayList<>();
        if (json.has("mobs")) json.getAsJsonArray("mobs").forEach(m -> mobs.add(m.getAsString()));
        else mobs.addAll(List.of("sofe:void_wretch", "sofe:void_stalker"));
        List<QuestDefinition.Wave> waves = new ArrayList<>();
        for (JsonElement e : array(json, "waves")) {
            JsonObject w = e.getAsJsonObject();
            List<String> from = new ArrayList<>();
            w.getAsJsonArray("from").forEach(s -> from.add(s.getAsString()));
            waves.add(new QuestDefinition.Wave(from, w.get("count").getAsInt()));
        }
        return new QuestDefinition.Invasion(center, json.has("reach") ? json.get("reach").getAsInt() : 48,
                json.has("distance") ? json.get("distance").getAsInt() : 22, points, mobs, waves);
    }

    public static DialogueDefinition dialogue(String id, JsonObject json) {
        List<DialogueDefinition.Line> lines = new ArrayList<>();
        for (JsonElement e : array(json, "lines")) {
            JsonObject line = e.getAsJsonObject();
            List<DialogueDefinition.Answer> answers = new ArrayList<>();
            if (line.has("answers")) {
                for (JsonElement a : line.getAsJsonArray("answers")) {
                    JsonObject answer = a.getAsJsonObject();
                    answers.add(new DialogueDefinition.Answer(str(answer, "text", null),
                            answer.has("next") ? answer.get("next").getAsInt() : -1, effects(answer, "effects")));
                }
            }
            Condition condition = line.has("condition") ? ConditionParser.parse(line.get("condition")) : null;
            lines.add(new DialogueDefinition.Line(str(line, "speaker", "narrator"), str(line, "text", null), condition, answers, effects(line, "effects")));
        }
        Condition requires = json.has("requires") ? ConditionParser.parse(json.get("requires")) : null;
        return new DialogueDefinition(id, str(json, "style", "sulthari"), json.has("cinematic") && json.get("cinematic").getAsBoolean(),
                json.has("npc") ? json.get("npc").getAsString() : null, requires, json.has("priority") ? json.get("priority").getAsInt() : 0, lines,
                effects(json, "on_end"));
    }

    public static Objective objective(JsonObject json) {
        String type = str(json, "type", null);
        return switch (type) {
            case "kill" -> new Objective.Kill(str(json, "entity", null), json.has("count") ? json.get("count").getAsInt() : 1);
            case "talk" -> new Objective.Talk(str(json, "npc", null));
            case "reach_region" -> new Objective.ReachRegion(str(json, "region", null));
            case "reach" -> new Objective.Reach(json.get("x").getAsInt(), json.get("z").getAsInt(),
                    json.has("radius") ? json.get("radius").getAsInt() : 8);
            case "learn_skills" -> new Objective.LearnSkills(json.get("count").getAsInt());
            case "reach_level" -> new Objective.ReachLevel(json.get("level").getAsInt());
            case "defeat_boss" -> new Objective.DefeatBoss(str(json, "boss", null));
            case "manual" -> new Objective.Manual();
            case "obtain_item" -> new Objective.Obtain(str(json, "item", null));
            case "solve_puzzle" -> new Objective.SolvePuzzle(str(json, "puzzle", null));
            default -> throw new IllegalArgumentException("unknown objective type \"" + type + "\"");
        };
    }

    public static List<QuestEffect> effects(JsonObject parent, String key) {
        List<QuestEffect> result = new ArrayList<>();
        if (!parent.has(key)) return result;
        for (JsonElement e : parent.getAsJsonArray(key)) {
            JsonObject effect = e.getAsJsonObject();
            String type = str(effect, "type", null);
            result.add(switch (type) {
                case "start_quest" -> new QuestEffect.StartQuest(str(effect, "quest", null));
                case "advance_quest" -> new QuestEffect.AdvanceQuest(str(effect, "quest", null));
                case "set_fate" -> new QuestEffect.SetFate(str(effect, "region", null), str(effect, "fate", null));
                case "advance_act" -> new QuestEffect.AdvanceAct(effect.get("act").getAsInt());
                case "give_xp" -> new QuestEffect.GiveXp(effect.get("amount").getAsLong());
                case "spawn" -> new QuestEffect.Spawn(str(effect, "entity", null),
                        effect.has("count") ? effect.get("count").getAsInt() : 1, effect.has("radius") ? effect.get("radius").getAsDouble() : 8,
                        effect.has("elite") && effect.get("elite").getAsBoolean());
                case "open_dialogue" -> new QuestEffect.OpenDialogue(str(effect, "dialogue", null));
                case "give_item" -> new QuestEffect.GiveItem(str(effect, "item", null), effect.has("count") ? effect.get("count").getAsInt() : 1);
                case "open_class_select" -> new QuestEffect.OpenClassSelect();
                case "give_relic" -> new QuestEffect.GiveRelic(str(effect, "relic", null));
                case "hire_companion" -> new QuestEffect.HireCompanion(str(effect, "bearer", null));
                case "hire_soldier" -> new QuestEffect.HireSoldier(str(effect, "empire", null), str(effect, "rank", "soldier"));
                case "revive_soldiers" -> new QuestEffect.ReviveSoldiers();
                case "companion_order" -> new QuestEffect.CompanionOrder(str(effect, "order", null));
                case "play_sound" -> new QuestEffect.PlaySound(str(effect, "sound", null),
                        effect.has("volume") ? effect.get("volume").getAsFloat() : 1f, effect.has("pitch") ? effect.get("pitch").getAsFloat() : 1f);
                case "particles" -> new QuestEffect.Particles(str(effect, "particle", null), effect.has("count") ? effect.get("count").getAsInt() : 20);
                case "award_advancement" -> new QuestEffect.AwardAdvancement(str(effect, "advancement", null));
                case "play_scene" -> new QuestEffect.PlayScene(str(effect, "scene", null), effect.has("then") ? str(effect, "then", null) : null);
                default -> throw new IllegalArgumentException("unknown effect type \"" + type + "\"");
            });
        }
        return result;
    }

    private static JsonArray array(JsonObject json, String key) {
        if (!json.has(key) || !json.get(key).isJsonArray()) throw new IllegalArgumentException("missing \"" + key + "\" array");
        return json.getAsJsonArray(key);
    }

    private static String str(JsonObject json, String key, String fallback) {
        if (json.has(key)) return json.get(key).getAsString();
        if (fallback == null) throw new IllegalArgumentException("missing \"" + key + "\"");
        return fallback;
    }
}
