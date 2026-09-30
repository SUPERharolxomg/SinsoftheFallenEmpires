package com.sofe.story;

import net.minecraft.world.entity.player.Player;

/**
 * The act a player is in (1 to 5). Placeholder until StoryProgress arrives with the main quests
 * in Sprint 4: every player is in Act I, so only the region ranges set mob levels for now.
 */
public final class StoryAct {

    private StoryAct() {
    }

    public static int of(Player player) {
        return 1;
    }
}
