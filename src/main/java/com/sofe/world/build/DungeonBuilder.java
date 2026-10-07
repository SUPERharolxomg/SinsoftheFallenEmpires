package com.sofe.world.build;

import com.sofe.registry.EntityRegistry;
import com.sofe.registry.SoFEBlocks;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;
import java.util.function.Supplier;

import static com.sofe.world.build.SultharisBuilder.set;

/**
 * The lesser dungeons of every region (docs/Mundo.md, W6), found on the way and given as dungeon quests: a small
 * Crypt (one hall of the dead, two spawners, a chest) and a medium Ruin (a court with a rune puzzle before a Sealed
 * Gate, and behind it a roofed hall with its spawners and its treasure; the guardian wakes when the runes burn).
 * Built in the region's own stone; the same place is built the same way every time.
 */
final class DungeonBuilder {
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    /**
     * The stone, trim, floor and light of a region, and the dead that haunt its dungeons (never the ruin's guardian,
     * so the guardian is the one a dungeon quest asks for: scripts/make_dungeons.py).
     */
    private record Palette(BlockState wall, BlockState trim, BlockState floor, BlockState light,
                           Supplier<? extends EntityType<?>> first, Supplier<? extends EntityType<?>> second, BlockState stairs,
                           BlockState rock, BlockState band, BlockState top, BlockState statue) {
    }

    private DungeonBuilder() {
    }

    private static Palette palette(String region) {
        return switch (region) {
            case "nordrath" -> new Palette(b(SoFEBlocks.NORDRATH_RUNESTONE_BRICKS), b(SoFEBlocks.NORDRATH_DARK_PLANKS), Blocks.COBBLED_DEEPSLATE.defaultBlockState(),
                    Blocks.SOUL_LANTERN.defaultBlockState(), EntityRegistry.DRAUGR, () -> EntityType.STRAY, Blocks.COBBLED_DEEPSLATE_STAIRS.defaultBlockState(), Blocks.STONE.defaultBlockState(), Blocks.ANDESITE.defaultBlockState(), Blocks.SNOW_BLOCK.defaultBlockState(), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
            case "parsivan" -> new Palette(b(SoFEBlocks.PARSIVAN_WHITE_PLASTER), b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES), b(SoFEBlocks.PARSIVAN_LAPIS_MOSAIC),
                    Blocks.LANTERN.defaultBlockState(), EntityRegistry.MIRAGE_DANCER, () -> EntityType.CAVE_SPIDER, Blocks.QUARTZ_STAIRS.defaultBlockState(), Blocks.STONE.defaultBlockState(), Blocks.CALCITE.defaultBlockState(), Blocks.MOSS_BLOCK.defaultBlockState(), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
            case "khemet" -> new Palette(b(SoFEBlocks.KHEMET_CARVED_SANDSTONE), b(SoFEBlocks.KHEMET_GOLD_HIEROGLYPHS), b(SoFEBlocks.KHEMET_PAINTED_LIMESTONE),
                    Blocks.SOUL_LANTERN.defaultBlockState(), EntityRegistry.BOG_MUMMY, () -> EntityType.HUSK, Blocks.SMOOTH_SANDSTONE_STAIRS.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), Blocks.ORANGE_TERRACOTTA.defaultBlockState(), Blocks.SMOOTH_SANDSTONE.defaultBlockState(), Blocks.CHISELED_SANDSTONE.defaultBlockState());
            case "aureum" -> new Palette(b(SoFEBlocks.AUREUM_MARBLE_BRICKS), b(SoFEBlocks.AUREUM_GOLD_MOSAIC), b(SoFEBlocks.AUREUM_POLISHED_MARBLE),
                    Blocks.LANTERN.defaultBlockState(), EntityRegistry.GILDED_LEGIONNAIRE, () -> EntityType.SKELETON, Blocks.QUARTZ_STAIRS.defaultBlockState(), Blocks.STONE.defaultBlockState(), Blocks.TUFF.defaultBlockState(), Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
            case "void" -> new Palette(Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), Blocks.CRYING_OBSIDIAN.defaultBlockState(),
                    Blocks.BLACKSTONE.defaultBlockState(), Blocks.SOUL_LANTERN.defaultBlockState(), EntityRegistry.VOID_ZOMBIE, EntityRegistry.VOID_SKELETON, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState(), Blocks.BASALT.defaultBlockState(), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState());
            default -> new Palette(b(SoFEBlocks.SULTHARI_SANDSTONE_BRICKS), b(SoFEBlocks.SULTHARI_BRASS_PLATING), b(SoFEBlocks.SULTHARI_GLAZED_TILES),
                    b(SoFEBlocks.SULTHARI_AETHERIUM_LAMP), EntityRegistry.SAND_GHOUL, EntityRegistry.SAND_GHOUL, Blocks.SANDSTONE_STAIRS.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), Blocks.RED_SANDSTONE.defaultBlockState(), Blocks.SMOOTH_SANDSTONE.defaultBlockState(), Blocks.CHISELED_SANDSTONE.defaultBlockState());
        };
    }

    /** The build of a "&lt;region&gt;/crypt" or "&lt;region&gt;/ruin"; false for any other piece. */
    static boolean blockout(ServerLevel level, StructurePositions.Structure s, String piece) {
        String region = piece.substring(0, piece.indexOf('/'));
        int y = SultharisBuilder.surfaceY(level, s.x(), s.z());
        if (piece.endsWith("/crypt") || piece.contains("/tomb_")) crypt(level, s, y, palette(region)); // a tomb is built as a Crypt is (make_chapters.py)
        else if (piece.endsWith("/ruin")) ruin(level, s, y, palette(region));
        else return false;
        StructureBuilder.placeGates(level, s, y);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the crypt

    /** How far the Crypt reaches round its center (its place is 49 across), and how deep its three levels lie. */
    static final int CRYPT_REACH = 24, FIRST_DEPTH = 10, SECOND_DEPTH = 18, DEEPEST = 26;
    /** The deepest chamber's center, from the Crypt's center (its lord's lair: scripts/make_dungeons.py). */
    static final int CHAMBER_DX = 18, CHAMBER_DZ = 0;

    /**
     * A tomb cut into a hill, gone down through: a terraced hill of the region's rock rises over the dungeon, and the tomb's
     * carved front stands in its southern side, a double door between two pillars with their statues; before it a court of
     * tiles with two pools and a causeway between them, and a small stepped altar with a chest and the bones of those who
     * came before. Behind the door a stair goes down at once, into the hill: the Hall of the Dead ten blocks under
     * (sarcophagi, two spawners, a chest); a passage west and a stair down to the Ossuary (bones, two spawners, a chest);
     * a passage east and a last stair down to the burial chamber, 26 blocks under the ground, where the lord of the crypt
     * sleeps before its altar and the treasure. Every room and passage is lined with the region's stone, so a cave or water
     * that crosses it is shut out; where the Crypt stands in water, the water round it is filled with rock first.
     */
    private static void crypt(ServerLevel level, StructurePositions.Structure s, int y, Palette p) {
        int cx = s.x(), cz = s.z();
        Random random = new Random(cx * 31L + cz);
        int ground = SultharisBuilder.surfaceY(level, cx, cz + 5); // the court's level
        islet(level, cx, cz, ground, p);
        hill(level, cx, cz, ground, p, random);
        crags(level, cx, cz, ground, p, random);
        court(level, cx, cz, ground, p, random);
        front(level, cx, cz, ground, p);

        int l1 = ground - FIRST_DEPTH, l2 = ground - SECOND_DEPTH, l3 = ground - DEEPEST;
        Dig dig = new Dig(ground);
        dig.stair(cx, cz - 3, 0, -1, ground, FIRST_DEPTH, p);                  // behind the door, down into the hill, northwards
        dig.room(cx - 1, l1, cz - 15, cx + 1, cz - 13, 4);                    // the landing
        dig.room(cx - 8, l1, cz - 24, cx + 8, cz - 16, 6);                    // the Hall of the Dead
        dig.room(cx - 20, l1, cz - 22, cx - 9, cz - 20, 4);                   // the passage west
        dig.stair(cx - 19, cz - 19, 0, 1, l1, SECOND_DEPTH - FIRST_DEPTH, p); // down, southwards
        dig.room(cx - 24, l2, cz - 11, cx - 14, cz + 2, 5);                   // the Ossuary
        dig.room(cx - 13, l2, cz - 1, cx + 3, cz + 1, 4);                     // the passage east
        dig.stair(cx + 4, cz, 1, 0, l2, DEEPEST - SECOND_DEPTH, p);           // the last stair down, eastwards
        dig.room(cx + 12, l3, cz - 6, cx + 24, cz + 6, 8);                    // the burial chamber
        dig.apply(level, p, random);
        hang(level, cx, ground - 4, cz - 8, p); // a light over the stair

        // the Hall of the Dead: sarcophagi along its long walls, four pillars, bones
        for (int x = cx - 6; x <= cx + 6; x += 3) {
            if (x != cx) sarcophagus(level, x, l1, cz - 23, p);
            if (Math.abs(x - cx) > 1) sarcophagus(level, x, l1, cz - 17, p);
        }
        for (int dx : new int[]{-4, 4}) for (int z : new int[]{cz - 22, cz - 18}) pillar(level, cx + dx, z, l1, 6, p);
        spawner(level, cx - 6, l1, cz - 20, p.first().get());
        spawner(level, cx + 6, l1, cz - 20, p.second().get());
        chest(level, cx, l1, cz - 24, "sofe:chests/dungeon_small", random);
        hang(level, cx, l1 + 5, cz - 20, p);
        bones(level, cx - 7, cz - 23, cx + 7, cz - 17, l1, random, 10);
        cobwebs(level, cx - 7, cz - 23, cx + 7, cz - 17, l1, 6, random);

        // the Ossuary: walls of bone and skulls, its two spawners and a chest in a corner
        for (int z = cz - 10; z <= cz + 1; z += 2) {
            set(level, cx - 24, l2, z, Blocks.BONE_BLOCK.defaultBlockState());
            set(level, cx - 24, l2 + 1, z, Blocks.SKELETON_SKULL.defaultBlockState());
        }
        spawner(level, cx - 21, l2, cz - 7, p.first().get());
        spawner(level, cx - 17, l2, cz - 3, p.second().get());
        chest(level, cx - 23, l2, cz + 2, "sofe:chests/dungeon_small", random);
        hang(level, cx - 19, l2 + 4, cz - 4, p);
        bones(level, cx - 23, cz - 10, cx - 15, cz + 1, l2, random, 14);
        cobwebs(level, cx - 23, cz - 10, cx - 15, cz + 1, l2, 5, random);

        // the burial chamber: pillars with lights, the lord's tomb in the middle, the altar and the treasure at its east end
        for (int x : new int[]{cx + 14, cx + 22}) {
            for (int z : new int[]{cz - 4, cz + 4}) {
                pillar(level, x, z, l3, 8, p);
                set(level, x, l3 + 3, z + (z < cz + CHAMBER_DZ ? 1 : -1), Blocks.SOUL_LANTERN.defaultBlockState());
            }
        }
        for (int dx = -1; dx <= 1; dx++) sarcophagus(level, cx + CHAMBER_DX + dx, l3, cz + CHAMBER_DZ, p);
        for (int dz = -1; dz <= 1; dz++) set(level, cx + 23, l3, cz + CHAMBER_DZ + dz, p.trim());
        set(level, cx + 23, l3 + 1, cz + CHAMBER_DZ - 1, Blocks.SOUL_LANTERN.defaultBlockState());
        set(level, cx + 23, l3 + 1, cz + CHAMBER_DZ + 1, Blocks.SOUL_LANTERN.defaultBlockState());
        chest(level, cx + 24, l3, cz + CHAMBER_DZ, "sofe:chests/dungeon_medium", random);
        hang(level, cx + CHAMBER_DX, l3 + 7, cz + CHAMBER_DZ, p);
        bones(level, cx + 13, cz - 5, cx + 21, cz + 5, l3, random, 8);
        cobwebs(level, cx + 13, cz - 5, cx + 23, cz + 5, l3, 8, random);
    }

    /**
     * Where the Crypt stands in water: the water round it filled with the region's rock up to the court, an islet with a
     * ragged shore. Dry land is left as it is.
     */
    private static void islet(ServerLevel level, int cx, int cz, int ground, Palette p) {
        for (int x = cx - CRYPT_REACH; x <= cx + CRYPT_REACH; x++) {
            for (int z = cz - CRYPT_REACH; z <= cz + CRYPT_REACH; z++) {
                double r = Math.sqrt((double) (x - cx) * (x - cx) + (double) (z - cz) * (z - cz));
                if (r > 20 + 3 * Math.sin(x * 0.4) * Math.cos(z * 0.33)) continue;
                boolean wet = false;
                for (int y = ground - 1; y >= ground - 4 && !wet; y--) wet = !level.getFluidState(new BlockPos(x, y, z)).isEmpty();
                if (!wet) continue;
                for (int y = ground - 1; y > level.getMinBuildHeight() + 1; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (level.getFluidState(pos).isEmpty() && !level.getBlockState(pos).isAir()) break;
                    set(level, x, y, z, y == ground - 1 ? p.top() : p.rock());
                }
            }
        }
    }

    /**
     * The hill the tomb is cut into: terraced, two blocks a step, highest behind the tomb, of the region's rock with bands
     * of a second one (sandstone and red sandstone in the desert) and its own top; its southern side is cut straight where
     * the court begins.
     */
    private static void hill(ServerLevel level, int cx, int cz, int ground, Palette p, Random random) {
        for (int x = cx - CRYPT_REACH; x <= cx + CRYPT_REACH; x++) {
            for (int z = cz - CRYPT_REACH; z <= cz - 2; z++) {
                double dx = (x - cx) / 21.0, dz = (z - (cz - 10)) / 13.0;
                double r = Math.sqrt(dx * dx + dz * dz) + 0.07 * Math.sin(x * 0.45) * Math.cos(z * 0.38);
                int height = (int) ((1 - r) * 24 + 2 * Precinct.patches(x * 3, z * 3)) / 2 * 2 + (Precinct.patches(x * 7, z * 7) > 0.5 ? 1 : 0);
                int top = ground + height;
                if (height <= 0 || top <= SultharisBuilder.surfaceY(level, x, z)) continue;
                for (int yy = ground - 3; yy < top; yy++) {
                    BlockPos pos = new BlockPos(x, yy, z);
                    if (yy < ground && !level.getBlockState(pos).isAir() && level.getFluidState(pos).isEmpty()) continue;
                    BlockState rock = yy == top - 1 ? p.top() : Math.floorMod(yy - ground, 6) == 4 ? p.band() : p.rock();
                    set(level, x, yy, z, rock);
                }
                for (int yy = top; yy < top + 6; yy++) { // nothing stands on it (a tree, a cactus)
                    if (!level.getBlockState(new BlockPos(x, yy, z)).isAir()) set(level, x, yy, z, AIR);
                }
            }
        }
    }

    /** Spires and fallen boulders on a tomb's hill, so its slopes are not smooth steps. */
    private static void crags(ServerLevel level, int cx, int cz, int ground, Palette p, Random random) {
        for (int i = 0; i < 26; i++) {
            int x = cx + random.nextInt(41) - 20, z = cz - 2 - random.nextInt(22);
            int top = SultharisBuilder.surfaceY(level, x, z);
            if (top <= ground + 2 || Math.abs(x - cx) <= 6 && z > cz - 8) continue; // only on the hill, never over the tomb's front
            boolean spire = random.nextInt(3) == 0;
            int h = spire ? 3 + random.nextInt(5) : 1 + random.nextInt(2);
            for (int dy = 0; dy < h; dy++) {
                int w = spire ? (dy < h / 2 ? 1 : 0) : 1;
                for (int dx = 0; dx <= w; dx++) for (int dz = 0; dz <= w; dz++) {
                    if (random.nextInt(4) == 0 && dy > 0) continue;
                    set(level, x + dx, top + dy, z + dz, Math.floorMod(top + dy - ground, 6) == 4 ? p.band() : p.rock());
                }
            }
        }
    }

    /**
     * The court before the tomb: tiles, two pools with a causeway of the region's top between them to the door, bones,
     * pots and candles, and a small stepped altar to the west with a chest on it.
     */
    private static void court(ServerLevel level, int cx, int cz, int ground, Palette p, Random random) {
        SultharisBuilder.pad(level, cx - 17, cz - 1, cx + 11, cz + 11, ground, p.floor(), 8);
        BlockState water = Blocks.WATER.defaultBlockState();
        for (int side : new int[]{-1, 1}) { // the pools, two deep, their bottoms strewn with what fell in
            int x0 = side < 0 ? cx - 9 : cx + 3, x1 = side < 0 ? cx - 3 : cx + 9;
            for (int x = x0; x <= x1; x++) {
                for (int z = cz + 2; z <= cz + 9; z++) {
                    set(level, x, ground - 3, z, p.rock());
                    set(level, x, ground - 2, z, water);
                    set(level, x, ground - 1, z, water);
                }
            }
            for (int i = 0; i < 4; i++) {
                int x = x0 + random.nextInt(x1 - x0 + 1), z = cz + 2 + random.nextInt(8);
                set(level, x, ground - 2, z, random.nextBoolean() ? Blocks.SKELETON_SKULL.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.SkullBlock.ROTATION, random.nextInt(16)) : Blocks.DECORATED_POT.defaultBlockState());
            }
        }
        for (int z = cz - 1; z <= cz + 11; z++) { // the causeway
            for (int x = cx - 2; x <= cx + 2; x++) set(level, x, ground - 1, z, p.top());
        }
        // the stepped altar: three tiers, a chest on the second
        for (int tier = 0; tier < 3; tier++) {
            int r = 3 - tier;
            for (int x = cx - 14 - r; x <= cx - 14 + r; x++) for (int z = cz + 5 - r; z <= cz + 5 + r; z++) set(level, x, ground + tier, z, p.top());
        }
        chest(level, cx - 14, ground + 2, cz + 7, "sofe:chests/dungeon_small", random);
        set(level, cx - 14, ground + 3, cz + 5, p.statue());
        bones(level, cx - 16, cz, cx + 10, cz + 11, ground, random, 12);
        for (int[] at : new int[][]{{cx - 11, cz + 1}, {cx + 11, cz + 1}, {cx - 11, cz + 10}, {cx + 11, cz + 10}}) { // the court's lamps
            set(level, at[0], ground, at[1], p.trim());
            set(level, at[0], ground + 1, at[1], p.light());
        }
    }

    /** The tomb's front, cut into the hill: dressed stone, a lintel and a cornice, a double door, two pillars with their statues. */
    private static void front(ServerLevel level, int cx, int cz, int ground, Palette p) {
        int z = cz - 2, height = 9;
        for (int x = cx - 5; x <= cx + 5; x++) {
            for (int dy = 0; dy < height; dy++) {
                boolean edge = x == cx - 5 || x == cx + 5 || dy == height - 1 || dy == 5;
                set(level, x, ground + dy, z, edge ? p.trim() : p.wall());
            }
            set(level, x, ground + height, z + 1, p.top()); // the cornice
        }
        for (int x = cx - 2; x <= cx + 1; x++) set(level, x, ground + 3, z, p.trim()); // the lintel over the door
        for (int dy = 0; dy < 3; dy++) {                                            // its posts
            set(level, cx - 2, ground + dy, z, p.trim());
            set(level, cx + 1, ground + dy, z, p.trim());
        }
        for (int x = cx - 1; x <= cx; x++) { // the double door, opening to the south
            var door = Blocks.SPRUCE_DOOR.defaultBlockState().setValue(net.minecraft.world.level.block.DoorBlock.FACING, net.minecraft.core.Direction.SOUTH)
                    .setValue(net.minecraft.world.level.block.DoorBlock.HINGE, x == cx - 1 ? net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT
                            : net.minecraft.world.level.block.state.properties.DoorHingeSide.LEFT);
            set(level, x, ground, z, door.setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER));
            set(level, x, ground + 1, z, door.setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
            set(level, x, ground + 2, z, AIR);
        }
        for (int x : new int[]{cx - 4, cx + 3}) { // the pillars and their statues
            for (int dy = 0; dy < 5; dy++) set(level, x, ground + dy, z + 1, dy == 0 || dy == 4 ? p.trim() : p.top());
            set(level, x, ground + 5, z + 1, p.statue());
            set(level, x, ground + 6, z + 1, p.statue());
        }
    }

    /** A sarcophagus: a lid of slab on a block of the region's trim. */
    private static void sarcophagus(ServerLevel level, int x, int y, int z, Palette p) {
        set(level, x, y, z, p.trim());
        set(level, x, y + 1, z, Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
    }

    /** Bones, skulls and a few lit candles on a floor, where there is room. */
    private static void bones(ServerLevel level, int x0, int z0, int x1, int z1, int y, Random random, int count) {
        for (int i = 0; i < count; i++) {
            int x = x0 + random.nextInt(x1 - x0 + 1), z = z0 + random.nextInt(z1 - z0 + 1);
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).isAir() || level.getBlockState(pos.below()).isAir() || !level.getFluidState(pos.below()).isEmpty()) continue;
            BlockState bone = switch (random.nextInt(5)) {
                case 0 -> Blocks.SKELETON_SKULL.defaultBlockState().setValue(net.minecraft.world.level.block.SkullBlock.ROTATION, random.nextInt(16));
                case 1 -> Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true)
                        .setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 1 + random.nextInt(3));
                case 2 -> Blocks.DECORATED_POT.defaultBlockState();
                default -> Blocks.BONE_BLOCK.defaultBlockState();
            };
            set(level, x, y, z, bone);
        }
    }

    /**
     * The rooms, passages and stairs dug under the ground: first every open place is noted, then the region's stone is
     * laid round all of them at once (walls, floors, ceilings), so a passage opens into the room it reaches.
     */
    private static final class Dig {
        private final java.util.Set<BlockPos> open = new java.util.HashSet<>();
        private final java.util.Map<BlockPos, BlockState> steps = new java.util.HashMap<>();
        /** The ground's level: from it up the hill stands round the stair, so nothing is lined there. */
        private final int ground;

        Dig(int ground) {
            this.ground = ground;
        }

        /** A room from (x0, z0) to (x1, z1) with its floor at y, this high inside. */
        void room(int x0, int y, int z0, int x1, int z1, int height) {
            for (int x = x0; x <= x1; x++) for (int z = z0; z <= z1; z++) for (int dy = 0; dy < height; dy++) open.add(new BlockPos(x, y + dy, z));
        }

        /** A stair three wide going down one block a step from (x, z) towards (dx, dz), from a floor at y, this many steps. */
        void stair(int x, int z, int dx, int dz, int y, int count, Palette p) {
            net.minecraft.core.Direction up = net.minecraft.core.Direction.fromDelta(-dx, 0, -dz);
            for (int i = 0; i < count; i++) {
                int sx = x + dx * i, sz = z + dz * i, floor = y - i;
                for (int w = -1; w <= 1; w++) {
                    int px = sx + (dx == 0 ? w : 0), pz = sz + (dz == 0 ? w : 0);
                    steps.put(new BlockPos(px, floor - 1, pz), p.stairs().setValue(net.minecraft.world.level.block.StairBlock.FACING, up));
                    for (int dy = 0; dy < 5; dy++) open.add(new BlockPos(px, floor + dy, pz));
                }
            }
        }

        void apply(ServerLevel level, Palette p, Random random) {
            java.util.Set<BlockPos> lining = new java.util.HashSet<>();
            for (BlockPos pos : open) {
                for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
                    BlockPos n = pos.offset(dx, dy, dz);
                    if (!open.contains(n) && !steps.containsKey(n) && n.getY() < ground) lining.add(n);
                }
            }
            for (BlockPos pos : lining) {
                BlockState stone = open.contains(pos.above()) ? p.floor() : random.nextInt(9) == 0 ? p.trim() : p.wall();
                set(level, pos.getX(), pos.getY(), pos.getZ(), stone);
            }
            for (BlockPos pos : open) set(level, pos.getX(), pos.getY(), pos.getZ(), AIR);
            steps.forEach((pos, state) -> set(level, pos.getX(), pos.getY(), pos.getZ(), state));
        }
    }

    // ------------------------------------------------------------------------------------------------ the ruin

    /**
     * An open court (where the rune puzzle stands, data/sofe/puzzles) behind a gate in the outer wall, then a wall with a
     * Sealed Gate that the puzzle opens, and behind it a roofed hall: four pillars, two spawners and two chests.
     */
    private static void ruin(ServerLevel level, StructurePositions.Structure s, int y, Palette p) {
        int x0 = s.x() - 18, x1 = s.x() + 18, z0 = s.z() - 14, z1 = s.z() + 14, height = 7, inner = s.z() - 3;
        Random random = new Random(s.x() * 17L + s.z());
        SultharisBuilder.pad(level, x0, z0, x1, z1, y, p.floor(), height + 5);
        walls(level, x0, z0, x1, z1, y, height, p, random, true);
        for (int x = x0 + 1; x < x1; x++) { // the inner wall: its Sealed Gate stands in the middle (structure_positions.json)
            for (int dy = 0; dy < height; dy++) set(level, x, y + dy, inner, dy == height - 1 ? p.trim() : p.wall());
        }
        roof(level, x0, z0, x1, inner, y + height, p, random, 0.15);
        door(level, s.x(), z1, y);
        // the court: broken columns and rubble
        for (int x = x0 + 4; x <= x1 - 4; x += 8) {
            for (int z : new int[]{inner + 4, z1 - 4}) {
                int h = 2 + random.nextInt(4);
                for (int dy = 0; dy < h; dy++) set(level, x, y + dy, z, dy == h - 1 && h > 4 ? p.trim() : p.wall());
            }
        }
        spawner(level, x1 - 3, y, z1 - 3, p.second().get());
        // the hall behind the seal
        for (int x : new int[]{s.x() - 9, s.x() + 9}) {
            pillar(level, x, z0 + 4, y, height, p);
            pillar(level, x, inner - 4, y, height, p);
        }
        spawner(level, x0 + 3, y, z0 + 3, p.first().get());
        spawner(level, x1 - 3, y, z0 + 3, p.first().get());
        set(level, s.x(), y, z0 + 2, p.trim()); // an altar with the treasure on either side
        set(level, s.x(), y + 1, z0 + 2, p.light());
        chest(level, s.x() - 2, y, z0 + 1, "sofe:chests/dungeon_medium", random);
        chest(level, s.x() + 2, y, z0 + 1, "sofe:chests/dungeon_medium", random);
        hang(level, s.x(), y + height - 1, (z0 + inner) / 2, p);
        cobwebs(level, x0 + 1, z0 + 1, x1 - 1, inner - 1, y, height, random);
    }

    // ------------------------------------------------------------------------------------------------ parts

    /** The outer walls, their top broken here and there. */
    private static void walls(ServerLevel level, int x0, int z0, int x1, int z1, int y, int height, Palette p, Random random, boolean ruined) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (x != x0 && x != x1 && z != z0 && z != z1) continue;
                int top = ruined && random.nextInt(5) == 0 ? height - 1 - random.nextInt(2) : height;
                for (int dy = 0; dy < top; dy++) {
                    boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
                    set(level, x, y + dy, z, corner || dy == 0 ? p.trim() : p.wall());
                }
            }
        }
    }

    /** A flat roof of the region's stone, a share of it fallen in (light and rain come through). */
    private static void roof(ServerLevel level, int x0, int z0, int x1, int z1, int y, Palette p, Random random, double fallen) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                if (!edge && random.nextDouble() < fallen) continue;
                set(level, x, y, z, edge ? p.trim() : p.wall());
            }
        }
    }

    /** The way in: three wide and three high in the south wall. */
    private static void door(ServerLevel level, int x, int z, int y) {
        for (int dx = -1; dx <= 1; dx++) for (int dy = 0; dy < 3; dy++) set(level, x + dx, y + dy, z, AIR);
    }

    private static void pillar(ServerLevel level, int x, int z, int y, int height, Palette p) {
        for (int dy = 0; dy < height; dy++) set(level, x, y + dy, z, dy == 0 || dy == height - 1 ? p.trim() : p.wall());
    }

    /** A lantern hung on a chain from the roof. */
    private static void hang(ServerLevel level, int x, int y, int z, Palette p) {
        set(level, x, y + 1, z, p.wall());
        BlockState light = p.light().getBlock() instanceof net.minecraft.world.level.block.LanternBlock
                ? p.light().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true) : p.light();
        set(level, x, y, z, light);
    }

    private static void cobwebs(ServerLevel level, int x0, int z0, int x1, int z1, int y, int height, Random random) {
        for (int i = 0; i < 10; i++) {
            int x = x0 + random.nextInt(x1 - x0 + 1), z = z0 + random.nextInt(z1 - z0 + 1), dy = height - 1 - random.nextInt(2);
            if (level.getBlockState(new BlockPos(x, y + dy, z)).isAir()) set(level, x, y + dy, z, Blocks.COBWEB.defaultBlockState());
        }
    }

    /**
     * A spawner of this creature that works in any light: a dungeon keeps its lanterns, and its dead still come (a spawner's
     * own rules, not the darkness the wild asks of a monster).
     */
    static void spawner(ServerLevel level, int x, int y, int z, EntityType<?> type) {
        BlockPos pos = new BlockPos(x, y, z);
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (!(level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner)) return;
        spawner.setEntityId(type, level.getRandom());
        net.minecraft.nbt.CompoundTag tag = spawner.saveWithoutMetadata();
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        net.minecraft.nbt.CompoundTag entity = new net.minecraft.nbt.CompoundTag();
        entity.putString("id", String.valueOf(net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(type)));
        data.put("entity", entity);
        net.minecraft.nbt.CompoundTag rules = new net.minecraft.nbt.CompoundTag();
        for (String limit : new String[]{"block_light_limit", "sky_light_limit"}) {
            net.minecraft.nbt.CompoundTag range = new net.minecraft.nbt.CompoundTag();
            range.putInt("min_inclusive", 0);
            range.putInt("max_inclusive", 15);
            rules.put(limit, range);
        }
        data.put("custom_spawn_rules", rules);
        tag.put("SpawnData", data);
        tag.remove("SpawnPotentials");
        spawner.load(tag);
    }

    /** A chest that fills from its loot table the first time it is opened (gear rolled for the Bearer who opens it). */
    static void chest(ServerLevel level, int x, int y, int z, String table, Random random) {
        BlockPos pos = new BlockPos(x, y, z);
        level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, net.minecraft.core.Direction.SOUTH), 2);
        if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity chest) {
            chest.setLootTable(new ResourceLocation(table), random.nextLong());
        }
    }

    private static BlockState b(net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.Block> block) {
        return block.get().defaultBlockState();
    }
}
