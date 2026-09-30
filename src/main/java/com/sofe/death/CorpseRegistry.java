package com.sofe.death;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Where every player's bodies lie, so the Quest Compass can point to the latest one even while
 * its chunk is unloaded. Saved with the world (data/sofe_corpses.dat).
 */
public class CorpseRegistry extends SavedData {
    private static final String NAME = "sofe_corpses";

    public record Corpse(UUID entity, ResourceKey<Level> dimension, BlockPos pos) {
    }

    private final Map<UUID, List<Corpse>> byOwner = new HashMap<>();

    public static CorpseRegistry get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(CorpseRegistry::load, CorpseRegistry::new, NAME);
    }

    public void add(UUID owner, UUID entity, ResourceKey<Level> dimension, BlockPos pos) {
        byOwner.computeIfAbsent(owner, o -> new ArrayList<>()).add(new Corpse(entity, dimension, pos.immutable()));
        setDirty();
    }

    public void remove(UUID owner, UUID entity) {
        List<Corpse> list = byOwner.get(owner);
        if (list != null && list.removeIf(c -> c.entity().equals(entity))) {
            if (list.isEmpty()) byOwner.remove(owner);
            setDirty();
        }
    }

    /** The most recent body of a player, the one the compass follows. */
    public Optional<Corpse> latest(UUID owner) {
        List<Corpse> list = byOwner.get(owner);
        return list == null || list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size() - 1));
    }

    public List<Corpse> all(UUID owner) {
        return List.copyOf(byOwner.getOrDefault(owner, List.of()));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag owners = new ListTag();
        byOwner.forEach((owner, list) -> list.forEach(c -> {
            CompoundTag t = new CompoundTag();
            t.putUUID("owner", owner);
            t.putUUID("entity", c.entity());
            t.putString("dimension", c.dimension().location().toString());
            t.putLong("pos", c.pos().asLong());
            owners.add(t);
        }));
        tag.put("corpses", owners);
        return tag;
    }

    private static CorpseRegistry load(CompoundTag tag) {
        CorpseRegistry data = new CorpseRegistry();
        ListTag list = tag.getList("corpses", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            ResourceLocation dim = ResourceLocation.tryParse(t.getString("dimension"));
            if (dim == null) continue;
            data.byOwner.computeIfAbsent(t.getUUID("owner"), o -> new ArrayList<>()).add(new Corpse(t.getUUID("entity"),
                    ResourceKey.create(Registries.DIMENSION, dim), BlockPos.of(t.getLong("pos"))));
        }
        return data;
    }
}
