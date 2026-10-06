package com.sofe.quest;

/** Something the player did that may count toward a quest objective. */
public sealed interface QuestEvent {

    record Killed(String entity) implements QuestEvent {
    }

    record Talked(String npc) implements QuestEvent {
    }

    record EnteredRegion(String region) implements QuestEvent {
    }

    record At(int x, int z) implements QuestEvent {
    }

    /** @param total skills the player has learned so far (rank 1 or more) */
    record SkillsLearned(int total) implements QuestEvent {
    }

    record LevelReached(int level) implements QuestEvent {
    }

    record BossDefeated(String boss) implements QuestEvent {
    }

    /** The ids of the items the player carries, sent every second while a quest asks for one. */
    record Carries(java.util.Set<String> items) implements QuestEvent {
    }
}
