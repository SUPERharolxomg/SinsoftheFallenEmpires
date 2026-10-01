package com.sofe.world.build;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.IntFunction;

import static com.sofe.world.build.SultharisBuilder.set;

/**
 * Dirt roads between the places of a city and out of its gates. A road follows the ground; where it
 * leaves a raised city it goes down in steps with stairs, and where it crosses water or runs above
 * the ground it becomes a causeway, filled from the bottom, with fences and lantern posts in the
 * city's colors on both sides.
 */
final class Roads {
    private static final BlockState PATH = Blocks.DIRT_PATH.defaultBlockState();
    private static final BlockState EDGE = Blocks.COARSE_DIRT.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    /** The look of a city's roads: the fences and posts of its causeways and the fill under them. */
    record Style(BlockState fence, BlockState post, BlockState fill, IntFunction<BlockState> sides) {
    }

    private Roads() {
    }

    /**
     * A straight road on flat ground inside a city, from (x0, z0) to (x1, z1) along one axis, its
     * walking surface on the blocks at height y - 1. Lantern posts stand along it every 20 blocks.
     */
    static void street(ServerLevel level, int x0, int z0, int x1, int z1, int width, Style style) {
        boolean alongX = z0 == z1;
        int from = alongX ? Math.min(x0, x1) : Math.min(z0, z1), to = alongX ? Math.max(x0, x1) : Math.max(z0, z1);
        int half = width / 2;
        for (int a = from; a <= to; a++) {
            for (int b = -half; b <= half; b++) {
                int x = alongX ? a : x0 + b, z = alongX ? z0 + b : a;
                int y = SultharisBuilder.surfaceY(level, x, z) - 1;
                if (!natural(level, x, y, z)) continue; // never over a building's floor
                set(level, x, y, z, Math.abs(b) == half ? EDGE : PATH);
            }
            if (Math.floorMod(a, 10) == 5) {
                int side = Math.floorMod(a, 20) == 5 ? half + 1 : -half - 1;
                int x = alongX ? a : x0 + side, z = alongX ? z0 + side : a;
                int y = SultharisBuilder.surfaceY(level, x, z);
                if (natural(level, x, y - 1, z)) lanternPost(level, x, y, z, style);
            }
        }
    }

    /**
     * A road leaving a gate at walking height top (the surface blocks at top - 1), going out in
     * direction out for length blocks. It goes down one block every 3, never below the ground or the
     * sea, and becomes a fenced causeway where it is raised or over water.
     */
    static void outbound(ServerLevel level, int x0, int z0, Direction out, int length, int top, int width, Style style) {
        int half = width / 2, sea = level.getSeaLevel();
        int previous = top - 1;
        for (int t = 1; t <= length; t++) {
            int cx = x0 + out.getStepX() * t, cz = z0 + out.getStepZ() * t;
            int ground = naturalTop(level, cx, cz);
            boolean water = ground < sea - 1 || !level.getFluidState(new BlockPos(cx, ground + 1, cz)).isEmpty();
            int walk = Math.max(Math.max(ground, water ? sea : ground), top - 1 - t / 3); // the surface block's height
            boolean raised = water || walk > ground + 1;
            for (int b = -half - 1; b <= half + 1; b++) {
                int x = cx + (out.getAxis() == Direction.Axis.X ? 0 : b), z = cz + (out.getAxis() == Direction.Axis.Z ? 0 : b);
                boolean side = Math.abs(b) == half + 1;
                if (side && !raised) continue;
                int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                for (int y = Math.min(floor, walk - 3); y < walk; y++) {
                    set(level, x, y, z, side || Math.abs(b) == half ? style.sides().apply(y) : style.fill());
                }
                set(level, x, walk, z, side ? style.sides().apply(walk) : Math.abs(b) == half ? EDGE : PATH);
                for (int y = walk + 1; y <= walk + 4; y++) set(level, x, y, z, AIR);
                if (side) {
                    boolean post = Math.floorMod(t, 6) == 0;
                    set(level, x, walk + 1, z, post ? style.post() : style.fence());
                    if (post) set(level, x, walk + 2, z, Blocks.LANTERN.defaultBlockState());
                }
                // a stair where the road steps down, so it can be walked without jumping
                if (walk < previous && !side) {
                    set(level, x, walk + 1, z, Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, out.getOpposite()));
                }
            }
            previous = walk;
        }
    }

    /** The height of the highest natural solid block in a column (ignoring plants and water). */
    private static int naturalTop(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z) - 1;
    }

    private static void lanternPost(ServerLevel level, int x, int y, int z, Style style) {
        set(level, x, y, z, style.post());
        set(level, x, y + 1, z, style.fence());
        set(level, x, y + 2, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
    }

    /** Ground a road may be laid on: not a building's floor. */
    static boolean natural(ServerLevel level, int x, int y, int z) {
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.SANDSTONE) || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PACKED_MUD) || state.is(Blocks.DIRT_PATH) || state.is(Blocks.GRAVEL)
                || state.is(net.minecraft.tags.BlockTags.TERRACOTTA) || state.is(Blocks.STONE);
    }

    static Direction facing(Direction out) {
        return out;
    }
}
