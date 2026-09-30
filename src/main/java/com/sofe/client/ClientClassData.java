package com.sofe.client;

import com.sofe.player.PlayerClass;

import java.util.Optional;

/** The local player's Bearer as last sent by the server. */
public final class ClientClassData {
    private static Optional<PlayerClass> playerClass = Optional.empty();

    private ClientClassData() {
    }

    public static Optional<PlayerClass> get() {
        return playerClass;
    }

    static void set(Optional<PlayerClass> value) {
        playerClass = value;
    }
}
