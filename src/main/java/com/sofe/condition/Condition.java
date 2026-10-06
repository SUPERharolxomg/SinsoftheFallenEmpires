package com.sofe.condition;

import java.util.List;

/**
 * A progression requirement (docs/Mundo.md, "Conditions"). Composite: the combinators
 * hold other conditions, so any lock can be described in JSON without code.
 */
public sealed interface Condition {

    boolean test(ProgressView progress);

    record ActReached(int act) implements Condition {
        @Override
        public boolean test(ProgressView progress) {
            return progress.act() >= act;
        }
    }

    record BossDefeated(String boss) implements Condition {
        @Override
        public boolean test(ProgressView progress) {
            return progress.hasDefeated(boss);
        }
    }

    record QuestStep(String quest, int step) implements Condition {
        @Override
        public boolean test(ProgressView progress) {
            return progress.questStep(quest) >= step;
        }
    }

    /** A rune puzzle solved by the player: the seals before the bosses open this way. */
    record PuzzleSolved(String puzzle) implements Condition {
        @Override
        public boolean test(ProgressView progress) {
            return progress.hasSolved(puzzle);
        }
    }

    record ItemOwned(String item, int count) implements Condition {
        @Override
        public boolean test(ProgressView progress) {
            return progress.countItem(item) >= count;
        }
    }

    /** A region's fate chosen in a side quest (docs/Jugabilidad.md, "Fates and epilogues"). */
    record FateIs(String region, String fate) implements Condition {
        @Override
        public boolean test(ProgressView progress) {
            return fate.equals(progress.fate(region));
        }
    }

    /** The player's Bearer, for scenes that change with the hero (the King's variant of the Council). */
    record ClassIs(String playerClass) implements Condition {
        @Override
        public boolean test(ProgressView progress) {
            return playerClass.equals(progress.playerClass());
        }
    }

    record AllOf(List<Condition> conditions) implements Condition {
        public AllOf {
            conditions = List.copyOf(conditions);
        }

        @Override
        public boolean test(ProgressView progress) {
            return conditions.stream().allMatch(c -> c.test(progress));
        }
    }

    record AnyOf(List<Condition> conditions) implements Condition {
        public AnyOf {
            conditions = List.copyOf(conditions);
        }

        @Override
        public boolean test(ProgressView progress) {
            return conditions.stream().anyMatch(c -> c.test(progress));
        }
    }

    record Not(Condition condition) implements Condition {
        @Override
        public boolean test(ProgressView progress) {
            return !condition.test(progress);
        }
    }
}
