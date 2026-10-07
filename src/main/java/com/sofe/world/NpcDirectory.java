package com.sofe.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Where each person of the story stands in the overworld, by NPC id: the Quest Compass leads to the one
 * the player must talk to even when they are far away and not loaded. Every NPC writes itself here when
 * it is placed or loaded.
 */
public final class NpcDirectory extends SavedData {
    private static final String NAME = "sofe_npc_directory";
    private final Map<String, BlockPos> places = new HashMap<>();

    public static NpcDirectory get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(NpcDirectory::load, NpcDirectory::new, NAME);
    }

    public void record(String npc, BlockPos pos) {
        if (npc.isEmpty() || pos.equals(places.get(npc))) return;
        places.put(npc, pos.immutable());
        setDirty();
    }

    public Optional<BlockPos> find(String npc) {
        return Optional.ofNullable(places.get(npc));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        places.forEach((npc, pos) -> tag.put(npc, NbtUtils.writeBlockPos(pos)));
        return tag;
    }

    private static NpcDirectory load(CompoundTag tag) {
        NpcDirectory data = new NpcDirectory();
        for (String npc : tag.getAllKeys()) data.places.put(npc, NbtUtils.readBlockPos(tag.getCompound(npc)));
        return data;
    }
}
