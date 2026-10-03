package com.sofe.quest;

/**
 * Something a quest step or a dialogue answer makes happen. Shared by quests and dialogue so a
 * conversation can start a quest, set a fate or open the next act (docs/Jugabilidad.md, G3).
 */
public sealed interface QuestEffect {

    record StartQuest(String quest) implements QuestEffect {
    }

    /** Finishes the current step of a quest (for steps completed by a conversation). */
    record AdvanceQuest(String quest) implements QuestEffect {
    }

    record SetFate(String region, String fate) implements QuestEffect {
    }

    record AdvanceAct(int act) implements QuestEffect {
    }

    record GiveXp(long amount) implements QuestEffect {
    }

    /** Spawns enemies around the player (the Void invasion, a boss). */
    record Spawn(String entity, int count, double radius) implements QuestEffect {
    }

    record OpenDialogue(String dialogue) implements QuestEffect {
    }

    record GiveItem(String item, int count) implements QuestEffect {
    }

    /** A sound for a scene (the shard striking, a boss waking), heard by the player and those nearby. */
    record PlaySound(String sound, float volume, float pitch) implements QuestEffect {
    }

    /** Particles around the player, for scenes. */
    record Particles(String particle, int count) implements QuestEffect {
    }

    /** Gives a Relic (a unique), bound to the player: a quest's reward. */
    record GiveRelic(String relic) implements QuestEffect {
    }

    /** Hires a Bearer as the player's companion (UC-22). */
    record HireCompanion(String bearer) implements QuestEffect {
    }

    /** An order to the player's companion: follow, stay or dismiss. */
    record CompanionOrder(String order) implements QuestEffect {
    }

    /** Opens the Bearer selection for a player who has not chosen yet (end of the festival intro). */
    record OpenClassSelect() implements QuestEffect {
    }
}
