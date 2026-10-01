package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Random;

/**
 * Sulthari, after its concept (art/concepts, the city on the terracotta mesa): walls of banded
 * terracotta on a stepped escarpment, crenellated in sandstone, with towers and red banners; the
 * palace with its golden dome, four red domes and four white minarets; the golden-domed Observatory;
 * houses in ochre, orange and white with timber roofs; the bazaar's striped awnings and acacias.
 * These stand in until hand-made templates exist ({@link StructureBuilder}). Also holds the small
 * helpers the other empires' builders share.
 */
public final class SultharisBuilder {
    private static final int CLEAR_ABOVE = 12, FOUNDATION = 12;

    private SultharisBuilder() {
    }

    /** The build of a Sulthari piece; false when the piece has none. */
    static boolean blockout(ServerLevel level, StructurePositions.Structure structure, String piece) {
        int y = surfaceY(level, structure.x(), structure.z());
        switch (piece) {
            case "sulthari/city" -> cityWall(level, structure);
            case "sulthari/plaza" -> plaza(level, structure, y);
            case "sulthari/palace" -> palace(level, structure, y);
            case "sulthari/great_observatory" -> observatory(level, structure, y);
            case "sulthari/low_bazaar" -> bazaar(level, structure, y);
            case "sulthari/lower_district" -> district(level, structure);
            case "sulthari/training_grounds" -> trainingGrounds(level, structure, y);
            case "sulthari/forge" -> forge(level, structure, y);
            case "sulthari/bank" -> bank(level, structure, y);
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

    // --- helpers

    static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    /** A flat floor at height y: fills the ground below and clears the air above. */
    static void pad(ServerLevel level, int minX, int minZ, int maxX, int maxZ, int y, BlockState floor) {
        pad(level, minX, minZ, maxX, maxZ, y, floor, CLEAR_ABOVE);
    }

    static void pad(ServerLevel level, int minX, int minZ, int maxX, int maxZ, int y, BlockState floor, int clear) {
        BlockState fill = Blocks.SANDSTONE.defaultBlockState(), air = Blocks.AIR.defaultBlockState();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int dy = 1; dy <= FOUNDATION; dy++) {
                    BlockPos below = new BlockPos(x, y - 1 - dy, z);
                    if (level.getBlockState(below).isAir() || !level.getFluidState(below).isEmpty()) set(level, x, y - 1 - dy, z, fill);
                }
                set(level, x, y - 1, z, floor);
                for (int dy = 0; dy < clear; dy++) set(level, x, y + dy, z, air);
            }
        }
    }

    static int half(int size) {
        return size / 2;
    }

    // --- palette

    private static BlockState bricks() {
        return SoFEBlocks.SULTHARI_SANDSTONE_BRICKS.get().defaultBlockState();
    }

    private static BlockState trim() {
        return SoFEBlocks.SULTHARI_BRASS_TRIM.get().defaultBlockState();
    }

    private static BlockState tiles() {
        return SoFEBlocks.SULTHARI_GLAZED_TILES.get().defaultBlockState();
    }

    private static BlockState lamp() {
        return SoFEBlocks.SULTHARI_AETHERIUM_LAMP.get().defaultBlockState();
    }

    private static final BlockState GOLD_DOME = Blocks.HONEYCOMB_BLOCK.defaultBlockState();
    private static final BlockState RED_DOME = Blocks.RED_NETHER_BRICKS.defaultBlockState();
    private static final BlockState GOLD = Blocks.GOLD_BLOCK.defaultBlockState();
    private static final BlockState WHITE = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    private static final BlockState SANDSTONE = Blocks.SMOOTH_SANDSTONE.defaultBlockState();

    /** Bands of colored terracotta by height, like the strata of the mesa the city stands on. */
    private static final Block[] STRATA = {Blocks.TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.RED_TERRACOTTA,
            Blocks.WHITE_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.RED_TERRACOTTA};

    private static BlockState stratum(int y) {
        return STRATA[Math.floorMod(y, STRATA.length)].defaultBlockState();
    }

    private static Architecture.HouseStyle houseStyle(Random random) {
        BlockState[] walls = {Blocks.YELLOW_TERRACOTTA.defaultBlockState(), Blocks.ORANGE_TERRACOTTA.defaultBlockState(),
                SANDSTONE, Blocks.WHITE_TERRACOTTA.defaultBlockState(), bricks(), Blocks.TERRACOTTA.defaultBlockState()};
        BlockState[] awnings = {Blocks.RED_WOOL.defaultBlockState(), Blocks.WHITE_WOOL.defaultBlockState(),
                Blocks.YELLOW_WOOL.defaultBlockState(), Blocks.CYAN_WOOL.defaultBlockState(), Blocks.ORANGE_WOOL.defaultBlockState()};
        boolean dark = random.nextBoolean();
        return new Architecture.HouseStyle(walls[random.nextInt(walls.length)],
                dark ? Blocks.DARK_OAK_PLANKS.defaultBlockState() : Blocks.SPRUCE_PLANKS.defaultBlockState(),
                dark ? Blocks.DARK_OAK_STAIRS.defaultBlockState() : Blocks.SPRUCE_STAIRS.defaultBlockState(),
                dark ? Blocks.DARK_OAK_SLAB.defaultBlockState() : Blocks.SPRUCE_SLAB.defaultBlockState(),
                Blocks.SPRUCE_DOOR.defaultBlockState(), awnings[random.nextInt(awnings.length)], random.nextInt(3) > 0);
    }

    // --- the city walls

    /**
     * The walls on the edge of the city: banded terracotta crowned in sandstone with merlons, on a
     * stepped escarpment that runs down to the ground outside. Towers stand along them every 40
     * blocks and at the corners; each side has a gatehouse in its middle.
     */
    private static void cityWall(ServerLevel level, StructurePositions.Structure city) {
        int minX = city.x() - half(city.sizeX()), maxX = city.x() + half(city.sizeX()) - 1;
        int minZ = city.z() - half(city.sizeZ()), maxZ = city.z() + half(city.sizeZ()) - 1;
        for (int x = minX; x <= maxX; x++) {
            wallColumn(level, x, minZ, 0, -1, x - city.x());
            wallColumn(level, x, maxZ, 0, 1, x - city.x());
        }
        for (int z = minZ; z <= maxZ; z++) {
            wallColumn(level, minX, z, -1, 0, z - city.z());
            wallColumn(level, maxX, z, 1, 0, z - city.z());
        }
        // towers along the walls and at the corners
        for (int a = -half(city.sizeX()) + 40; a < half(city.sizeX()) - 20; a += 40) {
            if (Math.abs(a) < 20) continue; // the gatehouse is there
            wallTower(level, city.x() + a, minZ, Direction.NORTH, 4, 17);
            wallTower(level, city.x() + a, maxZ, Direction.SOUTH, 4, 17);
            wallTower(level, minX, city.z() + a, Direction.WEST, 4, 17);
            wallTower(level, maxX, city.z() + a, Direction.EAST, 4, 17);
        }
        wallTower(level, minX, minZ, Direction.NORTH, 5, 20);
        wallTower(level, maxX, minZ, Direction.EAST, 5, 20);
        wallTower(level, minX, maxZ, Direction.WEST, 5, 20);
        wallTower(level, maxX, maxZ, Direction.SOUTH, 5, 20);
        gatehouse(level, city.x(), minZ, Direction.NORTH);
        gatehouse(level, city.x(), maxZ, Direction.SOUTH);
        gatehouse(level, minX, city.z(), Direction.WEST);
        gatehouse(level, maxX, city.z(), Direction.EAST);
    }

    private static void wallColumn(ServerLevel level, int x, int z, int outX, int outZ, int along) {
        if (Math.abs(along) <= 3) return; // the gate
        int ground = surfaceY(level, x, z);
        // three blocks thick: the outer face, the core and the walk behind the merlons
        for (int k = 0; k < 3; k++) {
            int wx = x - outX * k, wz = z - outZ * k;
            for (int dy = -2; dy < 11; dy++) set(level, wx, ground + dy, wz, dy < 7 ? stratum(ground + dy) : SANDSTONE);
            set(level, wx, ground + 11, wz, k == 0 ? trim() : SoFEBlocks.SULTHARI_SANDSTONE_BRICK_SLAB.get().defaultBlockState());
            for (int dy = 12; dy < 15; dy++) set(level, wx, ground + dy, wz, Blocks.AIR.defaultBlockState());
        }
        Architecture.merlon(level, x, ground + 12, z, along, SANDSTONE);
        if (Math.floorMod(along, 16) == 8) set(level, x - outX * 2, ground + 12, z - outZ * 2, lamp());
        // the escarpment: steps of banded terracotta down to the ground outside
        for (int k = 1; k <= 6; k++) {
            int ex = x + outX * k, ez = z + outZ * k;
            int top = ground + 7 - k;
            int base = surfaceY(level, ex, ez);
            for (int y = Math.min(base, top) - 3; y < top; y++) set(level, ex, y, ez, stratum(y));
        }
    }

    /** A crenellated tower set in the wall, with a red banner on its outer face. */
    private static void wallTower(ServerLevel level, int x, int z, Direction out, int half, int height) {
        int ground = surfaceY(level, x, z);
        Architecture.tower(level, x, ground - 2, z, half, height + 2, SANDSTONE, trim());
        for (int dy = -2; dy < 7; dy++) { // the banded foot, like the walls
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    if (Math.abs(dx) == half || Math.abs(dz) == half) set(level, x + dx, ground + dy, z + dz, stratum(ground + dy));
                }
            }
        }
        Architecture.banner(level, x + out.getStepX() * (half + 1), ground + height - 4, z + out.getStepZ() * (half + 1), out, Blocks.RED_WALL_BANNER);
        set(level, x, ground + height + 1, z, lamp());
    }

    /** Two taller towers on either side of the gate and an arch of brass above it. */
    private static void gatehouse(ServerLevel level, int x, int z, Direction out) {
        boolean alongX = out.getAxis() == Direction.Axis.Z;
        for (int side : new int[]{-7, 7}) {
            wallTower(level, alongX ? x + side : x, alongX ? z : z + side, out, 3, 20);
        }
        int ground = surfaceY(level, x, z);
        for (int a = -3; a <= 3; a++) {
            for (int k = 0; k < 3; k++) {
                int gx = alongX ? x + a : x - out.getStepX() * k, gz = alongX ? z - out.getStepZ() * k : z + a;
                for (int dy = 7; dy < 12; dy++) set(level, gx, ground + dy, gz, dy == 7 ? trim() : SANDSTONE);
                if (Math.abs(a) == 3) for (int dy = 0; dy < 7; dy++) set(level, gx, ground + dy, gz, trim());
            }
        }
        set(level, x, ground + 6, z, lamp());
    }

    // --- the plaza

    private static void plaza(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        pad(level, minX, minZ, maxX, maxZ, y, bricks());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (Math.floorMod(x - minX, 6) == 0 || Math.floorMod(z - minZ, 6) == 0) set(level, x, y - 1, z, tiles());
            }
        }
        // an octagonal fountain with a brass column and a lamp
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                int d = Math.max(Math.abs(dx), Math.abs(dz)) + Math.min(Math.abs(dx), Math.abs(dz)) / 2;
                if (d > 6) continue;
                if (d >= 5) set(level, s.x() + dx, y, s.z() + dz, Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState());
                else set(level, s.x() + dx, y - 1, s.z() + dz, Blocks.WATER.defaultBlockState());
            }
        }
        for (int dy = -1; dy < 4; dy++) set(level, s.x(), y + dy, s.z(), dy == 3 ? lamp() : trim());
        Random random = new Random(s.x() * 31L + s.z());
        for (int[] c : new int[][]{{minX + 4, minZ + 4}, {maxX - 4, minZ + 4}, {minX + 4, maxZ - 4}, {maxX - 4, maxZ - 4}}) {
            Architecture.acacia(level, c[0], y, c[1], random);
        }
        for (int[] c : new int[][]{{minX + 1, s.z()}, {maxX - 1, s.z()}, {s.x(), minZ + 1}, {s.x(), maxZ - 1}}) {
            set(level, c[0], y, c[1], trim());
            set(level, c[0], y + 1, c[1], trim());
            set(level, c[0], y + 2, c[1], lamp());
        }
    }

    // --- the palace

    /**
     * The palace: a hall with arched windows and a crenellated roof, a golden ribbed dome over the
     * throne hall, red domes on the four corner pavilions, four white minarets and a great portal
     * facing the plaza.
     */
    private static void palace(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        pad(level, minX, minZ, maxX, maxZ, y, tiles(), 34);
        boolean doorNorth = s.z() > 0;
        int bx0 = minX + 5, bx1 = maxX - 5, bz0 = minZ + 5, bz1 = maxZ - 5, height = 12;
        int doorZ = doorNorth ? bz0 : bz1;
        for (int x = bx0; x <= bx1; x++) {
            for (int z = bz0; z <= bz1; z++) {
                boolean edge = x == bx0 || x == bx1 || z == bz0 || z == bz1;
                for (int dy = 0; dy < height; dy++) {
                    if (!edge) {
                        set(level, x, y + dy, z, Blocks.AIR.defaultBlockState());
                        continue;
                    }
                    int along = (x == bx0 || x == bx1) ? z : x;
                    int slot = Math.floorMod(along, 5);
                    boolean window = (dy >= 2 && dy <= 4 || dy >= 7 && dy <= 9) && (slot == 2 || slot == 3);
                    boolean arch = (dy == 5 || dy == 10) && (slot == 2 || slot == 3);
                    BlockState state = dy == 6 || dy == height - 1 ? trim() : window ? Blocks.GLASS_PANE.defaultBlockState()
                            : arch ? Blocks.CUT_SANDSTONE.defaultBlockState() : bricks();
                    set(level, x, y + dy, z, state);
                }
                set(level, x, y + height, z, edge ? SANDSTONE : SoFEBlocks.SULTHARI_SANDSTONE_BRICK_SLAB.get().defaultBlockState());
                if (edge) Architecture.merlon(level, x, y + height + 1, z, x + z, SANDSTONE);
            }
        }
        // the inner hall: carpets down the middle and lamps hanging from the ceiling
        for (int z = bz0 + 1; z < bz1; z++) {
            for (int x = s.x() - 2; x <= s.x() + 2; x++) set(level, x, y, z, Math.abs(x - s.x()) == 2 ? Blocks.YELLOW_CARPET.defaultBlockState() : Blocks.RED_CARPET.defaultBlockState());
        }
        for (int x = bx0 + 6; x < bx1; x += 8) {
            for (int z = bz0 + 6; z < bz1; z += 8) set(level, x, y + height - 1, z, lamp());
        }
        // the golden dome over the throne hall, on a windowed drum
        int r = Math.min(11, (bx1 - bx0) / 4);
        Architecture.openRoof(level, s.x(), y + height, s.z(), r);
        Architecture.drum(level, s.x(), y + height + 1, s.z(), r, 4, bricks(), trim());
        Architecture.dome(level, s.x(), y + height + 5, s.z(), r, GOLD_DOME, GOLD, Blocks.LIGHTNING_ROD.defaultBlockState());
        // red domes on the corner pavilions
        for (int[] c : new int[][]{{bx0 + 5, bz0 + 5}, {bx1 - 5, bz0 + 5}, {bx0 + 5, bz1 - 5}, {bx1 - 5, bz1 - 5}}) {
            Architecture.openRoof(level, c[0], y + height, c[1], 4);
            Architecture.drum(level, c[0], y + height + 1, c[1], 4, 3, bricks(), trim());
            Architecture.dome(level, c[0], y + height + 4, c[1], 4, RED_DOME, GOLD, Blocks.LIGHTNING_ROD.defaultBlockState());
        }
        // four minarets on the corners of the grounds
        for (int[] c : new int[][]{{minX + 2, minZ + 2}, {maxX - 2, minZ + 2}, {minX + 2, maxZ - 2}, {maxX - 2, maxZ - 2}}) {
            Architecture.minaret(level, c[0], y, c[1], 30, WHITE, trim(), GOLD);
        }
        portal(level, s.x(), y, doorZ, doorNorth ? Direction.NORTH : Direction.SOUTH, height);
    }

    /** A tall arched portal (an iwan) standing out of the facade, framed in brass, with banners. */
    private static void portal(ServerLevel level, int cx, int y, int z, Direction out, int wallHeight) {
        int step = out.getStepZ();
        for (int dx = -5; dx <= 5; dx++) {
            for (int k = 0; k <= 2; k++) {
                int pz = z + step * k;
                for (int dy = 0; dy < wallHeight + 4; dy++) {
                    int open = dy < 6 ? 2 : dy == 6 ? 1 : dy == 7 ? 0 : -1; // a pointed arch
                    boolean opening = Math.abs(dx) <= open;
                    boolean frame = Math.abs(dx) == 5 || dy >= wallHeight + 2 || Math.abs(dx) == open + 1 && dy <= 8;
                    BlockState state = opening ? Blocks.AIR.defaultBlockState() : frame ? trim() : tiles();
                    set(level, cx + dx, y + dy, pz, state);
                }
                if (Math.floorMod(dx, 2) == 1) set(level, cx + dx, y + wallHeight + 4, pz, SANDSTONE);
            }
        }
        for (int side : new int[]{-4, 4}) {
            Architecture.banner(level, cx + side, y + 9, z + step * 3, out, Blocks.RED_WALL_BANNER);
        }
        set(level, cx, y + 10, z + step * 3, lamp());
    }

    // --- the Great Observatory

    /**
     * A round tower with a golden dome and the great lens on top. The tower keeps a sensible size
     * whatever the plot (at most 11 blocks across from the center), and the dome is a little flattened.
     */
    private static void observatory(ServerLevel level, StructurePositions.Structure s, int y) {
        int r = Math.min(11, Math.min(half(s.sizeX()), half(s.sizeZ())) - 2);
        pad(level, s.x() - r - 4, s.z() - r - 4, s.x() + r + 4, s.z() + r + 4, y, tiles(), 40);
        int height = 24;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r) continue;
                boolean wallRing = d > r - 1.2;
                boolean door = dz > 0 && Math.abs(dx) <= 1; // the entrance faces the plaza, to the south
                for (int dy = 0; dy < height; dy++) {
                    if (!wallRing || door && dy < 4) continue;
                    double angle = Math.atan2(dz, dx);
                    boolean window = dy % 6 >= 2 && dy % 6 <= 3 && Math.abs(Math.sin(angle * 5)) < 0.2;
                    boolean band = dy % 6 == 5;
                    set(level, s.x() + dx, y + dy, s.z() + dz, band ? trim() : window ? Blocks.GLASS_PANE.defaultBlockState()
                            : dy < 6 ? stratum(y + dy) : bricks());
                }
            }
        }
        // a cornice ring, then the golden dome with its lantern
        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d <= r + 1 && d > r - 1.2) {
                    set(level, s.x() + dx, y + height, s.z() + dz, trim());
                    Architecture.merlon(level, s.x() + dx, y + height + 1, s.z() + dz, dx + dz, SANDSTONE);
                }
            }
        }
        Architecture.dome(level, s.x(), y + height + 1, s.z(), r - 2, GOLD_DOME, GOLD, lamp());
        // lamps up the inside of the tower
        for (int dy = 0; dy < height; dy += 5) set(level, s.x(), y + dy, s.z(), lamp());
    }

    // --- the bazaar

    /** Rows of stalls with striped awnings between shade trees, and shops along its edges. */
    private static void bazaar(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        pad(level, minX, minZ, maxX, maxZ, y, bricks());
        Random random = new Random(s.x() * 31L + s.z());
        BlockState[][] awnings = {
                {Blocks.RED_WOOL.defaultBlockState(), Blocks.WHITE_WOOL.defaultBlockState()},
                {Blocks.ORANGE_WOOL.defaultBlockState(), Blocks.YELLOW_WOOL.defaultBlockState()},
                {Blocks.CYAN_WOOL.defaultBlockState(), Blocks.WHITE_WOOL.defaultBlockState()},
                {Blocks.BLUE_WOOL.defaultBlockState(), Blocks.YELLOW_WOOL.defaultBlockState()},
                {Blocks.LIME_WOOL.defaultBlockState(), Blocks.WHITE_WOOL.defaultBlockState()}};
        int i = 0;
        for (int x = minX + 4; x + 5 <= maxX - 4; x += 8) {
            for (int z = minZ + 12; z + 4 <= maxZ - 12; z += 9) {
                BlockState[] a = awnings[i++ % awnings.length];
                Architecture.stall(level, x, y, z, a[0], a[1], random);
            }
        }
        // a cobbled lane down the middle and shade trees
        for (int x = minX; x <= maxX; x++) {
            for (int dz = -1; dz <= 1; dz++) set(level, x, y - 1, s.z() + dz, Blocks.COBBLESTONE.defaultBlockState());
        }
        for (int x = minX + 8; x < maxX - 4; x += 16) Architecture.acacia(level, x, y, s.z() + 3, random);
        // shops along the north and south edges, doors facing the stalls
        for (int x = minX + 1; x + 8 <= maxX; x += 10) {
            Architecture.house(level, x, minZ + 1, 8, 7, y, 5, Direction.SOUTH, houseStyle(random));
            Architecture.house(level, x, maxZ - 7, 8, 7, y, 5, Direction.NORTH, houseStyle(random));
        }
    }

    // --- the lower district

    /**
     * Houses on plots of 13 blocks along cobbled streets, each with its own colors and roof, a few
     * gardens with trees. Each plot follows the ground.
     */
    private static void district(ServerLevel level, StructurePositions.Structure s) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        Random random = new Random(s.x() * 31L + s.z());
        for (int x = minX; x + 12 <= maxX; x += 13) {
            for (int z = minZ; z + 12 <= maxZ; z += 13) {
                int y = surfaceY(level, x + 6, z + 6);
                pad(level, x, z, x + 12, z + 12, y, Blocks.COBBLESTONE.defaultBlockState());
                for (int a = 0; a <= 12; a++) {
                    for (int b = 0; b <= 12; b++) {
                        boolean street = a < 2 || b < 2;
                        set(level, x + a, y - 1, z + b, street ? (random.nextInt(4) == 0 ? Blocks.GRAVEL : Blocks.COBBLESTONE).defaultBlockState()
                                : Blocks.PACKED_MUD.defaultBlockState());
                    }
                }
                if (random.nextInt(6) == 0) { // a garden
                    Architecture.acacia(level, x + 7, y, z + 7, random);
                    set(level, x + 4, y, z + 4, Blocks.FLOWERING_AZALEA.defaultBlockState());
                    continue;
                }
                int w = 7 + random.nextInt(3), d = 7 + random.nextInt(3);
                Architecture.house(level, x + 3, z + 3, w, d, y, 4 + random.nextInt(3), random.nextBoolean() ? Direction.NORTH : Direction.WEST,
                        houseStyle(random));
                if (random.nextInt(4) == 0) set(level, x + 2, y, z + 11, Blocks.POTTED_CACTUS.defaultBlockState());
            }
        }
    }

    // --- the smaller places

    private static void trainingGrounds(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        pad(level, minX, minZ, maxX, maxZ, y, Blocks.PACKED_MUD.defaultBlockState());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean edge = x == minX || x == maxX || z == minZ || z == maxZ;
                boolean gate = z == minZ && Math.abs(x - s.x()) <= 1;
                if (edge && !gate) {
                    set(level, x, y, z, Blocks.SANDSTONE_WALL.defaultBlockState());
                    if (Math.floorMod(x + z, 8) == 0) set(level, x, y + 1, z, lamp());
                }
            }
        }
        for (int x = minX + 6; x < maxX - 4; x += 8) {
            set(level, x, y, maxZ - 4, Blocks.HAY_BLOCK.defaultBlockState());
            set(level, x, y + 1, maxZ - 4, Blocks.TARGET.defaultBlockState());
        }
        Random random = new Random(s.x() * 31L + s.z());
        Architecture.stall(level, minX + 3, y, minZ + 3, Blocks.RED_WOOL.defaultBlockState(), Blocks.WHITE_WOOL.defaultBlockState(), random);
    }

    /** The brass forge: a workshop with smoking chimneys. */
    private static void forge(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()) + 3, minZ = s.z() - half(s.sizeZ()) + 3;
        pad(level, minX - 3, minZ - 3, minX + s.sizeX() - 4, minZ + s.sizeZ() - 4, y, Blocks.COBBLESTONE.defaultBlockState());
        Architecture.HouseStyle style = new Architecture.HouseStyle(SoFEBlocks.SULTHARI_BRASS_PLATING.get().defaultBlockState(), bricks(),
                Blocks.DARK_OAK_STAIRS.defaultBlockState(), Blocks.DARK_OAK_SLAB.defaultBlockState(), Blocks.DARK_OAK_DOOR.defaultBlockState(),
                Blocks.BLACK_WOOL.defaultBlockState(), false);
        int w = s.sizeX() - 6, d = s.sizeZ() - 6;
        Architecture.house(level, minX, minZ, w, d, y, 7, s.z() > 0 ? Direction.NORTH : Direction.SOUTH, style);
        Architecture.chimney(level, minX + 2, y + 8, minZ + 2, 4, Blocks.BRICKS.defaultBlockState());
        Architecture.chimney(level, minX + w - 3, y + 8, minZ + d - 3, 4, Blocks.BRICKS.defaultBlockState());
        set(level, s.x() - 2, y, s.z(), Blocks.BLAST_FURNACE.defaultBlockState());
        set(level, s.x() + 2, y, s.z(), Blocks.ANVIL.defaultBlockState());
        set(level, s.x(), y, s.z() + 2, Blocks.SMITHING_TABLE.defaultBlockState());
    }

    /** The bank: a stout white house with a small red dome. */
    private static void bank(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()) + 2, minZ = s.z() - half(s.sizeZ()) + 2;
        pad(level, minX - 2, minZ - 2, minX + s.sizeX() - 3, minZ + s.sizeZ() - 3, y, tiles(), 20);
        Architecture.HouseStyle style = new Architecture.HouseStyle(WHITE, trim(), Blocks.SANDSTONE_STAIRS.defaultBlockState(),
                Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState(), Blocks.IRON_DOOR.defaultBlockState(), Blocks.RED_WOOL.defaultBlockState(), false);
        int w = s.sizeX() - 4, d = s.sizeZ() - 4;
        Architecture.house(level, minX, minZ, w, d, y, 7, s.z() > 0 ? Direction.NORTH : Direction.SOUTH, style);
        Architecture.openRoof(level, minX + w / 2, y + 7, minZ + d / 2, 4);
        Architecture.dome(level, minX + w / 2, y + 8, minZ + d / 2, 4, RED_DOME, GOLD, Blocks.LIGHTNING_ROD.defaultBlockState());
    }

    /** The Homestead is left to the player: only corner posts with lamps mark the plot. */
    private static void homestead(ServerLevel level, StructurePositions.Structure s) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        for (int[] c : new int[][]{{minX, minZ}, {maxX, minZ}, {minX, maxZ}, {maxX, maxZ}}) {
            int y = surfaceY(level, c[0], c[1]);
            set(level, c[0], y, c[1], trim());
            set(level, c[0], y + 1, c[1], lamp());
        }
    }
}
