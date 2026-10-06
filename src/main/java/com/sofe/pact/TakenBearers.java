package com.sofe.pact;

import com.sofe.player.PlayerClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** With uniqueBearersPerServer: which player is each Bearer on this server (data/sofe_taken_bearers.dat). */
public class TakenBearers extends SavedData {
    private static final String NAME = "sofe_taken_bearers";
    private final Map<PlayerClass, UUID> taken = new EnumMap<>(PlayerClass.class);

    public static TakenBearers get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TakenBearers::load, TakenBearers::new, NAME);
    }

    /** Whether the Bearer is someone else's. */
    public boolean takenByOther(PlayerClass bearer, UUID player) {
        UUID who = taken.get(bearer);
        return who != null && !who.equals(player);
    }

    public Optional<UUID> who(PlayerClass bearer) {
        return Optional.ofNullable(taken.get(bearer));
    }

    public void take(PlayerClass bearer, UUID player) {
        taken.put(bearer, player);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        taken.forEach((c, id) -> tag.putUUID(c.id(), id));
        return tag;
    }

    private static TakenBearers load(CompoundTag tag) {
        TakenBearers data = new TakenBearers();
        for (PlayerClass c : PlayerClass.values()) if (tag.hasUUID(c.id())) data.taken.put(c, tag.getUUID(c.id()));
        return data;
    }
}
