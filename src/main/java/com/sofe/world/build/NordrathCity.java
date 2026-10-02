package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import com.sofe.world.StoryPlacements;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.sofe.world.build.SultharisBuilder.set;

/**
 * The hold of the Nordrath clans, after its concept (art/concepts/city_nordrath.png): a fortress
 * city climbing the flank of a volcano in four walled terraces of grey stone, with round towers
 * under dark conical roofs; longhouses of dark timber with steep slate roofs; the jarl's great hall
 * at the top; rivers of lava running down from the crater into stone basins; a fjord on the west
 * with a long stair down to the harbour, piers and longboats; trestle bridges on the roads in;
 * snow everywhere and spruce forests around. Stairs climb from every terrace to the next.
 */
final class NordrathCity {
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final int[] RISE = {0, 9, 18, 27};           // the height of each terrace above the lowest
    private static final int[] RING = {140, 96, 62, 30};         // the outer edge of each terrace, from the hold
    private static final int VOLCANO_RADIUS = 80, VOLCANO_HEIGHT = 92, CRATER = 10;

    private final ServerLevel level;
    private final int cx, cz, base, sea;
    private final int fx, fz;   // the hold: the top of the city
    private final int vx, vz;   // the crater
    private final Random random;
    private final List<int[]> taken = new ArrayList<>();      // x0, z0, x1, z1 of what is built, plus margins
    private final List<int[]> doors = new ArrayList<>();      // where citizens live
    private final List<int[]> lava = new ArrayList<>();       // lava basins and rivers, kept away from timber

    /** The materials of a longhouse: dark slate-roofed houses for the clans, warm wood for the jarl's hall. */
    record Timber(BlockState planks, BlockState post, BlockState roofStairs, BlockState roofSlab, BlockState floor) {
    }

    private static Timber clanTimber() {
        return clanTimber(false);
    }

    /** The clans' houses: dark timber, and a roof of grey slate or, like the concept, of blue slate. */
    private static Timber clanTimber(boolean blue) {
        return new Timber(SoFEBlocks.NORDRATH_DARK_PLANKS.get().defaultBlockState(),
                SoFEBlocks.NORDRATH_DARK_TIMBER.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y),
                blue ? Blocks.DARK_PRISMARINE_STAIRS.defaultBlockState() : Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState(),
                blue ? Blocks.DARK_PRISMARINE_SLAB.defaultBlockState() : Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState());
    }

    private static Timber hallTimber() {
        return new Timber(Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(),
                Blocks.SPRUCE_STAIRS.defaultBlockState(), Blocks.SPRUCE_SLAB.defaultBlockState(), Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState());
    }

    private final java.util.Set<Long> lavaCells = new java.util.HashSet<>();
    private final List<int[]> towers = new ArrayList<>(); // x, z, radius of every tower, kept whole by later builds
    private int[] harbourArea = {0, 0, -1, -1};

    private NordrathCity(ServerLevel level, StructurePositions.Structure city) {
        this.level = level;
        this.cx = city.x();
        this.cz = city.z();
        this.sea = level.getSeaLevel();
        this.fx = cx;
        this.fz = cz - 30;
        this.vx = cx + 40;
        this.vz = cz - 96; // far enough back that its slope rises behind the hold, not over the hall
        this.random = new Random(city.x() * 31L + city.z() + 11);
        this.base = baseLevel(level, cx, cz, sea);
    }

    static void build(ServerLevel level, StructurePositions.Structure city) {
        NordrathCity c = new NordrathCity(level, city);
        c.shape();
        c.fjord();
        c.walls();
        c.volcanoFire();
        c.routes();
        c.buildings();
        c.harbour();
        c.trestle(c.fx, c.fz + RING[0] + 1, Direction.SOUTH, 90);
        c.trestle(c.fx + RING[0] + 1, c.fz, Direction.EAST, 70);
        c.forest();
        c.snow();
        c.sealLava(); // again, after everything that could have opened a side
        c.citizens();
    }

    // ------------------------------------------------------------------ the land

    private static int baseLevel(ServerLevel level, int cx, int cz, int sea) {
        List<Integer> heights = new ArrayList<>();
        for (int x = cx - 120; x <= cx + 120; x += 20) {
            for (int z = cz - 120; z <= cz + 120; z += 20) heights.add(SultharisBuilder.surfaceY(level, x, z));
        }
        heights.sort(Integer::compare);
        return Math.max(sea + 14, Math.min(heights.get(heights.size() / 2) + 4, sea + 36));
    }

    private double wobble(double angle) {
        return 3 * Math.sin(angle * 3) + 2 * Math.sin(angle * 7 + 1);
    }

    /** The terrace a column belongs to (0 the lowest, 3 the hold), or -1 outside the city. */
    private int tier(int x, int z) {
        double dx = x - fx, dz = z - fz;
        double d = Math.sqrt(dx * dx + dz * dz) + wobble(Math.atan2(dz, dx));
        for (int t = 3; t >= 0; t--) if (d < RING[t]) return t;
        return -1;
    }

    /** The height of the walking surface of the volcano at a column, or MIN_VALUE off its slopes. */
    private int volcano(int x, int z) {
        double d = Math.sqrt((x - vx) * (x - vx) + (z - vz) * (z - vz));
        if (d >= VOLCANO_RADIUS) return Integer.MIN_VALUE;
        if (d < CRATER) return base + VOLCANO_HEIGHT - 8;     // the crater floor, under its lava
        if (d < CRATER + 3) return base + VOLCANO_HEIGHT;     // the rim
        double t = (d - CRATER) / (VOLCANO_RADIUS - CRATER);
        double bumps = 2.5 * Math.sin(x / 9.0) * Math.cos(z / 11.0);
        return base + (int) Math.round(VOLCANO_HEIGHT * Math.pow(1 - t, 1.5) + bumps * (1 - t));
    }

    /** Where people walk at a column: the terrace, unless the volcano rises above it. */
    private int surface(int x, int z) {
        int t = tier(x, z);
        int terrace = t < 0 ? Integer.MIN_VALUE : base + RISE[t];
        return Math.max(terrace, volcano(x, z));
    }

    private boolean volcanic(int x, int z) {
        int t = tier(x, z);
        return volcano(x, z) > (t < 0 ? Integer.MIN_VALUE : base + RISE[t]);
    }

    private BlockState rock(int x, int y, int z) {
        double n = Math.sin(x * 0.31 + y * 0.17) + Math.cos(z * 0.27 - y * 0.11) + Math.sin((x + z) * 0.13);
        if (n > 1.6) return Blocks.MAGMA_BLOCK.defaultBlockState();
        if (n > 0.6) return Blocks.BLACKSTONE.defaultBlockState();
        if (n > -0.3) return Blocks.BASALT.defaultBlockState();
        if (n > -1.2) return Blocks.DEEPSLATE.defaultBlockState();
        return Blocks.TUFF.defaultBlockState();
    }

    private BlockState stone(int x, int y, int z) {
        double n = Math.sin(x * 0.4 + y * 0.3) + Math.cos(z * 0.35 + y * 0.2);
        if (n > 1.1) return Blocks.ANDESITE.defaultBlockState();
        if (n > 0.2) return Blocks.STONE.defaultBlockState();
        if (n > -0.8) return Blocks.COBBLESTONE.defaultBlockState();
        return Blocks.TUFF.defaultBlockState();
    }

    /** Grey wall stone: bricks with cracked, mossy and plain stone mixed in. */
    private BlockState wall(int x, int y, int z) {
        int h = Math.floorMod(x * 734287 + y * 912931 + z * 438289, 100);
        if (h < 55) return Blocks.STONE_BRICKS.defaultBlockState();
        if (h < 70) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        if (h < 80) return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        if (h < 90) return Blocks.COBBLESTONE.defaultBlockState();
        return Blocks.ANDESITE.defaultBlockState();
    }

    private BlockState ground(int x, int z) {
        double n = Math.sin(x * 0.17) + Math.sin(z * 0.13) + Math.sin((x - z) * 0.09);
        if (n > 1.9) return Blocks.SNOW_BLOCK.defaultBlockState();
        if (n > 1.4) return Blocks.COARSE_DIRT.defaultBlockState();
        if (n > 1.0) return Blocks.PODZOL.defaultBlockState();
        // the tundra tints grass grey-green; moss keeps its own bright green
        double m = Math.sin(x * 0.29 + 1) + Math.cos(z * 0.23) + Math.sin((x + z) * 0.17);
        if (m > 0.4) return Blocks.MOSS_BLOCK.defaultBlockState();
        return Blocks.GRASS_BLOCK.defaultBlockState(); // green between the houses, as in the concept
    }

    /** The terraces and the volcano, filled up from the ground or cut down into it, cliffs of stone around. */
    private void shape() {
        int r = RING[0] + 30;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = fx - r; x <= fx + r; x++) {
            for (int z = fz - r - 50; z <= fz + r; z++) {
                level.getChunk(x >> 4, z >> 4);
                int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                int natural = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                int top = surface(x, z);
                boolean volcanic = volcanic(x, z);
                if (top == Integer.MIN_VALUE) {
                    // outside: the hill the city stands on falls away in a steep cliff to the land
                    double dx = x - fx, dz = z - fz;
                    double out = Math.sqrt(dx * dx + dz * dz) + wobble(Math.atan2(dz, dx)) - RING[0];
                    int cliff = base - 1 - (int) Math.round(out * 1.8);
                    if (out > 28 || cliff < floor) continue;
                    for (int y = floor - 1; y <= cliff; y++) set(level, x, y, z, stone(x, y, z));
                    continue;
                }
                for (int y = floor - 1; y < top - 1; y++) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (y >= top - 4 || state.isAir() || !state.getFluidState().isEmpty() || state.canBeReplaced()) {
                        set(level, x, y, z, volcanic ? rock(x, y, z) : stone(x, y, z));
                    }
                }
                set(level, x, top - 1, z, volcanic ? rock(x, top - 1, z) : ground(x, z));
                for (int y = top; y <= Math.max(natural, top) + 1; y++) set(level, x, y, z, AIR);
            }
        }
    }

    /** A fjord cut west from the foot of the city down to the sea, between cliffs. */
    private void fjord() {
        for (int x = fx - RING[0] - 4; x >= fx - RING[0] - 220; x--) {
            double mid = fz + 8 + 6 * Math.sin(x / 23.0), halfWidth = 17 + 5 * Math.sin(x / 37.0) + Math.max(0, (fx - RING[0] - x) * 0.06);
            for (int z = (int) (mid - halfWidth - 1); z <= mid + halfWidth + 1; z++) {
                level.getChunk(x >> 4, z >> 4);
                if (tier(x, z) >= 0) continue;
                int natural = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                int bottom = sea - 7 + (int) Math.round(Math.abs(z - mid) / halfWidth * 4);
                for (int y = bottom - 2; y < bottom; y++) set(level, x, y, z, y == bottom - 1 ? Blocks.GRAVEL.defaultBlockState() : Blocks.STONE.defaultBlockState());
                for (int y = bottom; y < sea; y++) set(level, x, y, z, Blocks.WATER.defaultBlockState());
                for (int y = sea; y <= natural + 1; y++) set(level, x, y, z, AIR);
            }
        }
    }

    // ------------------------------------------------------------------ walls and towers

    /** Retaining walls between the terraces, the curtain wall round the lowest one, towers along them. */
    private void walls() {
        int r = RING[0] + 8; // the wavy edge reaches up to five blocks past RING[0]
        for (int x = fx - r; x <= fx + r; x++) {
            for (int z = fz - r; z <= fz + r; z++) {
                int t = tier(x, z);
                if (t < 0) continue;
                boolean onSlope = volcanic(x, z);
                int below = Integer.MAX_VALUE;
                for (int[] n : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    int nt = tier(x + n[0], z + n[1]);
                    if (nt < 0 || nt < t && !volcanic(x + n[0], z + n[1])) below = Math.min(below, nt);
                }
                if (below == Integer.MAX_VALUE) continue;
                if (onSlope && below >= 0) continue; // the volcano itself is the wall between terraces
                int top = onSlope ? surface(x, z) : base + RISE[t]; // on the volcano's flank the curtain follows the slope
                if (below < 0) { // the curtain wall: five blocks above the lowest terrace
                    if (gate(x, z)) continue;
                    int foot = SultharisBuilder.surfaceY(level, x, z) - 6;
                    for (int y = Math.min(foot, top - 1); y < top + 5; y++) set(level, x, y, z, wall(x, y, z));
                    if (Math.floorMod(x + z, 2) == 0) set(level, x, top + 5, z, wall(x, top + 5, z));
                } else {          // a retaining wall with a low parapet
                    for (int y = base + RISE[below] - 1; y < top; y++) set(level, x, y, z, wall(x, y, z));
                    set(level, x, top, z, Blocks.STONE_BRICK_WALL.defaultBlockState());
                    if (Math.floorMod(x * 3 + z * 7, 13) == 0) set(level, x, top + 1, z, SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
                }
            }
        }
        // round towers along each wall, and two at each gate
        for (int t = 0; t <= 3; t++) {
            int count = new int[]{14, 10, 7, 5}[t];
            for (int i = 0; i < count; i++) {
                double a = Math.PI * 2 * i / count + 0.2 * t;
                double d = RING[t] - 1 - wobble(a);
                int x = fx + (int) Math.round(Math.cos(a) * d), z = fz + (int) Math.round(Math.sin(a) * d);
                if (volcanic(x, z) || nearRoute(x, z, t == 0 ? 22 : 9)) continue; // clear of the gatehouses and their towers
                int foot = t == 0 ? SultharisBuilder.surfaceY(level, x, z) - 8 : base + RISE[Math.max(0, t - 1)] - 1;
                roundTower(x, z, Math.min(foot, base + RISE[t] - 1), base + RISE[t] + (t == 0 ? 15 : 12), t == 0 ? 5 : 4);
            }
        }
        for (Direction out : new Direction[]{Direction.SOUTH, Direction.EAST, Direction.WEST}) gatehouse(out);
    }

    /** How far out from the hold the lowest terrace ends along a direction: where the curtain wall stands. */
    private int edge(Direction out) {
        int d = 0;
        while (d < RING[0] + 20 && tier(fx + out.getStepX() * d, fz + out.getStepZ() * d) >= 0) d++;
        return d - 1;
    }

    /**
     * A gatehouse over the curtain wall: a solid block of stone six deep and fifteen wide, a passage
     * five wide and five high with an arch of timber and a portcullis raised above it, a crenellated
     * top with braziers, two towers beside it, giant banners and crossed axes over the gate.
     */
    private void gatehouse(Direction out) {
        int e = edge(out);
        Direction across = out.getClockWise();
        int top = base + 9;
        for (int k = -4; k <= 2; k++) {
            for (int w = -7; w <= 7; w++) {
                int x = fx + out.getStepX() * (e + k) + across.getStepX() * w, z = fz + out.getStepZ() * (e + k) + across.getStepZ() * w;
                int foot = Math.min(base - 1, SultharisBuilder.surfaceY(level, x, z) - 4);
                boolean passage = Math.abs(w) <= 2;
                for (int y = foot; y <= top; y++) {
                    BlockState state = wall(x, y, z);
                    if (passage && y >= base && y < base + 5) state = AIR;
                    if (passage && y == base + 5) state = timber(across.getAxis());             // the lintel
                    if (passage && y == base + 4 && Math.abs(w) == 2) state = timber(Direction.Axis.Y);
                    if (passage && y == base - 1) state = Blocks.COBBLESTONE.defaultBlockState();
                    set(level, x, y, z, state);
                }
                if (Math.floorMod(w, 2) == 0) set(level, x, top + 1, z, wall(x, top + 1, z));
                for (int y = top + 2; y < top + 5; y++) set(level, x, y, z, AIR);
                if (passage && k == 0) set(level, x, base + 4, z, Blocks.IRON_BARS.defaultBlockState()); // the portcullis, raised
            }
        }
        // the front: braziers on the top corners, a giant banner either side, crossed axes over the arch
        int fxo = fx + out.getStepX() * (e + 3), fzo = fz + out.getStepZ() * (e + 3);
        for (int w : new int[]{-7, 7}) {
            int x = fxo - out.getStepX() + across.getStepX() * w, z = fzo - out.getStepZ() + across.getStepZ() * w;
            fire(x, top + 1, z);
        }
        for (int w : new int[]{-5, 5}) {
            giantBanner(fxo + across.getStepX() * w, base + 2, fzo + across.getStepZ() * w, out, across);
        }
        for (int i = -2; i <= 2; i++) { // crossed axes: two diagonals of iron bars with a shield in the middle
            int x = fxo + across.getStepX() * i, z = fzo + across.getStepZ() * i;
            set(level, x, base + 7 + i, z, Blocks.IRON_BARS.defaultBlockState());
            set(level, x, base + 7 - i, z, Blocks.IRON_BARS.defaultBlockState());
        }
        set(level, fxo, base + 7, fzo, Blocks.RED_WOOL.defaultBlockState());
        for (int w : new int[]{-3, 3}) {
            int x = fxo + across.getStepX() * w, z = fzo + across.getStepZ() * w;
            brazierPost(x, base, z);
        }
        for (int w : new int[]{-5, 5}) { // flagpoles on the top of the gatehouse
            int x = fxo - out.getStepX() * 2 + across.getStepX() * w, z = fzo - out.getStepZ() * 2 + across.getStepZ() * w;
            flagpole(x, top + 1, z, across);
        }
        // the towers beside it
        for (int w : new int[]{-10, 10}) {
            int x = fx + out.getStepX() * (e - 1) + across.getStepX() * w, z = fz + out.getStepZ() * (e - 1) + across.getStepZ() * w;
            roundTower(x, z, Math.min(base - 1, SultharisBuilder.surfaceY(level, x, z) - 8), base + 19, 5);
        }
        set(level, fx + out.getStepX() * (e - 1), base + 4, fz + out.getStepZ() * (e - 1),
                Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        taken.add(new int[]{Math.min(fx + out.getStepX() * (e - 6), fx + out.getStepX() * (e + 3)) - 12, Math.min(fz + out.getStepZ() * (e - 6), fz + out.getStepZ() * (e + 3)) - 12,
                Math.max(fx + out.getStepX() * (e - 6), fx + out.getStepX() * (e + 3)) + 12, Math.max(fz + out.getStepZ() * (e - 6), fz + out.getStepZ() * (e + 3)) + 12});
    }

    /**
     * A flag of the clans (Supplementaries): red with the white knot, flying toward direction from a
     * pole, the pattern set as on a banner.
     */
    private void clanFlag(int x, int y, int z, Direction flying) {
        BlockPos pos = new BlockPos(x, y, z);
        set(level, x, y, z, Furniture.flag("red", flying));
        var be = level.getBlockEntity(pos);
        if (be == null) return;
        net.minecraft.nbt.CompoundTag tag = be.saveWithoutMetadata();
        net.minecraft.nbt.ListTag patterns = new net.minecraft.nbt.ListTag();
        for (String[] layer : new String[][]{{"mr", "white"}, {"cr", "white"}, {"flo", "red"}, {"bo", "white"}}) {
            net.minecraft.nbt.CompoundTag pat = new net.minecraft.nbt.CompoundTag();
            pat.putString("Pattern", layer[0]);
            pat.putInt("Color", net.minecraft.world.item.DyeColor.byName(layer[1], net.minecraft.world.item.DyeColor.WHITE).getId());
            patterns.add(pat);
        }
        tag.put("Patterns", patterns);
        be.load(tag);
        be.setChanged();
        level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
    }

    /** A tall flagpole with two flags of the clans, one above the other. */
    private void flagpole(int x, int y, int z, Direction flying) {
        for (int dy = 0; dy < 7; dy++) set(level, x, y + dy, z, dy == 0 ? Blocks.COBBLESTONE_WALL.defaultBlockState() : Blocks.SPRUCE_FENCE.defaultBlockState());
        clanFlag(x + flying.getStepX(), y + 6, z + flying.getStepZ(), flying);
        clanFlag(x + flying.getStepX(), y + 5, z + flying.getStepZ(), flying);
        set(level, x, y + 7, z, Blocks.LANTERN.defaultBlockState());
    }

    /** A brazier with fire on top, on the battlements. */
    private void fire(int x, int y, int z) {
        set(level, x, y, z, SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
        set(level, x, y + 1, z, Blocks.CAMPFIRE.defaultBlockState());
    }

    /**
     * A banner three wide and seven tall hung flat on a wall facing out: red wool with a white knot of
     * the clans in the middle, a dark timber bar on top and a pointed hem at the bottom.
     */
    private void giantBanner(int x, int y, int z, Direction out, Direction across) {
        // a great banner of the clans: its bar at the top, the cloth hangs six blocks and moves in the wind
        set(level, x, y + 7, z, SoFEBlocks.CLAN_BANNER.get().defaultBlockState().setValue(com.sofe.block.ClanBanner.FACING, out));
        for (int dy = 0; dy < 7; dy++) set(level, x, y + dy, z, AIR);
    }

    private boolean gate(int x, int z) {
        return (Math.abs(x - fx) <= 7 && z > fz) || (Math.abs(z - fz) <= 7 && x != fx);
    }

    private BlockState timber(Direction.Axis axis) {
        return SoFEBlocks.NORDRATH_DARK_TIMBER.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    /**
     * A round stone tower: windows of dark trapdoors, a wooden hoarding round its top on brackets with
     * a giant banner hung on its outer face, a solid cone of slate with a spire, and fire on its top.
     */
    private void roundTower(int x, int z, int foot, int top, int r) {
        double ox = x - fx, oz = z - fz;
        Direction out = Math.abs(ox) >= Math.abs(oz) ? (ox > 0 ? Direction.EAST : Direction.WEST) : (oz > 0 ? Direction.SOUTH : Direction.NORTH);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r + 0.3) continue;
                boolean shell = d > r - 1.1;
                for (int y = foot; y < top; y++) {
                    boolean window = shell && (y - base) % 5 == 3 && y > base && (dx == 0 || dz == 0);
                    BlockState state = shell ? (window ? Blocks.DARK_OAK_TRAPDOOR.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN, true)
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, dx > 0 ? Direction.EAST : dx < 0 ? Direction.WEST : dz > 0 ? Direction.SOUTH : Direction.NORTH)
                            : wall(x + dx, y, z + dz)) : y < base ? stone(x, y, z) : AIR;
                    set(level, x + dx, y, z + dz, state);
                }
                set(level, x + dx, top, z + dz, Blocks.SPRUCE_PLANKS.defaultBlockState());
            }
        }
        // the way up: a door on the city side at the level of the terrace, a floor inside, a ladder to a hatch
        Direction in = out.getOpposite();
        int dxIn = x + in.getStepX() * (r + 2), dzIn = z + in.getStepZ() * (r + 2);
        int ground = Math.max(base, surface(dxIn, dzIn));
        if (ground < top - 4) {
            for (int dx = -r + 1; dx <= r - 1; dx++) {
                for (int dz = -r + 1; dz <= r - 1; dz++) {
                    if (dx * dx + dz * dz > (r - 1.1) * (r - 1.1)) continue;
                    for (int y = foot; y < ground - 1; y++) set(level, x + dx, y, z + dz, stone(x + dx, y, z + dz));
                    set(level, x + dx, ground - 1, z + dz, Blocks.SPRUCE_PLANKS.defaultBlockState());
                    for (int y = ground; y < top; y++) set(level, x + dx, y, z + dz, AIR);
                }
            }
            int doorX = x + in.getStepX() * r, doorZ = z + in.getStepZ() * r;
            BlockState door = Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, in);
            set(level, doorX, ground, doorZ, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            set(level, doorX, ground + 1, doorZ, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            set(level, doorX, ground + 2, doorZ, wall(doorX, ground + 2, doorZ));
            for (int k = 1; k <= 2; k++) { // the wall is two blocks thick on the axes: open the inner block too
                int ix = x + in.getStepX() * (r - k), iz = z + in.getStepZ() * (r - k);
                if ((ix - x) * (ix - x) + (iz - z) * (iz - z) <= (r - 1.1) * (r - 1.1)) break;
                set(level, ix, ground, iz, AIR);
                set(level, ix, ground + 1, iz, AIR);
            }
            set(level, doorX + in.getStepX(), ground - 1, doorZ + in.getStepZ(), Blocks.COBBLESTONE.defaultBlockState());
            for (int h = 0; h < 2; h++) set(level, doorX + in.getStepX(), ground + h, doorZ + in.getStepZ(), AIR);
            // the ladder against the far wall, through a hatch in the top floor
            int lx = x + out.getStepX() * (r - 2), lz = z + out.getStepZ() * (r - 2);
            for (int y = ground; y <= top; y++) {
                set(level, lx, y, lz, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, in));
            }
            set(level, x, ground, z, Blocks.BARREL.defaultBlockState());
            set(level, x + in.getClockWise().getStepX() * (r - 2), ground, z + in.getClockWise().getStepZ() * (r - 2), Furniture.crate("spruce"));
        }
        // the hoarding: a ring of planks one block out, on brackets, with a fence and posts
        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r + 1.3 || d <= r + 0.3) continue;
                set(level, x + dx, top, z + dz, Blocks.SPRUCE_PLANKS.defaultBlockState());
                set(level, x + dx, top - 1, z + dz, Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Math.abs(dx) >= Math.abs(dz) ? (dx > 0 ? Direction.WEST : Direction.EAST) : (dz > 0 ? Direction.NORTH : Direction.SOUTH))
                        .setValue(StairBlock.HALF, Half.TOP));
                boolean postHere = Math.floorMod(dx * 3 + dz * 5, 4) == 0;
                set(level, x + dx, top + 1, z + dz, postHere ? Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState() : Blocks.SPRUCE_FENCE.defaultBlockState());
                set(level, x + dx, top + 2, z + dz, postHere ? Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState() : AIR);
                set(level, x + dx, top + 3, z + dz, postHere ? Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState() : AIR);
            }
        }
        // the cone: solid slate, stairs on its skin, high enough to walk under it on the top floor
        int coneBase = top + 4; // three blocks to walk under it
        for (int k = 0; k <= r + 2; k++) {
            double ring = r + 1.5 - k;
            for (int dx = -r - 2; dx <= r + 2; dx++) {
                for (int dz = -r - 2; dz <= r + 2; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > ring + 0.3) continue;
                    boolean skin = d > ring - 0.9;
                    Direction inward = Math.abs(dx) >= Math.abs(dz) ? (dx > 0 ? Direction.WEST : Direction.EAST) : (dz > 0 ? Direction.NORTH : Direction.SOUTH);
                    set(level, x + dx, coneBase + k, z + dz, skin && ring >= 0.8 ? Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, inward.getOpposite())
                            : Blocks.DEEPSLATE_TILES.defaultBlockState());
                }
            }
        }
        int peak = coneBase + r + 2; // on the top of the cone, not floating above it
        for (int dy = 0; dy < 4; dy++) set(level, x, peak + dy, z, Blocks.SPRUCE_FENCE.defaultBlockState());
        clanFlag(x + out.getClockWise().getStepX(), peak + 3, z + out.getClockWise().getStepZ(), out.getClockWise());
        // a giant banner on the outer face, fire on the hoarding, light inside
        int bx = x + out.getStepX() * (r + 1), bz = z + out.getStepZ() * (r + 1);
        giantBanner(bx, top - 9, bz, out, out.getClockWise());
        int fx2 = x - out.getStepX() * (r + 1), fz2 = z - out.getStepZ() * (r + 1);
        set(level, fx2, top + 1, fz2, SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
        set(level, x, top + 3, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        set(level, x, Math.max(foot + 1, base + 1), z, SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
        taken.add(new int[]{x - r - 3, z - r - 3, x + r + 3, z + r + 3});
        towers.add(new int[]{x, z, r});
    }

    private void banner(int x, int y, int z, Direction facing, Block banner) {
        set(level, x, y, z, banner.defaultBlockState().setValue(WallBannerBlock.FACING, facing));
    }

    // ------------------------------------------------------------------ the volcano's fire

    private void lavaAt(int x, int y, int z) {
        set(level, x, y, z, Blocks.LAVA.defaultBlockState());
        lavaCells.add(BlockPos.asLong(x, y, z));
    }

    /**
     * Keeps the lava where it was put: every side of a lava block that is not lava becomes magma or
     * blackstone, and so does the block under it. Lava only flows into open space, so none of it runs.
     */
    private void sealLava() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (long cell : lavaCells) {
            int x = BlockPos.getX(cell), y = BlockPos.getY(cell), z = BlockPos.getZ(cell);
            for (int[] n : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, -1, 0}}) {
                long next = BlockPos.asLong(x + n[0], y + n[1], z + n[2]);
                if (lavaCells.contains(next)) continue;
                pos.set(x + n[0], y + n[1], z + n[2]);
                BlockState state = level.getBlockState(pos);
                if (state.isAir() || state.canBeReplaced() || !state.getFluidState().isEmpty()) {
                    set(level, pos.getX(), pos.getY(), pos.getZ(), Math.floorMod(x + z, 3) == 0 ? Blocks.BLACKSTONE.defaultBlockState()
                            : Blocks.MAGMA_BLOCK.defaultBlockState());
                }
            }
        }
    }

    /** Lava in the crater and three rivers down the slope into stone basins; smoke from the rim. */
    private void volcanoFire() {
        int floor = base + VOLCANO_HEIGHT - 8;
        for (int dx = -CRATER; dx <= CRATER; dx++) {
            for (int dz = -CRATER; dz <= CRATER; dz++) {
                if (dx * dx + dz * dz >= (CRATER - 1) * (CRATER - 1)) continue;
                lavaAt(vx + dx, floor - 2, vz + dz);
                lavaAt(vx + dx, floor - 1, vz + dz);
            }
        }
        for (int deg = 0; deg < 360; deg += 72) { // smoke
            int x = vx + (int) Math.round((CRATER + 1) * Math.cos(Math.toRadians(deg))), z = vz + (int) Math.round((CRATER + 1) * Math.sin(Math.toRadians(deg)));
            int y = surface(x, z);
            set(level, x, y, z, Blocks.HAY_BLOCK.defaultBlockState());
            set(level, x, y + 1, z, Blocks.CAMPFIRE.defaultBlockState());
        }
        lava.add(new int[]{vx - CRATER - 2, vz - CRATER - 2, vx + CRATER + 2, vz + CRATER + 2});
        // three rivers: west of the hold, down its south-east side, and toward the east
        for (double heading : new double[]{Math.toRadians(185), Math.toRadians(122), Math.toRadians(75)}) river(heading, 2, true);
        river(Math.toRadians(25), 4, false); // the great flow, down the eastern flank to its foot
        sealLava();
    }

    private void river(double heading, int width, boolean stopAtCity) {
        int lastX = vx, lastZ = vz;
        for (int d = CRATER + 2; d < VOLCANO_RADIUS; d++) {
            double a = heading + 0.18 * Math.sin(d / 7.0);
            int x = vx + (int) Math.round(d * Math.cos(a)), z = vz + (int) Math.round(d * Math.sin(a));
            boolean nearHall = Math.abs(x - fx) < 16 && Math.abs(z - fz) < 26;
            if (!volcanic(x, z) || stopAtCity && (volcano(x, z) - surfaceNoVolcano(x, z) < 6 || nearHall)) { // it nears the city: a basin
                basin(lastX, lastZ);
                return;
            }
            for (int w = 0; w < width; w++) {
                int px = x + (int) Math.round(-Math.sin(a) * w), pz = z + (int) Math.round(Math.cos(a) * w);
                int y = surface(px, pz) - 1;
                lavaAt(px, y, pz);
                set(level, px, y + 1, pz, AIR);
                for (int side : new int[]{-1, width}) { // raised banks of blackstone
                    int bx = x + (int) Math.round(-Math.sin(a) * side), bz = z + (int) Math.round(Math.cos(a) * side);
                    set(level, bx, surface(bx, bz), bz, Blocks.BLACKSTONE.defaultBlockState());
                }
            }
            lava.add(new int[]{x - 3, z - 3, x + 3, z + 3});
            lastX = x;
            lastZ = z;
        }
    }

    private int surfaceNoVolcano(int x, int z) {
        int t = tier(x, z);
        return t < 0 ? SultharisBuilder.surfaceY(level, x, z) : base + RISE[t];
    }

    /** A round basin of lava with a blackstone rim two high, the forges' fire. */
    private void basin(int x, int z) {
        int y = surface(x, z) - 1;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 4.3) continue;
                if (d > 3.2) {
                    set(level, x + dx, y + 1, z + dz, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                    set(level, x + dx, y + 2, z + dz, Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
                } else {
                    set(level, x + dx, y - 1, z + dz, Blocks.BLACKSTONE.defaultBlockState());
                    lavaAt(x + dx, y, z + dz);
                    set(level, x + dx, y + 1, z + dz, AIR);
                }
            }
        }
        set(level, x, y + 3, z, AIR);
        lava.add(new int[]{x - 9, z - 9, x + 9, z + 9});
    }

    // ------------------------------------------------------------------ the ways up

    /** Stairs from every terrace to the next on three sides, paths round each terrace, the street up the middle. */
    private void routes() {
        for (int t = 0; t < 3; t++) {
            if (t == 1) { // the market fills the main street on terrace 1: two flights beside it instead
                sideStair[0] = clearOffset(t, -1);
                sideStair[1] = clearOffset(t, 1);
                stairsUp(t, Direction.NORTH, sideStair[0]);
                stairsUp(t, Direction.NORTH, sideStair[1]);
            } else {
                stairsUp(t, Direction.NORTH);   // up the main street, from the south gate
            }
            stairsUp(t, Direction.EAST);    // from the west side
            stairsUp(t, Direction.WEST);    // from the east side
        }
        // the main street and the two cross streets
        for (int z = fz + RING[0] - 1; z >= fz + 6; z--) {
            path(fx - 2, z, 5, true);
            if (Math.floorMod(z, 6) == 0) { // lantern posts on both sides of the main street
                for (int side : new int[]{-4, 4}) {
                    int t = tier(fx + side, z);
                    if (t < 0 || volcanic(fx + side, z) || nearStair(fx + side, z)) continue;
                    int y = base + RISE[t];
                    if (!level.getBlockState(new BlockPos(fx + side, y, z)).isAir()) continue;
                    set(level, fx + side, y, z, Blocks.SPRUCE_FENCE.defaultBlockState());
                    set(level, fx + side, y + 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
                    set(level, fx + side, y + 2, z, Blocks.LANTERN.defaultBlockState());
                }
            }
        }
        for (int x = fx - RING[0] + 1; x <= fx + RING[0] - 1; x++) if (!volcanic(x, fz)) path(x, fz - 2, 5, false);
        // a path round each terrace, just inside its edge
        for (int t = 0; t <= 2; t++) {
            for (int deg = 0; deg < 360; deg++) {
                double a = Math.toRadians(deg);
                for (double w = 0; w < 3; w += 0.5) {
                    double d = RING[t] - 6 - w - wobble(a);
                    int x = fx + (int) Math.round(Math.cos(a) * d), z = fz + (int) Math.round(Math.sin(a) * d);
                    if (tier(x, z) != t || volcanic(x, z)) continue;
                    pathAt(x, z);
                }
                if (deg % 20 == 0) {
                    double d = RING[t] - 10 - wobble(a);
                    int x = fx + (int) Math.round(Math.cos(a) * d), z = fz + (int) Math.round(Math.sin(a) * d);
                    if (tier(x, z) == t && !volcanic(x, z)) brazierPost(x, base + RISE[t], z);
                }
            }
        }
    }

    /** A flight of stairs five wide from terrace t up to terrace t + 1, climbing in direction up. */
    private void stairsUp(int t, Direction up) {
        stairsUp(t, up, 0);
    }

    /** Where the flights beside the market go: the first offset on that side whose flight misses every tower. */
    private final int[] sideStair = {-30, 30};

    private int clearOffset(int t, int side) {
        for (int d : new int[]{30, 34, 26, 38, 22, 42}) {
            int x = fx + side * d, z = fz + RING[0];
            while (z > fz && tier(x, z) <= t) z--;
            int rise = RISE[t + 1] - RISE[t];
            boolean clear = true;
            for (int k = -2; k <= rise + 2 && clear; k++) {
                for (int w = -3; w <= 3 && clear; w++) clear = !inTower(x + w, z + k, 2);
            }
            if (clear) return side * d;
        }
        return side * 30;
    }

    /** As above, the flight moved sideways by offset blocks from the axis (north only). */
    private void stairsUp(int t, Direction up, int offset) {
        // walk inward from the outside along the axis until the terrace rises
        int step = 0;
        int x = fx - up.getStepX() * RING[0], z = fz - up.getStepZ() * RING[0];
        if (up == Direction.NORTH) {
            x = fx + offset;
            z = fz + RING[0];
        }
        while (step++ < RING[0] * 2 && tier(x, z) <= t) {
            x += up.getStepX();
            z += up.getStepZ();
        }
        if (tier(x, z) != t + 1 || volcanic(x, z)) return;
        int lower = base + RISE[t], rise = RISE[t + 1] - RISE[t];
        Direction across = up.getClockWise();
        for (int i = 0; i < rise; i++) {
            int sx = x - up.getStepX() * (rise - i), sz = z - up.getStepZ() * (rise - i);
            for (int w = -2; w <= 2; w++) {
                int px = sx + across.getStepX() * w, pz = sz + across.getStepZ() * w;
                for (int y = lower - 1; y < lower + i; y++) set(level, px, y, pz, wall(px, y, pz));
                boolean edge = Math.abs(w) == 2;
                set(level, px, lower + i, pz, edge ? Blocks.STONE_BRICKS.defaultBlockState()
                        : Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, up));
                set(level, px, lower + i + 1, pz, edge && i % 3 == 0 ? SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState()
                        : edge ? Blocks.STONE_BRICK_WALL.defaultBlockState() : AIR);
                for (int h = 2; h <= 4; h++) set(level, px, lower + i + h, pz, AIR);
            }
        }
        // open the parapet at the top of the flight
        for (int k = 0; k <= 2; k++) {
            for (int w = -1; w <= 1; w++) {
                int px = x + up.getStepX() * k + across.getStepX() * w, pz = z + up.getStepZ() * k + across.getStepZ() * w;
                for (int h = 0; h <= 2; h++) set(level, px, base + RISE[t + 1] + h, pz, AIR);
            }
        }
        taken.add(new int[]{Math.min(x, x - up.getStepX() * rise) - 4, Math.min(z, z - up.getStepZ() * rise) - 4,
                Math.max(x, x - up.getStepX() * rise) + 4, Math.max(z, z - up.getStepZ() * rise) + 4});
    }

    /** Close to the edge of a terrace, where a flight of stairs or a wall stands. */
    private boolean nearStair(int x, int z) {
        int t = tier(x, z);
        for (int d = 1; d <= 3; d++) if (tier(x, z + d) != t || tier(x, z - d) != t) return true;
        return false;
    }

    private boolean nearRoute(int x, int z, int margin) {
        return Math.abs(x - fx) <= margin && z > fz || Math.abs(z - fz) <= margin;
    }

    private void path(int x0, int z0, int width, boolean alongZ) {
        for (int w = 0; w < width; w++) {
            int x = alongZ ? x0 + w : x0, z = alongZ ? z0 : z0 + w;
            if (tier(x, z) < 0 || volcanic(x, z)) continue;
            pathAt(x, z);
        }
    }

    private void pathAt(int x, int z) {
        int y = surface(x, z) - 1;
        BlockState below = level.getBlockState(new BlockPos(x, y, z));
        if (!(below.is(Blocks.SNOW_BLOCK) || below.is(Blocks.GRAVEL) || below.is(Blocks.COARSE_DIRT) || below.is(Blocks.STONE)
                || below.is(Blocks.DIRT_PATH) || below.is(Blocks.COBBLESTONE))) return;
        int h = Math.floorMod(x * 31 + z * 17, 10);
        set(level, x, y, z, (h < 4 ? Blocks.COBBLESTONE : h < 6 ? Blocks.STONE_BRICKS : h < 8 ? Blocks.GRAVEL : Blocks.DIRT_PATH).defaultBlockState());
    }

    private void brazierPost(int x, int y, int z) {
        set(level, x, y, z, Blocks.COBBLESTONE_WALL.defaultBlockState());
        set(level, x, y + 1, z, SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
    }

    // ------------------------------------------------------------------ the buildings

    private boolean free(int x0, int z0, int x1, int z1, int t) {
        for (int[] r : taken) if (x0 <= r[2] && x1 >= r[0] && z0 <= r[3] && z1 >= r[1]) return false;
        for (int[] r : lava) if (x0 - 6 <= r[2] && x1 + 6 >= r[0] && z0 - 6 <= r[3] && z1 + 6 >= r[1]) return false;
        for (int x = x0; x <= x1; x += 2) {
            for (int z = z0; z <= z1; z += 2) {
                if (tier(x, z) != t || volcanic(x, z)) return false;
                double dx = x - fx, dz = z - fz;
                double d = Math.sqrt(dx * dx + dz * dz) + wobble(Math.atan2(dz, dx));
                if (d > RING[t] - 10) return false; // clear of the path round the edge
            }
        }
        return !(Math.abs((x0 + x1) / 2 - fx) < (x1 - x0) / 2 + 5 && z1 > fz) && !(Math.abs((z0 + z1) / 2 - fz) < (z1 - z0) / 2 + 5);
    }

    private void buildings() {
        // the jarl's great hall on the hold, its door facing the main street
        greatHall();
        market(); // before the houses: its square is cleared, and nothing must stand there
        for (int i = 0; i < 6; i++) pen(i);   // the farms before the houses, so they always find room
        for (int i = 0; i < 6; i++) field(i);
        // longhouses on the three lower terraces, a smithy by the fire, pens and fields below
        int[] wanted = {24, 14, 8};
        for (int t = 0; t <= 2; t++) {
            int placed = 0;
            for (int attempt = 0; attempt < 2500 && placed < wanted[t]; attempt++) {
                double a = random.nextDouble() * Math.PI * 2;
                double d = (t == 2 ? RING[3] : RING[t + 1]) + 8 + random.nextDouble() * (RING[t] - (t == 2 ? RING[3] : RING[t + 1]) - 18);
                int x = fx + (int) Math.round(Math.cos(a) * d), z = fz + (int) Math.round(Math.sin(a) * d);
                boolean alongZ = random.nextBoolean();
                int w = 7 + random.nextInt(2) * 2, l = 11 + random.nextInt(4) * 2;
                int x0 = alongZ ? x - w / 2 : x - l / 2, z0 = alongZ ? z - l / 2 : z - w / 2;
                int x1 = x0 + (alongZ ? w : l) - 1, z1 = z0 + (alongZ ? l : w) - 1;
                if (!free(x0 - 2, z0 - 2, x1 + 2, z1 + 2, t)) continue;
                longhouse(x0, z0, w, l, alongZ, base + RISE[t], 4, false);
                taken.add(new int[]{x0 - 3, z0 - 3, x1 + 3, z1 + 3});
                placed++;
            }
        }
        smithy();
        greenery();
        giantSword(fx - 7, fz + RING[3] + 5);
        giantSword(fx + 7, fz + RING[3] + 5);
    }

    /**
     * A longhouse: a rectangle w across and l long, with a footing of cobblestone, walls of dark planks
     * between timber posts, small windows, a door at each gable end, and a steep slate roof with
     * crossed beams at its ends. Inside: a long hearth, tables and benches, beds and stores.
     */
    private void longhouse(int x0, int z0, int w, int l, boolean alongZ, int y, int height, boolean hall) {
        longhouse(x0, z0, w, l, alongZ, y, height, hall, hall ? hallTimber() : clanTimber(random.nextInt(3) > 0));
    }

    private void longhouse(int x0, int z0, int w, int l, boolean alongZ, int y, int height, boolean hall, Timber t) {
        BlockState planks = t.planks();
        BlockState post = t.post();
        for (int a = 0; a < w; a++) {
            for (int b = 0; b < l; b++) {
                int x = alongZ ? x0 + a : x0 + b, z = alongZ ? z0 + b : z0 + a;
                boolean edgeA = a == 0 || a == w - 1, edgeB = b == 0 || b == l - 1;
                set(level, x, y - 1, z, edgeA || edgeB ? Blocks.COBBLESTONE.defaultBlockState() : t.floor());
                for (int dy = 0; dy < height; dy++) {
                    BlockState s = AIR;
                    if (edgeA || edgeB) {
                        boolean corner = edgeA && edgeB;
                        boolean postHere = corner || (edgeA && b % 4 == 0) || (edgeB && (a == 1 || a == w - 2));
                        boolean window = !corner && !postHere && dy == 2 && edgeA && b % 4 == 2;
                        boolean upper = hall && !corner && !postHere && dy == 5 && edgeA && b % 4 == 2;
                        s = dy == 0 ? Blocks.COBBLESTONE.defaultBlockState() : postHere ? post : window || upper ? Blocks.GLASS_PANE.defaultBlockState() : planks;
                        if (dy == height - 1 && !postHere) s = hall ? Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState()
                                : timber(alongZ ? (edgeA ? Direction.Axis.Z : Direction.Axis.X) : (edgeA ? Direction.Axis.X : Direction.Axis.Z));
                    }
                    set(level, x, y + dy, z, s);
                }
            }
        }
        // doors at both gable ends
        int mid = w / 2;
        for (int b : new int[]{0, l - 1}) {
            int x = alongZ ? x0 + mid : x0 + b, z = alongZ ? z0 + b : z0 + mid;
            Direction out = alongZ ? (b == 0 ? Direction.NORTH : Direction.SOUTH) : (b == 0 ? Direction.WEST : Direction.EAST);
            BlockState door = Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING, out.getOpposite());
            set(level, x, y, z, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            set(level, x, y + 1, z, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            int ox = x + out.getStepX(), oz = z + out.getStepZ();
            Block banner = random.nextBoolean() ? Blocks.BLUE_WALL_BANNER : Blocks.RED_WALL_BANNER;
            Direction side = out.getClockWise();
            set(level, x + side.getStepX() * 2, y + 2, z + side.getStepZ() * 2, AIR);
            banner(ox + side.getStepX() * 2, y + 2, oz + side.getStepZ() * 2, out, banner);
            set(level, ox - side.getStepX() * 2, y + 2, oz - side.getStepZ() * 2, Blocks.LANTERN.defaultBlockState());
            set(level, ox - side.getStepX() * 2, y + 1, oz - side.getStepZ() * 2, Blocks.SPRUCE_FENCE.defaultBlockState());
            set(level, ox - side.getStepX() * 2, y, oz - side.getStepZ() * 2, Blocks.SPRUCE_FENCE.defaultBlockState());
            if (b == l - 1 || !hall) doors.add(new int[]{ox + out.getStepX(), oz + out.getStepZ()});
        }
        roof(x0, z0, w, l, alongZ, y + height, t);
        furnishLonghouse(x0, z0, w, l, alongZ, y, height, hall);
    }

    /** A steep slate roof along the length, one block of overhang, gables of dark planks, crossed beams at the ends. */
    private void roof(int x0, int z0, int w, int l, boolean alongZ, int y) {
        roof(x0, z0, w, l, alongZ, y, clanTimber());
    }

    private int roof(int x0, int z0, int w, int l, boolean alongZ, int y, Timber t) {
        BlockState planks = t.planks();
        Direction upLow = alongZ ? Direction.EAST : Direction.SOUTH, upHigh = upLow.getOpposite();
        for (int b = 0; b < l; b++) { // the wall plate
            for (int a : new int[]{0, w - 1}) set(level, alongZ ? x0 + a : x0 + b, y, alongZ ? z0 + b : z0 + a, planks);
        }
        int k = 0;
        for (; -1 + k <= w - k; k++) {
            int lo = -1 + k, hi = w - k;
            for (int b = -1; b <= l; b++) {
                int xl = alongZ ? x0 + lo : x0 + b, zl = alongZ ? z0 + b : z0 + lo;
                int xh = alongZ ? x0 + hi : x0 + b, zh = alongZ ? z0 + b : z0 + hi;
                if (lo == hi) {
                    set(level, xl, y + k, zl, t.roofSlab());
                    continue;
                }
                BlockState shingle = k == 0 ? Blocks.DARK_OAK_STAIRS.defaultBlockState() : t.roofStairs(); // a timber eave
                set(level, xl, y + k, zl, shingle.setValue(StairBlock.FACING, upLow));
                set(level, xh, y + k, zh, shingle.setValue(StairBlock.FACING, upHigh));
                if (b == 0 || b == l - 1) { // the gables, from the wall plate up, so no gap opens under the roof
                    for (int a = lo + 1; a < hi; a++) set(level, alongZ ? x0 + a : x0 + b, y + k, alongZ ? z0 + b : z0 + a, planks);
                }
            }
        }
        // crossed beams over each gable, like horns
        int ridge = y + k - 1, ma = (w - 1) / 2;
        for (int b : new int[]{-1, l}) {
            for (int s : new int[]{-1, 1}) {
                int a = ma + (w % 2 == 0 ? (s > 0 ? 1 : 0) : s);
                int x = alongZ ? x0 + a : x0 + b, z = alongZ ? z0 + b : z0 + a;
                set(level, x, ridge + 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
            }
        }
        return ridge;
    }

    private void furnishLonghouse(int x0, int z0, int w, int l, boolean alongZ, int y, int height, boolean hall) {
        int mid = w / 2;
        java.util.function.BiConsumer<int[], BlockState> put = (ab, s) -> set(level, alongZ ? x0 + ab[0] : x0 + ab[1], y + ab[2], alongZ ? z0 + ab[1] : z0 + ab[0], s);
        Direction along = alongZ ? Direction.SOUTH : Direction.EAST;
        Direction across = alongZ ? Direction.EAST : Direction.SOUTH;
        // the long hearth down the middle
        for (int b = 3; b < l - 3; b++) {
            put.accept(new int[]{mid, b, -1}, Blocks.COBBLESTONE.defaultBlockState());
            if ((b - 3) % 3 == 1) put.accept(new int[]{mid, b, 0}, Blocks.CAMPFIRE.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.CampfireBlock.SIGNAL_FIRE, false));
            else put.accept(new int[]{mid, b, 0}, Blocks.COBBLESTONE_SLAB.defaultBlockState());
        }
        // tables and benches along both sides, beds at the far end, stores by the door
        for (int b = 3; b < l - 4; b += 3) {
            for (int a : new int[]{1, w - 2}) {
                if (a == mid) continue;
                put.accept(new int[]{a, b, 0}, Furniture.table("spruce"));
                put.accept(new int[]{a, b + 1, 0}, Furniture.chair("spruce", along.getOpposite()));
                put.accept(new int[]{a, b, 1}, Blocks.CANDLE.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
            }
            put.accept(new int[]{mid, b, height - 1}, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
        BlockState bed = Blocks.WHITE_BED.defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.FACING, along.getOpposite());
        for (int a : new int[]{1, w - 2}) {
            put.accept(new int[]{a, l - 3, 0}, bed.setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD));
            put.accept(new int[]{a, l - 2, 0}, bed.setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.FOOT));
        }
        put.accept(new int[]{1, 1, 0}, Blocks.BARREL.defaultBlockState());
        put.accept(new int[]{w - 2, 1, 0}, Blocks.CHEST.defaultBlockState());
        put.accept(new int[]{w - 2, 2, 0}, Furniture.crate("spruce"));
        put.accept(new int[]{1, 2, 0}, Furniture.cabinet("spruce", across));
        put.accept(new int[]{1, 2, 1}, Furniture.jar("spruce", across));
        put.accept(new int[]{w - 2, 2, 1}, Furniture.cuttingBoard("spruce", across.getOpposite()));
        // fur-covered benches by the hearth and a stool at each table
        for (int b = 4; b < l - 4; b += 6) {
            if (mid - 1 > 1) put.accept(new int[]{mid - 1, b, 0}, Furniture.divan("brown", across));
            if (mid + 1 < w - 2) put.accept(new int[]{mid + 1, b, 0}, Furniture.divan("brown", across.getOpposite()));
        }
        for (int b = 3; b < l - 4; b += 3) {
            for (int a : new int[]{1, w - 2}) put.accept(new int[]{a, b - 1, 0}, Furniture.stool("brown"));
        }
        put.accept(new int[]{mid, 1, height - 1}, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        put.accept(new int[]{mid, l - 2, height - 1}, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        if (hall) { // the jarl's seat at the far end, on a step, under shields
            for (int a = 1; a < w - 1; a++) put.accept(new int[]{a, l - 2, -1}, Blocks.SPRUCE_PLANKS.defaultBlockState());
            put.accept(new int[]{mid, l - 3, 0}, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, along));
            put.accept(new int[]{mid - 1, l - 3, 0}, Blocks.WHITE_WOOL.defaultBlockState());
            put.accept(new int[]{mid + 1, l - 3, 0}, Blocks.WHITE_WOOL.defaultBlockState());
            for (int a = 2; a < w - 2; a += 2) {
                set(level, alongZ ? x0 + a : x0 + l - 2, y + 3, alongZ ? z0 + l - 2 : z0 + a,
                        (a % 4 == 0 ? Blocks.BLUE_WALL_BANNER : Blocks.RED_WALL_BANNER).defaultBlockState().setValue(WallBannerBlock.FACING, along.getOpposite()));
            }
        }
    }

    /**
     * The jarl's great hall, after the reference: on a stone podium with a wooden deck and railing all
     * round, a tall hall of warm spruce under a steep wooden roof, a cross-wing over its middle, carved
     * dragon heads at every gable, a great round shield over the door, braziers before it and a row
     * of runestones along the edge of the hold.
     */
    private void greatHall() {
        int w = 15, l = 33, height = 8;
        int x0 = fx - w / 2, z0 = fz - l / 2 + 2;
        int y = base + RISE[3] + 2; // on the podium
        // the podium: two blocks of stone, the deck of planks one block wider all round, a railing
        for (int x = x0 - 4; x <= x0 + w + 3; x++) {
            for (int z = z0 - 4; z <= z0 + l + 3; z++) {
                boolean edge = x == x0 - 4 || x == x0 + w + 3 || z == z0 - 4 || z == z0 + l + 3;
                for (int dy = 0; dy < 2; dy++) set(level, x, base + RISE[3] + dy, z, edge ? wall(x, base + RISE[3] + dy, z) : stone(x, y, z));
                set(level, x, y - 1, z, Blocks.SPRUCE_PLANKS.defaultBlockState());
                for (int dy = 0; dy < 4; dy++) set(level, x, y + dy, z, AIR);
                if (edge) {
                    set(level, x, y, z, Math.floorMod(x + z, 4) == 0 ? Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState() : Blocks.SPRUCE_FENCE.defaultBlockState());
                    if (Math.floorMod(x + z, 8) == 0) set(level, x, y + 1, z, Blocks.LANTERN.defaultBlockState());
                }
            }
        }
        // a stair up the podium from the main street
        int front = z0 + l + 3;
        for (int i = 0; i < 2; i++) {
            for (int dx = -2; dx <= 2; dx++) {
                set(level, fx + dx, base + RISE[3] + i, front + 2 - i, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
                set(level, fx + dx, base + RISE[3] + i + 1, front + 2 - i, AIR);
            }
        }
        for (int dx = -2; dx <= 2; dx++) set(level, fx + dx, y, front, AIR);
        // the hall
        longhouse(x0, z0, w, l, true, y, height, true);
        for (int b = 4; b < l - 4; b += 4) {
            for (int a : new int[]{3, w - 4}) {
                for (int dy = 0; dy < height; dy++) set(level, x0 + a, y + dy, z0 + b, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
            }
        }
        hallFront(x0, z0, w, l, y, height);
        // a cross-wing over the middle: a gable roof across, rising as high as the hall's
        int cz0 = z0 + l / 2 - 4, cl = 9, cw = w + 6;
        int crossY = y + height - 1;
        for (int a = 0; a < cw; a++) {
            for (int bb = 0; bb < cl; bb++) {
                int x = x0 - 3 + a, z = cz0 + bb;
                boolean outer = a < 3 || a >= cw - 3;
                if (!outer) continue;
                boolean edge = bb == 0 || bb == cl - 1 || a == 0 || a == cw - 1;
                for (int dy = -height + 1; dy <= 0; dy++) {
                    set(level, x, crossY + dy, z, edge ? (bb == 0 || bb == cl - 1 ? Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()
                            : (crossY + dy) % 3 == 0 ? Blocks.SPRUCE_TRAPDOOR.defaultBlockState() : Blocks.SPRUCE_PLANKS.defaultBlockState()) : AIR);
                }
            }
        }
        int crossRidge = roof(x0 - 3, cz0, cl, cw, false, crossY + 1, hallTimber());
        int hallRidge = y + height + (w + 1) / 2;
        // dragon heads at the four gable ends of the hall and the wing
        dragon(fx, hallRidge, z0 - 2, Direction.NORTH);
        dragon(fx, hallRidge, z0 + l + 1, Direction.SOUTH);
        dragon(x0 - 5, crossRidge, cz0 + cl / 2, Direction.WEST);
        dragon(x0 + w + 4, crossRidge, cz0 + cl / 2, Direction.EAST);
        // the great round shield over the door
        shield(fx, y + height + 3, z0 + l - 1, Direction.SOUTH);
        for (int s2 : new int[]{-6, 6}) brazierPost(fx + s2, y, z0 + l + 2);
        for (int s2 : new int[]{-9, 9}) flagpole(fx + s2, y, z0 + l + 2, Direction.EAST);
        // runestones along the front of the hold
        for (int dx = -18; dx <= 18; dx += 6) {
            int rx = fx + dx, rz = front + 6;
            if (Math.abs(dx) < 4 || tier(rx, rz) != 3) continue;
            int ry = base + RISE[3];
            set(level, rx, ry, rz, Blocks.STONE_BRICKS.defaultBlockState());
            set(level, rx, ry + 1, rz, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
            set(level, rx, ry + 2, rz, Blocks.STONE_BRICK_SLAB.defaultBlockState());
            banner(rx, ry + 1, rz + 1, Direction.SOUTH, Blocks.RED_WALL_BANNER);
        }
        taken.add(new int[]{x0 - 8, z0 - 6, x0 + w + 8, front + 9});
    }

    /**
     * The outside of the jarl's hall, after the concept: a plinth of stone bricks, posts of timber
     * standing out of the long walls, two rows of wide windows between them with sconces, and great
     * banners of the clans on the front gable.
     */
    private void hallFront(int x0, int z0, int w, int l, int y, int height) {
        for (int b = 0; b < l; b++) {
            for (int[] side : new int[][]{{x0, -1}, {x0 + w - 1, 1}}) {
                int wx = side[0], ox = wx + side[1];
                Direction out = side[1] < 0 ? Direction.WEST : Direction.EAST;
                for (int dy = 0; dy < 2; dy++) set(level, wx, y + dy, z0 + b, Blocks.STONE_BRICKS.defaultBlockState());
                set(level, ox, y, z0 + b, Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, out.getOpposite()));
                if (b % 4 == 0) { // a post standing out of the wall
                    for (int dy = 0; dy < height; dy++) set(level, ox, y + dy, z0 + b, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
                    set(level, ox, y + height, z0 + b, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, out.getOpposite())
                            .setValue(StairBlock.HALF, Half.TOP));
                } else if (b > 0 && b < l - 1) { // wide windows on two storeys, a sconce between them
                    for (int dy : new int[]{2, 3, 5, 6}) set(level, wx, y + dy, z0 + b, Blocks.GLASS_PANE.defaultBlockState());
                    if (b % 4 == 2) set(level, ox, y + 4, z0 + b, Furniture.sconce(out));
                }
            }
        }
        // the front: a plinth, great banners either side of the door, sconces by it
        int fz2 = z0 + l - 1;
        for (int a = 0; a < w; a++) for (int dy = 0; dy < 2; dy++) {
            if (Math.abs(x0 + a - fx) > 1) set(level, x0 + a, y + dy, fz2, Blocks.STONE_BRICKS.defaultBlockState());
        }
        for (int dx : new int[]{-5, 5}) {
            set(level, fx + dx, y + height + 1, fz2 + 1, SoFEBlocks.CLAN_BANNER.get().defaultBlockState()
                    .setValue(com.sofe.block.ClanBanner.FACING, Direction.SOUTH));
            for (int dy = 1; dy <= height; dy++) set(level, fx + dx, y + dy, fz2 + 1, AIR);
        }
        for (int dx : new int[]{-2, 2}) set(level, fx + dx, y + 2, fz2 + 1, Furniture.sconce(Direction.SOUTH));
    }

    /** A carved dragon head on a gable: a neck of stairs leaning out and up, a fence for the horn. */
    private void dragon(int x, int y, int z, Direction out) {
        BlockState stair = Blocks.SPRUCE_STAIRS.defaultBlockState();
        set(level, x, y, z, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        int x1 = x + out.getStepX(), z1 = z + out.getStepZ();
        set(level, x1, y + 1, z1, stair.setValue(StairBlock.FACING, out.getOpposite()));
        set(level, x1, y, z1, stair.setValue(StairBlock.FACING, out).setValue(StairBlock.HALF, Half.TOP));
        int x2 = x1 + out.getStepX(), z2 = z1 + out.getStepZ();
        set(level, x2, y + 2, z2, stair.setValue(StairBlock.FACING, out));
        set(level, x2, y + 1, z2, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        set(level, x1, y + 2, z1, Blocks.SPRUCE_FENCE.defaultBlockState());
        set(level, x1, y + 3, z1, Blocks.SPRUCE_FENCE.defaultBlockState());
    }

    /** A round shield of red and white quarters with an iron boss, flat on a wall facing out. */
    private void shield(int cx, int cy, int z, Direction out) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d > 3.3) continue;
                BlockState s2 = d > 2.4 ? Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState()
                        : d < 0.9 ? Blocks.IRON_BLOCK.defaultBlockState()
                        : (dx >= 0) == (dy >= 0) ? Blocks.RED_WOOL.defaultBlockState() : Blocks.WHITE_WOOL.defaultBlockState();
                set(level, cx + dx, cy + dy, z + out.getStepZ(), s2);
            }
        }
    }

    /** The smithy by the nearest lava basin: an open forge under a roof, anvils, furnaces and stores. */
    private void smithy() {
        for (int t = 2; t >= 1; t--) {
            for (int attempt = 0; attempt < 300; attempt++) {
                double a = random.nextDouble() * Math.PI * 2, d = RING[t + 1] + 6 + random.nextDouble() * (RING[t] - RING[t + 1] - 14);
                int x = fx + (int) Math.round(Math.cos(a) * d), z = fz + (int) Math.round(Math.sin(a) * d);
                if (!free(x - 6, z - 5, x + 6, z + 5, t)) continue;
                int y = base + RISE[t];
                for (int dx = -5; dx <= 5; dx++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        set(level, x + dx, y - 1, z + dz, Blocks.COBBLESTONE.defaultBlockState());
                        boolean corner = Math.abs(dx) == 5 && Math.abs(dz) == 4;
                        boolean wallHere = (dz == -4 || Math.abs(dx) == 5) && !corner; // back and sides; the front stays open
                        for (int dy = 0; dy < 4; dy++) {
                            BlockState state = AIR;
                            if (corner || (Math.abs(dx) == 5 && dz % 4 == 0)) state = timber(Direction.Axis.Y);
                            else if (wallHere) state = dy == 0 ? Blocks.COBBLESTONE.defaultBlockState()
                                    : dy == 2 && Math.floorMod(dx + dz, 3) == 0 ? Blocks.GLASS_PANE.defaultBlockState()
                                    : SoFEBlocks.NORDRATH_DARK_PLANKS.get().defaultBlockState();
                            set(level, x + dx, y + dy, z + dz, state);
                        }
                    }
                }
                roof(x - 5, z - 4, 9, 11, false, y + 4);
                set(level, x, y, z - 2, SoFEBlocks.IMPERIAL_FORGE.get().defaultBlockState());
                set(level, x - 3, y, z, Blocks.ANVIL.defaultBlockState());
                set(level, x + 3, y, z, Blocks.ANVIL.defaultBlockState());
                set(level, x - 4, y, z - 3, Blocks.BLAST_FURNACE.defaultBlockState());
                set(level, x + 4, y, z - 3, Blocks.BLAST_FURNACE.defaultBlockState());
                set(level, x - 4, y, z + 3, Blocks.GRINDSTONE.defaultBlockState());
                set(level, x + 4, y, z + 3, Blocks.SMITHING_TABLE.defaultBlockState());
                set(level, x, y, z + 3, Blocks.CAULDRON.defaultBlockState());
                for (int dx = -1; dx <= 1; dx++) set(level, x + dx, y, z - 4, SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
                Architecture.chandelier(level, x, y + 4, y + 2, z);
                taken.add(new int[]{x - 8, z - 7, x + 8, z + 7});
                doors.add(new int[]{x, z + 6});
                return;
            }
        }
    }

    /**
     * The great market of the clans, on the second terrace by the main street: a paved square with rows
     * of stalls under striped awnings and hide roofs, carts with crates, shelves of goods, barrels and
     * chests, lantern posts, flags, and a fire pit with benches in the middle.
     */
    /** Inside or on the shell of a tower (one block of margin). */
    private boolean inTower(int x, int z) {
        return inTower(x, z, 1);
    }

    private boolean inTower(int x, int z, int margin) {
        for (int[] t : towers) if ((x - t[0]) * (x - t[0]) + (z - t[1]) * (z - t[1]) <= (t[2] + margin) * (t[2] + margin)) return true;
        return false;
    }

    private void market() {
        int t = 1, y = base + RISE[t];
        int cz = fz + RING[2] + 14, half = 23, depth = 11;
        taken.add(new int[]{fx - half - 4, cz - depth - 4, fx + half + 4, cz + depth + 4});
        // the square: cobblestone with a border of stone bricks, cleared
        for (int x = fx - half; x <= fx + half; x++) {
            for (int z = cz - depth; z <= cz + depth; z++) {
                if (tier(x, z) != t || volcanic(x, z) || inTower(x, z)) continue;
                boolean border = Math.abs(x - fx) == half || Math.abs(z - cz) == depth;
                set(level, x, y - 1, z, border ? Blocks.STONE_BRICKS.defaultBlockState()
                        : Math.floorMod(x * 7 + z * 3, 5) == 0 ? Blocks.GRAVEL.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
                for (int dy = 0; dy < 6; dy++) set(level, x, y + dy, z, AIR);
            }
        }
        gallery(fx, cz, half, depth, y, t);
        for (int side : new int[]{-1, 1}) { // paved ways from the market to the flights up beside it
            for (int x = fx + side * (half + 1); Math.abs(x - fx) <= Math.abs(sideStair[side < 0 ? 0 : 1]) + 2; x += side) {
                for (int dz = -1; dz <= 1; dz++) if (tier(x, cz + dz) == t) pathAt(x, cz + dz);
            }
            for (int z = cz; z > cz - 40; z--) {
                int sx = fx + sideStair[side < 0 ? 0 : 1];
                for (int dx = -1; dx <= 1; dx++) if (tier(sx + dx, z) == t) pathAt(sx + dx, z);
            }
        }
        // rows of stalls on both sides of the street through the square
        int kind = 0;
        for (int side : new int[]{-1, 1}) {
            for (int row = -1; row <= 1; row++) {
                for (int col = 0; col < 2; col++) {
                    int x = fx + side * (5 + col * 7) - (side < 0 ? 4 : 0);
                    int z = cz + row * 7 - 2;
                    if (tier(x, z) != t || tier(x + 4, z + 4) != t) continue;
                    BlockState hide = kind % 3 == 0 ? Blocks.BROWN_WOOL.defaultBlockState() : Blocks.WHITE_WOOL.defaultBlockState();
                    vikingStall(x, y, z, hide, kind++);
                }
            }
        }
        // carts with crates, barrels and chests by the stalls
        cart(fx - 3, y, cz - depth + 2);
        cart(fx + 3, y, cz + depth - 3);
        for (int[] c : new int[][]{{-half + 1, -depth + 1}, {half - 1, -depth + 1}, {-half + 1, depth - 1}, {half - 1, depth - 1}}) {
            set(level, fx + c[0], y, cz + c[1], Blocks.BARREL.defaultBlockState());
            set(level, fx + c[0] - Integer.signum(c[0]), y, cz + c[1], Furniture.crate("spruce"));
            set(level, fx + c[0], y, cz + c[1] - Integer.signum(c[1]), Blocks.CHEST.defaultBlockState());
        }
        // lantern posts round the square and flags at its corners
        for (int x = fx - half + 2; x <= fx + half - 2; x += 6) {
            for (int z : new int[]{cz - depth + 1, cz + depth - 1}) {
                if (Math.abs(x - fx) <= 2) continue;
                set(level, x, y, z, Blocks.SPRUCE_FENCE.defaultBlockState());
                set(level, x, y + 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
                set(level, x, y + 2, z, Blocks.LANTERN.defaultBlockState());
            }
        }
        flagpole(fx - half + 1, y, cz, Direction.EAST);
        flagpole(fx + half - 1, y, cz, Direction.WEST);
        // the fire pit with benches round it, in the middle
        int px = fx, pz = cz;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) set(level, px + dx, y - 1, pz + dz, Blocks.COBBLESTONE.defaultBlockState());
        set(level, px, y, pz, Blocks.CAMPFIRE.defaultBlockState());
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (d.getAxis() == Direction.Axis.Z) continue; // the street runs through north and south
            for (int w = -1; w <= 1; w++) {
                Direction a = d.getClockWise();
                set(level, px + d.getStepX() * 3 + a.getStepX() * w, y, pz + d.getStepZ() * 3 + a.getStepZ() * w,
                        Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, d));
            }
        }
        taken.add(new int[]{fx - half - 2, cz - depth - 2, fx + half + 2, cz + depth + 2});
        doors.add(new int[]{fx + 2, cz - 5});
        doors.add(new int[]{fx - 2, cz + 5});
    }

    /**
     * A covered gallery round the market square, as a market hall: three blocks deep, an outer wall of
     * dark timber with windows, posts toward the square, a sloped roof of spruce, lanterns under it,
     * benches and goods along the wall. It opens to the street on the north and south, and outside it
     * trees and planters line the walls.
     */
    private void gallery(int cx, int cz, int half, int depth, int y, int t) {
        for (int x = cx - half; x <= cx + half; x++) {
            for (int z = cz - depth; z <= cz + depth; z++) {
                int ax = Math.abs(x - cx), az = Math.abs(z - cz);
                int ring = Math.min(half - ax, depth - az); // 0 on the outer wall, 2 at the posts
                if (ring > 2 || tier(x, z) != t || volcanic(x, z) || inTower(x, z)) continue;
                // ways in on every side: the street on the north and south, and doors in the middle of each side
                boolean street = ax <= 2 || az <= 1 || Math.abs(ax - half / 2) <= 1;
                boolean outer = ring == 0, posts = ring == 2;
                for (int dy = 0; dy < 4; dy++) {
                    BlockState state = AIR;
                    if (outer && !street) {
                        boolean postHere = (ax + az) % 4 == 0;
                        state = postHere ? Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()
                                : dy == 0 ? Blocks.COBBLESTONE.defaultBlockState()
                                : dy <= 2 ? Blocks.GLASS_PANE.defaultBlockState() : SoFEBlocks.NORDRATH_DARK_PLANKS.get().defaultBlockState();
                    } else if (posts && (ax + az) % 4 == 0 && !street) {
                        state = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
                    }
                    set(level, x, y + dy, z, state);
                }
                // the roof slopes down toward the square
                Direction toSquare = half - ax < depth - az ? (x > cx ? Direction.WEST : Direction.EAST) : (z > cz ? Direction.NORTH : Direction.SOUTH);
                BlockState roofBlock = ring == 0 ? Blocks.SPRUCE_PLANKS.defaultBlockState()
                        : Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, toSquare.getOpposite());
                if (!street) set(level, x, y + 4 + (ring == 0 ? 1 : ring == 1 ? 0 : -1) + 1, z, roofBlock);
                if (!street && ring == 1 && (ax + az) % 6 == 3) set(level, x, y + 3, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
                if (!street && ring == 1 && (ax + az) % 8 == 1) set(level, x, y, z, (ax + az) % 16 == 1 ? Blocks.BARREL.defaultBlockState()
                        : Furniture.crate("spruce"));
            }
        }
        // outside the gallery: trees and planters along its walls
        for (int x = cx - half - 2; x <= cx + half + 2; x += 5) {
            for (int z : new int[]{cz - depth - 2, cz + depth + 2}) {
                if (Math.abs(x - cx) <= 4 || tier(x, z) != t) continue;
                planter(x, y, z);
            }
        }
        for (int z = cz - depth; z <= cz + depth; z += 5) {
            for (int x : new int[]{cx - half - 2, cx + half + 2}) if (tier(x, z) == t) planter(x, y, z);
        }
    }

    /** A planter of spruce logs with a bush, flowers or a small tree. */
    private void planter(int x, int y, int z) {
        if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) return;
        set(level, x, y - 1, z, Blocks.GRASS_BLOCK.defaultBlockState());
        switch (random.nextInt(4)) {
            case 0 -> tree(x, y, z);
            case 1 -> set(level, x, y, z, Blocks.FLOWERING_AZALEA.defaultBlockState());
            case 2 -> set(level, x, y, z, Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE, 3));
            default -> set(level, x, y, z, Blocks.AZALEA.defaultBlockState());
        }
    }

    /** A hand cart: a bed of planks on two wheels (trapdoors), shafts of fence, loaded with crates. */
    private void cart(int x, int y, int z) {
        for (int dx = 0; dx <= 1; dx++) {
            set(level, x + dx, y, z, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
            set(level, x + dx, y + 1, z, dx == 0 ? Furniture.crate("spruce") : Blocks.HAY_BLOCK.defaultBlockState());
        }
        set(level, x, y, z - 1, Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN, true)
                .setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, Direction.NORTH));
        set(level, x, y, z + 1, Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN, true)
                .setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, Direction.SOUTH));
        set(level, x + 2, y, z, Blocks.SPRUCE_FENCE.defaultBlockState());
        set(level, x + 3, y, z, Blocks.SPRUCE_FENCE.defaultBlockState());
    }

    /** A stall: four spruce posts, a sloped roof of hides, a counter and the goods of its trade. */
    private void vikingStall(int x, int y, int z, BlockState hide, int kind) {
        for (int[] p2 : new int[][]{{0, 0}, {4, 0}, {0, 4}, {4, 4}}) {
            for (int dy = 0; dy < (p2[1] == 0 ? 4 : 3); dy++) set(level, x + p2[0], y + dy, z + p2[1], Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        }
        for (int dx = -1; dx <= 5; dx++) {
            set(level, x + dx, y + 4, z, Blocks.SPRUCE_SLAB.defaultBlockState());
            Block[] stripes = {Blocks.RED_WOOL, Blocks.GREEN_WOOL, Blocks.BLUE_WOOL, Blocks.YELLOW_WOOL};
            BlockState cover = Math.abs(kind) % 3 != 0 ? (Math.floorMod(dx, 2) == 0 ? stripes[Math.abs(kind) % stripes.length] : Blocks.WHITE_WOOL).defaultBlockState() : hide;
            for (int dz = 1; dz <= 4; dz++) set(level, x + dx, y + (dz <= 2 ? 4 : 3), z + dz, cover);
        }
        for (int dx = 1; dx <= 3; dx++) {
            set(level, x + dx, y, z + 4, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP)); // the counter
            set(level, x + dx, y + 1, z + 4, Furniture.facing(Furniture.from("supplementaries", "item_shelf", AIR), Direction.SOUTH));
        }
        set(level, x - 1, y, z + 4, Blocks.BARREL.defaultBlockState());
        switch (Math.abs(kind) % 3 + 1) {
            case 1 -> { // the fishmonger: a drying rack of fences hung with fish (as cod in barrels below)
                for (int dx = 1; dx <= 3; dx++) set(level, x + dx, y + 2, z + 1, Blocks.SPRUCE_FENCE.defaultBlockState());
                set(level, x + 1, y, z + 1, Blocks.BARREL.defaultBlockState());
                set(level, x + 3, y, z + 1, Blocks.BARREL.defaultBlockState());
            }
            case 2 -> { // the furrier: furs on a rack
                for (int dx = 1; dx <= 3; dx++) for (int dy = 1; dy <= 2; dy++) set(level, x + dx, y + dy, z + 1, (dx == 2 ? Blocks.WHITE_WOOL : Blocks.BROWN_WOOL).defaultBlockState());
            }
            default -> {
                set(level, x + 1, y, z + 1, Furniture.crate("spruce"));
                set(level, x + 3, y, z + 1, Blocks.HAY_BLOCK.defaultBlockState());
            }
        }
        set(level, x + 2, y + 3, z + 2, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    /** Spruces, berry bushes, ferns and shrubs on the free ground of the terraces, as in the concept. */
    private void greenery() {
        for (int i = 0; i < 1200; i++) {
            double a = random.nextDouble() * Math.PI * 2, d = 12 + random.nextDouble() * (RING[0] - 16);
            int x = fx + (int) Math.round(Math.cos(a) * d), z = fz + (int) Math.round(Math.sin(a) * d);
            int t = tier(x, z);
            if (t < 0 || volcanic(x, z)) continue;
            boolean tree = random.nextInt(4) == 0;
            int m = tree ? 3 : 1;
            if (!free(x - m, z - m, x + m, z + m, t)) continue;
            int y = base + RISE[t];
            BlockState below = level.getBlockState(new BlockPos(x, y - 1, z));
            if (!(below.is(Blocks.SNOW_BLOCK) || below.is(Blocks.PODZOL) || below.is(Blocks.COARSE_DIRT) || below.is(Blocks.GRASS_BLOCK)
                    || below.is(Blocks.MOSS_BLOCK))) continue;
            if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) continue;
            if (tree) {
                set(level, x, y - 1, z, Blocks.PODZOL.defaultBlockState());
                spruce(x, y, z, 5 + random.nextInt(3));
                taken.add(new int[]{x - 2, z - 2, x + 2, z + 2});
            } else {
                BlockState plant = switch (random.nextInt(7)) {
                    case 0 -> Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE, 3);
                    case 1 -> Blocks.FERN.defaultBlockState();
                    case 2 -> Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
                    case 3 -> Blocks.FLOWERING_AZALEA.defaultBlockState();
                    case 4 -> Blocks.MOSS_CARPET.defaultBlockState();
                    case 5 -> Blocks.LILY_OF_THE_VALLEY.defaultBlockState();
                    default -> Blocks.AZALEA.defaultBlockState();
                };
                set(level, x, y - 1, z, Blocks.GRASS_BLOCK.defaultBlockState());
                set(level, x, y, z, plant);
            }
        }
    }

    /** A giant sword of the clans driven into the ground: an iron blade, a gold cross-guard, a dark grip and pommel. */
    private void giantSword(int x, int z) {
        int t = tier(x, z);
        if (t < 0) return;
        int y = base + RISE[t];
        for (int dy = 0; dy < 8; dy++) {
            set(level, x, y + dy, z, Blocks.IRON_BLOCK.defaultBlockState());
            if (dy < 7) {
                set(level, x - 1, y + dy, z, Blocks.IRON_BARS.defaultBlockState());
                set(level, x + 1, y + dy, z, Blocks.IRON_BARS.defaultBlockState());
            }
        }
        for (int dx = -3; dx <= 3; dx++) set(level, x + dx, y + 8, z, Math.abs(dx) == 3 ? Blocks.GOLD_BLOCK.defaultBlockState()
                : Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        for (int dy = 9; dy <= 10; dy++) set(level, x, y + dy, z, SoFEBlocks.NORDRATH_DARK_TIMBER.get().defaultBlockState());
        set(level, x, y + 11, z, Blocks.GOLD_BLOCK.defaultBlockState());
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            if (dx != 0 || dz != 0) set(level, x + dx, y, z + dz, Blocks.COBBLESTONE_WALL.defaultBlockState());
        }
        taken.add(new int[]{x - 4, z - 2, x + 4, z + 2});
    }

    /** A fenced field of wheat or barley-coloured beetroot on the lowest terrace, watered by a channel. */
    private void field(int n) {
        for (int attempt = 0; attempt < 200; attempt++) {
            double a = random.nextDouble() * Math.PI * 2, d = RING[1] + 8 + random.nextDouble() * (RING[0] - RING[1] - 20);
            int x = fx + (int) Math.round(Math.cos(a) * d), z = fz + (int) Math.round(Math.sin(a) * d);
            if (!free(x - 6, z - 5, x + 6, z + 5, 0)) continue;
            BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 7);
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    boolean edge = Math.abs(dx) == 5 || Math.abs(dz) == 4;
                    if (edge) {
                        set(level, x + dx, base - 1, z + dz, Blocks.COARSE_DIRT.defaultBlockState());
                        set(level, x + dx, base, z + dz, dx == 0 && dz == -4 ? Blocks.SPRUCE_FENCE_GATE.defaultBlockState() : Blocks.SPRUCE_FENCE.defaultBlockState());
                    } else if (dz == 0) {
                        set(level, x + dx, base - 1, z + dz, Blocks.WATER.defaultBlockState());
                        set(level, x + dx, base, z + dz, AIR);
                    } else {
                        set(level, x + dx, base - 1, z + dz, farmland);
                        Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.WHEAT, Blocks.BEETROOTS, Blocks.CARROTS};
                        var crop = (net.minecraft.world.level.block.CropBlock) crops[n % crops.length];
                        set(level, x + dx, base, z + dz, crop.getStateForAge(Math.max(1, crop.getMaxAge() - random.nextInt(2))));
                    }
                }
            }
            brazierPost(x - 5, base + 1, z - 4);
            set(level, x + 6, base, z, Blocks.HAY_BLOCK.defaultBlockState());
            taken.add(new int[]{x - 8, z - 7, x + 8, z + 7});
            return;
        }
    }

    /** A pen of sheep or goats on the lowest terrace, fenced in spruce, with hay. */
    private void pen(int n) {
        for (int attempt = 0; attempt < 200; attempt++) {
            double a = random.nextDouble() * Math.PI * 2, d = RING[1] + 8 + random.nextDouble() * (RING[0] - RING[1] - 20);
            int x = fx + (int) Math.round(Math.cos(a) * d), z = fz + (int) Math.round(Math.sin(a) * d);
            if (!free(x - 6, z - 6, x + 6, z + 6, 0)) continue;
            int y = base;
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    boolean edge = Math.abs(dx) == 5 || Math.abs(dz) == 5;
                    set(level, x + dx, y - 1, z + dz, Blocks.GRASS_BLOCK.defaultBlockState());
                    set(level, x + dx, y, z + dz, edge ? (dx == 0 && dz == -5 ? Blocks.SPRUCE_FENCE_GATE.defaultBlockState() : Blocks.SPRUCE_FENCE.defaultBlockState()) : AIR);
                }
            }
            set(level, x - 3, y, z - 3, Blocks.HAY_BLOCK.defaultBlockState());
            set(level, x + 3, y, z + 3, Blocks.HAY_BLOCK.defaultBlockState());
            brazierPost(x - 5, y + 1, z - 5);
            EntityType<?>[] beasts = {EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.CHICKEN, EntityType.COW, EntityType.GOAT};
            EntityType<?> kind = beasts[n % beasts.length];
            for (int i = 0; i < 4; i++) {
                var animal = kind.create(level);
                if (animal == null) continue;
                animal.moveTo(x - 2.5 + random.nextInt(5), y, z - 2.5 + random.nextInt(5), random.nextFloat() * 360, 0);
                if (animal instanceof net.minecraft.world.entity.Mob mob) mob.setPersistenceRequired();
                level.addFreshEntity(animal);
            }
            taken.add(new int[]{x - 8, z - 8, x + 8, z + 8});
            return;
        }
    }

    // ------------------------------------------------------------------ the harbour

    /**
     * Down from the west gate: a long stone stair on a buttress to the water, then a pier on posts
     * along the fjord, with jetties and longboats moored at them.
     */
    private void harbour() {
        int x = fx - RING[0], z = fz;
        int y = base;
        Direction down = Direction.WEST;
        while (y > sea + 1) { // the stair, five wide, one step down per block, on solid stone
            x--;
            y--;
            for (int w = -2; w <= 2; w++) {
                int px = x, pz = z + w;
                int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, px, pz);
                for (int fy = Math.min(floor, y) - 1; fy < y; fy++) set(level, px, fy, pz, wall(px, fy, pz));
                boolean edge = Math.abs(w) == 2;
                set(level, px, y, pz, edge ? Blocks.STONE_BRICKS.defaultBlockState()
                        : Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, down.getOpposite()));
                set(level, px, y + 1, pz, edge ? (y % 4 == 0 ? SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState() : Blocks.STONE_BRICK_WALL.defaultBlockState()) : AIR);
                for (int h = 2; h <= 4; h++) set(level, px, y + h, pz, AIR);
            }
        }
        // the pier
        int deck = sea + 1;
        int px0 = x - 1, px1 = x - 30;
        for (int px = px0; px >= px1; px--) {
            for (int w = -2; w <= 2; w++) {
                set(level, px, deck - 1, z + w, Blocks.SPRUCE_PLANKS.defaultBlockState());
                set(level, px, deck, z + w, AIR);
                if (Math.abs(w) == 2) {
                    set(level, px, deck, z + w, (px % 6 == 0) ? Blocks.SPRUCE_FENCE.defaultBlockState() : AIR);
                    if (px % 6 == 0) set(level, px, deck + 1, z + w, Blocks.LANTERN.defaultBlockState());
                }
                if (px % 4 == 0 && Math.abs(w) == 2) post(px, z + w, deck - 2);
            }
        }
        // a longship of blocks riding at anchor further out in the fjord
        longboat(px1 - 30, fz + 8, sea, true);
        harbourArea = new int[]{px1 - 50, z - 24, x + 4, z + 24};
        // two jetties with a ship beside each
        for (int k = 0; k < 2; k++) {
            int jx = px0 - 10 - k * 12;
            for (int side : new int[]{-1, 1}) {
                for (int j = 3; j <= 16; j++) {
                    int jz = z + side * j;
                    for (int w = 0; w <= 1; w++) set(level, jx + w, deck - 1, jz, Blocks.SPRUCE_PLANKS.defaultBlockState());
                    if (j % 4 == 0) post(jx, jz, deck - 2);
                }
                if (!drakkar(jx + 5, z + side * 10, side > 0 ? 0f : 180f)) longboat(jx + 5, z + side * 10, deck - 1, k == 0);
            }
        }
    }

    private void post(int x, int z, int top) {
        if (top == Integer.MIN_VALUE) return;
        int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        for (int y = floor; y <= top; y++) set(level, x, y, z, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
    }

    /**
     * A drakkar of Small Ships moored at a jetty: a real ship of spruce the players can board and sail.
     * Returns false when the mod is not there, so a ship of blocks is built instead.
     */
    private boolean drakkar(int x, int z, float yaw) {
        var type = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("smallships", "drakkar"));
        if (type == null || type == EntityType.PIG) return false;
        var ship = type.create(level);
        if (ship == null) return false;
        if (ship instanceof net.minecraft.world.entity.vehicle.Boat boat) boat.setVariant(net.minecraft.world.entity.vehicle.Boat.Type.SPRUCE);
        ship.moveTo(x + 0.5, sea - 0.2, z + 0.5, yaw, 0);
        // a red sail: Small Ships keeps the sail's colour in its save data
        net.minecraft.nbt.CompoundTag tag = ship.saveWithoutId(new net.minecraft.nbt.CompoundTag());
        net.minecraft.nbt.CompoundTag sail = tag.getCompound("Sail");
        sail.putString("Color", "red");
        tag.put("Sail", sail);
        tag.putString("Color", sail.getString("Color"));
        ship.load(tag);
        level.addFreshEntity(ship);
        return true;
    }

    /**
     * A drakkar lying along the z axis: a long spruce hull that narrows and rises at both ends into a
     * carved dragon prow and a curled stern, a deck, a mast with a yard and a large striped sail,
     * shields along both rails and oars out of the sides.
     */
    private void longboat(int x, int z, int waterline, boolean red) {
        int half = 11;
        BlockState plank = Blocks.SPRUCE_PLANKS.defaultBlockState(), dark = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        for (int b = -half; b <= half; b++) {
            int fromEnd = half - Math.abs(b);
            int width = fromEnd >= 4 ? 2 : fromEnd >= 2 ? 1 : 0;
            int rise = fromEnd >= 3 ? 0 : 3 - fromEnd; // the ends sweep up
            for (int a = -width; a <= width; a++) {
                set(level, x + a, waterline - 1 + rise, z + b, dark);                       // the keel and the bottom
                set(level, x + a, waterline + rise, z + b, Math.abs(a) == width ? dark : plank); // the deck
                if (Math.abs(a) == width) set(level, x + a, waterline + 1 + rise, z + b, Blocks.SPRUCE_SLAB.defaultBlockState());
                else set(level, x + a, waterline + 1 + rise, z + b, AIR);
            }
        }
        // the dragon prow and the curled stern
        for (int[] end : new int[][]{{-half - 1, -1}, {half + 1, 1}}) {
            int zb = z + end[0];
            set(level, x, waterline + 3, zb, dark);
            set(level, x, waterline + 4, zb, dark);
            set(level, x, waterline + 5, zb + end[1], dark);
            set(level, x, waterline + 6, zb + end[1], Blocks.DARK_OAK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, end[1] < 0 ? Direction.SOUTH : Direction.NORTH));
            if (end[1] < 0) { // the dragon's head and eyes
                set(level, x, waterline + 6, zb - 2, Blocks.DARK_OAK_SLAB.defaultBlockState());
                set(level, x - 1, waterline + 6, zb - 1, Blocks.RED_WOOL.defaultBlockState());
                set(level, x + 1, waterline + 6, zb - 1, Blocks.RED_WOOL.defaultBlockState());
            }
        }
        // the mast, the yard and a large striped sail
        for (int dy = 1; dy <= 11; dy++) set(level, x, waterline + dy, z, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        for (int a = -4; a <= 4; a++) set(level, x + a, waterline + 10, z + 1, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        for (int a = -4; a <= 4; a++) {
            for (int dy = 4; dy <= 9; dy++) {
                boolean stripe = Math.floorMod(a, 2) == 0;
                set(level, x + a, waterline + dy, z + 1, stripe ? (red ? Blocks.RED_WOOL : Blocks.BLUE_WOOL).defaultBlockState() : Blocks.WHITE_WOOL.defaultBlockState());
            }
        }
        // shields along the rails, oars out of the sides
        for (int b = -6; b <= 6; b += 2) {
            Block left = b % 4 == 0 ? Blocks.RED_WALL_BANNER : Blocks.YELLOW_WALL_BANNER;
            Block right = b % 4 == 0 ? Blocks.BLUE_WALL_BANNER : Blocks.WHITE_WALL_BANNER;
            banner(x - 3, waterline + 1, z + b, Direction.WEST, left);
            banner(x + 3, waterline + 1, z + b, Direction.EAST, right);
            set(level, x - 4, waterline, z + b + 1, Blocks.SPRUCE_FENCE.defaultBlockState());
            set(level, x + 4, waterline, z + b + 1, Blocks.SPRUCE_FENCE.defaultBlockState());
        }
        set(level, x, waterline + 12, z, Blocks.LANTERN.defaultBlockState());
    }

    // ------------------------------------------------------------------ the roads in

    /**
     * A road out of a gate: where the land falls away it runs on a trestle bridge of spruce, stepping
     * down slowly; where it meets the land it becomes a gravel road.
     */
    private void trestle(int x0, int z0, Direction out, int length) {
        int deck = base;
        Direction across = out.getClockWise();
        for (int t = 0; t < length; t++) {
            int x = x0 + out.getStepX() * t, z = z0 + out.getStepZ() * t;
            level.getChunk(x >> 4, z >> 4);
            int ground = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
            boolean water = !level.getFluidState(new BlockPos(x, ground, z)).isEmpty() || ground < sea;
            int target = Math.max(water ? sea + 2 : ground, base - t / 3);
            boolean bridge = water || target > ground + 1;
            int previous = deck;
            deck = target;
            for (int w = -3; w <= 3; w++) {
                int px = x + across.getStepX() * w, pz = z + across.getStepZ() * w;
                boolean rail = Math.abs(w) == 3;
                if (!bridge) {
                    if (rail) continue;
                    int gy = SultharisBuilder.surfaceY(level, px, pz) - 1;
                    set(level, px, gy, pz, (Math.abs(w) == 2 ? Blocks.COARSE_DIRT : Blocks.GRAVEL).defaultBlockState());
                    for (int h = 1; h <= 3; h++) set(level, px, gy + h, pz, AIR);
                    continue;
                }
                set(level, px, deck - 1, pz, rail ? Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState() : Blocks.SPRUCE_PLANKS.defaultBlockState());
                set(level, px, deck, pz, rail ? (t % 8 == 0 ? Blocks.SPRUCE_FENCE.defaultBlockState() : Blocks.SPRUCE_FENCE.defaultBlockState()) : AIR);
                if (rail && t % 8 == 0) set(level, px, deck + 1, pz, Blocks.LANTERN.defaultBlockState());
                for (int h = 1; h <= 3; h++) if (!rail) set(level, px, deck + h, pz, AIR);
                if (deck < previous && !rail) set(level, px, deck, pz, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, out.getOpposite()));
            }
            if (bridge && t % 6 == 0) { // a trestle: two legs, braces and a cap beam
                for (int w : new int[]{-3, 3}) {
                    int px = x + across.getStepX() * w, pz = z + across.getStepZ() * w;
                    int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, px, pz);
                    for (int y = floor - 1; y < deck - 1; y++) set(level, px, y, pz, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
                }
                for (int w = -2; w <= 2; w++) {
                    int px = x + across.getStepX() * w, pz = z + across.getStepZ() * w;
                    set(level, px, deck - 2, pz, Blocks.SPRUCE_PLANKS.defaultBlockState());
                    if (deck - ground > 5) set(level, px, deck - 2 - Math.abs(w), pz, Blocks.SPRUCE_FENCE.defaultBlockState());
                }
            }
        }
    }

    // ------------------------------------------------------------------ around the city

    /** Snowy spruces on the land round the city and below its cliffs. */
    private void forest() {
        for (int i = 0; i < 260; i++) {
            double a = random.nextDouble() * Math.PI * 2, d = RING[0] + 22 + random.nextDouble() * 60;
            int x = fx + (int) Math.round(Math.cos(a) * d), z = fz + (int) Math.round(Math.sin(a) * d);
            level.getChunk(x >> 4, z >> 4);
            if (x >= harbourArea[0] && x <= harbourArea[2] && z >= harbourArea[1] && z <= harbourArea[3]) continue;
            int y = SultharisBuilder.surfaceY(level, x, z);
            BlockState below = level.getBlockState(new BlockPos(x, y - 1, z));
            boolean natural = below.is(Blocks.SNOW_BLOCK) || below.is(Blocks.GRASS_BLOCK) || below.is(Blocks.PODZOL) || below.is(Blocks.DIRT)
                    || below.is(Blocks.COARSE_DIRT) || below.is(Blocks.STONE) || below.is(Blocks.GRAVEL) || below.is(Blocks.SNOW);
            if (!natural || !level.getFluidState(new BlockPos(x, y, z)).isEmpty() || !level.getFluidState(new BlockPos(x, y - 1, z)).isEmpty()) continue;
            spruce(x, y, z, 6 + random.nextInt(5));
        }
    }

    /** A spruce or pine as the game grows them, from its own tree features; a simple one if that fails. */
    private boolean tree(int x, int y, int z) {
        var features = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE);
        var key = switch (random.nextInt(4)) {
            case 0 -> net.minecraft.data.worldgen.features.TreeFeatures.PINE;
            case 1 -> net.minecraft.data.worldgen.features.TreeFeatures.MEGA_SPRUCE;
            default -> net.minecraft.data.worldgen.features.TreeFeatures.SPRUCE;
        };
        var feature = features.getHolder(key);
        if (feature.isEmpty()) return false;
        set(level, x, y - 1, z, Blocks.PODZOL.defaultBlockState());
        return feature.get().value().place(level, level.getChunkSource().getGenerator(), level.getRandom(), new BlockPos(x, y, z));
    }

    private void spruce(int x, int y, int z, int height) {
        if (tree(x, y, z)) return;
        BlockState leaves = Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int dy = 0; dy < height; dy++) set(level, x, y + dy, z, Blocks.SPRUCE_LOG.defaultBlockState());
        for (int dy = 2; dy <= height; dy++) {
            int r = Math.max(0, (height - dy) / 2 - (dy % 2 == 0 ? 0 : 1));
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) > r + 1 || (dx == 0 && dz == 0 && dy < height)) continue;
                    set(level, x + dx, y + dy, z + dz, leaves);
                }
            }
        }
        set(level, x, y + height, z, leaves);
        set(level, x, y + height + 1, z, leaves);
    }

    /** Snow over the city and the land round it: a layer or two on every open, solid top, none near the fire. */
    private void snow() {
        int r = RING[0] + 90;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = fx - r; x <= fx + r; x++) {
            for (int z = fz - r; z <= fz + r; z++) {
                if (volcanic(x, z) && volcano(x, z) > base + 40) continue;
                boolean nearLava = false;
                for (int[] l : lava) if (x >= l[0] - 2 && x <= l[2] + 2 && z >= l[1] - 2 && z <= l[3] + 2) nearLava = true;
                if (nearLava) continue;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                pos.set(x, y - 1, z);
                BlockState top = level.getBlockState(pos);
                double patch = Math.sin(x * 0.21) + Math.cos(z * 0.19) + Math.sin((x + z) * 0.11);
                if (patch < -0.4) continue; // bare patches, so the city is not one white sheet
                if ((top.is(Blocks.GRASS_BLOCK) || top.is(Blocks.MOSS_BLOCK)) && tier(x, z) >= 0 && patch < 1.4) continue; // the city stays green, a little snow at the edges
                if (top.is(Blocks.COBBLESTONE) || top.is(Blocks.GRAVEL) || top.is(Blocks.STONE_BRICKS) || top.is(Blocks.CRACKED_STONE_BRICKS)
                        || top.is(Blocks.MOSSY_STONE_BRICKS) || top.is(Blocks.ANDESITE) || top.is(Blocks.SPRUCE_PLANKS) || top.is(Blocks.STRIPPED_SPRUCE_LOG)) continue;
                if (!top.isFaceSturdy(level, pos, Direction.UP) || top.is(Blocks.DIRT_PATH) || top.is(Blocks.CAMPFIRE) || top.is(Blocks.LAVA)
                        || top.is(Blocks.MAGMA_BLOCK) || top.is(SoFEBlocks.NORDRATH_IRON_BRAZIER.get()) || top.is(Blocks.SPRUCE_PLANKS)) continue;
                if (!level.getBlockState(pos.above()).isAir()) continue;
                set(level, x, y, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1));
            }
        }
    }

    // ------------------------------------------------------------------ the people

    static final List<String> CITIZENS = List.of("nordrath_shieldmaiden", "nordrath_fisher", "nordrath_widow", "nordrath_apprentice",
            "nordrath_hunter", "nordrath_elder", "nordrath_child", "nordrath_brewer", "nordrath_raider");

    private void citizens() {
        java.util.Collections.shuffle(doors, random);
        int i = 0;
        for (String id : CITIZENS) {
            if (i >= doors.size()) break;
            int[] at = doors.get(i++);
            StoryPlacements.spawnNpc(level, new StructurePositions.Npc(id, "citizen", at[0], at[1], random.nextInt(360), null));
        }
        // the skald by the hall's braziers, the furrier and the runesmith at the market
        int hallFront = fz - 33 / 2 + 2 + 33 + 4;
        StoryPlacements.spawnNpc(level, new StructurePositions.Npc("nordrath_skald", "story", fx, hallFront, 0, null));
        int market = fz + RING[2] + 12;
        StoryPlacements.spawnNpc(level, new StructurePositions.Npc("nordrath_furrier", "merchant", fx - 8, market + 2, 180, "quartermaster"));
        StoryPlacements.spawnNpc(level, new StructurePositions.Npc("nordrath_runesmith", "merchant", fx + 8, market + 2, 180, "smith"));
        // Hrafna and her bone dice at the far stall: the gambler of the clans
        StoryPlacements.spawnNpc(level, new StructurePositions.Npc("nordrath_gambler", "merchant", fx + 14, market + 9, 180, "gambler"));
    }
}
