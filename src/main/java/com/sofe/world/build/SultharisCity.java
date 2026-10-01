package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import com.sofe.world.StoryPlacements;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.sofe.world.build.SultharisBuilder.set;

/**
 * The ground and the frame of Sulthari, after its concept (the city on the terracotta mesa): a
 * rounded mesa raised above the desert and the sea, with cliffs of banded terracotta; cream walls
 * with merlons along its rim, square towers with crescent banners and four gatehouses; raised
 * terraces for the palace and the Observatory; dirt avenues from the gates to the plaza, roads going
 * down from the gates (fenced causeways over the water) and to every place; and houses, gardens and
 * citizens filling the rest of the city. The places themselves are built afterwards on this ground.
 */
final class SultharisCity {
    /** The rim of the mesa is a rounded square: |x/a|^8 + |z/a|^8 = 1. */
    private static final int SHAPE = 8, CLIFF = 16, WALL_HEIGHT = 6, PLOT = 13;
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState CREAM = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
    private static final BlockState CREAM_CUT = Blocks.CUT_SANDSTONE.defaultBlockState();
    private static final BlockState[] GROUNDS = {Blocks.TERRACOTTA.defaultBlockState(), Blocks.COARSE_DIRT.defaultBlockState(),
            Blocks.DIRT.defaultBlockState(), Blocks.SAND.defaultBlockState(), Blocks.PACKED_MUD.defaultBlockState()};

    /** The ground at a column: soft patches of earth, coarse earth, terracotta, sand and packed mud. */
    static BlockState ground(int x, int z) {
        double n = Math.sin(x * 0.13) + Math.sin(z * 0.11) + Math.sin((x + z) * 0.07) + 0.6 * Math.sin((x - z) * 0.23);
        if (n > 1.6) return GROUNDS[1];
        if (n > -0.5) return Blocks.GRASS_BLOCK.defaultBlockState(); // the city is green between its houses
        if (n > -1.1) return GROUNDS[2];
        if (n > -1.6) return GROUNDS[0];
        if (n > -2.1) return GROUNDS[4];
        return GROUNDS[3];
    }
    private static final BlockState FILL = Blocks.SANDSTONE.defaultBlockState();

    /** Banded terracotta, mostly red and orange with pale bands, as on the cliffs of the concept. */
    private static final Block[] STRATA = {Blocks.RED_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.TERRACOTTA, Blocks.ORANGE_TERRACOTTA,
            Blocks.RED_TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.ORANGE_TERRACOTTA,
            Blocks.RED_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.ORANGE_TERRACOTTA};

    static final Roads.Style ROADS = new Roads.Style(Blocks.ACACIA_FENCE.defaultBlockState(), Blocks.RED_TERRACOTTA.defaultBlockState(),
            FILL, SultharisCity::stratum);

    private SultharisCity() {
    }

    static BlockState stratum(int y) {
        return STRATA[Math.floorMod(y, STRATA.length)].defaultBlockState();
    }

    private record Rect(int minX, int minZ, int maxX, int maxZ) {
        Rect grow(int by) {
            return new Rect(minX - by, minZ - by, maxX + by, maxZ + by);
        }

        boolean overlaps(Rect o) {
            return minX <= o.maxX && maxX >= o.minX && minZ <= o.maxZ && maxZ >= o.minZ;
        }

        static Rect of(StructurePositions.Structure s) {
            return new Rect(s.x() - s.sizeX() / 2, s.z() - s.sizeZ() / 2, s.x() + s.sizeX() / 2 - 1, s.z() + s.sizeZ() / 2 - 1);
        }
    }

    static void build(ServerLevel level, StructurePositions.Structure city) {
        int a = Math.min(city.sizeX(), city.sizeZ()) / 2;
        int top = plateauLevel(level, city, a);
        mesa(level, city, a, top);
        StructurePositions.Layout layout = StructurePositions.get();
        layout.structure("sofe:sulthari/palace").ifPresent(p -> squareTerrace(level, Rect.of(p).grow(3), top, 7, Direction.NORTH, p.x()));
        layout.structure("sofe:sulthari/great_observatory").ifPresent(o -> roundTerrace(level, o.x(), o.z(), 16, top, 5, Direction.SOUTH));
        List<Rect> roads = roads(level, city, a, top, layout);
        walls(level, city, a, top);
        fill(level, city, a, top, layout, roads);
    }

    // --- the mesa

    /** How far out of the rim a column is (negative: inside), in blocks. */
    private static double outside(StructurePositions.Structure city, int a, int x, int z) {
        double nx = Math.abs(x - city.x()) / (double) a, nz = Math.abs(z - city.z()) / (double) a;
        double rho = Math.pow(Math.pow(nx, SHAPE) + Math.pow(nz, SHAPE), 1.0 / SHAPE);
        return (rho - 1) * a;
    }

    /** The mesa stands well above the desert and the sea, like the concept, and above most of the ground it covers. */
    private static int plateauLevel(ServerLevel level, StructurePositions.Structure city, int a) {
        List<Integer> heights = new ArrayList<>();
        for (int x = city.x() - a; x <= city.x() + a; x += 16) {
            for (int z = city.z() - a; z <= city.z() + a; z += 16) heights.add(SultharisBuilder.surfaceY(level, x, z));
        }
        heights.sort(Integer::compare);
        int median = heights.get(heights.size() / 2), sea = level.getSeaLevel();
        return Math.max(sea + 18, Math.min(median + 12, sea + 34));
    }

    private static void mesa(ServerLevel level, StructurePositions.Structure city, int a, int top) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = city.x() - a - CLIFF; x <= city.x() + a + CLIFF; x++) {
            for (int z = city.z() - a - CLIFF; z <= city.z() + a + CLIFF; z++) {
                double out = outside(city, a, x, z);
                if (out > CLIFF) continue;
                level.getChunk(x >> 4, z >> 4);
                int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                double angle = Math.atan2(z - city.z(), x - city.x());
                int shift = (int) Math.round(1.5 * Math.sin(angle * 3) + Math.sin(angle * 11)); // the bands wave a little
                if (out <= 0) {
                    // the top of the mesa: fill up to it, clear the hills above it
                    for (int y = floor - 1; y < top - 1; y++) {
                        pos.set(x, y, z);
                        BlockState state = level.getBlockState(pos);
                        if (y >= top - 4 || state.isAir() || !state.getFluidState().isEmpty() || state.canBeReplaced()) {
                            set(level, x, y, z, out > -4 ? stratum(y + shift) : FILL);
                        }
                    }
                    set(level, x, top - 1, z, ground(x, z));
                    for (int y = top; y <= surface + 1; y++) set(level, x, y, z, AIR);
                } else {
                    // the cliffs: steep, uneven, banded, down to the ground or into the sea
                    double bulge = 1.6 * Math.sin(angle * 7) + Math.sin(angle * 17 + 1);
                    int cliffTop = top - 1 - (int) Math.round(out * 2.6 + bulge);
                    if (cliffTop < floor) continue;
                    for (int y = floor - 2; y <= cliffTop; y++) set(level, x, y, z, stratum(y + shift));
                    for (int y = cliffTop + 1; y <= Math.max(surface, cliffTop + 1) + 1 && y < top + 2; y++) {
                        pos.set(x, y, z);
                        if (level.getFluidState(pos).isEmpty()) set(level, x, y, z, AIR);
                    }
                }
            }
        }
    }

    // --- terraces

    /** A raised terrace with banded retaining walls and a grand stair in the middle of one side. */
    private static void squareTerrace(ServerLevel level, Rect r, int top, int rise, Direction stairSide, int stairCenter) {
        for (int x = r.minX(); x <= r.maxX(); x++) {
            for (int z = r.minZ(); z <= r.maxZ(); z++) {
                boolean edge = x - r.minX() < 2 || r.maxX() - x < 2 || z - r.minZ() < 2 || r.maxZ() - z < 2;
                for (int y = top - 1; y < top - 1 + rise; y++) set(level, x, y, z, edge ? stratum(y) : FILL);
                set(level, x, top - 1 + rise, z, edge ? CREAM_CUT : ground(x, z));
                if (edge && Math.floorMod(x + z, 2) == 0) {
                    set(level, x, top + rise, z, CREAM);
                    if (Math.floorMod(x + z, 10) == 0) set(level, x, top + rise + 1, z, Blocks.LANTERN.defaultBlockState());
                }
            }
        }
        int edgeZ = stairSide == Direction.NORTH ? r.minZ() : r.maxZ();
        stairs(level, stairCenter, edgeZ, stairSide, top, rise, 3);
    }

    /** A round terrace (the Observatory's), banded, with a stair on one side. */
    private static void roundTerrace(ServerLevel level, int cx, int cz, int radius, int top, int rise, Direction stairSide) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > radius + 0.3) continue;
                boolean edge = d > radius - 1.5;
                for (int y = top - 1; y < top - 1 + rise; y++) set(level, cx + dx, y, cz + dz, edge ? stratum(y) : FILL);
                set(level, cx + dx, top - 1 + rise, cz + dz, edge ? CREAM_CUT : ground(cx + dx, cz + dz));
                if (edge && Math.floorMod(dx + dz, 2) == 0) {
                    set(level, cx + dx, top + rise, cz + dz, CREAM);
                    if (Math.floorMod(dx + dz, 10) == 0) set(level, cx + dx, top + rise + 1, cz + dz, Blocks.LANTERN.defaultBlockState());
                }
            }
        }
        stairs(level, cx, cz + stairSide.getStepZ() * radius, stairSide, top, rise, 2);
    }

    /** A stair going down from a terrace's edge, out in direction out, half wide on each side of center. */
    private static void stairs(ServerLevel level, int center, int edge, Direction out, int top, int rise, int half) {
        for (int k = 1; k <= 2; k++) { // open the merlons of the terrace's rim above the stair
            for (int dx = -half + 1; dx < half; dx++) set(level, center + dx, top + rise, edge - out.getStepZ() * k, AIR);
        }
        for (int i = 0; i < rise; i++) {
            int z = edge + out.getStepZ() * i;
            int y = top - 1 + rise - i;
            for (int dx = -half; dx <= half; dx++) {
                for (int yy = top - 1; yy < y; yy++) set(level, center + dx, yy, z, Math.abs(dx) == half ? stratum(yy) : FILL);
                set(level, center + dx, y, z, Math.abs(dx) == half ? CREAM : Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, out.getOpposite()));
                set(level, center + dx, y + 1, z, Math.abs(dx) == half && i % 2 == 0 ? Blocks.SANDSTONE_WALL.defaultBlockState() : AIR);
                set(level, center + dx, y + 2, z, AIR);
            }
        }
    }

    // --- the walls

    private static void walls(ServerLevel level, StructurePositions.Structure city, int a, int top) {
        for (int x = city.x() - a - 1; x <= city.x() + a + 1; x++) {
            for (int z = city.z() - a - 1; z <= city.z() + a + 1; z++) {
                double out = outside(city, a, x, z);
                if (out > 0 || out < -3) continue;
                if (Math.abs(x - city.x()) <= 2 || Math.abs(z - city.z()) <= 2) continue; // the gates
                for (int dy = 0; dy < WALL_HEIGHT; dy++) set(level, x, top + dy, z, dy == WALL_HEIGHT - 2 ? CREAM_CUT : CREAM);
                if (out > -1) {
                    if (Math.floorMod(x + z, 2) == 0) {
                        set(level, x, top + WALL_HEIGHT, z, CREAM);
                        if (Math.floorMod(x + z, 12) == 0) set(level, x, top + WALL_HEIGHT + 1, z, Blocks.LANTERN.defaultBlockState());
                    }
                } else if (Math.floorMod(x * 7 + z * 13, 11) == 0) {
                    set(level, x, top + WALL_HEIGHT, z, SoFEBlocks.SULTHARI_AETHERIUM_LAMP.get().defaultBlockState());
                }
            }
        }
        // towers along the rim, and two at each gate
        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2 * i / 24 + Math.PI / 24;
            double c = Math.cos(angle), s = Math.sin(angle);
            double scale = Math.pow(Math.pow(Math.abs(c), SHAPE) + Math.pow(Math.abs(s), SHAPE), -1.0 / SHAPE);
            int x = city.x() + (int) Math.round(c * scale * (a - 2)), z = city.z() + (int) Math.round(s * scale * (a - 2));
            Direction out = Math.abs(c) > Math.abs(s) ? (c > 0 ? Direction.EAST : Direction.WEST) : (s > 0 ? Direction.SOUTH : Direction.NORTH);
            tower(level, x, z, top, out, 3, 13);
        }
        for (Direction out : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            int gx = city.x() + out.getStepX() * (a - 2), gz = city.z() + out.getStepZ() * (a - 2);
            for (int side : new int[]{-6, 6}) {
                int tx = gx + (out.getAxis() == Direction.Axis.Z ? side : 0), tz = gz + (out.getAxis() == Direction.Axis.X ? side : 0);
                tower(level, tx, tz, top, out, 3, 15);
            }
            // the arch over the gate
            for (int b = -3; b <= 3; b++) {
                for (int k = -2; k <= 0; k++) {
                    int x = gx + (out.getAxis() == Direction.Axis.Z ? b : out.getStepX() * k);
                    int z = gz + (out.getAxis() == Direction.Axis.X ? b : out.getStepZ() * k);
                    for (int dy = 4; dy < WALL_HEIGHT + 2; dy++) set(level, x, top + dy, z, dy == 4 ? SoFEBlocks.SULTHARI_BRASS_TRIM.get().defaultBlockState() : CREAM);
                    if (Math.abs(b) == 3) for (int dy = 0; dy < 4; dy++) set(level, x, top + dy, z, CREAM_CUT);
                }
            }
            crescentBanner(level, gx + out.getStepX(), top + 6, gz + out.getStepZ(), out);
        }
    }

    /** A square cream tower with merlons, a dark window and a crescent banner facing out. */
    private static void tower(ServerLevel level, int x, int z, int top, Direction out, int half, int height) {
        Architecture.tower(level, x, top - 2, z, half, height + 2, CREAM, CREAM_CUT);
        set(level, x + out.getStepX() * half, top + height - 3, z + out.getStepZ() * half, Blocks.BLACK_STAINED_GLASS_PANE.defaultBlockState());
        crescentBanner(level, x + out.getStepX() * (half + 1), top + height - 6, z + out.getStepZ() * (half + 1), out);
    }

    /** A red banner with a white crescent: a white roundel cut on its right side. */
    static void crescentBanner(ServerLevel level, int x, int y, int z, Direction facing) {
        BlockPos pos = new BlockPos(x, y, z);
        level.setBlock(pos, Blocks.RED_WALL_BANNER.defaultBlockState().setValue(WallBannerBlock.FACING, facing), Block.UPDATE_CLIENTS);
        if (level.getBlockEntity(pos) instanceof BannerBlockEntity banner) {
            ListTag patterns = new ListTag();
            for (String[] p : new String[][]{{"mc", "white"}, {"rs", "red"}}) {
                CompoundTag layer = new CompoundTag();
                layer.putString("Pattern", p[0]);
                layer.putInt("Color", DyeColor.byName(p[1], DyeColor.WHITE).getId());
                patterns.add(layer);
            }
            CompoundTag data = new CompoundTag();
            data.put("Patterns", patterns);
            ItemStack stack = new ItemStack(Items.RED_BANNER);
            BlockItem.setBlockEntityData(stack, BlockEntityType.BANNER, data);
            banner.fromItem(stack, DyeColor.RED);
            banner.setChanged();
            level.sendBlockUpdated(pos, banner.getBlockState(), banner.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // --- roads

    /** The avenues, the roads down from the gates and a branch to every place; returns where they run. */
    private static List<Rect> roads(ServerLevel level, StructurePositions.Structure city, int a, int top, StructurePositions.Layout layout) {
        List<Rect> used = new ArrayList<>();
        int cx = city.x(), cz = city.z();
        Roads.street(level, cx, cz - a + 4, cx, cz + a - 4, 5, ROADS);
        Roads.street(level, cx - a + 4, cz, cx + a - 4, cz, 5, ROADS);
        used.add(new Rect(cx - 4, cz - a, cx + 4, cz + a));
        used.add(new Rect(cx - a, cz - 4, cx + a, cz + 4));
        for (Direction out : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            int gx = cx + out.getStepX() * a, gz = cz + out.getStepZ() * a;
            Roads.outbound(level, gx, gz, out, 3 * 34 + 16, top, 5, ROADS);
        }
        for (StructurePositions.Structure s : layout.structures().values()) {
            if (s == city || s.id().endsWith("/homestead") || outside(city, a, s.x(), s.z()) > 0) continue;
            // an L from the place to the nearer avenue
            if (Math.abs(s.x() - cx) < Math.abs(s.z() - cz)) {
                Roads.street(level, s.x(), s.z(), cx, s.z(), 3, ROADS);
                used.add(new Rect(Math.min(s.x(), cx) - 3, s.z() - 3, Math.max(s.x(), cx) + 3, s.z() + 3));
            } else {
                Roads.street(level, s.x(), s.z(), s.x(), cz, 3, ROADS);
                used.add(new Rect(s.x() - 3, Math.min(s.z(), cz) - 3, s.x() + 3, Math.max(s.z(), cz) + 3));
            }
        }
        return used;
    }

    // --- houses, gardens and citizens

    /** Citizens who talk about what is happening (data/sofe/dialogue/citizen_*). */
    static final List<String> CITIZENS = List.of("citizen_baker", "citizen_scholar", "citizen_guard", "citizen_weaver",
            "citizen_pilgrim", "citizen_widow", "citizen_clockmaker", "citizen_child", "citizen_tram_keeper", "citizen_fisherman",
            "citizen_veteran", "citizen_courier", "citizen_astronomer", "citizen_tea_seller");

    private static void fill(ServerLevel level, StructurePositions.Structure city, int a, int top, StructurePositions.Layout layout, List<Rect> roads) {
        List<Rect> taken = new ArrayList<>(roads);
        for (StructurePositions.Structure s : layout.structures().values()) {
            if (s != city) taken.add(Rect.of(s).grow(5));
        }
        layout.structure("sofe:sulthari/great_observatory").ifPresent(o -> taken.add(new Rect(o.x() - 22, o.z() - 22, o.x() + 22, o.z() + 22)));
        for (StructurePositions.Npc npc : layout.npcs()) taken.add(new Rect(npc.x() - 6, npc.z() - 6, npc.x() + 6, npc.z() + 6));
        for (BlockPos w : layout.waystones().values()) taken.add(new Rect(w.getX() - 6, w.getZ() - 6, w.getX() + 6, w.getZ() + 6));
        Random random = new Random(city.x() * 31L + city.z() + 7);
        List<int[]> doors = new ArrayList<>(); // x, z, yaw in front of each house
        for (int x = city.x() - a; x + PLOT <= city.x() + a; x += PLOT) {
            for (int z = city.z() - a; z + PLOT <= city.z() + a; z += PLOT) {
                Rect plot = new Rect(x, z, x + PLOT - 1, z + PLOT - 1);
                if (taken.stream().anyMatch(plot::overlaps)) continue;
                boolean nearWall = Arrays.stream(new int[][]{{x, z}, {x + PLOT, z}, {x, z + PLOT}, {x + PLOT, z + PLOT}})
                        .anyMatch(c -> outside(city, a, c[0], c[1]) > -12);
                if (nearWall) continue;
                int roll = random.nextInt(100);
                int y = top;
                set(level, x, y, z, Blocks.RED_TERRACOTTA.defaultBlockState()); // a lantern post at the corner of each plot
                set(level, x, y + 1, z, Blocks.ACACIA_FENCE.defaultBlockState());
                set(level, x, y + 2, z, Blocks.LANTERN.defaultBlockState());
                if (roll >= 10 && roll < 18) { // a field of crops
                    field(level, x, z, y, random);
                } else if (roll >= 18 && roll < 24) { // a pen with animals
                    pen(level, x, z, y, random);
                } else if (roll < 10) { // a garden with a tree and a well
                    Architecture.acacia(level, x + 4, y, z + 4, random);
                    well(level, x + 8, y, z + 8);
                    set(level, x + 10, y, z + 3, Blocks.RED_TERRACOTTA.defaultBlockState());
                    set(level, x + 10, y + 1, z + 3, Blocks.ACACIA_FENCE.defaultBlockState());
                    set(level, x + 10, y + 2, z + 3, Blocks.LANTERN.defaultBlockState());
                } else if (roll < 28) { // a little white chapel with a golden dome and a minaret
                    chapel(level, x + 2, z + 2, y, random);
                } else {
                    int w = 7 + random.nextInt(3), d = 7 + random.nextInt(3);
                    Direction door = random.nextBoolean() ? Direction.NORTH : Direction.WEST;
                    int height = 4 + random.nextInt(4);
                    Architecture.house(level, x + 3, z + 3, w, d, y, height, door, houseStyle(random));
                    if (random.nextInt(3) == 0) Architecture.acacia(level, x + 1, y, z + PLOT - 2, random);
                    furnish(level, x + 3, z + 3, w, d, y, random);
                    int nx = door == Direction.NORTH ? x + 3 + w / 2 : x + 1;
                    int nz = door == Direction.NORTH ? z + 1 : z + 3 + d / 2;
                    doors.add(new int[]{nx, nz, door == Direction.NORTH ? 180 : 90});
                }
            }
        }
        // each citizen lives at one of the houses, spread over the whole city
        if (doors.isEmpty()) return;
        java.util.Collections.shuffle(doors, random);
        for (int i = 0; i < CITIZENS.size() && i < doors.size(); i++) {
            int[] at = doors.get(i);
            StoryPlacements.spawnNpc(level, new StructurePositions.Npc(CITIZENS.get(i), "citizen", at[0], at[1], at[2], null));
        }
    }

    private static final String[] DIVANS = {"red", "orange", "yellow", "brown"};

    /**
     * The inside of a house, with the furniture mod: a bed with a carpet, a table with two chairs and
     * a candle, a divan by the wall, a drawer, storage jars and a crate, and a cutting board.
     */
    private static void furnish(ServerLevel level, int minX, int minZ, int w, int d, int y, Random random) {
        String wood = random.nextBoolean() ? "spruce" : "dark_oak";
        int tx = minX + 2, tz = minZ + d - 3;
        set(level, tx, y, tz, Furniture.table(wood));
        set(level, tx, y + 1, tz, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 3)
                .setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        set(level, tx + 1, y, tz, Furniture.chair(wood, Direction.WEST));
        set(level, tx - 1, y, tz, Furniture.chair(wood, Direction.EAST));
        String color = DIVANS[random.nextInt(DIVANS.length)];
        for (int dz = 0; dz < 2; dz++) set(level, minX + w - 2, y, minZ + d - 3 + dz, Furniture.divan(color, Direction.WEST));
        set(level, minX + w - 2, y, minZ + 1, Furniture.drawer(wood, Direction.WEST));
        set(level, minX + w - 2, y + 1, minZ + 1, Furniture.jar(wood, Direction.WEST));
        set(level, minX + w - 3, y, minZ + 1, Furniture.crate(wood));
        set(level, minX + w - 3, y + 1, minZ + 1, Furniture.cuttingBoard(wood, Direction.SOUTH));
        BlockState bed = Blocks.RED_BED.defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.FACING, Direction.SOUTH);
        set(level, minX + 1, y, minZ + 2, bed.setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD));
        set(level, minX + 1, y, minZ + 1, bed.setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.FOOT));
        set(level, minX + 2, y, minZ + 1, Blocks.RED_CARPET.defaultBlockState());
        set(level, minX + 1, y, minZ + 3, Blocks.POTTED_CACTUS.defaultBlockState());
    }

    private static Architecture.HouseStyle houseStyle(Random random) {
        BlockState[] walls = {Blocks.YELLOW_TERRACOTTA.defaultBlockState(), Blocks.YELLOW_TERRACOTTA.defaultBlockState(),
                Blocks.ORANGE_TERRACOTTA.defaultBlockState(), CREAM, Blocks.WHITE_TERRACOTTA.defaultBlockState(),
                SoFEBlocks.SULTHARI_SANDSTONE_BRICKS.get().defaultBlockState()};
        BlockState[] awnings = {Blocks.RED_WOOL.defaultBlockState(), Blocks.WHITE_WOOL.defaultBlockState(),
                Blocks.YELLOW_WOOL.defaultBlockState(), Blocks.RED_WOOL.defaultBlockState()};
        boolean dark = random.nextInt(3) == 0;
        BlockState wall = walls[random.nextInt(walls.length)];
        BlockState frame = wall.is(Blocks.WHITE_TERRACOTTA) ? Blocks.RED_TERRACOTTA.defaultBlockState()
                : dark ? Blocks.DARK_OAK_PLANKS.defaultBlockState() : Blocks.SPRUCE_PLANKS.defaultBlockState();
        return new Architecture.HouseStyle(wall, frame,
                dark ? Blocks.DARK_OAK_STAIRS.defaultBlockState() : Blocks.SPRUCE_STAIRS.defaultBlockState(),
                dark ? Blocks.DARK_OAK_SLAB.defaultBlockState() : Blocks.SPRUCE_SLAB.defaultBlockState(),
                Blocks.SPRUCE_DOOR.defaultBlockState(), awnings[random.nextInt(awnings.length)], random.nextInt(4) > 0);
    }

    private static final net.minecraft.world.level.block.Block[] CROPS = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS};

    /** A fenced field: rows of wheat, carrots, potatoes or beetroots on farmland, watered by a channel. */
    private static void field(ServerLevel level, int x, int z, int y, Random random) {
        net.minecraft.world.level.block.Block crop = CROPS[random.nextInt(CROPS.length)];
        BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 7);
        fence(level, x + 1, z + 1, x + 11, z + 11, y);
        for (int a = x + 2; a <= x + 10; a++) {
            for (int b = z + 2; b <= z + 10; b++) {
                if (b == z + 6) {
                    set(level, a, y - 1, b, Blocks.WATER.defaultBlockState());
                    set(level, a, y, b, Blocks.AIR.defaultBlockState());
                    continue;
                }
                set(level, a, y - 1, b, farmland);
                int max = ((net.minecraft.world.level.block.CropBlock) crop).getMaxAge();
                set(level, a, y, b, ((net.minecraft.world.level.block.CropBlock) crop).getStateForAge(Math.max(1, max - random.nextInt(3))));
            }
        }
        set(level, x + 1, y, z + 12, Blocks.COMPOSTER.defaultBlockState());
        set(level, x + 11, y, z + 12, Blocks.HAY_BLOCK.defaultBlockState());
    }

    /** A pen of cows, pigs, sheep or chickens: grass, a fence with a gate, hay, a trough and a little shelter. */
    private static void pen(ServerLevel level, int x, int z, int y, Random random) {
        fence(level, x + 1, z + 1, x + 11, z + 11, y);
        for (int a = x + 2; a <= x + 10; a++) {
            for (int b = z + 2; b <= z + 10; b++) {
                set(level, a, y - 1, b, Blocks.GRASS_BLOCK.defaultBlockState());
                set(level, a, y, b, Blocks.AIR.defaultBlockState());
            }
        }
        set(level, x + 3, y, z + 3, Blocks.HAY_BLOCK.defaultBlockState());
        set(level, x + 4, y, z + 3, Blocks.HAY_BLOCK.defaultBlockState());
        set(level, x + 9, y - 1, z + 9, Blocks.WATER.defaultBlockState());
        set(level, x + 9, y - 1, z + 8, Blocks.WATER.defaultBlockState());
        for (int a = x + 7; a <= x + 10; a++) { // the shelter
            for (int dy = 0; dy < 3; dy++) {
                set(level, a, y + dy, z + 2, dy == 2 ? Blocks.DARK_OAK_SLAB.defaultBlockState() : a == x + 7 || a == x + 10 ? Blocks.DARK_OAK_FENCE.defaultBlockState() : Blocks.AIR.defaultBlockState());
            }
            set(level, a, y + 2, z + 3, Blocks.DARK_OAK_SLAB.defaultBlockState());
        }
        net.minecraft.world.entity.EntityType<?>[] kinds = {net.minecraft.world.entity.EntityType.COW, net.minecraft.world.entity.EntityType.PIG,
                net.minecraft.world.entity.EntityType.SHEEP, net.minecraft.world.entity.EntityType.CHICKEN};
        net.minecraft.world.entity.EntityType<?> kind = kinds[random.nextInt(kinds.length)];
        for (int i = 0; i < 3 + random.nextInt(2); i++) {
            net.minecraft.world.entity.Entity animal = kind.create(level);
            if (animal == null) continue;
            animal.moveTo(x + 4.5 + random.nextInt(4), y, z + 5.5 + random.nextInt(4), random.nextFloat() * 360, 0);
            if (animal instanceof net.minecraft.world.entity.Mob mob) mob.setPersistenceRequired();
            level.addFreshEntity(animal);
        }
    }

    /** An acacia fence around a rectangle, with a gate in the middle of its north side and lanterns at the corners. */
    private static void fence(ServerLevel level, int minX, int minZ, int maxX, int maxZ, int y) {
        for (int a = minX; a <= maxX; a++) {
            for (int b = minZ; b <= maxZ; b++) {
                if (a != minX && a != maxX && b != minZ && b != maxZ) continue;
                boolean gate = b == minZ && a == (minX + maxX) / 2;
                boolean corner = (a == minX || a == maxX) && (b == minZ || b == maxZ);
                set(level, a, y, b, gate ? Blocks.ACACIA_FENCE_GATE.defaultBlockState() : corner ? Blocks.RED_TERRACOTTA.defaultBlockState()
                        : Blocks.ACACIA_FENCE.defaultBlockState());
                if (corner) set(level, a, y + 1, b, Blocks.LANTERN.defaultBlockState());
            }
        }
    }

    private static void well(ServerLevel level, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean rim = dx != 0 || dz != 0;
                set(level, x + dx, y - 1, z + dz, rim ? CREAM : Blocks.WATER.defaultBlockState());
                if (rim) set(level, x + dx, y, z + dz, Blocks.SANDSTONE_WALL.defaultBlockState());
            }
        }
        set(level, x, y + 2, z, Blocks.SPRUCE_SLAB.defaultBlockState());
    }

    private static void chapel(ServerLevel level, int x, int z, int y, Random random) {
        Architecture.HouseStyle white = new Architecture.HouseStyle(Blocks.SMOOTH_QUARTZ.defaultBlockState(), CREAM_CUT,
                Blocks.SANDSTONE_STAIRS.defaultBlockState(), Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState(), Blocks.SPRUCE_DOOR.defaultBlockState(),
                Blocks.RED_WOOL.defaultBlockState(), false);
        Architecture.house(level, x, z, 9, 9, y, 5, Direction.NORTH, white);
        Architecture.openRoof(level, x + 4, y + 5, z + 4, 3);
        BlockState shell = random.nextBoolean() ? Blocks.HONEYCOMB_BLOCK.defaultBlockState() : Blocks.RED_NETHER_BRICKS.defaultBlockState();
        Architecture.dome(level, x + 4, y + 6, z + 4, 3, shell, Blocks.GOLD_BLOCK.defaultBlockState(), Blocks.LIGHTNING_ROD.defaultBlockState());
        Architecture.minaret(level, x + 9, y, z + 9, 16, Blocks.SMOOTH_QUARTZ.defaultBlockState(), SoFEBlocks.SULTHARI_BRASS_TRIM.get().defaultBlockState(),
                Blocks.GOLD_BLOCK.defaultBlockState());
    }
}
