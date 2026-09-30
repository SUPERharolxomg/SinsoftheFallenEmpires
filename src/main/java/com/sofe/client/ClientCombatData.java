package com.sofe.client;

import com.sofe.combat.Rune;
import com.sofe.network.SyncCombatPacket;

import java.util.List;
import java.util.Optional;

/** The local player's combat state as last sent by the server; read by the HUD. */
public final class ClientCombatData {
    private static SyncCombatPacket state;

    private ClientCombatData() {
    }

    public static void update(SyncCombatPacket packet) {
        state = packet;
    }

    public static Optional<SyncCombatPacket> get() {
        return Optional.ofNullable(state);
    }

    public static List<Rune> runes() {
        return state == null ? List.of() : state.runes();
    }

    public static void clear() {
        state = null;
    }
}
