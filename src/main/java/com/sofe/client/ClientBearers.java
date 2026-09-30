package com.sofe.client;

import com.sofe.player.PlayerClass;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** The Bearer of every player this client can see, for their outfit layer. */
public final class ClientBearers {
    private static final Map<Integer, PlayerClass> BY_ENTITY = new ConcurrentHashMap<>();

    private ClientBearers() {
    }

    public static void set(int entityId, Optional<PlayerClass> playerClass) {
        if (playerClass.isPresent()) BY_ENTITY.put(entityId, playerClass.get());
        else BY_ENTITY.remove(entityId);
    }

    public static Optional<PlayerClass> of(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && player.getId() == minecraft.player.getId()) return ClientClassData.get();
        return Optional.ofNullable(BY_ENTITY.get(player.getId()));
    }

    public static void clear() {
        BY_ENTITY.clear();
    }
}
