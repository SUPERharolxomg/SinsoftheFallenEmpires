package com.sofe.client;

import com.sofe.network.SyncStoryPacket;

import java.util.Optional;

/** The local player's story as last sent by the server: act, quests, fates and compass target. */
public final class ClientStoryData {
    private static SyncStoryPacket state;

    private ClientStoryData() {
    }

    public static void update(SyncStoryPacket packet) {
        Optional<SyncStoryPacket> before = get();
        state = packet;
        ClientLockData.storyChanged(before, packet);
    }

    public static Optional<SyncStoryPacket> get() {
        return Optional.ofNullable(state);
    }

    public static void clear() {
        state = null;
    }
}
