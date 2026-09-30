package com.sofe.quest;

import com.sofe.condition.Condition;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * A quest from data/sofe/quests/*.json (docs/Jugabilidad.md, G3). Every text is a lang key:
 * quest.sofe.&lt;name&gt; for the title and quest.sofe.&lt;name&gt;.step&lt;n&gt; for each step.
 */
public record QuestDefinition(String id, Type type, int act, List<Step> steps, List<QuestEffect> rewards, Condition requires) {

    public enum Type {
        MAIN, BEARER, SIDE;

        public String translationKey() {
            return "quest.sofe.type." + name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * One step: its objective, what happens when the player reaches it (enemies appear, a scene plays)
     * and where the Quest Compass points (null: the objective's own place, if it has one).
     */
    public record Step(Objective objective, List<QuestEffect> onStart, Target target) {
        public Step {
            onStart = List.copyOf(onStart);
        }

        public Optional<Target> compassTarget() {
            if (target != null) return Optional.of(target);
            if (objective instanceof Objective.Reach r) return Optional.of(new Target(r.x(), r.z()));
            return Optional.empty();
        }
    }

    /** A block position on the map, for the Quest Compass. */
    public record Target(int x, int z) {
    }

    public QuestDefinition {
        steps = List.copyOf(steps);
        rewards = List.copyOf(rewards);
        if (steps.isEmpty()) throw new IllegalArgumentException(id + ": a quest needs at least one step");
    }

    /** "sofe:act1_eclipse" -> "quest.sofe.act1_eclipse"; "sofe:side/embers" -> "quest.sofe.side.embers". */
    public static String translationKey(String id) {
        return "quest.sofe." + id.substring(id.indexOf(':') + 1).replace('/', '.');
    }

    public static String stepKey(String id, int step) {
        return translationKey(id) + ".step" + (step + 1);
    }

    public String translationKey() {
        return translationKey(id);
    }

    public String stepKey(int step) {
        return stepKey(id, step);
    }

    public Optional<Step> step(int index) {
        return index >= 0 && index < steps.size() ? Optional.of(steps.get(index)) : Optional.empty();
    }
}
