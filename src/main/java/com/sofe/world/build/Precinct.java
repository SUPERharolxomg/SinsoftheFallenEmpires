package com.sofe.world.build;

import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.sofe.world.build.SultharisBuilder.set;

/**
 * The grounds round a boss's place, so it stands in a land of its own and not as a box on the open ground: the ground
 * itself turned to the place's (basalt, magma and slag round the Forge, marble round the Treasury, snow round the
 * Arena), a paved way up to its door between pillars and lights, a ring of broken walls, and what belongs there
 * scattered about: lava running in channels and smoking chimneys at the Forge, pools and palms at the Baths, hedges,
 * flowers and fountains in the Gardens, tents and carts on the Silk Road, black pools and dead trees in the Marsh,
 * statues and heaps of gold in Aureum. The place's own footprint is never touched, nor the ground before its door
 * where its Waystone and its runes stand. The same place is laid out the same way every time.
 */
final class Precinct {
    /** How far the grounds reach beyond the place's walls, and how near its door nothing is set. */
    static final int REACH = 24, DOOR_CLEAR = 7;

    enum Feature { LAVA, CHIMNEY, ANVIL, SLAG, BONFIRE, RACK, POOL, PALM, HEDGE, FLOWERS, FOUNTAIN, TENT, CART, OBELISK, STATUE, GOLD, TREE_DEAD,
        REEDS, COLUMN, SPIKE, CRYSTAL, BANNER }

    /**
     * A place's look: its ground (weighted), its way in, its walls and their trim, its lights, the columns that line its
     * way in, and what is scattered about.
     */
    record Theme(List<BlockState> ground, BlockState path, BlockState wall, BlockState trim, BlockState light, BlockState column, List<Feature> features) {
    }

    private static BlockState b(Block block) {
        return block.defaultBlockState();
    }

    private static final Theme FORGE = new Theme(List.of(b(Blocks.BASALT), b(Blocks.BLACKSTONE), b(Blocks.BLACKSTONE), b(Blocks.MAGMA_BLOCK), b(Blocks.GRAVEL), b(Blocks.SMOOTH_BASALT)),
            b(Blocks.POLISHED_BLACKSTONE_BRICKS), b(Blocks.DEEPSLATE_BRICKS), b(Blocks.POLISHED_BLACKSTONE), b(Blocks.CAMPFIRE), b(Blocks.POLISHED_BASALT),
            List.of(Feature.LAVA, Feature.LAVA, Feature.CHIMNEY, Feature.CHIMNEY, Feature.ANVIL, Feature.SLAG, Feature.SLAG, Feature.SPIKE));
    private static final Theme ARENA = new Theme(List.of(b(Blocks.SNOW_BLOCK), b(Blocks.SNOW_BLOCK), b(Blocks.PACKED_ICE), b(Blocks.STONE), b(Blocks.GRAVEL)),
            b(Blocks.STONE_BRICKS), b(Blocks.COBBLESTONE), b(Blocks.SPRUCE_LOG), b(Blocks.CAMPFIRE), b(Blocks.STRIPPED_SPRUCE_LOG),
            List.of(Feature.BONFIRE, Feature.RACK, Feature.RACK, Feature.BANNER, Feature.TREE_DEAD, Feature.SLAG));
    private static final Theme CITADEL = new Theme(List.of(b(Blocks.NETHERRACK), b(Blocks.BLACKSTONE), b(Blocks.BASALT), b(Blocks.MAGMA_BLOCK), b(Blocks.COARSE_DIRT)),
            b(Blocks.POLISHED_BLACKSTONE_BRICKS), b(Blocks.NETHER_BRICKS), b(Blocks.RED_NETHER_BRICKS), b(Blocks.CAMPFIRE), b(Blocks.NETHER_BRICKS),
            List.of(Feature.LAVA, Feature.BONFIRE, Feature.SPIKE, Feature.SPIKE, Feature.BANNER, Feature.TREE_DEAD, Feature.CHIMNEY));
    private static final Theme CAVERNS = new Theme(List.of(b(Blocks.STONE), b(Blocks.ANDESITE), b(Blocks.SNOW_BLOCK), b(Blocks.PACKED_ICE), b(Blocks.GRAVEL)),
            b(Blocks.COBBLED_DEEPSLATE), b(Blocks.COBBLED_DEEPSLATE), b(Blocks.DEEPSLATE_BRICKS), b(Blocks.SOUL_CAMPFIRE), b(Blocks.DEEPSLATE_BRICKS),
            List.of(Feature.SPIKE, Feature.BONFIRE, Feature.RACK, Feature.TREE_DEAD));
    private static final Theme BATHS = new Theme(List.of(b(Blocks.GRASS_BLOCK), b(Blocks.GRASS_BLOCK), b(Blocks.MOSS_BLOCK), b(Blocks.CALCITE)),
            b(Blocks.SMOOTH_QUARTZ), b(Blocks.QUARTZ_BRICKS), b(Blocks.PRISMARINE_BRICKS), b(Blocks.LANTERN), b(Blocks.QUARTZ_PILLAR),
            List.of(Feature.POOL, Feature.POOL, Feature.PALM, Feature.PALM, Feature.COLUMN, Feature.FLOWERS, Feature.FOUNTAIN));
    private static final Theme SILK_ROAD = new Theme(List.of(b(Blocks.SAND), b(Blocks.SAND), b(Blocks.COARSE_DIRT), b(Blocks.SANDSTONE), b(Blocks.PACKED_MUD)),
            b(Blocks.SMOOTH_SANDSTONE), b(Blocks.SANDSTONE), b(Blocks.CUT_SANDSTONE), b(Blocks.LANTERN), b(Blocks.STRIPPED_ACACIA_LOG),
            List.of(Feature.TENT, Feature.TENT, Feature.CART, Feature.CART, Feature.PALM, Feature.BANNER, Feature.BONFIRE));
    private static final Theme GARDENS = new Theme(List.of(b(Blocks.GRASS_BLOCK), b(Blocks.GRASS_BLOCK), b(Blocks.MOSS_BLOCK), b(Blocks.FLOWERING_AZALEA_LEAVES)),
            b(Blocks.SMOOTH_QUARTZ), b(Blocks.QUARTZ_BRICKS), b(Blocks.AMETHYST_BLOCK), b(Blocks.LANTERN), b(Blocks.QUARTZ_PILLAR),
            List.of(Feature.HEDGE, Feature.HEDGE, Feature.FLOWERS, Feature.FLOWERS, Feature.FOUNTAIN, Feature.PALM, Feature.CRYSTAL, Feature.COLUMN));
    private static final Theme CATACOMBS = new Theme(List.of(b(Blocks.SAND), b(Blocks.SAND), b(Blocks.SANDSTONE), b(Blocks.SMOOTH_SANDSTONE), b(Blocks.COARSE_DIRT)),
            b(Blocks.CUT_SANDSTONE), b(Blocks.SANDSTONE), b(Blocks.CHISELED_SANDSTONE), b(Blocks.SOUL_LANTERN), b(Blocks.SMOOTH_SANDSTONE),
            List.of(Feature.OBELISK, Feature.OBELISK, Feature.STATUE, Feature.PALM, Feature.REEDS, Feature.SLAG));
    private static final Theme MARSH = new Theme(List.of(b(Blocks.MUD), b(Blocks.MUD), b(Blocks.MUDDY_MANGROVE_ROOTS), b(Blocks.PACKED_MUD), b(Blocks.MOSS_BLOCK)),
            b(Blocks.MUD_BRICKS), b(Blocks.MUD_BRICKS), b(Blocks.CHISELED_SANDSTONE), b(Blocks.SOUL_LANTERN), b(Blocks.SMOOTH_SANDSTONE),
            List.of(Feature.POOL, Feature.POOL, Feature.REEDS, Feature.REEDS, Feature.TREE_DEAD, Feature.TREE_DEAD, Feature.OBELISK));
    private static final Theme GOLD = new Theme(List.of(b(Blocks.GRASS_BLOCK), b(Blocks.SMOOTH_QUARTZ), b(Blocks.QUARTZ_BLOCK), b(Blocks.CALCITE)),
            b(Blocks.POLISHED_DIORITE), b(Blocks.QUARTZ_BRICKS), b(Blocks.GOLD_BLOCK), b(Blocks.LANTERN), b(Blocks.QUARTZ_PILLAR),
            List.of(Feature.STATUE, Feature.STATUE, Feature.GOLD, Feature.FOUNTAIN, Feature.COLUMN, Feature.COLUMN, Feature.HEDGE, Feature.BANNER));
    private static final Theme COLOSSEUM = new Theme(List.of(b(Blocks.SAND), b(Blocks.SMOOTH_SANDSTONE), b(Blocks.GRAVEL), b(Blocks.GRASS_BLOCK)),
            b(Blocks.POLISHED_DIORITE), b(Blocks.QUARTZ_BRICKS), b(Blocks.CHISELED_QUARTZ_BLOCK), b(Blocks.CAMPFIRE), b(Blocks.QUARTZ_PILLAR),
            List.of(Feature.COLUMN, Feature.COLUMN, Feature.STATUE, Feature.RACK, Feature.BANNER, Feature.BONFIRE));
    private static final Theme SHADOW = new Theme(List.of(b(Blocks.BLACKSTONE), b(Blocks.DEEPSLATE), b(Blocks.CRYING_OBSIDIAN), b(Blocks.SCULK), b(Blocks.TUFF)),
            b(Blocks.POLISHED_DEEPSLATE), b(Blocks.DEEPSLATE_TILES), b(Blocks.OBSIDIAN), b(Blocks.SOUL_CAMPFIRE), b(Blocks.POLISHED_BASALT),
            List.of(Feature.SPIKE, Feature.SPIKE, Feature.COLUMN, Feature.STATUE, Feature.CRYSTAL, Feature.TREE_DEAD));
    private static final Theme TEMPLE = new Theme(List.of(b(Blocks.SAND), b(Blocks.SMOOTH_SANDSTONE), b(Blocks.SANDSTONE), b(Blocks.TERRACOTTA)),
            b(Blocks.CUT_SANDSTONE), b(Blocks.SANDSTONE), b(Blocks.GOLD_BLOCK), b(Blocks.CAMPFIRE), b(Blocks.SMOOTH_SANDSTONE),
            List.of(Feature.OBELISK, Feature.STATUE, Feature.PALM, Feature.BONFIRE, Feature.COLUMN, Feature.BANNER));
    private static final Theme VOID = new Theme(List.of(b(Blocks.BLACKSTONE), b(Blocks.END_STONE), b(Blocks.OBSIDIAN), b(Blocks.CRYING_OBSIDIAN), b(Blocks.BASALT)),
            b(Blocks.POLISHED_BLACKSTONE_BRICKS), b(Blocks.POLISHED_BLACKSTONE_BRICKS), b(Blocks.CRYING_OBSIDIAN), b(Blocks.SOUL_CAMPFIRE), b(Blocks.OBSIDIAN),
            List.of(Feature.CRYSTAL, Feature.CRYSTAL, Feature.SPIKE, Feature.SPIKE, Feature.TREE_DEAD));

    /** The boss places with grounds of their own, by piece. */
    private static final Map<String, Theme> THEMES = Map.ofEntries(
            Map.entry("nordrath/forge", FORGE), Map.entry("nordrath/arena", ARENA), Map.entry("nordrath/burning_citadel", CITADEL),
            Map.entry("nordrath/caverns_entrance", CAVERNS), Map.entry("parsivan/baths", BATHS), Map.entry("parsivan/silk_road", SILK_ROAD),
            Map.entry("parsivan/enchanted_gardens", GARDENS), Map.entry("khemet/catacombs", CATACOMBS), Map.entry("khemet/stagnant_marsh", MARSH),
            Map.entry("aureum/treasury", GOLD), Map.entry("aureum/market", GOLD), Map.entry("aureum/golden_vaults", GOLD),
            Map.entry("aureum/colosseum", COLOSSEUM), Map.entry("aureum/shadow_throne", SHADOW), Map.entry("sulthari/temple", TEMPLE),
            Map.entry("sulthari/celestial_spire", VOID), Map.entry("sulthari/inverted_throne", VOID));

    private Precinct() {
    }

    static boolean has(String piece) {
        return THEMES.containsKey(piece);
    }

    /** Lays out the grounds of a boss's place built just now; false when it has none. */
    static boolean build(ServerLevel level, StructurePositions.Structure s, String piece) {
        Theme theme = THEMES.get(piece);
        if (theme == null) return false;
        Random random = new Random(s.x() * 7919L + s.z() * 31L);
        int hx = s.sizeX() / 2, hz = s.sizeZ() / 2;
        Direction door = door(s);
        BlockPos waystone = StructurePositions.get().waystones().get(s.id());
        boolean mountain = MountainHalls.has(piece); // a hall in a mountain has its own terrace and stair: the grounds keep to the land below
        int land = SultharisBuilder.surfaceY(level, s.x(), s.z() + s.sizeZ() / 2 + REACH) + 3;
        // 1. the ground: turned to the place's, its plants and trees cleared
        for (int x = s.x() - hx - REACH; x <= s.x() + hx + REACH; x++) {
            for (int z = s.z() - hz - REACH; z <= s.z() + hz + REACH; z++) {
                int out = outside(s, x, z), edge = edge(x, z);
                if (out <= 1 || out > edge) continue;
                if (out > edge - 4 && random.nextInt(edge - out + 2) == 0) continue; // the grounds fray into the land round them
                BlockPos top = ground(level, x, z);
                if (top == null || mountain && top.getY() > land) continue;
                // the place's main ground, with patches of the others; the paving of its court near its walls
                int n = theme.ground().size();
                double p = (patches(x, z) + 1) / 2;
                int pick = p < 0.55 ? 0 : Math.min(n - 1, 1 + (int) ((p - 0.55) / 0.45 * (n - 1)));
                if (random.nextInt(9) == 0) pick = random.nextInt(n);
                BlockState ground = out < 4 ? theme.path() : out == 4 ? theme.trim() : theme.ground().get(pick);
                set(level, x, top.getY(), z, ground);
            }
        }
        // 2. the way in: paved, from the door out past the grounds, between columns with lights
        int doorX = s.x() + door.getStepX() * (hx + 1), doorZ = s.z() + door.getStepZ() * (hz + 1);
        Direction across = door.getClockWise();
        int length = mountain ? 0 : REACH + 6;
        for (int i = 0; i < length; i++) {
            int px = doorX + door.getStepX() * i, pz = doorZ + door.getStepZ() * i;
            for (int w = -2; w <= 2; w++) {
                BlockPos top = ground(level, px + across.getStepX() * w, pz + across.getStepZ() * w);
                if (top != null) set(level, top.getX(), top.getY(), top.getZ(), Math.abs(w) == 2 ? theme.trim() : theme.path());
            }
            if (i >= DOOR_CLEAR && i % 6 == 0) {
                for (int side : new int[]{-4, 4}) column(level, px + across.getStepX() * side, pz + across.getStepZ() * side, theme, 3 + random.nextInt(2));
            }
        }
        // 3. a ring of broken walls, open where the way in passes
        int ring = REACH - 6;
        for (int x = s.x() - hx - ring; x <= s.x() + hx + ring; x++) {
            for (int z = s.z() - hz - ring; z <= s.z() + hz + ring; z++) {
                if (outside(s, x, z) != ring || onWay(s, door, x, z, 5) || patches(x * 3, z * 3) < -0.35) continue; // broken here and there
                BlockPos top = ground(level, x, z);
                if (top == null) continue;
                int height = 1 + random.nextInt(4);
                for (int dy = 1; dy <= height; dy++) set(level, x, top.getY() + dy, z, dy == height && height > 2 ? theme.trim() : theme.wall());
            }
        }
        // 4. what belongs there, scattered over the grounds (never on the way in, nor by the Waystone and its runes)
        int count = (2 * (hx + hz) + 8 * REACH) / 7;
        for (int i = 0; i < count * 3 && count > 0; i++) {
            int x = s.x() + random.nextInt(2 * (hx + REACH) + 1) - hx - REACH, z = s.z() + random.nextInt(2 * (hz + REACH) + 1) - hz - REACH;
            int out = outside(s, x, z);
            if (out < 5 || out > ring - 3 || onWay(s, door, x, z, 7)) continue;
            if (waystone != null && Math.abs(waystone.getX() - x) < 12 && Math.abs(waystone.getZ() - z) < 12) continue;
            BlockPos top = ground(level, x, z);
            if (top == null || mountain && top.getY() > land) continue;
            feature(level, top, theme.features().get(random.nextInt(theme.features().size())), theme, random, s);
            count--;
        }
        return true;
    }

    /** Which side of the place its door is on: towards its Waystone, which stands outside the door (south if it has none). */
    static Direction door(StructurePositions.Structure s) {
        BlockPos w = StructurePositions.get().waystones().get(s.id());
        if (w == null) return Direction.SOUTH;
        int dx = w.getX() - s.x(), dz = w.getZ() - s.z();
        return Math.abs(dx) > Math.abs(dz) ? (dx > 0 ? Direction.EAST : Direction.WEST) : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
    }

    /**
     * How far a column is outside the place's footprint (0 inside it), round the corners: the grounds and their ring of
     * walls curve round the place instead of drawing a square about it.
     */
    static int outside(StructurePositions.Structure s, int x, int z) {
        int dx = Math.max(0, Math.abs(x - s.x()) - s.sizeX() / 2), dz = Math.max(0, Math.abs(z - s.z()) - s.sizeZ() / 2);
        return (int) Math.round(Math.sqrt((double) dx * dx + (double) dz * dz));
    }

    /** A slow, patchy wave over the ground (between -1 and 1): patches of a block, not a block here and another there. */
    static double patches(int x, int z) {
        return (Math.sin(x * 0.19 + z * 0.07) + Math.sin(x * 0.06 - z * 0.17) + Math.sin((x + z) * 0.11 + 1.3)) / 3;
    }

    /** Where the grounds end: an uneven edge, farther out in some places than in others. */
    static int edge(int x, int z) {
        return REACH - 4 + (int) Math.round(4 * (Math.sin(x * 0.13) * Math.cos(z * 0.11) + 0.5 * Math.sin((x - z) * 0.07)));
    }

    /** Whether a column is on the way in (within this half-width of its middle line, before the door). */
    private static boolean onWay(StructurePositions.Structure s, Direction door, int x, int z, int half) {
        int along = door.getStepX() * (x - s.x()) + door.getStepZ() * (z - s.z());
        int side = door.getStepZ() * (x - s.x()) - door.getStepX() * (z - s.z());
        return along > 0 && Math.abs(side) <= half;
    }

    /** The top of the ground in a column, its plants, logs and leaves cleared; null over water or lava. */
    private static BlockPos ground(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        for (int i = 0; i < 24; i++) { // down through trees and plants to the ground
            BlockState st = level.getBlockState(pos);
            if (!level.getFluidState(pos).isEmpty()) return null;
            boolean plant = st.is(BlockTags.LOGS) || st.is(BlockTags.LEAVES) || st.is(BlockTags.REPLACEABLE) || st.is(BlockTags.FLOWERS) || st.isAir()
                    || st.is(Blocks.SNOW) || st.is(Blocks.CACTUS) || st.is(Blocks.SUGAR_CANE) || st.is(Blocks.BAMBOO) || st.is(Blocks.VINE);
            if (!plant) break;
            if (!st.isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            pos.move(0, -1, 0);
        }
        return pos.immutable();
    }

    // ------------------------------------------------------------------------------------------------ what belongs there

    private static void column(ServerLevel level, int x, int z, Theme t, int height) {
        BlockPos top = ground(level, x, z);
        if (top == null) return;
        set(level, x, top.getY() + 1, z, t.trim());
        for (int dy = 2; dy < height + 1; dy++) set(level, x, top.getY() + dy, z, t.column());
        set(level, x, top.getY() + height + 1, z, t.trim());
        set(level, x, top.getY() + height + 2, z, lit(t.light()));
    }

    private static BlockState lit(BlockState light) {
        return light.getBlock() instanceof CampfireBlock ? light.setValue(CampfireBlock.SIGNAL_FIRE, false) : light;
    }

    private static void feature(ServerLevel level, BlockPos top, Feature f, Theme t, Random r, StructurePositions.Structure s) {
        int x = top.getX(), y = top.getY(), z = top.getZ();
        switch (f) {
            case LAVA -> { // a channel of lava with basalt banks, running a while
                boolean alongX = r.nextBoolean();
                int len = 6 + r.nextInt(8);
                for (int i = 0; i < len; i++) {
                    int px = x + (alongX ? i : 0), pz = z + (alongX ? 0 : i);
                    if (outside(s, px, pz) < 4) break;
                    BlockPos g = ground(level, px, pz);
                    if (g == null) continue;
                    set(level, px, g.getY(), pz, Blocks.LAVA.defaultBlockState());
                    set(level, px, g.getY() - 1, pz, Blocks.BASALT.defaultBlockState());
                    for (int side : new int[]{-1, 1}) {
                        int bx = px + (alongX ? 0 : side), bz = pz + (alongX ? side : 0);
                        BlockPos bank = ground(level, bx, bz);
                        if (bank != null && !level.getBlockState(bank).is(Blocks.LAVA)) set(level, bx, bank.getY(), bz, Blocks.SMOOTH_BASALT.defaultBlockState());
                    }
                }
            }
            case CHIMNEY -> { // a smoking chimney of a forge
                int h = 5 + r.nextInt(5);
                for (int dx = 0; dx <= 1; dx++) for (int dz = 0; dz <= 1; dz++) for (int dy = 1; dy <= h; dy++) {
                    set(level, x + dx, y + dy, z + dz, dy == h ? t.trim() : t.wall());
                }
                set(level, x, y + h + 1, z, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.SIGNAL_FIRE, true));
                set(level, x + 1, y + h + 1, z + 1, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.SIGNAL_FIRE, true));
            }
            case ANVIL -> {
                set(level, x, y + 1, z, Blocks.ANVIL.defaultBlockState());
                set(level, x + 1, y + 1, z, Blocks.CAULDRON.defaultBlockState());
                set(level, x - 1, y + 1, z, Blocks.BLAST_FURNACE.defaultBlockState());
                set(level, x, y + 1, z + 1, Blocks.CHAIN.defaultBlockState());
            }
            case SLAG -> mound(level, x, z, 2 + r.nextInt(2), List.of(b(Blocks.BLACKSTONE), b(Blocks.MAGMA_BLOCK), b(Blocks.BASALT), t.ground().get(0)), r);
            case BONFIRE -> {
                set(level, x, y + 1, z, Blocks.CAMPFIRE.defaultBlockState());
                for (Direction d : Direction.Plane.HORIZONTAL) set(level, x + d.getStepX(), y + 1, z + d.getStepZ(), Blocks.COBBLESTONE_SLAB.defaultBlockState());
            }
            case RACK -> { // a rack of spears and a shield
                for (int i = -1; i <= 1; i++) {
                    set(level, x + i, y + 1, z, Blocks.SPRUCE_FENCE.defaultBlockState());
                    set(level, x + i, y + 2, z, Blocks.LIGHTNING_ROD.defaultBlockState());
                }
                set(level, x, y + 1, z + 1, Blocks.BARREL.defaultBlockState());
            }
            case POOL -> { // a pool with a rim of the place's trim
                int rx = 2 + r.nextInt(2), rz = 2 + r.nextInt(2);
                for (int dx = -rx - 1; dx <= rx + 1; dx++) for (int dz = -rz - 1; dz <= rz + 1; dz++) {
                    boolean rim = Math.abs(dx) == rx + 1 || Math.abs(dz) == rz + 1;
                    if (outside(s, x + dx, z + dz) < 3) continue;
                    set(level, x + dx, y, z + dz, rim ? t.trim() : Blocks.WATER.defaultBlockState());
                    if (!rim) set(level, x + dx, y - 1, z + dz, t.path());
                    if (!rim) set(level, x + dx, y + 1, z + dz, Blocks.AIR.defaultBlockState());
                }
                if (t == MARSH) set(level, x, y + 1, z, Blocks.LILY_PAD.defaultBlockState());
            }
            case PALM -> {
                int h = 4 + r.nextInt(3);
                for (int dy = 1; dy <= h; dy++) set(level, x, y + dy, z, Blocks.JUNGLE_LOG.defaultBlockState());
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    for (int i = 1; i <= 2; i++) set(level, x + d.getStepX() * i, y + h + (i == 2 ? 0 : 1), z + d.getStepZ() * i, leaves(Blocks.JUNGLE_LEAVES));
                }
                set(level, x, y + h + 1, z, leaves(Blocks.JUNGLE_LEAVES));
            }
            case HEDGE -> {
                boolean alongX = r.nextBoolean();
                for (int i = 0; i < 5 + r.nextInt(4); i++) {
                    int px = x + (alongX ? i : 0), pz = z + (alongX ? 0 : i);
                    BlockPos g = ground(level, px, pz);
                    if (g == null || outside(s, px, pz) < 4) continue;
                    set(level, px, g.getY() + 1, pz, leaves(t == GARDENS ? Blocks.FLOWERING_AZALEA_LEAVES : Blocks.OAK_LEAVES));
                    if (t == GARDENS) set(level, px, g.getY() + 2, pz, leaves(Blocks.AZALEA_LEAVES));
                }
            }
            case FLOWERS -> {
                Block[] flowers = {Blocks.ALLIUM, Blocks.LILAC, Blocks.PEONY, Blocks.ROSE_BUSH, Blocks.BLUE_ORCHID, Blocks.AZURE_BLUET, Blocks.PINK_TULIP, Blocks.LILY_OF_THE_VALLEY};
                for (int i = 0; i < 10; i++) {
                    int px = x + r.nextInt(5) - 2, pz = z + r.nextInt(5) - 2;
                    BlockPos g = ground(level, px, pz);
                    if (g == null || !level.getBlockState(g).is(BlockTags.DIRT)) continue;
                    Block flower = flowers[r.nextInt(flowers.length)];
                    if (flower == Blocks.LILAC || flower == Blocks.PEONY || flower == Blocks.ROSE_BUSH) {
                        level.setBlock(g.above(), flower.defaultBlockState(), Block.UPDATE_CLIENTS);
                        level.setBlock(g.above(2), flower.defaultBlockState().setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,
                                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
                    } else {
                        set(level, px, g.getY() + 1, pz, flower.defaultBlockState());
                    }
                }
            }
            case FOUNTAIN -> {
                for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                    boolean rim = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                    set(level, x + dx, y, z + dz, t.path());
                    set(level, x + dx, y + 1, z + dz, rim ? t.trim() : Blocks.WATER.defaultBlockState());
                }
                for (int dy = 1; dy <= 3; dy++) set(level, x, y + dy, z, t.column());
                set(level, x, y + 4, z, Blocks.WATER.defaultBlockState());
            }
            case TENT -> { // a merchant's tent: posts and a wool roof
                Block[] wools = {Blocks.RED_WOOL, Blocks.ORANGE_WOOL, Blocks.PURPLE_WOOL, Blocks.CYAN_WOOL};
                Block wool = wools[r.nextInt(wools.length)];
                for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                    int roof = 4 - Math.max(Math.abs(dx), Math.abs(dz));
                    set(level, x + dx, y + 1 + roof, z + dz, wool.defaultBlockState());
                    if (Math.abs(dx) == 2 && Math.abs(dz) == 2) for (int dy = 1; dy <= 2; dy++) set(level, x + dx, y + dy, z + dz, Blocks.ACACIA_FENCE.defaultBlockState());
                }
                set(level, x, y + 1, z, Blocks.BARREL.defaultBlockState());
                set(level, x + 1, y + 1, z, Blocks.CHEST.defaultBlockState());
            }
            case CART -> {
                for (int i = 0; i < 3; i++) set(level, x + i, y + 1, z, Blocks.SPRUCE_SLAB.defaultBlockState());
                set(level, x, y + 1, z + 1, Blocks.BARREL.defaultBlockState());
                set(level, x + 1, y + 2, z, Blocks.HAY_BLOCK.defaultBlockState());
                set(level, x + 2, y + 2, z, Blocks.BARREL.defaultBlockState());
            }
            case OBELISK -> {
                int h = 6 + r.nextInt(5);
                for (int dy = 1; dy <= h; dy++) set(level, x, y + dy, z, dy == 1 ? t.trim() : dy == h ? Blocks.GOLD_BLOCK.defaultBlockState() : t.column());
            }
            case STATUE -> { // a figure on a plinth: legs, body, arms and head of stone
                set(level, x, y + 1, z, t.trim());
                set(level, x, y + 2, z, t.column());
                set(level, x, y + 3, z, t.column());
                set(level, x, y + 4, z, t.wall());
                set(level, x + 1, y + 4, z, Blocks.STONE_BRICK_WALL.defaultBlockState());
                set(level, x - 1, y + 4, z, Blocks.STONE_BRICK_WALL.defaultBlockState());
                set(level, x, y + 5, z, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
            }
            case GOLD -> {
                mound(level, x, z, 2, List.of(b(Blocks.GOLD_BLOCK), b(Blocks.RAW_GOLD_BLOCK), b(Blocks.GOLD_BLOCK), t.ground().get(0)), r);
                set(level, x + 2, y + 1, z, Blocks.CHEST.defaultBlockState());
            }
            case TREE_DEAD -> {
                int h = 3 + r.nextInt(4);
                Block log = t == ARENA || t == CAVERNS ? Blocks.SPRUCE_LOG : t == MARSH ? Blocks.MANGROVE_LOG : Blocks.DARK_OAK_LOG;
                for (int dy = 1; dy <= h; dy++) set(level, x, y + dy, z, log.defaultBlockState());
                set(level, x + 1, y + h, z, log.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.X));
                set(level, x, y + h - 1, z - 1, log.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Z));
            }
            case REEDS -> {
                for (int i = 0; i < 6; i++) {
                    int px = x + r.nextInt(3) - 1, pz = z + r.nextInt(3) - 1;
                    BlockPos g = ground(level, px, pz);
                    if (g == null) continue;
                    set(level, px, g.getY(), pz, Blocks.MUD.defaultBlockState());
                    set(level, px, g.getY() + 1, pz, Blocks.TALL_GRASS.defaultBlockState());
                    level.setBlock(g.above(2), Blocks.TALL_GRASS.defaultBlockState().setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,
                            net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
                }
            }
            case COLUMN -> { // a column, whole or broken
                int h = 2 + r.nextInt(5);
                set(level, x, y + 1, z, t.trim());
                for (int dy = 2; dy <= h; dy++) set(level, x, y + dy, z, t.column());
                if (h >= 5) set(level, x, y + h + 1, z, t.trim());
            }
            case SPIKE -> { // a jagged spire of the place's rock
                int h = 4 + r.nextInt(6);
                for (int dy = 1; dy <= h; dy++) {
                    set(level, x, y + dy, z, dy > h - 2 ? t.trim() : t.wall());
                    if (dy <= h / 2) set(level, x + 1, y + dy, z, t.wall());
                    if (dy <= h / 3) set(level, x, y + dy, z + 1, t.wall());
                }
            }
            case CRYSTAL -> {
                int h = 3 + r.nextInt(4);
                for (int dy = 1; dy <= h; dy++) set(level, x, y + dy, z, dy == h ? Blocks.AMETHYST_CLUSTER.defaultBlockState() : Blocks.AMETHYST_BLOCK.defaultBlockState());
                set(level, x + 1, y + 1, z, Blocks.BUDDING_AMETHYST.defaultBlockState());
                set(level, x - 1, y + 1, z, Blocks.CRYING_OBSIDIAN.defaultBlockState());
            }
            case BANNER -> { // a tall pole with the place's colours
                for (int dy = 1; dy <= 5; dy++) set(level, x, y + dy, z, Blocks.SPRUCE_FENCE.defaultBlockState());
                Block banner = t == ARENA || t == CITADEL ? Blocks.RED_WALL_BANNER : t == GOLD || t == COLOSSEUM ? Blocks.BLUE_WALL_BANNER
                        : t == TEMPLE ? Blocks.YELLOW_WALL_BANNER : Blocks.PURPLE_WALL_BANNER;
                set(level, x, y + 5, z + 1, banner.defaultBlockState().setValue(net.minecraft.world.level.block.WallBannerBlock.FACING, Direction.SOUTH));
            }
        }
    }

    private static BlockState leaves(Block block) {
        return block.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true);
    }

    /** A low heap of these blocks, wider at the bottom. */
    private static void mound(ServerLevel level, int x, int z, int radius, List<BlockState> blocks, Random r) {
        for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
            int h = radius + 1 - Math.max(Math.abs(dx), Math.abs(dz)) - r.nextInt(2);
            BlockPos g = ground(level, x + dx, z + dz);
            if (g == null) continue;
            for (int dy = 1; dy <= h; dy++) set(level, x + dx, g.getY() + dy, z + dz, blocks.get(r.nextInt(blocks.size())));
        }
    }
}
