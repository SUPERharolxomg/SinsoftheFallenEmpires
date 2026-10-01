package com.sofe.world.build;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.Random;

import static com.sofe.world.build.SultharisBuilder.set;

/**
 * The shared pieces of the cities' architecture (art/concepts/city_*.png): domes with ribs and a
 * finial, minarets, crenellated walls and towers, houses with gabled roofs or parapets, market stalls
 * and trees. The empire builders compose them with their own palettes.
 */
final class Architecture {
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private Architecture() {
    }

    // --- domes

    /**
     * A dome of radius r standing on height y: an ellipsoid shell (a little less tall than wide) with
     * 8 ribs, a ring at its foot and a finial on top. Inside stays hollow so the hall below sees it.
     * Returns the height of its crown (the block under the finial).
     */
    static int dome(ServerLevel level, int cx, int y, int cz, int r, BlockState shell, BlockState rib, BlockState finial) {
        int height = Math.max(2, Math.round(r * 0.95f));
        for (int dy = 0; dy <= height; dy++) {
            double radius = radiusAt(r, height, dy), above = radiusAt(r, height, dy + 1);
            int ri = (int) Math.ceil(radius);
            for (int dx = -ri; dx <= ri; dx++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > radius + 0.3) continue;
                    boolean surface = d > radius - 1.3 || d > above - 0.5 || dy == height;
                    if (!surface) {
                        set(level, cx + dx, y + dy, cz + dz, AIR);
                        continue;
                    }
                    double angle = Math.atan2(dz, dx);
                    boolean onRib = Math.abs(Math.sin(angle * 4)) < 0.16 && d > 1.5;
                    set(level, cx + dx, y + dy, cz + dz, dy == 0 || onRib ? rib : shell);
                }
            }
        }
        set(level, cx, y + height + 1, cz, rib);
        set(level, cx, y + height + 2, cz, finial);
        return y + height;
    }

    /**
     * A chandelier: five lanterns in a cross, each hanging on its own chain from height top, the one
     * in the middle lowest, at height bottom. Light enough for a hall, and light to look at.
     */
    static void chandelier(ServerLevel level, int x, int top, int bottom, int z) {
        BlockState chain = Blocks.CHAIN.defaultBlockState();
        BlockState lantern = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        for (int y = top; y > bottom; y--) set(level, x, y, z, chain);
        set(level, x, bottom, z, lantern);
        for (Direction d : Direction.Plane.HORIZONTAL) {
            int ax = x + d.getStepX() * 2, az = z + d.getStepZ() * 2;
            for (int y = top; y > bottom + 1; y--) set(level, ax, y, az, chain);
            set(level, ax, bottom + 1, az, lantern);
        }
    }

    /**
     * A grand chandelier for a dome: a ring of lanterns on chains around a central one, with a gold
     * crown at the top of the chains.
     */
    static void grandChandelier(ServerLevel level, int x, int top, int bottom, int z, int radius) {
        BlockState chain = Blocks.CHAIN.defaultBlockState();
        BlockState lantern = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        for (int y = top; y > bottom; y--) set(level, x, y, z, chain);
        set(level, x, bottom, z, lantern);
        int ringTop = Math.max(bottom + 3, top - 4);
        for (int deg = 0; deg < 360; deg += 45) {
            int rx = x + (int) Math.round(radius * Math.cos(Math.toRadians(deg)));
            int rz = z + (int) Math.round(radius * Math.sin(Math.toRadians(deg)));
            for (int y = top; y > bottom + 2; y--) set(level, rx, y, rz, chain);
            set(level, rx, bottom + 2, rz, lantern);
            if (deg % 90 == 0) set(level, (x + rx) / 2 + (rx > x ? 0 : 0), ringTop, (z + rz) / 2, Blocks.GOLD_BLOCK.defaultBlockState());
        }
    }

    private static double radiusAt(int r, int height, int dy) {
        if (dy > height) return -1;
        double t = (double) dy / (height + 0.5);
        return r * Math.sqrt(Math.max(0, 1 - t * t));
    }

    /** A round drum under a dome: a wall of radius r and the given height with small windows. */
    static void drum(ServerLevel level, int cx, int y, int cz, int r, int height, BlockState wall, BlockState band) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r + 0.3) continue;
                for (int dy = 0; dy < height; dy++) {
                    if (d <= r - 1.0) {
                        set(level, cx + dx, y + dy, cz + dz, AIR);
                        continue;
                    }
                    double angle = Math.atan2(dz, dx);
                    boolean window = dy >= 1 && dy < height - 1 && Math.abs(Math.sin(angle * 6)) < 0.25;
                    set(level, cx + dx, y + dy, cz + dz, window ? Blocks.GLASS_PANE.defaultBlockState() : dy == height - 1 ? band : wall);
                }
            }
        }
    }

    /** A hole in a flat roof under a dome, so the dome is seen from the hall. */
    static void openRoof(ServerLevel level, int cx, int y, int cz, int r) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz <= (r - 1) * (r - 1)) set(level, cx + dx, y, cz + dz, AIR);
            }
        }
    }

    // --- minarets and towers

    /** A slender minaret: a round shaft with bands, a balcony at two thirds, a pointed cap and a spire. */
    static void minaret(ServerLevel level, int x, int y, int z, int height, BlockState shaft, BlockState band, BlockState cap) {
        int balcony = Math.round(height * 0.68f);
        for (int dy = 0; dy < height; dy++) {
            BlockState s = dy % 7 == 6 ? band : shaft;
            for (int[] p : new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}}) set(level, x + p[0], y + dy, z + p[1], s);
            if (dy < balcony) for (int[] p : new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}) set(level, x + p[0], y + dy, z + p[1], s);
        }
        // the balcony: a ring of slabs with a low wall
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > 3) continue;
                set(level, x + dx, y + balcony - 1, z + dz, band);
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) set(level, x + dx, y + balcony, z + dz, Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState());
            }
        }
        // the pointed cap
        for (int[] p : new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}}) set(level, x + p[0], y + height, z + p[1], cap);
        set(level, x, y + height + 1, z, cap);
        set(level, x, y + height + 2, z, cap);
        set(level, x, y + height + 3, z, Blocks.LIGHTNING_ROD.defaultBlockState());
    }

    /** A square tower with crenellations, hollow, standing on height y. */
    static void tower(ServerLevel level, int cx, int y, int cz, int half, int height, BlockState wall, BlockState trim) {
        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                boolean edge = Math.abs(dx) == half || Math.abs(dz) == half;
                for (int dy = 0; dy < height; dy++) set(level, cx + dx, y + dy, cz + dz, edge ? (dy % 8 == 7 ? trim : wall) : AIR);
                set(level, cx + dx, y + height, cz + dz, edge ? trim : wall); // the top floor
                if (edge && Math.floorMod(dx + dz, 2) == 0) set(level, cx + dx, y + height + 1, cz + dz, wall);
            }
        }
        // arrow slits
        for (int dy = 4; dy < height - 2; dy += 5) {
            set(level, cx + half, y + dy, cz, AIR);
            set(level, cx - half, y + dy, cz, AIR);
            set(level, cx, y + dy, cz + half, AIR);
            set(level, cx, y + dy, cz - half, AIR);
        }
    }

    /** A merlon on every other block: called for each block along the top of a wall. */
    static void merlon(ServerLevel level, int x, int y, int z, int along, BlockState state) {
        if (Math.floorMod(along, 2) == 0) set(level, x, y, z, state);
    }

    static void banner(ServerLevel level, int x, int y, int z, Direction facing, Block banner) {
        set(level, x, y, z, banner.defaultBlockState().setValue(WallBannerBlock.FACING, facing));
    }

    // --- houses

    /** The look of one house, picked from an empire's palette. */
    record HouseStyle(BlockState wall, BlockState frame, BlockState roofStairs, BlockState roofSlab, BlockState door,
                      BlockState awning, boolean gable) {
    }

    /**
     * A house on the floor at height y, w by d, with a door on the given side, windows, a lantern and
     * either a gabled roof or a flat roof with a parapet.
     */
    static void house(ServerLevel level, int minX, int minZ, int w, int d, int y, int height, Direction doorSide, HouseStyle style) {
        int maxX = minX + w - 1, maxZ = minZ + d - 1;
        int doorX = (minX + maxX) / 2, doorZ = (minZ + maxZ) / 2;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean edgeX = x == minX || x == maxX, edgeZ = z == minZ || z == maxZ;
                set(level, x, y - 1, z, style.frame());
                for (int dy = 0; dy < height; dy++) {
                    if (!edgeX && !edgeZ) {
                        set(level, x, y + dy, z, AIR);
                        continue;
                    }
                    boolean corner = edgeX && edgeZ;
                    boolean window = !corner && dy >= 1 && dy <= 2 && (edgeX ? Math.floorMod(z - minZ, 3) == 2 : Math.floorMod(x - minX, 3) == 2);
                    BlockState s = corner || dy == height - 1 ? style.frame() : window ? Blocks.GLASS_PANE.defaultBlockState() : style.wall();
                    set(level, x, y + dy, z, s);
                }
            }
        }
        // the door and the awning over it
        int[] door = switch (doorSide) {
            case NORTH -> new int[]{doorX, minZ, doorX, minZ - 1};
            case SOUTH -> new int[]{doorX, maxZ, doorX, maxZ + 1};
            case WEST -> new int[]{minX, doorZ, minX - 1, doorZ};
            default -> new int[]{maxX, doorZ, maxX + 1, doorZ};
        };
        set(level, door[0], y, door[1], style.door().setValue(DoorBlock.FACING, doorSide.getOpposite()).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(level, door[0], y + 1, door[1], style.door().setValue(DoorBlock.FACING, doorSide.getOpposite()).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        set(level, door[2], y + 2, door[3], style.awning());
        if (doorSide.getAxis() == Direction.Axis.Z) {
            set(level, door[2] - 1, y + 2, door[3], style.awning());
            set(level, door[2] + 1, y + 2, door[3], style.awning());
        } else {
            set(level, door[2], y + 2, door[3] - 1, style.awning());
            set(level, door[2], y + 2, door[3] + 1, style.awning());
        }
        BlockState hanging = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        set(level, (minX + maxX) / 2, y + height - 1, (minZ + maxZ) / 2, hanging);
        set(level, minX + 1, y + height - 1, maxZ - 1, hanging);
        set(level, maxX - 1, y + height - 1, minZ + 1, hanging);
        if (doorSide.getAxis() == Direction.Axis.Z) {
            set(level, door[2] - 1, y + 1, door[3], hanging);
            set(level, door[2] + 1, y + 1, door[3], hanging);
        } else {
            set(level, door[2], y + 1, door[3] - 1, hanging);
            set(level, door[2], y + 1, door[3] + 1, hanging);
        }
        if (style.gable()) gableRoof(level, minX, minZ, maxX, maxZ, y + height, style);
        else flatRoof(level, minX, minZ, maxX, maxZ, y + height, style);
    }

    /** A gabled roof along the longer side, overhanging by one block, with walled gable ends. */
    static void gableRoof(ServerLevel level, int minX, int minZ, int maxX, int maxZ, int y, HouseStyle style) {
        // the wall plate: one row of frame on top of the walls, so no gap opens under the first row of the roof
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x == minX || x == maxX || z == minZ || z == maxZ) set(level, x, y, z, style.frame());
            }
        }
        boolean alongX = maxX - minX >= maxZ - minZ;
        int lo = alongX ? minZ - 1 : minX - 1, hi = alongX ? maxZ + 1 : maxX + 1;
        int from = alongX ? minX - 1 : minZ - 1, to = alongX ? maxX + 1 : maxZ + 1;
        for (int k = 0; lo + k <= hi - k; k++) {
            for (int a = from; a <= to; a++) {
                boolean end = a == from + 1 || a == to - 1; // the gable walls stand on the house's end walls
                if (lo + k == hi - k) {
                    place(level, alongX, a, y + k, lo + k, style.roofSlab());
                    continue;
                }
                Direction upLow = alongX ? Direction.SOUTH : Direction.EAST, upHigh = upLow.getOpposite();
                place(level, alongX, a, y + k, lo + k, style.roofStairs().setValue(StairBlock.FACING, upLow));
                place(level, alongX, a, y + k, hi - k, style.roofStairs().setValue(StairBlock.FACING, upHigh));
                if (end && k > 0) {
                    for (int b = lo + k + 1; b < hi - k; b++) place(level, alongX, a, y + k, b, style.frame());
                }
            }
        }
    }

    private static void place(ServerLevel level, boolean alongX, int a, int y, int b, BlockState state) {
        if (alongX) set(level, a, y, b, state);
        else set(level, b, y, a, state);
    }

    /** A flat roof with a parapet of alternating merlons. */
    static void flatRoof(ServerLevel level, int minX, int minZ, int maxX, int maxZ, int y, HouseStyle style) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean edge = x == minX || x == maxX || z == minZ || z == maxZ;
                set(level, x, y, z, edge ? style.frame() : style.roofSlab().setValue(SlabBlock.TYPE, SlabType.TOP));
                if (edge && Math.floorMod(x + z, 2) == 0) set(level, x, y + 1, z, style.wall());
            }
        }
    }

    // --- streets and gardens

    /** A market stall: four posts and a striped awning, with goods under it. */
    static void stall(ServerLevel level, int x, int y, int z, BlockState awningA, BlockState awningB, Random random) {
        for (int[] post : new int[][]{{0, 0}, {3, 0}, {0, 3}, {3, 3}}) {
            for (int dy = 0; dy < 3; dy++) set(level, x + post[0], y + dy, z + post[1], Blocks.SPRUCE_FENCE.defaultBlockState());
        }
        for (int dx = -1; dx <= 4; dx++) {
            for (int dz = 0; dz <= 3; dz++) set(level, x + dx, y + 3, z + dz, Math.floorMod(dx, 2) == 0 ? awningA : awningB);
        }
        Block[] goods = {Blocks.BARREL, Blocks.CHEST, Blocks.HAY_BLOCK, Blocks.PUMPKIN, Blocks.MELON, Blocks.COMPOSTER};
        set(level, x + 1, y, z + 1, goods[random.nextInt(goods.length)].defaultBlockState());
        set(level, x + 2, y, z + 1, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        set(level, x + 2, y + 1, z + 1, Blocks.FLOWER_POT.defaultBlockState());
    }

    /** A small acacia with a flat crown, like the trees of the desert cities. */
    static void acacia(ServerLevel level, int x, int y, int z, Random random) {
        int trunk = 3 + random.nextInt(2);
        for (int dy = 0; dy < trunk; dy++) set(level, x, y + dy, z, Blocks.ACACIA_LOG.defaultBlockState());
        BlockState leaves = Blocks.ACACIA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                set(level, x + dx, y + trunk, z + dz, leaves);
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) set(level, x + dx, y + trunk + 1, z + dz, leaves);
            }
        }
    }

    /** A chimney with a campfire on top: its smoke rises over the roofs. */
    static void chimney(ServerLevel level, int x, int y, int z, int height, BlockState brick) {
        for (int dy = 0; dy < height; dy++) set(level, x, y + dy, z, brick);
        set(level, x, y + height, z, Blocks.HAY_BLOCK.defaultBlockState()); // a signal fire: taller smoke
        set(level, x, y + height + 1, z, Blocks.CAMPFIRE.defaultBlockState());
    }
}
