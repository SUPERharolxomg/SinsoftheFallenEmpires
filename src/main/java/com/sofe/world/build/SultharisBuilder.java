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
            case "sulthari/city" -> SultharisCity.build(level, structure);
            case "sulthari/plaza" -> plaza(level, structure, y);
            case "sulthari/palace" -> palace(level, structure, y);
            case "sulthari/great_observatory" -> observatory(level, structure, y);
            case "sulthari/low_bazaar" -> bazaar(level, structure, y);
            case "sulthari/lower_district" -> district(level, structure);
            case "sulthari/training_grounds" -> trainingGrounds(level, structure, y);
            case "sulthari/forge" -> forge(level, structure, y);
            case "sulthari/bank" -> bank(level, structure, y);
            case "sulthari/homestead" -> homestead(level, structure);
            case "sulthari/void_gate" -> voidGate(level, structure, y);
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

    /**
     * Places a block. Panes, fences, walls, bars and stairs take their shape from the blocks around
     * them, and the blocks around them are told, so windows and railings join up whatever the order
     * the builders place them in.
     */
    static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if (connects(state)) state = Block.updateFromNeighbourShapes(state, level, pos);
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        if (state.getBlock() instanceof net.minecraft.world.level.block.LanternBlock) LANTERNS.add(pos);
    }

    /** Lanterns set during the build in progress, checked when it ends ({@link #settleLanterns}). */
    private static final java.util.List<BlockPos> LANTERNS = new java.util.ArrayList<>();

    /**
     * After a build: every lantern must hold. Blocks are set without telling their neighbours, so a lantern
     * hung from the air looks fine until the first block update nearby, then drops. A hanging lantern gets a
     * chain up to the ceiling; one with no ceiling over it stands on the floor; a standing one with nothing
     * under it hangs from what is above; one that can do neither is taken away.
     */
    static void settleLanterns(ServerLevel level) {
        for (BlockPos pos : LANTERNS) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof net.minecraft.world.level.block.LanternBlock) || state.canSurvive(level, pos)) continue;
            var hanging = net.minecraft.world.level.block.LanternBlock.HANGING;
            if (state.getValue(hanging) && chainUp(level, pos)) continue;
            BlockState flipped = state.setValue(hanging, !state.getValue(hanging));
            level.setBlock(pos, flipped.canSurvive(level, pos) ? flipped : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        LANTERNS.clear();
    }

    /** A chain from a hanging lantern up to a ceiling at most four blocks above; false when there is none. */
    private static boolean chainUp(ServerLevel level, BlockPos lantern) {
        for (int up = 1; up <= 4; up++) {
            BlockPos above = lantern.above(up);
            if (Block.canSupportCenter(level, above, Direction.DOWN)) {
                BlockState chain = Blocks.CHAIN.defaultBlockState();
                for (int k = 1; k < up; k++) level.setBlock(lantern.above(k), chain, Block.UPDATE_CLIENTS);
                return true;
            }
            if (!level.getBlockState(above).isAir()) return false;
        }
        return false;
    }

    private static boolean connects(BlockState state) {
        Block block = state.getBlock();
        return block instanceof net.minecraft.world.level.block.CrossCollisionBlock || block instanceof net.minecraft.world.level.block.WallBlock
                || block instanceof net.minecraft.world.level.block.StairBlock || Furniture.isFurniture(state);
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

    // --- the plaza

    /**
     * The plaza: paving in a star of terracotta on cream sandstone, a sadirvan in the middle (an
     * octagonal fountain under a little golden dome on eight columns), flower beds with acacias and
     * benches around them, and a ring of lantern posts.
     */
    private static void plaza(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        pad(level, minX, minZ, maxX, maxZ, y, SANDSTONE, 16);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int dx = x - s.x(), dz = z - s.z();
                double d = Math.sqrt(dx * dx + dz * dz), angle = Math.atan2(dz, dx);
                BlockState floor = Blocks.CUT_SANDSTONE.defaultBlockState();
                if (Math.abs(Math.sin(angle * 4)) < 0.12 && d > 7) floor = Blocks.YELLOW_TERRACOTTA.defaultBlockState(); // the rays of the star
                if (Math.abs(d - 9) < 0.6 || Math.abs(d - 16) < 0.6) floor = RED;                                     // its rings
                if (Math.abs(d - 12.5) < 0.6) floor = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
                if (Math.max(Math.abs(dx), Math.abs(dz)) >= half(s.sizeX()) - 2) floor = bricks();                   // the border
                set(level, x, y - 1, z, floor);
            }
        }
        // the sadirvan: an octagonal basin, eight columns, a ring and a golden dome over the spout
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                int d = Math.max(Math.abs(dx), Math.abs(dz)) + Math.min(Math.abs(dx), Math.abs(dz)) / 2;
                if (d > 7) continue;
                if (d >= 6) set(level, s.x() + dx, y, s.z() + dz, Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState());
                else set(level, s.x() + dx, y - 1, s.z() + dz, Blocks.WATER.defaultBlockState());
            }
        }
        for (int deg = 0; deg < 360; deg += 45) {
            int cx = s.x() + (int) Math.round(4 * Math.cos(Math.toRadians(deg + 22.5)));
            int cz = s.z() + (int) Math.round(4 * Math.sin(Math.toRadians(deg + 22.5)));
            set(level, cx, y - 1, cz, Blocks.SMOOTH_QUARTZ.defaultBlockState());
            for (int dy = 0; dy < 5; dy++) set(level, cx, y + dy, cz, dy == 4 ? CHISELED : Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.DOUBLE));
        }
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= 25) set(level, s.x() + dx, y + 5, s.z() + dz, d2 > 12 ? trim() : Blocks.SMOOTH_QUARTZ.defaultBlockState());
            }
        }
        int crown = Architecture.dome(level, s.x(), y + 6, s.z(), 4, GOLD_DOME, GOLD, lamp());
        Architecture.openRoof(level, s.x(), y + 5, s.z(), 3);
        for (int dy = -1; dy < 2; dy++) set(level, s.x(), y + dy, s.z(), trim());
        set(level, s.x(), y + 2, s.z(), lamp());
        Architecture.chandelier(level, s.x(), crown - 1, y + 4, s.z());
        // flower beds with acacias in the four quarters, benches facing the fountain
        Random random = new Random(s.x() * 31L + s.z());
        for (int[] q : new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
            int bx = s.x() + q[0] * 13, bz = s.z() + q[1] * 13;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    boolean rim = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                    set(level, bx + dx, y - 1, bz + dz, Blocks.GRASS_BLOCK.defaultBlockState());
                    set(level, bx + dx, y, bz + dz, rim ? Blocks.SANDSTONE_WALL.defaultBlockState()
                            : random.nextBoolean() ? Blocks.POPPY.defaultBlockState() : Blocks.DANDELION.defaultBlockState());
                }
            }
            Architecture.acacia(level, bx, y, bz, random);
            Direction toFountain = q[0] > 0 ? Direction.WEST : Direction.EAST;
            for (int dz = -1; dz <= 1; dz++) {
                set(level, bx - q[0] * 4, y, bz + dz, Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.StairBlock.FACING, toFountain.getOpposite()));
            }
        }
        // a ring of lantern posts
        for (int deg = 0; deg < 360; deg += 30) {
            int lx = s.x() + (int) Math.round(19 * Math.cos(Math.toRadians(deg)));
            int lz = s.z() + (int) Math.round(19 * Math.sin(Math.toRadians(deg)));
            set(level, lx, y, lz, trim());
            set(level, lx, y + 1, lz, Blocks.ACACIA_FENCE.defaultBlockState());
            set(level, lx, y + 2, lz, Blocks.LANTERN.defaultBlockState());
        }
        // the storyteller tells the old tales by the fountain; a water carrier fills his jars
        citizen(level, "citizen_storyteller", "story", null, s.x() - 9, s.z(), 270);
        citizen(level, "citizen_water_carrier", "citizen", null, s.x(), s.z() - 9, 0);
        // the journey begins here, in front of the fountain, on the ground the city now stands on
        level.setDefaultSpawnPos(new BlockPos(s.x(), y, s.z() + 9), 0.0f);
    }

    // --- the palace

    private static final BlockState RED = Blocks.RED_TERRACOTTA.defaultBlockState();
    private static final BlockState CUT = Blocks.CUT_SANDSTONE.defaultBlockState();
    private static final BlockState CHISELED = Blocks.CHISELED_SANDSTONE.defaultBlockState();

    /**
     * The palace, after the concept and the hall reference: two stories of pointed arches with red
     * and cream voussoirs, an open arcade along the front, a crenellated roof with the golden dome,
     * four red domes and four minarets, and the great portal facing the plaza. Inside, a nave with
     * dark wooden columns and arches, a coffered red ceiling, chandeliers, banners, a carpet down the
     * middle and the throne at the far end.
     */
    private static void palace(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        pad(level, minX, minZ, maxX, maxZ, y, bricks(), 34);
        boolean doorNorth = s.z() > 0;
        int bx0 = minX + 5, bx1 = maxX - 5, bz0 = minZ + 6, bz1 = maxZ - 5, height = 14;
        int doorZ = doorNorth ? bz0 : bz1;
        for (int x = bx0; x <= bx1; x++) {
            for (int z = bz0; z <= bz1; z++) {
                boolean sideX = x == bx0 || x == bx1, edge = sideX || z == bz0 || z == bz1;
                if (!edge) {
                    set(level, x, y - 1, z, tiles()); // the blue tiles are the floor of the palace alone
                    for (int dy = 0; dy < height; dy++) set(level, x, y + dy, z, Blocks.AIR.defaultBlockState());
                } else {
                    int slot = Math.floorMod(sideX ? z - bz0 : x - bx0, 5);
                    for (int dy = 0; dy < height; dy++) set(level, x, y + dy, z, facade(slot, dy, height));
                }
                // a coffered ceiling seen from the hall, sandstone slabs on top
                boolean beam = Math.floorMod(x - bx0, 4) == 0 || Math.floorMod(z - bz0, 4) == 0;
                set(level, x, y + height, z, edge ? SANDSTONE : beam ? Blocks.ORANGE_TERRACOTTA.defaultBlockState() : RED);
                set(level, x, y + height + 1, z, edge ? SANDSTONE : SoFEBlocks.SULTHARI_SANDSTONE_BRICK_SLAB.get().defaultBlockState());
                if (edge) Architecture.merlon(level, x, y + height + 2, z, x + z, SANDSTONE);
            }
        }
        int r = 13, dir = doorNorth ? 1 : -1;
        int domeZ = s.z() - 5 * dir, throneZ = doorNorth ? bz1 - 7 : bz0 + 7;
        hall(level, s, bx0, bz0, bx1, bz1, y, height, doorNorth, domeZ, r, throneZ);
        // arcades with columns along the front and both sides
        arcade(level, bx0, bx1, doorNorth ? bz0 - 1 : bz1 + 1, doorNorth ? Direction.NORTH : Direction.SOUTH, y, s.x());
        arcade(level, bz0 + 1, bz1 - 1, bx0 - 1, Direction.WEST, y, Integer.MIN_VALUE);
        arcade(level, bz0 + 1, bz1 - 1, bx1 + 1, Direction.EAST, y, Integer.MIN_VALUE);
        // the golden dome over the hall, a second one over the throne, each with a chandelier from its crown
        int crown = domeOver(level, s.x(), domeZ, y + height, r, 4, GOLD_DOME);
        Architecture.grandChandelier(level, s.x(), crown - 1, y + height - 4, domeZ, 3);
        int throneCrown = domeOver(level, s.x(), throneZ, y + height, 6, 3, GOLD_DOME);
        Architecture.chandelier(level, s.x(), throneCrown - 1, y + height - 2, throneZ);
        // red domes on the corner pavilions
        for (int[] c : new int[][]{{bx0 + 8, bz0 + 8}, {bx1 - 8, bz0 + 8}, {bx0 + 8, bz1 - 8}, {bx1 - 8, bz1 - 8}}) {
            int top = domeOver(level, c[0], c[1], y + height, 6, 3, RED_DOME);
            Architecture.chandelier(level, c[0], top - 1, y + height - 2, c[1]);
        }
        for (int[] c : new int[][]{{minX + 2, minZ + 2}, {maxX - 2, minZ + 2}, {minX + 2, maxZ - 2}, {maxX - 2, maxZ - 2}}) {
            Architecture.minaret(level, c[0], y, c[1], 32, WHITE, trim(), GOLD);
        }
        portal(level, s.x(), y, doorZ, doorNorth ? Direction.NORTH : Direction.SOUTH, height);
        // lamps on brass posts all around the grounds, so the palace glows at night
        for (int x = minX + 1; x <= maxX - 1; x += 7) {
            for (int z : new int[]{minZ + 1, maxZ - 1}) {
                if (Math.abs(x - s.x()) <= 3) continue; // the way in
                set(level, x, y, z, trim());
                set(level, x, y + 1, z, trim());
                set(level, x, y + 2, z, lamp());
            }
        }
        for (int z = minZ + 8; z <= maxZ - 8; z += 7) {
            for (int x : new int[]{minX + 1, maxX - 1}) {
                set(level, x, y, z, trim());
                set(level, x, y + 1, z, trim());
                set(level, x, y + 2, z, lamp());
            }
        }
    }

    /**
     * One block of the outer walls, in bays of 5: a pier, then a pointed arch three wide on each story
     * with red and cream voussoirs (the Ottoman ablaq), the bands between the stories in brass.
     */
    private static BlockState facade(int slot, int dy, int height) {
        if (dy == 0) return CUT;
        if (dy == 7 || dy == height - 1) return trim();
        if (slot == 0) return dy == 6 || dy == 12 ? CHISELED : CUT;
        if (slot == 4) return bricks();
        int base = dy < 7 ? 1 : 8; // the first row of the opening of each story
        int d = dy - base;
        if (d >= 0 && d <= 3) return Blocks.GLASS_PANE.defaultBlockState();
        if (d == 4) return slot == 2 ? Blocks.GLASS_PANE.defaultBlockState() : RED;
        if (d == 5) return slot == 2 ? SANDSTONE : bricks();
        return bricks();
    }

    /**
     * An open gallery of pointed arches, four blocks deep, along one wall of the palace: from and to
     * run along the wall at the line wallLine; out is the side the gallery faces. Columns of dark wood
     * on sandstone bases, red and cream voussoirs, a lantern in every bay. skip leaves the portal free.
     */
    private static void arcade(ServerLevel level, int from, int to, int wallLine, Direction out, int y, int skip) {
        boolean alongX = out.getAxis() == Direction.Axis.Z;
        int step = alongX ? out.getStepZ() : out.getStepX();
        BlockState topSlab = SoFEBlocks.SULTHARI_SANDSTONE_BRICK_SLAB.get().defaultBlockState()
                .setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.TOP);
        for (int a = from; a <= to; a++) {
            if (Math.abs(a - skip) <= 6) continue; // the portal
            int slot = Math.floorMod(a - from, 5);
            for (int k = 0; k < 4; k++) {
                int line = wallLine + step * k;
                int x = alongX ? a : line, z = alongX ? line : a;
                boolean front = k == 3;
                for (int dy = 0; dy < 7; dy++) {
                    BlockState state = Blocks.AIR.defaultBlockState();
                    if (front) {
                        if (slot == 0) state = dy == 0 || dy == 5 ? CHISELED : Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
                        else if (dy == 4) state = slot == 2 ? Blocks.AIR.defaultBlockState() : RED;
                        else if (dy == 5) state = slot == 2 ? SANDSTONE : bricks();
                        else if (dy == 6) state = trim();
                    } else if (dy == 6) {
                        state = topSlab;
                    }
                    set(level, x, y + dy, z, state);
                }
                set(level, x, y + 7, z, front ? SANDSTONE : SoFEBlocks.SULTHARI_SANDSTONE_BRICK_SLAB.get().defaultBlockState());
                if (front) {
                    Architecture.merlon(level, x, y + 8, z, a, SANDSTONE);
                    if (Math.floorMod(a, 10) == 0) set(level, x, y + 9, z, Blocks.LANTERN.defaultBlockState());
                }
                if (slot == 2 && k == 1) set(level, x, y + 5, z, Blocks.LANTERN.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
            }
        }
    }

    /**
     * A dome standing on the roof of a hall: the ceiling opens under it, a solid ring carries its drum
     * (no slab and no gap between the roof and the drum) and a gold ring frames the opening seen from
     * below. Returns the height of its crown.
     */
    private static int domeOver(ServerLevel level, int cx, int cz, int roofY, int r, int drumHeight, BlockState shell) {
        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 <= (r - 1) * (r - 1)) {
                    set(level, cx + dx, roofY, cz + dz, Blocks.AIR.defaultBlockState());
                    set(level, cx + dx, roofY + 1, cz + dz, Blocks.AIR.defaultBlockState());
                } else if (d2 <= (r + 1) * (r + 1)) {
                    if (d2 <= r * r) set(level, cx + dx, roofY, cz + dz, GOLD);
                    set(level, cx + dx, roofY + 1, cz + dz, CUT);
                }
            }
        }
        Architecture.drum(level, cx, roofY + 2, cz, r, drumHeight, bricks(), trim());
        return Architecture.dome(level, cx, roofY + 2 + drumHeight, cz, r, shell, GOLD, Blocks.LIGHTNING_ROD.defaultBlockState());
    }

    /**
     * The hall inside the palace, after the references: a nave lined with square columns of dark wood
     * banded in gold, arches between them, the carpet lit by aetherium lamps on brass posts, sitting
     * places in the aisles, and at the far end the throne on a dais of three steps under its own dome.
     */
    private static void hall(ServerLevel level, StructurePositions.Structure s, int bx0, int bz0, int bx1, int bz1, int y, int height,
                             boolean doorNorth, int domeZ, int domeRadius, int throneZ) {
        int dir = doorNorth ? 1 : -1; // from the door toward the throne
        int daisFront = throneZ; // the lowest step of the dais
        int firstColumn = doorNorth ? bz0 + 4 : bz1 - 5, lastColumn = daisFront - dir * 4;
        // square columns two wide, every 6 blocks, and the pointed arches between them
        for (int side : new int[]{-1, 1}) {
            for (int k = 0; ; k++) {
                int z = firstColumn + dir * k;
                if (dir * (z - lastColumn) > 0) break;
                int off = Math.floorMod(k, 6);
                boolean underDome = Math.abs(z - domeZ) < domeRadius - 1 && Math.abs(side * 8) < domeRadius;
                for (int w = 0; w <= 1; w++) {
                    int x = s.x() + side * (8 + w);
                    if (off <= 1) {
                        set(level, x, y, z, CHISELED);
                        set(level, x, y + 1, z, CUT);
                        for (int dy = 2; dy <= 8; dy++) set(level, x, y + dy, z, dy == 5 ? GOLD : Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
                        set(level, x, y + 9, z, GOLD);
                        set(level, x, y + 10, z, CHISELED);
                        for (int dy = 11; dy < height; dy++) set(level, x, y + dy, z, CUT);
                    } else if (!underDome) {
                        int underside = 10 + Math.min(off - 1, 6 - off); // 11, 12, 12, 11
                        for (int dy = underside; dy < height; dy++) set(level, x, y + dy, z, (off + dy) % 2 == 0 ? RED : SANDSTONE);
                    }
                }
                if (off == 0) {
                    SultharisCity.crescentBanner(level, s.x() + side * 7, y + 7, z, side > 0 ? Direction.WEST : Direction.EAST);
                    set(level, s.x() + side * 7, y, z + dir * 3, Blocks.POTTED_FLOWERING_AZALEA.defaultBlockState());
                }
            }
        }
        // the carpet down the nave to the dais, medallions of terracotta, and lamps on brass posts along it
        for (int k = 1; ; k++) {
            int z = (doorNorth ? bz0 : bz1) + dir * k;
            if (dir * (z - daisFront) >= 0) break;
            for (int x = s.x() - 2; x <= s.x() + 2; x++) {
                set(level, x, y, z, Math.abs(x - s.x()) == 2 ? Blocks.YELLOW_CARPET.defaultBlockState() : Blocks.RED_CARPET.defaultBlockState());
            }
            if (k % 8 == 4) {
                for (int side : new int[]{-5, 5}) {
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            set(level, s.x() + side + dx, y - 1, z + dz, Math.floorMod(dx + dz, 2) == 0 ? Blocks.ORANGE_TERRACOTTA.defaultBlockState() : RED);
                        }
                    }
                }
            }
            if (k % 6 == 3) {
                for (int side : new int[]{-4, 4}) {
                    set(level, s.x() + side, y, z, trim());
                    set(level, s.x() + side, y + 1, z, trim());
                    set(level, s.x() + side, y + 2, z, lamp());
                }
            }
        }
        // chandeliers down the nave (the domes have their own) and over the sitting places in the aisles
        for (int z = bz0 + 7; z < bz1 - 5; z += 12) {
            if (Math.abs(z - domeZ) > domeRadius + 1 && Math.abs(z - throneZ) > 8) {
                Architecture.chandelier(level, s.x(), y + height - 1, y + height - 3, z);
            }
            for (int side : new int[]{-1, 1}) {
                int ax = s.x() + side * 18;
                Architecture.chandelier(level, ax, y + height - 1, y + height - 3, z);
                // a sitting place on a patterned rug: two divans facing each other across a low table,
                // an armchair at each end, plants at the corners
                for (int dz = -3; dz <= 3; dz++) {
                    for (int dx = -4; dx <= 4; dx++) {
                        boolean border = Math.abs(dz) == 3 || Math.abs(dx) == 4;
                        boolean heart = Math.abs(dx) + Math.abs(dz) <= 2;
                        set(level, ax + dx, y, z + dz, (border ? Blocks.YELLOW_CARPET : heart ? Blocks.ORANGE_CARPET : Blocks.RED_CARPET).defaultBlockState());
                    }
                }
                for (int dz = -1; dz <= 1; dz++) {
                    set(level, ax - 2, y, z + dz, Furniture.divan("red", Direction.EAST));
                    set(level, ax + 2, y, z + dz, Furniture.divan("red", Direction.WEST));
                }
                set(level, ax, y, z - 2, Furniture.divan("orange", Direction.SOUTH));
                set(level, ax, y, z + 2, Furniture.divan("orange", Direction.NORTH));
                for (int[] c : new int[][]{{-4, -3}, {4, -3}, {-4, 3}, {4, 3}}) {
                    set(level, ax + c[0], y, z + c[1], Blocks.POTTED_FERN.defaultBlockState());
                }
                set(level, ax, y, z, Furniture.table("dark_oak"));
                set(level, ax, y + 1, z, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 4)
                        .setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
            }
        }
        // along the side walls: banners, chests and plants below, and a lantern on a bracket above each
        Block[] goods = {Blocks.CHEST, Blocks.BARREL, Blocks.POTTED_CACTUS, Blocks.BOOKSHELF, Blocks.POTTED_FERN};
        for (int z = bz0 + 5; z < bz1 - 2; z += 5) {
            SultharisCity.crescentBanner(level, bx0 + 1, y + 5, z, Direction.EAST);
            SultharisCity.crescentBanner(level, bx1 - 1, y + 5, z, Direction.WEST);
            set(level, bx0 + 1, y, z + 2, goods[Math.floorMod(z, goods.length)].defaultBlockState());
            set(level, bx1 - 1, y, z + 2, goods[Math.floorMod(z + 2, goods.length)].defaultBlockState());
            for (int x : new int[]{bx0 + 1, bx1 - 1}) {
                set(level, x, y + 8, z + 2, trim());
                set(level, x, y + 9, z + 2, Blocks.LANTERN.defaultBlockState());
            }
        }
        // the dais: two steps up to the throne, with a carpet over them
        Direction toThrone = doorNorth ? Direction.SOUTH : Direction.NORTH;
        BlockState step = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, toThrone);
        int farZ = doorNorth ? bz1 - 1 : bz0 + 1;
        for (int k = 0; k <= 6; k++) {
            int z = farZ - dir * k;
            for (int dx = -6; dx <= 6; dx++) {
                int x = s.x() + dx;
                set(level, x, y, z, k == 6 ? step : CUT);
                set(level, x, y + 1, z, k <= 3 ? CUT : k == 4 ? step : Blocks.AIR.defaultBlockState());
                if (Math.abs(dx) <= 1) {
                    if (k <= 3) set(level, x, y + 2, z, Blocks.RED_CARPET.defaultBlockState());
                    if (k == 5) set(level, x, y + 1, z, Blocks.RED_CARPET.defaultBlockState());
                }
            }
        }
        // the throne, its back of red and gold, lanterns and plants beside it, a tapestry behind
        Direction facing = toThrone.getOpposite();
        set(level, s.x(), y + 2, farZ - dir, Blocks.QUARTZ_STAIRS.defaultBlockState()
                .setValue(net.minecraft.world.level.block.StairBlock.FACING, toThrone));
        set(level, s.x() - 1, y + 2, farZ - dir, GOLD);
        set(level, s.x() + 1, y + 2, farZ - dir, GOLD);
        for (int dy = 2; dy <= 6; dy++) set(level, s.x(), y + dy, farZ, dy == 6 ? GOLD : RED);
        for (int dx : new int[]{-3, 3}) {
            set(level, s.x() + dx, y + 2, farZ - dir, trim());
            set(level, s.x() + dx, y + 3, farZ - dir, lamp());
        }
        for (int dx : new int[]{-5, 5}) set(level, s.x() + dx, y + 2, farZ - dir * 2, Blocks.POTTED_FLOWERING_AZALEA.defaultBlockState());
        for (int dx = -4; dx <= 4; dx++) {
            for (int row = 0; row < 3; row++) {
                if (Math.abs(dx) <= 1 && row == 0) continue; // the throne's back stands there
                SultharisCity.crescentBanner(level, s.x() + dx, y + 6 + row * 2, farZ, facing);
            }
        }
    }

    /** A tall arched portal (an iwan) standing out of the facade: a brass frame, red and cream voussoirs, banners. */
    private static void portal(ServerLevel level, int cx, int y, int z, Direction out, int wallHeight) {
        int step = out.getStepZ();
        for (int dx = -5; dx <= 5; dx++) {
            for (int k = 0; k <= 2; k++) {
                int pz = z + step * k;
                for (int dy = 0; dy < wallHeight + 4; dy++) {
                    int open = dy < 7 ? 2 : dy == 7 ? 1 : dy == 8 ? 0 : -1; // a pointed arch
                    boolean opening = Math.abs(dx) <= open;
                    boolean voussoir = !opening && (Math.abs(dx) == open + 1 && dy <= 9 || dy == 9 && dx == 0);
                    BlockState state = opening ? Blocks.AIR.defaultBlockState()
                            : Math.abs(dx) == 5 || dy >= wallHeight + 2 ? trim()
                            : voussoir ? (Math.floorMod(dy + dx, 2) == 0 ? RED : SANDSTONE) : CUT;
                    set(level, cx + dx, y + dy, pz, state);
                }
                if (Math.floorMod(dx, 2) == 1) set(level, cx + dx, y + wallHeight + 4, pz, SANDSTONE);
            }
        }
        SultharisCity.crescentBanner(level, cx, y + 11, z + step * 3, out);
        for (int side : new int[]{-4, 4}) SultharisCity.crescentBanner(level, cx + side, y + 8, z + step * 3, out);
        set(level, cx, y + 7, z + step, Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
    }

    // --- the Great Observatory

    /**
     * A round tower with a golden dome and the great lens on top. The tower keeps a sensible size
     * whatever the plot (at most 11 blocks across from the center), and the dome is a little flattened.
     */
    /**
     * The Void Gate under the Great Observatory (docs/Mundo.md, W5): a small Sulthari pavilion beside the Observatory,
     * its door sealed until Envyris has fallen; inside, a ladder shaft goes down to a vault of deepslate, crying
     * obsidian and amethyst where twelve empty End portal frames wait in a ring for twelve Eyes of Ender.
     */
    public static void voidGate(ServerLevel level, StructurePositions.Structure s, int y) {
        int cx = s.x(), cz = s.z();
        BlockState tile = Blocks.POLISHED_DEEPSLATE.defaultBlockState(), brick = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState lapis = Blocks.LAPIS_BLOCK.defaultBlockState(), gold = Blocks.GOLD_BLOCK.defaultBlockState();
        // the pavilion: a sandstone floor, four lapis-and-gold pillars, walls with a door on the south, a dome of slabs
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                set(level, cx + dx, y - 1, cz + dz, Math.abs(dx) == 4 || Math.abs(dz) == 4 ? Blocks.CUT_SANDSTONE.defaultBlockState() : SANDSTONE);
                for (int dy = 0; dy <= 5; dy++) {
                    boolean wall = Math.abs(dx) == 4 || Math.abs(dz) == 4;
                    boolean corner = Math.abs(dx) == 4 && Math.abs(dz) == 4;
                    BlockState state = Blocks.AIR.defaultBlockState();
                    if (corner) state = dy % 2 == 0 ? lapis : gold;
                    else if (wall && dy < 5) state = dy == 2 && Math.abs(dx) < 4 && Math.abs(dz) < 4 ? Blocks.AIR.defaultBlockState()
                            : (dy == 4 ? Blocks.CHISELED_SANDSTONE.defaultBlockState() : Blocks.SMOOTH_SANDSTONE.defaultBlockState());
                    else if (dy == 5) state = Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState();
                    set(level, cx + dx, y + dy, cz + dz, state);
                }
            }
        }
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) set(level, cx + dx, y + 6, cz + dz, lapis);   // the dome
        set(level, cx, y + 7, cz, gold);
        for (int dx : new int[]{-1, 0, 1}) for (int dy = 0; dy < 4; dy++) set(level, cx + dx, y + dy, cz + 4, Blocks.AIR.defaultBlockState());
        StructureBuilder.placeGates(level, s, y);                                                                      // sealed until Envyris falls
        set(level, cx - 3, y + 3, cz + 3, Blocks.SOUL_LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, false));
        set(level, cx + 3, y + 3, cz + 3, Blocks.SOUL_LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, false));
        // the vault, deep under the city
        int floor = y - 26, top = floor + 7, pz = cz - 9;
        for (int dx = -9; dx <= 9; dx++) {
            for (int dz = -19; dz <= 3; dz++) {
                for (int yy = floor - 1; yy <= top; yy++) {
                    boolean shell = Math.abs(dx) == 9 || dz == -19 || dz == 3 || yy == floor - 1 || yy == top;
                    BlockState state = shell ? (yy == floor - 1 ? ((dx + dz) % 3 == 0 ? Blocks.PURPUR_BLOCK.defaultBlockState() : tile) : brick)
                            : Blocks.AIR.defaultBlockState();
                    set(level, cx + dx, yy, cz + dz, state);
                }
            }
        }
        for (int[] c : new int[][]{{-8, -18}, {8, -18}, {-8, 2}, {8, 2}, {-8, -8}, {8, -8}}) {     // pillars of crying obsidian
            for (int yy = floor; yy < top; yy++) set(level, cx + c[0], yy, cz + c[1], Blocks.CRYING_OBSIDIAN.defaultBlockState());
            set(level, cx + c[0], top - 1, cz + c[1] + (c[1] < 0 ? 1 : -1), Blocks.SOUL_LANTERN.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
        }
        for (int[] c : new int[][]{{-5, -13}, {5, -13}, {-5, -5}, {5, -5}}) {
            set(level, cx + c[0], floor, cz + c[1], Blocks.AMETHYST_BLOCK.defaultBlockState());
            set(level, cx + c[0], floor + 1, cz + c[1], Blocks.AMETHYST_CLUSTER.defaultBlockState());
        }
        // the ring of twelve frames, each facing the middle, all empty
        for (int i = -1; i <= 1; i++) {
            frame(level, cx + i, floor, pz - 2, Direction.SOUTH);
            frame(level, cx + i, floor, pz + 2, Direction.NORTH);
            frame(level, cx - 2, floor, pz + i, Direction.EAST);
            frame(level, cx + 2, floor, pz + i, Direction.WEST);
        }
        // the shaft: three wide in the pavilion's middle, a ladder on its north wall, down to the vault
        for (int yy = floor; yy < y; yy++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean wall = yy >= top && (Math.abs(dx) == 1 && Math.abs(dz) == 1);
                    set(level, cx + dx, yy, cz + dz, wall ? brick : Blocks.AIR.defaultBlockState());
                }
            }
            if (yy >= top) {
                for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
                    if (Math.abs(dx) == 2 || Math.abs(dz) == 2) set(level, cx + dx, yy, cz + dz, brick);
            }
            set(level, cx, yy, cz - 2, brick);
            set(level, cx, yy, cz - 1, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.SOUTH));
        }
    }

    private static void frame(ServerLevel level, int x, int y, int z, Direction facing) {
        set(level, x, y, z, Blocks.END_PORTAL_FRAME.defaultBlockState()
                .setValue(net.minecraft.world.level.block.EndPortalFrameBlock.FACING, facing)
                .setValue(net.minecraft.world.level.block.EndPortalFrameBlock.HAS_EYE, false));
    }

    private static void observatory(ServerLevel level, StructurePositions.Structure s, int y) {
        int r = Math.min(11, Math.min(half(s.sizeX()), half(s.sizeZ())) - 2);
        // a round floor of tiles on the Observatory's terrace, open to the sky
        for (int dx = -r - 4; dx <= r + 4; dx++) {
            for (int dz = -r - 4; dz <= r + 4; dz++) {
                if (dx * dx + dz * dz > (r + 3) * (r + 3)) continue;
                set(level, s.x() + dx, y - 1, s.z() + dz, (dx * dx + dz * dz) % 7 == 0 ? Blocks.CUT_SANDSTONE.defaultBlockState() : SANDSTONE);
                for (int dy = 0; dy < 40; dy++) set(level, s.x() + dx, y + dy, s.z() + dz, Blocks.AIR.defaultBlockState());
            }
        }
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
        int crown = Architecture.dome(level, s.x(), y + height + 1, s.z(), r - 2, GOLD_DOME, GOLD, lamp());
        observatoryInside(level, s.x(), s.z(), y, r, height);
        // the light of the Great Lens comes down the well in the middle, through every floor
        Architecture.chandelier(level, s.x(), crown - 1, y + 3, s.z());
    }

    private static final int FLOOR = 6;

    /**
     * Inside the Observatory: three floors around an open well, joined by a stair that climbs along
     * the wall. The ground floor receives visitors, the first holds the library, the second the maps
     * and charts, and the top one, under the dome, the brass telescope.
     */
    private static void observatoryInside(ServerLevel level, int cx, int cz, int y, int r, int height) {
        // straight flights two wide along the north and south walls, each step on a solid support
        java.util.Set<Long> stairs = new java.util.HashSet<>();
        for (int f = 0; f < 3; f++) {
            boolean north = f % 2 == 0;
            Direction up = north ? Direction.EAST : Direction.WEST;
            for (int i = 0; i < FLOOR - 1; i++) {
                int x = cx + (north ? -2 + i : 2 - i);
                int sy = y + f * FLOOR + i;
                for (int w = 0; w <= 1; w++) {
                    int z = cz + (north ? -6 - w : 6 + w);
                    for (int below = y + f * FLOOR; below < sy; below++) set(level, x, below, z, Blocks.CUT_SANDSTONE.defaultBlockState());
                    set(level, x, sy, z, Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, up));
                    for (int h = 1; h <= 3; h++) stairs.add(BlockPos.asLong(x, sy + h, z));
                    stairs.add(BlockPos.asLong(x, sy, z));
                }
                // a railing on the open side of the flight
                int rz = cz + (north ? -5 : 5);
                if (i > 0) set(level, x, sy + 1, rz, Blocks.SANDSTONE_WALL.defaultBlockState());
                stairs.add(BlockPos.asLong(x, sy + 1, rz));
            }
        }
        BlockState floor = Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState()
                .setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.TOP);
        for (int f = 1; f <= 3; f++) {
            int fy = y + f * FLOOR - 1;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > r - 1.2 || stairs.contains(BlockPos.asLong(cx + dx, fy, cz + dz))) continue;
                    if (d <= 2.5) continue; // the well
                    set(level, cx + dx, fy, cz + dz, floor);
                    if (d <= 3.5) set(level, cx + dx, fy + 1, cz + dz, Blocks.SANDSTONE_WALL.defaultBlockState()); // its railing
                }
            }
        }
        // what stands along the walls of each floor, away from the stair and the door
        for (int f = 0; f <= 3; f++) {
            int fy = y + f * FLOOR;
            for (int deg = 0; deg < 360; deg += 8) {
                double a = Math.toRadians(deg);
                int x = cx + (int) Math.round((r - 2) * Math.cos(a)), z = cz + (int) Math.round((r - 2) * Math.sin(a));
                if (f == 0 && z > cz && Math.abs(x - cx) <= 2) continue; // the door
                if (stairs.contains(BlockPos.asLong(x, fy + 1, z)) || stairs.contains(BlockPos.asLong(x, fy, z))) continue;
                if (!level.getBlockState(new BlockPos(x, fy, z)).isAir()) continue;
                Direction in = Math.abs(Math.cos(a)) > Math.abs(Math.sin(a)) ? (Math.cos(a) > 0 ? Direction.WEST : Direction.EAST)
                        : (Math.sin(a) > 0 ? Direction.NORTH : Direction.SOUTH);
                switch (f) {
                    case 1 -> { // the library: shelves two high
                        set(level, x, fy, z, Blocks.BOOKSHELF.defaultBlockState());
                        set(level, x, fy + 1, z, Blocks.BOOKSHELF.defaultBlockState());
                    }
                    case 2 -> set(level, x, fy, z, deg % 24 == 0 ? Blocks.CARTOGRAPHY_TABLE.defaultBlockState() : Furniture.cabinet("dark_oak", in));
                    case 3 -> { if (deg % 32 == 0) set(level, x, fy, z, Furniture.desk("dark_oak", in)); }
                    default -> { if (deg % 24 == 0) set(level, x, fy, z, Furniture.drawer("dark_oak", in)); }
                }
            }
            // lanterns under each floor above (the top floor has the dome) and desks with candles
            for (int[] o : new int[][]{{5, 0}, {-5, 0}, {0, 5}, {0, -5}}) {
                if (f < 3) set(level, cx + o[0], fy + FLOOR - 2, cz + o[1], Blocks.LANTERN.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
            }
            // a round rug around the well, and four lamps on brass posts
            for (int dx = -6; dx <= 6; dx++) {
                for (int dz = -6; dz <= 6; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d < 3.6 || d > 5.4 || stairs.contains(BlockPos.asLong(cx + dx, fy, cz + dz))) continue;
                    if (!level.getBlockState(new BlockPos(cx + dx, fy, cz + dz)).isAir()) continue;
                    set(level, cx + dx, fy, cz + dz, (d > 4.6 ? Blocks.YELLOW_CARPET : Blocks.RED_CARPET).defaultBlockState());
                }
            }
            for (int[] o : new int[][]{{7, 2}, {-7, 2}, {7, -2}, {-7, -2}}) {
                if (stairs.contains(BlockPos.asLong(cx + o[0], fy, cz + o[1]))) continue;
                set(level, cx + o[0], fy, cz + o[1], trim());
                set(level, cx + o[0], fy + 1, cz + o[1], lamp());
            }
            set(level, cx + 3, fy, cz + 7, Blocks.POTTED_FLOWERING_AZALEA.defaultBlockState());
            set(level, cx - 3, fy, cz + 7, Blocks.POTTED_FERN.defaultBlockState());
            if (f == 1 || f == 2) {
                set(level, cx + 5, fy, cz + 3, Furniture.desk("dark_oak", Direction.WEST));
                set(level, cx + 4, fy, cz + 3, Furniture.chair("dark_oak", Direction.EAST));
                set(level, cx + 5, fy + 1, cz + 3, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
                set(level, cx - 5, fy, cz - 3, Blocks.LECTERN.defaultBlockState());
            }
        }
        // the brass telescope on the top floor, pointing at the lens in the dome
        int ty = y + 3 * FLOOR;
        set(level, cx + 4, ty, cz - 4, trim());
        for (int i = 1; i <= 4; i++) set(level, cx + 4 - i / 2, ty + i, cz - 4 + i / 2, SoFEBlocks.SULTHARI_BRASS_PLATING.get().defaultBlockState());
        set(level, cx + 2, ty + 5, cz - 2, Blocks.GLASS.defaultBlockState());
        set(level, cx + 5, ty, cz - 3, Furniture.chair("dark_oak", Direction.NORTH));
        // an armillary ring of gold by the well
        for (int deg = 0; deg < 360; deg += 30) {
            double a = Math.toRadians(deg);
            set(level, cx - 5 + (int) Math.round(1.5 * Math.cos(a)), ty + 1 + (int) Math.round(1.5 * Math.sin(a)), cz + 4, Blocks.GOLD_BLOCK.defaultBlockState());
        }
        set(level, cx - 5, ty, cz + 4, trim());
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
        // the bazaar's own merchants, each behind a stall (Ferid and Yusuf have their places from the layout)
        String[][] merchants = {{"bazaar_spicer", "alchemist"}, {"bazaar_weaver", "quartermaster"},
                {"bazaar_fruiterer", "quartermaster"}, {"bazaar_lampwright", "smith"}, {"kasim", "gambler"}};
        int[][] spots = {{minX + 4, minZ + 12}, {minX + 20, minZ + 30}, {minX + 52, minZ + 12}, {minX + 28, minZ + 39}, {minX + 44, minZ + 30}};
        for (int m = 0; m < merchants.length; m++) {
            citizen(level, merchants[m][0], "merchant", merchants[m][1], spots[m][0] + 2, spots[m][1] + 2, 180);
        }
        // a dirt lane down the middle and shade trees
        for (int x = minX; x <= maxX; x++) {
            for (int dz = -1; dz <= 1; dz++) set(level, x, y - 1, s.z() + dz, dz == 0 ? Blocks.DIRT_PATH.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState());
        }
        for (int x = minX + 8; x < maxX - 4; x += 16) Architecture.acacia(level, x, y, s.z() + 3, random);
        // shops along the north and south edges, doors facing the stalls
        for (int x = minX + 1; x + 8 <= maxX; x += 10) {
            Architecture.house(level, x, minZ + 1, 8, 7, y, 5, Direction.SOUTH, houseStyle(random));
            Architecture.house(level, x, maxZ - 7, 8, 7, y, 5, Direction.NORTH, houseStyle(random));
        }
    }

    private static void citizen(ServerLevel level, String npc, String type, String role, int x, int z, float yaw) {
        com.sofe.world.StoryPlacements.spawnNpc(level, new StructurePositions.Npc(npc, type, x, z, yaw, role));
    }

    // --- the lower district

    /**
     * Houses on plots of 13 blocks along dirt streets, each with its own colors and roof, a few
     * gardens with trees. Each plot follows the ground.
     */
    private static void district(ServerLevel level, StructurePositions.Structure s) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        Random random = new Random(s.x() * 31L + s.z());
        for (int x = minX; x + 12 <= maxX; x += 13) {
            for (int z = minZ; z + 12 <= maxZ; z += 13) {
                int y = surfaceY(level, x + 6, z + 6);
                pad(level, x, z, x + 12, z + 12, y, Blocks.DIRT.defaultBlockState());
                for (int a = 0; a <= 12; a++) {
                    for (int b = 0; b <= 12; b++) {
                        boolean street = a < 2 || b < 2;
                        set(level, x + a, y - 1, z + b, street ? (random.nextInt(4) == 0 ? Blocks.COARSE_DIRT : Blocks.DIRT_PATH).defaultBlockState()
                                : SultharisCity.ground(x + a, z + b));
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

    /** The training grounds: a small fenced archery range of three lanes, with shade and benches around it. */
    private static void trainingGrounds(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()), maxX = s.x() + half(s.sizeX()) - 1;
        int minZ = s.z() - half(s.sizeZ()), maxZ = s.z() + half(s.sizeZ()) - 1;
        pad(level, minX, minZ, maxX, maxZ, y, Blocks.DIRT.defaultBlockState());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) set(level, x, y - 1, z, SultharisCity.ground(x, z));
        }
        int rx0 = s.x() - 7, rx1 = s.x() + 7, rz0 = s.z() - 12, rz1 = s.z() + 12;
        for (int x = rx0; x <= rx1; x++) {
            for (int z = rz0; z <= rz1; z++) {
                set(level, x, y - 1, z, Blocks.PACKED_MUD.defaultBlockState());
                boolean edge = x == rx0 || x == rx1 || z == rz0 || z == rz1;
                boolean gate = z == rz0 && Math.abs(x - s.x()) <= 1;
                if (edge && !gate) {
                    set(level, x, y, z, Blocks.ACACIA_FENCE.defaultBlockState());
                    if (Math.floorMod(x + z, 6) == 0) {
                        set(level, x, y, z, Blocks.RED_TERRACOTTA.defaultBlockState());
                        set(level, x, y + 1, z, Blocks.LANTERN.defaultBlockState());
                    }
                }
            }
        }
        for (int lane = -1; lane <= 1; lane++) { // three lanes: a target on hay at the far end, a mark to shoot from
            int x = s.x() + lane * 4;
            set(level, x, y, rz1 - 2, Blocks.HAY_BLOCK.defaultBlockState());
            set(level, x, y + 1, rz1 - 2, Blocks.TARGET.defaultBlockState());
            set(level, x, y - 1, rz0 + 3, Blocks.RED_TERRACOTTA.defaultBlockState());
        }
        Random random = new Random(s.x() * 31L + s.z());
        for (int[] c : new int[][]{{minX + 5, minZ + 5}, {maxX - 5, minZ + 5}, {minX + 5, maxZ - 5}, {maxX - 5, maxZ - 5}}) {
            Architecture.acacia(level, c[0], y, c[1], random);
        }
        for (int z = rz0 + 2; z < rz1; z += 5) { // benches along the range
            set(level, rx0 - 2, y, z, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.WEST));
            set(level, rx1 + 2, y, z, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(net.minecraft.world.level.block.StairBlock.FACING, Direction.EAST));
        }
        Architecture.stall(level, minX + 3, y, s.z() - 2, Blocks.RED_WOOL.defaultBlockState(), Blocks.WHITE_WOOL.defaultBlockState(), random);
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
        // the Imperial Forge in the middle, over a lava basin behind bars, anvils and grindstones around it
        set(level, s.x(), y, s.z() - 4, SoFEBlocks.IMPERIAL_FORGE.get().defaultBlockState());
        for (int dx = -2; dx <= 2; dx++) {
            set(level, s.x() + dx, y - 1, s.z() - 7, Blocks.LAVA.defaultBlockState());
            set(level, s.x() + dx, y, s.z() - 7, Blocks.IRON_BARS.defaultBlockState());
        }
        set(level, s.x() - 3, y, s.z() - 2, Blocks.ANVIL.defaultBlockState());
        set(level, s.x() + 3, y, s.z() - 2, Blocks.ANVIL.defaultBlockState());
        set(level, s.x() - 3, y, s.z() + 2, Blocks.GRINDSTONE.defaultBlockState());
        set(level, s.x() + 3, y, s.z() + 2, Blocks.SMITHING_TABLE.defaultBlockState());
        // furnaces and work benches along the walls
        for (int a = 2; a < w - 2; a += 2) {
            set(level, minX + a, y, minZ + 1, a % 4 == 0 ? Blocks.BLAST_FURNACE.defaultBlockState() : Blocks.FURNACE.defaultBlockState());
            set(level, minX + 1, y, minZ + a, a % 6 == 0 ? Blocks.CHEST.defaultBlockState() : Furniture.crate("dark_oak"));
            set(level, minX + w - 2, y, minZ + a, a % 4 == 0 ? Blocks.CRAFTING_TABLE.defaultBlockState() : Furniture.drawer("dark_oak", Direction.WEST));
        }
        for (int a = 3; a < w - 2; a += 6) {
            for (int b = 3; b < d - 2; b += 6) Architecture.chandelier(level, minX + a, y + 6, y + 4, minZ + b);
        }
    }

    /** The bank: a stout white house with a small red dome. */
    private static void bank(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - half(s.sizeX()) + 2, minZ = s.z() - half(s.sizeZ()) + 2;
        pad(level, minX - 2, minZ - 2, minX + s.sizeX() - 3, minZ + s.sizeZ() - 3, y, bricks(), 20);
        Architecture.HouseStyle style = new Architecture.HouseStyle(WHITE, trim(), Blocks.SANDSTONE_STAIRS.defaultBlockState(),
                Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState(), Blocks.IRON_DOOR.defaultBlockState(), Blocks.RED_WOOL.defaultBlockState(), false);
        int w = s.sizeX() - 4, d = s.sizeZ() - 4;
        Architecture.house(level, minX, minZ, w, d, y, 7, s.z() > 0 ? Direction.NORTH : Direction.SOUTH, style);
        Architecture.openRoof(level, minX + w / 2, y + 7, minZ + d / 2, 4);
        int crown = Architecture.dome(level, minX + w / 2, y + 8, minZ + d / 2, 4, RED_DOME, GOLD, Blocks.LIGHTNING_ROD.defaultBlockState());
        Architecture.chandelier(level, minX + w / 2, crown - 1, y + 4, minZ + d / 2);
        // the clerks' desks along the side walls, the strongroom behind bars at the far end
        for (int a = 3; a < d - 3; a += 3) {
            set(level, minX + 2, y, minZ + a, Furniture.desk("dark_oak", Direction.EAST));
            set(level, minX + 1, y, minZ + a, Furniture.chair("dark_oak", Direction.EAST));
            set(level, minX + w - 3, y, minZ + a, Furniture.desk("dark_oak", Direction.WEST));
            set(level, minX + w - 2, y, minZ + a, Furniture.chair("dark_oak", Direction.WEST));
            set(level, minX + 2, y + 1, minZ + a, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        }
        int far = s.z() > 0 ? minZ + d - 2 : minZ + 1, bars = s.z() > 0 ? far - 2 : far + 2;
        for (int x = minX + 1; x < minX + w - 1; x++) {
            set(level, x, y, far, x % 3 == 0 ? GOLD : Blocks.CHEST.defaultBlockState());
            if (Math.abs(x - (minX + w / 2)) > 1) for (int dy = 0; dy < 3; dy++) set(level, x, y + dy, bars, Blocks.IRON_BARS.defaultBlockState());
        }
        for (int[] c : new int[][]{{minX + 3, minZ + 3}, {minX + w - 4, minZ + 3}, {minX + 3, minZ + d - 4}, {minX + w - 4, minZ + d - 4}}) {
            set(level, c[0], y + 6, c[1], Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
        }
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
