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
}
