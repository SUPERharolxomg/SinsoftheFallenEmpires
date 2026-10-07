package com.sofe.quest;

import com.sofe.condition.Condition;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * A quest from data/sofe/quests/*.json (docs/Jugabilidad.md, G3). Every text is a lang key:
 * quest.sofe.&lt;name&gt; for the title and quest.sofe.&lt;name&gt;.step&lt;n&gt; for each step.
 */
public record QuestDefinition(String id, Type type, int act, List<Step> steps, List<QuestEffect> rewards, Condition requires,
                              Discovery discovery) {

    public QuestDefinition(String id, Type type, int act, List<Step> steps, List<QuestEffect> rewards, Condition requires) {
        this(id, type, act, steps, rewards, requires, null);
    }

    /** A quest that begins by itself when a Bearer comes this close to a place (a dungeon found on the way). */
    public record Discovery(int x, int z, int radius) {
    }

    public enum Type {
        MAIN, BEARER, SIDE, DUNGEON;

        public String translationKey() {
            return "quest.sofe.type." + name().toLowerCase(Locale.ROOT);
        }
    }

    /**
     * One step: its objective, what happens when the player reaches it (enemies appear, a scene plays)
     * and where the Quest Compass points (null: the objective's own place, if it has one).
     */
    public record Step(Objective objective, List<QuestEffect> onStart, Target target, Invasion invasion, Lair lair) {
        public Step {
            onStart = List.copyOf(onStart);
        }

        public Step(Objective objective, List<QuestEffect> onStart, Target target) {
            this(objective, onStart, target, null, null);
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

    /**
     * The Void attacks a place in waves (com.sofe.quest.VoidInvasion): each wave opens rifts on some of its sides and
     * the creatures pour out of them until the wave's count has fallen. The waves' counts add up to the step's kill count.
     *
     * @param center   where the place is (null: the step's target, or else where the Bearer stands when it begins)
     * @param reach    how close the Bearer must come for the rifts to open
     * @param distance how far from the center a side's rift opens, when the side has no point of its own
     * @param points   a side's own place ("east", "west", "north", "south"), such as a district with its garrison
     * @param mobs     the creatures that come out of the rifts
     */
    public record Invasion(Target center, int reach, int distance, java.util.Map<String, Target> points, List<String> mobs, List<Wave> waves) {
        public Invasion {
            points = java.util.Map.copyOf(points);
            mobs = List.copyOf(mobs);
            waves = List.copyOf(waves);
            if (waves.isEmpty() || mobs.isEmpty()) throw new IllegalArgumentException("an invasion needs its waves and its creatures");
        }

        /** Every side a wave can come from. */
        public static final List<String> SIDES = List.of("east", "west", "north", "south");

        public int total() {
            return waves.stream().mapToInt(Wave::count).sum();
        }

        /** The wave a count of fallen creatures is in (the last one once all have fallen). */
        public int waveAt(int fallen) {
            int sum = 0;
            for (int i = 0; i < waves.size(); i++) {
                sum += waves.get(i).count();
                if (fallen < sum) return i;
            }
            return waves.size() - 1;
        }

        /** How many must fall before this wave is over, counted from the start of the invasion. */
        public int endOf(int wave) {
            int sum = 0;
            for (int i = 0; i <= wave; i++) sum += waves.get(i).count();
            return sum;
        }

        /** Where a side's rift opens. */
        public Target point(Target at, String side) {
            Target own = points.get(side);
            if (own != null) return own;
            return switch (side) {
                case "east" -> new Target(at.x() + distance, at.z());
                case "west" -> new Target(at.x() - distance, at.z());
                case "north" -> new Target(at.x(), at.z() - distance);
                default -> new Target(at.x(), at.z() + distance);
            };
        }
    }

    /** One wave: the sides it comes from and how many come (shared among its rifts); in the last wave an elite leads each rift. */
    public record Wave(List<String> from, int count) {
        public Wave {
            from = List.copyOf(from);
            for (String side : from) {
                if (!Invasion.SIDES.contains(side)) throw new IllegalArgumentException("unknown side \"" + side + "\"");
            }
            if (from.isEmpty() || count < from.size()) throw new IllegalArgumentException("a wave needs a side and a creature for each");
        }
    }

    /**
     * The depths of a dungeon where its lord sleeps (com.sofe.quest.CryptLord): when the Bearer comes this close to the place,
     * this deep under the ground, the lord of the step's "lord:" kill rises, an elite with its own name.
     */
    public record Lair(int x, int z, int depth, String name) {
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
