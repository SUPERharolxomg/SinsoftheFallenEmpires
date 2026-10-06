package com.sofe.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Where someone stands: the floor a person can walk to, never a roof, a dome or a balcony with no stairs.
 * Story NPCs, Waystones, quest enemies and bosses are all put down through here.
 */
public final class Grounding {
    /** How far below the top of a column a building's ground floor is looked for. */
    private static final int SCAN_DEPTH = 48;

    private Grounding() {
    }

    /**
     * The ground floor of this column: the lowest room of a building (the palace hall, not its dome or
     * gallery), or the ground itself. Scans down from the top until it is deep in the natural terrain
     * (three blocks of it in a row: a terracotta ceiling is not the ground), so caves never count.
     */
    public static BlockPos groundFloor(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, top, z);
        BlockPos lowest = null;
        int naturalRun = 0;
        for (int y = top - 1; y > top - SCAN_DEPTH && y > level.getMinBuildHeight() + 1; y--) {
            pos.setY(y);
            naturalRun = natural(level.getBlockState(pos)) ? naturalRun + 1 : 0;
            if (naturalRun >= 3) break;
            if (naturalRun == 0 && standable(level, pos)) lowest = pos.immutable();
        }
        return lowest != null ? lowest : new BlockPos(x, top, z);
    }

    /**
     * Where an enemy called near a player stands: the ground outdoors; under a roof the player's own
     * floor, so inside the Observatory the Sentinel wakes in the hall and not on the dome.
     */
    public static BlockPos beside(ServerLevel level, int x, int z, net.minecraft.world.entity.Entity player) {
        if (level.canSeeSky(player.blockPosition())) {
            level.getChunk(x >> 4, z >> 4);
            return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        }
        return near(level, x, player.getBlockY(), z, 8);
    }

    /**
     * The spot nearest to a height in this column where someone fits on a floor (the ground when there is none).
     */
    public static BlockPos near(ServerLevel level, int x, int y, int z, int range) {
        level.getChunk(x >> 4, z >> 4);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int d = 0; d <= range; d++) {
            for (int sign : d == 0 ? new int[]{1} : new int[]{1, -1}) {
                pos.set(x, y + d * sign, z);
                if (standable(level, pos)) return pos.immutable();
            }
        }
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
    }

    /** Two blocks to stand in (air, a carpet, grass...) above a floor. */
    public static boolean standable(ServerLevel level, BlockPos pos) {
        return passable(level, pos) && passable(level, pos.above()) && !passable(level, pos.below())
                && level.getFluidState(pos).isEmpty();
    }

    /** A carpet or a pressure plate is walked through, as air is. */
    private static boolean passable(ServerLevel level, BlockPos pos) {
        var shape = level.getBlockState(pos).getCollisionShape(level, pos);
        return shape.isEmpty() || shape.max(net.minecraft.core.Direction.Axis.Y) <= 0.1875;
    }

    private static boolean natural(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.TERRACOTTA) || state.is(Blocks.SANDSTONE) || state.is(Blocks.GRAVEL);
    }
}
