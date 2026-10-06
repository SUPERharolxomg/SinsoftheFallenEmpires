package com.sofe.client;

import com.sofe.network.SyncPactPacket;

import java.util.List;

/** The local player's Pact as last sent by the server (empty when in none). */
public final class ClientPactData {
    private static List<SyncPactPacket.Member> members = List.of();

    private ClientPactData() {
    }

    public static void update(SyncPactPacket packet) {
        members = List.copyOf(packet.members());
    }

    public static List<SyncPactPacket.Member> members() {
        return members;
    }

    public static void clear() {
        members = List.of();
    }
}
