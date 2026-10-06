package com.sofe.pact;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Players downed in a boss arena, waiting for an ally to lift them (docs/Anexos.md, A5: Revive). */
public final class Downed {
    static final Map<UUID, Integer> LEFT = new HashMap<>();

    private Downed() {
    }

    public static boolean isDowned(ServerPlayer player) {
        return LEFT.containsKey(player.getUUID());
    }
}
