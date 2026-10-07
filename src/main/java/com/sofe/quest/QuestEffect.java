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
    /** @param elite each one an elite of the player's act (a ruin's guardian) */
    record Spawn(String entity, int count, double radius, boolean elite) implements QuestEffect {
        public Spawn(String entity, int count, double radius) {
            this(entity, count, radius, false);
        }
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
    /** Hires a soldier or an archer of an empire's army into the player's company (a captain's dialogue). */
    record HireSoldier(String empire, String rank) implements QuestEffect {
    }

    /** Brings back the fallen of the player's company (a captain's dialogue). */
    record ReviveSoldiers() implements QuestEffect {
    }

    record HireCompanion(String bearer) implements QuestEffect {
    }

    /** An order to the player's companion: follow, stay or dismiss. */
    record CompanionOrder(String order) implements QuestEffect {
    }

    /** Opens the Bearer selection for a player who has not chosen yet (end of the festival intro). */
    record OpenClassSelect() implements QuestEffect {
    }

    /** Grants sofe:&lt;advancement&gt; (the hidden ones only a choice can earn). */
    record AwardAdvancement(String advancement) implements QuestEffect {
    }

    /**
     * Plays a scene on the player's own screen (com.sofe.quest.SceneService), then opens the dialogue {@code then}
     * (null: none). The player cannot be hurt while it plays.
     */
    record PlayScene(String scene, String then) implements QuestEffect {
    }
}
