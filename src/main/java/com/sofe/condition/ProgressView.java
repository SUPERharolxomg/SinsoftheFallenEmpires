package com.sofe.condition;

/**
 * What a condition can ask about one player. Implemented on top of the player's
 * story progress; kept as an interface so conditions can be tested without Minecraft.
 */
public interface ProgressView {
    /** Current act, 1 to 5. */
    int act();

    /** Whether this player has credit for the boss (e.g. "sofe:vorath"). */
    boolean hasDefeated(String bossId);

    /** Highest step reached in a quest; 0 when not started. */
    int questStep(String questId);

    /** How many of an item the player carries (e.g. "minecraft:ender_eye"). */
    int countItem(String itemId);

    /** The fate the player chose for a region (e.g. "sulthari" -> "bazaar"), or null. */
    default String fate(String region) {
        return null;
    }

    /** The player's Bearer class id (e.g. "king"), or null before choosing. */
    default String playerClass() {
        return null;
    }

    /** Whether the player has solved a rune puzzle (e.g. "sofe:nordrath_forge"). */
    default boolean hasSolved(String puzzleId) {
        return false;
    }
}
