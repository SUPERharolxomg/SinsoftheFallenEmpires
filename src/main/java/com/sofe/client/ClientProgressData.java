package com.sofe.client;

import com.sofe.network.SyncProgressPacket;

import java.util.Optional;

/** The local player's level and points as last sent by the server. */
public final class ClientProgressData {
    private static SyncProgressPacket state;

    private ClientProgressData() {
    }

    public static void update(SyncProgressPacket packet) {
        state = packet;
    }

    public static Optional<SyncProgressPacket> get() {
        return Optional.ofNullable(state);
    }

    public static void clear() {
        state = null;
    }
}
