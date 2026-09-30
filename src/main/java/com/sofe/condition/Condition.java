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

    record ItemOwned(String item, int count) implements Condition {
        @Override
        public boolean test(ProgressView progress) {
            return progress.countItem(item) >= count;
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
