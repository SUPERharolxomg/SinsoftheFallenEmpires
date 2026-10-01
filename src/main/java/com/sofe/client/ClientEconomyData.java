package com.sofe.client;

import com.sofe.network.SyncEconomyPacket;

import java.util.Optional;

/** The local player's Dinars and Flask charges as last sent by the server. */
public final class ClientEconomyData {
    private static SyncEconomyPacket state;

    private ClientEconomyData() {
    }

    public static void update(SyncEconomyPacket packet) {
        state = packet;
    }

    public static Optional<SyncEconomyPacket> get() {
        return Optional.ofNullable(state);
    }

    public static void clear() {
        state = null;
    }
}
