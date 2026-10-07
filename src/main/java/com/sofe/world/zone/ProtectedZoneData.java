package com.sofe.world.zone;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

/**
 * The protected zones of one world, saved with it (data/sofe_protected_zones.dat). They are copied
 * from structure_positions.json when a journey starts for the first time, so a later mod update
 * that moves a structure never changes the zones of an existing world.
 */
public class ProtectedZoneData extends SavedData {
    private static final String NAME = "sofe_protected_zones";
    private final List<ProtectedZone> zones = new ArrayList<>();
    /** Blocks the players set in a protected place (a crafting table in a house): theirs to take away again. */
    private final java.util.Set<Long> placedByPlayers = new java.util.HashSet<>();
    private boolean initialized;

    public static ProtectedZoneData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(ProtectedZoneData::load, ProtectedZoneData::new, NAME);
    }

    public List<ProtectedZone> zones() {
        return List.copyOf(zones);
    }

    public boolean initialized() {
        return initialized;
    }

    /**
     * Adds the zones of the layout this world does not have yet (a new world, or structures added by a
     * mod update). Zones already saved are never moved, so an update cannot shift an existing city.
     */
    public int addMissing(List<ProtectedZone> fromLayout) {
        int added = 0;
        for (ProtectedZone zone : fromLayout) {
            if (zones.stream().noneMatch(z -> z.id().equals(zone.id()))) {
                zones.add(zone);
                added++;
            }
        }
        if (added > 0 || !initialized) {
            initialized = true;
            setDirty();
        }
        return added;
    }

    /** Structures placed later (camps, dungeons) register their own zones here. */
    public void add(ProtectedZone zone) {
        zones.removeIf(z -> z.id().equals(zone.id()));
        zones.add(zone);
        setDirty();
    }

    public void remove(String id) {
        if (zones.removeIf(z -> z.id().equals(id))) setDirty();
    }

    public void markPlaced(net.minecraft.core.BlockPos pos) {
        if (placedByPlayers.add(pos.asLong())) setDirty();
    }

    /** Whether a player set this block; true once, as it is taken away. */
    public boolean takePlaced(net.minecraft.core.BlockPos pos) {
        boolean placed = placedByPlayers.remove(pos.asLong());
        if (placed) setDirty();
        return placed;
    }

    public boolean placedByPlayer(net.minecraft.core.BlockPos pos) {
        return placedByPlayers.contains(pos.asLong());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (ProtectedZone z : zones) {
            CompoundTag t = new CompoundTag();
            t.putString("id", z.id());
            t.putString("kind", z.kind().id());
            t.putIntArray("box", new int[]{z.minX(), z.minY(), z.minZ(), z.maxX(), z.maxY(), z.maxZ()});
            list.add(t);
        }
        tag.put("zones", list);
        tag.putLongArray("placed_by_players", placedByPlayers.stream().mapToLong(Long::longValue).toArray());
        tag.putBoolean("initialized", initialized);
        return tag;
    }

    private static ProtectedZoneData load(CompoundTag tag) {
        ProtectedZoneData data = new ProtectedZoneData();
        ListTag list = tag.getList("zones", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            int[] b = t.getIntArray("box");
            if (b.length != 6) continue;
            data.zones.add(new ProtectedZone(t.getString("id"), ProtectedZone.Kind.byId(t.getString("kind")), b[0], b[1], b[2], b[3], b[4], b[5]));
        }
        for (long pos : tag.getLongArray("placed_by_players")) data.placedByPlayers.add(pos);
        data.initialized = tag.getBoolean("initialized");
        return data;
    }
}
