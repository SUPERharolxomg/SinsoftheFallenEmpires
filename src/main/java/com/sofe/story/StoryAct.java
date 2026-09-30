package com.sofe.story;

import net.minecraft.world.entity.player.Player;

/** The act a player is in (1 to 5), from their own story progress. */
public final class StoryAct {

    private StoryAct() {
    }

    public static int of(Player player) {
        return StoryCapability.get(player).map(StoryProgress::act).orElse(1);
    }
}
