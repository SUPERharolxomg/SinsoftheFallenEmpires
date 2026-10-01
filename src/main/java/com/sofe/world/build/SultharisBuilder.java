package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * The blockouts of Sulthari (docs/Mundo.md, W6): simple versions of the city's story places, made of
 * the Sulthari building blocks, that stand in until the hand-made templates exist ({@link StructureBuilder}).
 * Also holds the small helpers the other empires' blockouts share.
 */
public final class SultharisBuilder {
    private static final int CLEAR_ABOVE = 12, FOUNDATION = 12;

    private SultharisBuilder() {
    }

    /** The blockout of a Sulthari piece; false when the piece has none. */
    static boolean blockout(ServerLevel level, StructurePositions.Structure structure, String piece) {
        int y = surfaceY(level, structure.x(), structure.z());
        switch (piece) {
            case "sulthari/city" -> cityWall(level, structure);
            case "sulthari/plaza" -> plaza(level, structure, y);
            case "sulthari/palace" -> building(level, structure, y, 10, SoFEBlocks.SULTHARI_SANDSTONE_BRICKS.get(), true);
            case "sulthari/great_observatory" -> observatory(level, structure, y);
            case "sulthari/low_bazaar" -> bazaar(level, structure, y);
            case "sulthari/lower_district" -> district(level, structure, y);
            case "sulthari/training_grounds" -> trainingGrounds(level, structure, y);
            case "sulthari/forge" -> forge(level, structure, y);
            case "sulthari/bank" -> building(level, structure, y, 6, SoFEBlocks.SULTHARI_SANDSTONE_BRICKS.get(), false);
            case "sulthari/homestead" -> homestead(level, structure);
            default -> {
                return false;
            }
        }
        return true;
    }

    static int surfaceY(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    // --- blockouts

    static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    /** A flat floor at height y: fills the ground below and clears the air above. */
    static void pad(ServerLevel level, int minX, int minZ, int maxX, int maxZ, int y, BlockState floor) {
        BlockState fill = Blocks.SANDSTONE.defaultBlockState(), air = Blocks.AIR.defaultBlockState();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int dy = 1; dy <= FOUNDATION; dy++) {
                    BlockPos below = new BlockPos(x, y - 1 - dy, z);
                    if (level.getBlockState(below).isAir() || !level.getFluidState(below).isEmpty()) set(level, x, y - 1 - dy, z, fill);
                }
                set(level, x, y - 1, z, floor);
                for (int dy = 0; dy < CLEAR_ABOVE; dy++) set(level, x, y + dy, z, air);
            }
        }
    }

    static int half(int size) {
        return size / 2;
    }

    /** The walls of the city on the edge of its zone, following the ground, with a gate on each side. */
    private static void cityWall(ServerLevel level, StructurePositions.Structure city) {
        int minX = city.x() - half(city.sizeX()), maxX = city.x() + half(city.sizeX()) - 1;
        int minZ = city.z() - half(city.sizeZ()), maxZ = city.z() + half(city.sizeZ()) - 1;
        BlockState wall = SoFEBlocks.SULTHARI_SANDSTONE_BRICKS.get().defaultBlockState();
        BlockState top = SoFEBlocks.SULTHARI_BRASS_TRIM.get().defaultBlockState();
        for (int x = minX; x <= maxX; x++) {
            wallColumn(level, x, minZ, city.x(), wall, top, true);
            wallColumn(level, x, maxZ, city.x(), wall, top, true);
        }
        for (int z = minZ; z <= maxZ; z++) {
            wallColumn(level, minX, z, city.z(), wall, top, false);
            wallColumn(level, maxX, z, city.z(), wall, top, false);
        }
    }

    private static void wallColumn(ServerLevel level, int x, int z, int center, BlockState wall, BlockState top, boolean alongX) {
        int along = alongX ? x : z;
        if (Math.abs(along - center) <= 3) return; // gate
        int y = surfaceY(level, x, z);
        for (int dy = 0; dy < 7; dy++) set(level, x, y + dy, z, wall);
        set(level, x, y + 7, z, top);
        if (Math.floorMod(along, 12) == 0) set(level, x, y + 8, z, SoFEBlocks.SULTHARI_AETHERIUM_LAMP.get().defaultBlockState());
    }

    private static void plaza(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        pad(level, minX, minZ, maxX, maxZ, y, SoFEBlocks.SULTHARI_SANDSTONE_BRICKS.get().defaultBlockState());
        BlockState tiles = SoFEBlocks.SULTHARI_GLAZED_TILES.get().defaultBlockState();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (Math.floorMod(x - minX, 6) == 0 || Math.floorMod(z - minZ, 6) == 0) set(level, x, y - 1, z, tiles);
            }
        }
        // a fountain in the middle, lamps in the corners
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean rim = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                set(level, s.x() + dx, y, s.z() + dz, rim ? SoFEBlocks.SULTHARI_BRASS_PLATING.get().defaultBlockState() : Blocks.WATER.defaultBlockState());
            }
        }
        for (int[] c : new int[][]{{minX + 1, minZ + 1}, {maxX - 1, minZ + 1}, {minX + 1, maxZ - 1}, {maxX - 1, maxZ - 1}}) {
            set(level, c[0], y, c[1], SoFEBlocks.SULTHARI_BRASS_TRIM.get().defaultBlockState());
            set(level, c[0], y + 1, c[1], SoFEBlocks.SULTHARI_BRASS_TRIM.get().defaultBlockState());
            set(level, c[0], y + 2, c[1], SoFEBlocks.SULTHARI_AETHERIUM_LAMP.get().defaultBlockState());
        }
    }

    /** A walled building with a door facing the plaza (north when south of it, south when north). */
    private static void building(ServerLevel level, StructurePositions.Structure s, int y, int height, Block walls, boolean dome) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        pad(level, minX, minZ, maxX, maxZ, y, SoFEBlocks.SULTHARI_GLAZED_TILES.get().defaultBlockState());
        BlockState wall = walls.defaultBlockState(), trim = SoFEBlocks.SULTHARI_BRASS_TRIM.get().defaultBlockState();
        boolean doorNorth = s.z() > 0;
        int doorZ = doorNorth ? minZ : maxZ;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean edge = x == minX || x == maxX || z == minZ || z == maxZ;
                if (!edge) continue;
                boolean door = z == doorZ && Math.abs(x - s.x()) <= 1;
                for (int dy = 0; dy < height; dy++) {
                    if (door && dy < 4) continue;
                    boolean window = dy >= 2 && dy <= 3 && Math.floorMod(x + z, 5) == 0;
                    set(level, x, y + dy, z, window ? Blocks.GLASS_PANE.defaultBlockState() : wall);
                }
                set(level, x, y + height, z, trim);
            }
        }
        // roof
        for (int x = minX + 1; x < maxX; x++) {
            for (int z = minZ + 1; z < maxZ; z++) {
                set(level, x, y + height, z, SoFEBlocks.SULTHARI_SANDSTONE_BRICK_SLAB.get().defaultBlockState());
            }
        }
        if (dome) {
            int r = Math.min(half(s.sizeX()), half(s.sizeZ())) / 2;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    for (int dy = 0; dy <= r; dy++) {
                        double d = Math.sqrt(dx * dx + dz * dz + dy * dy);
                        if (d <= r && d > r - 1.2) set(level, s.x() + dx, y + height + 1 + dy, s.z() + dz, SoFEBlocks.SULTHARI_BRASS_PLATING.get().defaultBlockState());
                    }
                }
            }
        }
        // lamps inside
        for (int x = minX + 3; x < maxX; x += 8) {
            for (int z = minZ + 3; z < maxZ; z += 8) set(level, x, y + height - 1, z, SoFEBlocks.SULTHARI_AETHERIUM_LAMP.get().defaultBlockState());
        }
    }

    /** A round tower with a brass dome and the great lens on top. */
    private static void observatory(ServerLevel level, StructurePositions.Structure s, int y) {
        int r = Math.min(half(s.sizeX()), half(s.sizeZ())) - 2;
        pad(level, s.x() - r - 2, s.z() - r - 2, s.x() + r + 2, s.z() + r + 2, y, SoFEBlocks.SULTHARI_GLAZED_TILES.get().defaultBlockState());
        int height = 26;
        BlockState wall = SoFEBlocks.SULTHARI_SANDSTONE_BRICKS.get().defaultBlockState();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r || d <= r - 1.2) continue;
                boolean door = dz > 0 && Math.abs(dx) <= 1; // the entrance faces the plaza, to the south
                for (int dy = 0; dy < height; dy++) {
                    if (door && dy < 4) continue;
                    boolean band = dy % 6 == 5;
                    set(level, s.x() + dx, y + dy, s.z() + dz, band ? SoFEBlocks.SULTHARI_BRASS_TRIM.get().defaultBlockState() : wall);
                }
            }
        }
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = 0; dy <= r; dy++) {
                    double d = Math.sqrt(dx * dx + dz * dz + dy * dy);
                    if (d <= r && d > r - 1.2) set(level, s.x() + dx, y + height + dy, s.z() + dz, SoFEBlocks.SULTHARI_BRASS_PLATING.get().defaultBlockState());
                }
            }
        }
        set(level, s.x(), y + height + r + 1, s.z(), SoFEBlocks.SULTHARI_AETHERIUM_LAMP.get().defaultBlockState());
        for (int dy = 0; dy < height; dy += 5) set(level, s.x(), y + dy, s.z(), SoFEBlocks.SULTHARI_AETHERIUM_LAMP.get().defaultBlockState());
    }

    /** Market stalls with colored awnings. */
    private static void bazaar(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        pad(level, minX, minZ, maxX, maxZ, y, SoFEBlocks.SULTHARI_SANDSTONE_BRICKS.get().defaultBlockState());
        Block[] awnings = {Blocks.RED_WOOL, Blocks.ORANGE_WOOL, Blocks.YELLOW_WOOL, Blocks.CYAN_WOOL, Blocks.BLUE_WOOL};
        int i = 0;
        for (int x = minX + 3; x + 4 <= maxX; x += 9) {
            for (int z = minZ + 3; z + 4 <= maxZ; z += 10) {
                BlockState awning = awnings[i++ % awnings.length].defaultBlockState();
                for (int[] post : new int[][]{{x, z}, {x + 4, z}, {x, z + 4}, {x + 4, z + 4}}) {
                    for (int dy = 0; dy < 3; dy++) set(level, post[0], y + dy, post[1], Blocks.DARK_OAK_FENCE.defaultBlockState());
                }
                for (int dx = 0; dx <= 4; dx++) for (int dz = 0; dz <= 4; dz++) set(level, x + dx, y + 3, z + dz, awning);
                set(level, x + 2, y, z + 1, Blocks.BARREL.defaultBlockState());
            }
        }
    }

    /** Small houses in rows (the ordinary houses come from jigsaw pools with the final builds). */
    private static void district(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        for (int x = minX + 2; x + 7 <= maxX; x += 12) {
            for (int z = minZ + 2; z + 7 <= maxZ; z += 12) {
                StructurePositions.Structure house = new StructurePositions.Structure(s.id() + "/house", x + 3, z + 3, 7, 7, s.minY(), s.maxY(), null);
                building(level, house, surfaceY(level, x + 3, z + 3), 5, SoFEBlocks.SULTHARI_SANDSTONE_BRICKS.get(), false);
            }
        }
    }

    private static void trainingGrounds(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        pad(level, minX, minZ, maxX, maxZ, y, Blocks.PACKED_MUD.defaultBlockState());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean edge = x == minX || x == maxX || z == minZ || z == maxZ;
                boolean gate = z == minZ && Math.abs(x - s.x()) <= 1;
                if (edge && !gate) set(level, x, y, z, Blocks.DARK_OAK_FENCE.defaultBlockState());
            }
        }
        for (int x = minX + 6; x < maxX - 4; x += 8) {
            set(level, x, y, maxZ - 4, Blocks.HAY_BLOCK.defaultBlockState());
            set(level, x, y + 1, maxZ - 4, Blocks.TARGET.defaultBlockState());
        }
    }

    private static void forge(ServerLevel level, StructurePositions.Structure s, int y) {
        building(level, s, y, 6, SoFEBlocks.SULTHARI_BRASS_PLATING.get(), false);
        set(level, s.x() - 2, y, s.z(), Blocks.BLAST_FURNACE.defaultBlockState());
        set(level, s.x() + 2, y, s.z(), Blocks.ANVIL.defaultBlockState());
        set(level, s.x(), y, s.z() + 2, Blocks.SMITHING_TABLE.defaultBlockState());
    }

    /** The Homestead is left to the player: only corner posts with lamps mark the plot. */
    private static void homestead(ServerLevel level, StructurePositions.Structure s) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        for (int[] c : new int[][]{{minX, minZ}, {maxX, minZ}, {minX, maxZ}, {maxX, maxZ}}) {
            int y = surfaceY(level, c[0], c[1]);
            set(level, c[0], y, c[1], SoFEBlocks.SULTHARI_BRASS_TRIM.get().defaultBlockState());
            set(level, c[0], y + 1, c[1], SoFEBlocks.SULTHARI_AETHERIUM_LAMP.get().defaultBlockState());
        }
    }
}
