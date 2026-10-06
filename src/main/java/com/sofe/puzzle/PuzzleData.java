package com.sofe.puzzle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** The rune puzzles standing in the world: where their stones and tablet are, and which runes burn now. */
public final class PuzzleData extends SavedData {
    private static final String NAME = "sofe_puzzles";

    public static final class Placed {
        final String id;
        final List<BlockPos> stones;
        final BlockPos tablet;
        PuzzleLogic.State state;
        /** When a solved puzzle goes dark again for the next Bearer (game time), or 0. */
        long resetAt;

        Placed(String id, List<BlockPos> stones, BlockPos tablet, PuzzleLogic.State state, long resetAt) {
            this.id = id;
            this.stones = List.copyOf(stones);
            this.tablet = tablet;
            this.state = state;
            this.resetAt = resetAt;
        }
    }

    private final Map<String, Placed> placed = new HashMap<>();

    public static PuzzleData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(PuzzleData::load, PuzzleData::new, NAME);
    }

    /** A puzzle placed over the stones of another takes its place: a stone belongs to one puzzle only. */
    void put(Placed p) {
        placed.values().removeIf(other -> !other.id.equals(p.id) && (p.stones.contains(other.tablet) || other.stones.stream().anyMatch(
                s -> p.stones.contains(s) || s.equals(p.tablet))));
        placed.put(p.id, p);
        setDirty();
    }

    /** Where a placed puzzle's stones stand, left to right (for GameTests and admin tools). */
    public List<BlockPos> stones(String id) {
        Placed p = placed.get(id);
        return p == null ? List.of() : p.stones;
    }

    Optional<Placed> at(BlockPos pos) {
        return placed.values().stream().filter(p -> p.tablet.equals(pos) || p.stones.contains(pos)).findFirst();
    }

    Iterable<Placed> all() {
        return placed.values();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Placed p : placed.values()) {
            CompoundTag t = new CompoundTag();
            t.putString("id", p.id);
            ListTag stones = new ListTag();
            for (BlockPos s : p.stones) stones.add(NbtUtils.writeBlockPos(s));
            t.put("stones", stones);
            t.put("tablet", NbtUtils.writeBlockPos(p.tablet));
            byte[] lit = new byte[p.state.lit.length];
            for (int i = 0; i < lit.length; i++) lit[i] = (byte) (p.state.lit[i] ? 1 : 0);
            t.putByteArray("lit", lit);
            t.putInt("progress", p.state.progress);
            t.putLong("reset", p.resetAt);
            list.add(t);
        }
        tag.put("puzzles", list);
        return tag;
    }

    private static PuzzleData load(CompoundTag tag) {
        PuzzleData data = new PuzzleData();
        ListTag list = tag.getList("puzzles", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            List<BlockPos> stones = new ArrayList<>();
            ListTag s = t.getList("stones", Tag.TAG_COMPOUND);
            for (int k = 0; k < s.size(); k++) stones.add(NbtUtils.readBlockPos(s.getCompound(k)));
            byte[] raw = t.getByteArray("lit");
            boolean[] lit = new boolean[raw.length];
            for (int k = 0; k < raw.length; k++) lit[k] = raw[k] != 0;
            data.placed.put(t.getString("id"), new Placed(t.getString("id"), stones, NbtUtils.readBlockPos(t.getCompound("tablet")),
                    new PuzzleLogic.State(lit, t.getInt("progress")), t.getLong("reset")));
        }
        return data;
    }
}
