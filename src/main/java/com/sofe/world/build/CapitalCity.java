package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import com.sofe.world.StoryPlacements;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.sofe.world.build.SultharisBuilder.set;

/**
 * The capitals of Parsivan, Khemet and Aureum, after their concepts (art/concepts/city_*.png), built like Sulthari
 * and Skarnhold: the land levelled into a terrace, two avenues crossing at a plaza, walls with towers and four gates,
 * the empire's great buildings ({@link Isfaran}, {@link Neferet}, {@link Aurelion}), furnished houses on the free plots,
 * market stalls along the avenues, roads out of the gates and the city's people. Some house walls are of the empire's
 * corrupted blocks: they heal when the region's Archsin falls (RegionHealing).
 * <p>
 * This class holds what the three share: the land, the avenues, the walls, the houses and their furniture, and the
 * pieces the landmarks are made of (roofs, platforms, statues, braziers, rugs).
 */
final class CapitalCity {
    enum Empire { PARSIVAN, KHEMET, AUREUM }

    static final BlockState AIR = Blocks.AIR.defaultBlockState();
    static final int PLOT = 13, SHORE = 14, WALL = 8;

    final ServerLevel level;
    final Empire empire;
    final int cx, cz, a, top;
    final Random random;
    private final List<int[]> taken = new ArrayList<>(); // x0, z0, x1, z1
    private final List<int[]> doors = new ArrayList<>(); // x, z, yaw

    /** The people of each capital (scripts/story_catalog_act34.py, CAPITAL_FOLK). */
    static final java.util.Map<Empire, List<String>> FOLK = java.util.Map.of(
            Empire.PARSIVAN, List.of("isfaran_perfumer", "isfaran_guard", "isfaran_astronomer", "isfaran_child", "isfaran_carpet_weaver"),
            Empire.KHEMET, List.of("neferet_priest", "neferet_boatman", "neferet_potter", "neferet_child", "neferet_guard"),
            Empire.AUREUM, List.of("aurelion_magistrate", "aurelion_legionary", "aurelion_baker", "aurelion_child", "aurelion_sculptor"));

    private CapitalCity(ServerLevel level, StructurePositions.Structure city, Empire empire) {
        this.level = level;
        this.empire = empire;
        this.cx = city.x();
        this.cz = city.z();
        this.a = Math.min(city.sizeX(), city.sizeZ()) / 2;
        this.random = new Random(city.x() * 31L + city.z() + empire.ordinal());
        this.top = plateau();
    }

    static void build(ServerLevel level, StructurePositions.Structure city, Empire empire) {
        CapitalCity c = new CapitalCity(level, city, empire);
        com.sofe.SoFEMod.LOGGER.info("Building the capital of {} at {}, {} on a terrace at y {}", empire, c.cx, c.cz, c.top);
        c.shape();
        c.avenues();
        switch (empire) {
            case PARSIVAN -> Isfaran.landmarks(c);
            case KHEMET -> Neferet.landmarks(c);
            case AUREUM -> Aurelion.landmarks(c);
        }
        c.walls();
        switch (empire) {
            case PARSIVAN -> Isfaran.southGate(c);
            case KHEMET -> Neferet.southGate(c);
            case AUREUM -> Aurelion.southGate(c);
        }
        c.flagpoles();
        c.houses();
        c.roads();
        if (empire == Empire.AUREUM) Aurelion.outside(c);
        c.people();
    }

    // ------------------------------------------------------------------ small helpers for the builders

    static BlockState b(net.minecraftforge.registries.RegistryObject<Block> block) {
        return block.get().defaultBlockState();
    }

    void put(int x, int y, int z, BlockState state) {
        set(level, x, y, z, state);
    }

    void box(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) put(x, y, z, state);
            }
        }
    }

    /** The plot is the landmark's: no house is built on it. */
    void claim(int x0, int z0, int x1, int z1) {
        taken.add(new int[]{Math.min(x0, x1), Math.min(z0, z1), Math.max(x0, x1), Math.max(z0, z1)});
    }

    static BlockState stair(BlockState stairs, Direction facing) {
        return stairs.setValue(StairBlock.FACING, facing);
    }

    static BlockState stair(net.minecraftforge.registries.RegistryObject<Block> stairs, Direction facing) {
        return stair(b(stairs), facing);
    }

    static BlockState topSlab(BlockState slab) {
        return slab.setValue(SlabBlock.TYPE, SlabType.TOP);
    }

    static BlockState pillarY(BlockState pillar) {
        return pillar.hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)
                ? pillar.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Y) : pillar;
    }

    /**
     * A hipped roof over the walls from (minX, minZ) to (maxX, maxZ) on height y, overhanging by one: rings of stairs
     * climbing to the middle, at most rows of them, then flat full blocks. Returns the height of its top.
     */
    int hipRoof(int minX, int minZ, int maxX, int maxZ, int y, BlockState stairs, BlockState full, int rows) {
        for (int k = 0; ; k++) {
            int x0 = minX - 1 + k, x1 = maxX + 1 - k, z0 = minZ - 1 + k, z1 = maxZ + 1 - k;
            if (x0 > x1 || z0 > z1) return y + k - 1;
            if (k >= rows || x1 - x0 < 2 || z1 - z0 < 2) {
                box(x0, y + k, z0, x1, y + k, z1, full);
                return y + k;
            }
            for (int x = x0; x <= x1; x++) {
                put(x, y + k, z0, stair(stairs, Direction.SOUTH));
                put(x, y + k, z1, stair(stairs, Direction.NORTH));
            }
            for (int z = z0 + 1; z < z1; z++) {
                put(x0, y + k, z, stair(stairs, Direction.EAST));
                put(x1, y + k, z, stair(stairs, Direction.WEST));
            }
            if (k > 0) box(x0 + 1, y + k - 1, z0 + 1, x1 - 1, y + k - 1, z1 - 1, full); // under the ring, so no gap shows
        }
    }

    /**
     * A temple roof: a gable along z (or x) from wall to wall, overhanging by one, with stairs of the roof on both
     * slopes, the ridge of slabs, and the pediments at both ends filled with the gable block. Returns the ridge height.
     */
    int gable(int minX, int minZ, int maxX, int maxZ, int y, BlockState stairs, BlockState slab, BlockState pediment, boolean alongZ) {
        int lo = alongZ ? minX - 1 : minZ - 1, hi = alongZ ? maxX + 1 : maxZ + 1;
        int from = alongZ ? minZ - 1 : minX - 1, to = alongZ ? maxZ + 1 : maxX + 1;
        int k = 0;
        for (; lo + k <= hi - k; k++) {
            for (int s = from; s <= to; s++) {
                boolean end = s == from || s == to;
                if (lo + k == hi - k) {
                    place(alongZ, lo + k, y + k, s, slab);
                    continue;
                }
                Direction up = alongZ ? Direction.EAST : Direction.SOUTH;
                place(alongZ, lo + k, y + k, s, stair(stairs, up));
                place(alongZ, hi - k, y + k, s, stair(stairs, up.getOpposite()));
                if (end) for (int b = lo + k + 1; b < hi - k; b++) place(alongZ, b, y + k, s, pediment);
                else for (int b = lo + k + 1; b < hi - k; b++) if (k > 0) place(alongZ, b, y + k - 1, s, pediment);
            }
        }
        return y + k;
    }

    private void place(boolean alongZ, int across, int y, int along, BlockState state) {
        if (alongZ) put(across, y, along, state);
        else put(along, y, across, state);
    }

    /**
     * A raised platform with an edge band, a balustrade and a grand stair down its south side (and the other sides
     * named), its floor at top + rise - 1 and its top surface walkable at top + rise.
     */
    void platform(int x0, int z0, int hw, int hd, int rise, BlockState stone, BlockState band, BlockState floor, BlockState rail, int stairHalf,
                  Direction... stairs) {
        for (int x = x0 - hw; x <= x0 + hw; x++) {
            for (int z = z0 - hd; z <= z0 + hd; z++) {
                boolean edge = Math.abs(x - x0) == hw || Math.abs(z - z0) == hd;
                for (int y = top - 1; y < top - 1 + rise; y++) put(x, y, z, edge && y == top - 2 + rise ? band : stone);
                put(x, top - 1 + rise, z, floor);
                if (edge && rail != null) put(x, top + rise, z, rail);
            }
        }
        for (Direction d : stairs) {
            int ex = x0 + d.getStepX() * hw, ez = z0 + d.getStepZ() * hd;
            for (int i = 0; i <= rise; i++) {
                for (int w = -stairHalf; w <= stairHalf; w++) {
                    int x = d.getAxis() == Direction.Axis.Z ? x0 + w : ex + d.getStepX() * i;
                    int z = d.getAxis() == Direction.Axis.Z ? ez + d.getStepZ() * i : z0 + w;
                    for (int y = top + rise - i; y <= top + rise; y++) put(x, y, z, AIR);
                    put(x, top - 1 + rise - i, z, stair(stairsFor(), d.getOpposite()));
                    for (int y = top - 1; y < top - 1 + rise - i; y++) put(x, y, z, stone);
                }
            }
        }
    }

    private BlockState stairsFor() {
        return switch (empire) {
            case PARSIVAN -> b(SoFEBlocks.PARSIVAN_WHITE_PLASTER_STAIRS);
            case KHEMET -> b(SoFEBlocks.KHEMET_SANDSTONE_STAIRS);
            case AUREUM -> b(SoFEBlocks.AUREUM_MARBLE_BRICK_STAIRS);
        };
    }

    /**
     * A statue: an armor stand in gold (an emperor, a god), iron (a legionary, a guard) or leather and chain, with
     * arms, that no one can take apart, on a pedestal of two blocks.
     */
    void statue(int x, int y, int z, float yaw, String kind, BlockState pedestal) {
        if (pedestal != null) {
            put(x, y, z, pedestal);
            put(x, y + 1, z, pedestal);
            y += 2;
        }
        ArmorStand stand = EntityType.ARMOR_STAND.create(level);
        if (stand == null) return;
        stand.moveTo(x + 0.5, y, z + 0.5, yaw, 0);
        switch (kind) {
            case "gold" -> {
                stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
                stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE));
                stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS));
                stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS));
                stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
            }
            case "iron" -> {
                stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
                stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.IRON_LEGGINGS));
                stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.IRON_BOOTS));
                stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                stand.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
            }
            default -> {
                stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
                stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
                stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            }
        }
        CompoundTag tag = new CompoundTag();
        stand.addAdditionalSaveData(tag);
        tag.putBoolean("ShowArms", true);
        tag.putBoolean("NoBasePlate", true);
        tag.putInt("DisabledSlots", 4144959); // nothing can be taken or put
        tag.putBoolean("Invulnerable", true);
        stand.readAdditionalSaveData(tag);
        stand.setInvulnerable(true);
        stand.setNoGravity(true);
        stand.setYRot(yaw);
        stand.setYBodyRot(yaw);
        stand.setYHeadRot(yaw);
        level.addFreshEntity(stand);
    }

    /** A brazier: a post of the empire's stone with a lit campfire on it. */
    void brazier(int x, int y, int z, BlockState post) {
        put(x, y, z, post);
        put(x, y + 1, z, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true).setValue(CampfireBlock.SIGNAL_FIRE, false));
    }

    /** A rug on the floor at height y: a border of one colour round a field of another, under nothing else. */
    void rug(int x0, int z0, int x1, int z1, int y, BlockState field, BlockState border) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) continue;
                boolean edge = x == Math.min(x0, x1) || x == Math.max(x0, x1) || z == Math.min(z0, z1) || z == Math.max(z0, z1);
                put(x, y, z, edge ? border : field);
            }
        }
    }

    /** A wall banner; one in the empire's own colour carries its emblem. */
    void wallBanner(int x, int y, int z, Direction facing, Block banner) {
        put(x, y, z, banner.defaultBlockState().setValue(WallBannerBlock.FACING, facing));
        if (banner == banner(EmpireFlags.base(empire))) EmpireFlags.decorate(level, new BlockPos(x, y, z), empire);
    }

    /** A flag of the empire with its emblem. */
    void flag(int x, int y, int z, Direction flying) {
        EmpireFlags.flag(level, x, y, z, empire, flying);
    }

    void greatBanner(int x, int y, int z, Direction out) {
        EmpireFlags.greatBanner(level, x, y, z, empire, out);
    }

    /** Flagpoles with the empire's flags at the mouths of the four avenues round the plaza. */
    private void flagpoles() {
        int half = avenueHalf(), r = empire == Empire.AUREUM ? 18 : 13, d = r + 3;
        for (int side : new int[]{-1, 1}) {
            for (int end : new int[]{-1, 1}) {
                EmpireFlags.flagpole(level, cx + side * (half + 2), top, cz + end * d, empire, side > 0 ? Direction.EAST : Direction.WEST, wallBlock());
                EmpireFlags.flagpole(level, cx + end * d, top, cz + side * (half + 2), empire, side > 0 ? Direction.SOUTH : Direction.NORTH, wallBlock());
            }
        }
    }

    BlockState hanging() {
        return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
    }

    /** A lit candle group on a table or a shelf. */
    static BlockState candles(int n) {
        return Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, Math.max(1, Math.min(4, n))).setValue(CandleBlock.LIT, true);
    }

    /**
     * A round domed pavilion open on all sides: columns in a ring, a floor of the empire's paving, a dome on top and a
     * lantern under it.
     */
    void pavilion(int x0, int z0, int r, int h, BlockState column, BlockState floor, BlockState shell, BlockState rib, BlockState finial) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r + 0.3) continue;
                put(x0 + dx, top - 1, z0 + dz, floor);
                boolean col = d > r - 1 && Math.abs(Math.sin(Math.atan2(dz, dx) * 4)) < 0.3;
                for (int y = top; y < top + h; y++) put(x0 + dx, y, z0 + dz, col ? column : AIR);
                put(x0 + dx, top + h, z0 + dz, d > r - 1 ? rib : shell);
            }
        }
        Architecture.dome(level, x0, top + h + 1, z0, r, shell, rib, finial);
        put(x0, top + h - 1, z0, hanging());
        claim(x0 - r - 2, z0 - r - 2, x0 + r + 2, z0 + r + 2);
    }

    // ------------------------------------------------------------------ palettes

    private BlockState ground(int x, int z) {
        double n = Math.sin(x * 0.19) + Math.cos(z * 0.17) + Math.sin((x + z) * 0.11);
        return switch (empire) {
            case PARSIVAN -> n > 1.2 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
            case KHEMET -> n > 1.4 ? Blocks.SANDSTONE.defaultBlockState() : Blocks.SAND.defaultBlockState();
            case AUREUM -> n > 1.0 ? Blocks.GRASS_BLOCK.defaultBlockState() // a city of marble streets, with gardens here and there
                    : Math.floorMod(x * 7 + z * 13, 11) == 0 ? Blocks.CALCITE.defaultBlockState() : b(SoFEBlocks.AUREUM_POLISHED_MARBLE);
        };
    }

    /** Outside the walls the ground goes back to the land around (cleared of trees) over the shore. */
    private BlockState shoreGround(int x, int z) {
        return switch (empire) {
            case KHEMET -> Blocks.SAND.defaultBlockState();
            default -> Blocks.GRASS_BLOCK.defaultBlockState();
        };
    }

    BlockState fill() {
        return empire == Empire.KHEMET ? Blocks.SANDSTONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
    }

    /** The avenue's pavement at a column. */
    BlockState pave(int x, int z) {
        boolean checker = Math.floorMod(x + z, 2) == 0;
        return switch (empire) {
            case PARSIVAN -> checker ? b(SoFEBlocks.PARSIVAN_WHITE_PLASTER) : Blocks.SMOOTH_STONE.defaultBlockState();
            case KHEMET -> checker ? Blocks.CUT_SANDSTONE.defaultBlockState() : Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            case AUREUM -> checker ? b(SoFEBlocks.AUREUM_ROYAL_TILES) : b(SoFEBlocks.AUREUM_GOLD_MOSAIC);
        };
    }

    BlockState wallBlock() {
        return switch (empire) {
            case PARSIVAN -> Blocks.STONE_BRICKS.defaultBlockState();
            case KHEMET -> b(SoFEBlocks.KHEMET_CARVED_SANDSTONE);
            case AUREUM -> b(SoFEBlocks.AUREUM_MARBLE_BRICKS);
        };
    }

    BlockState trim() {
        return switch (empire) {
            case PARSIVAN -> b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES);
            case KHEMET -> b(SoFEBlocks.KHEMET_GOLD_HIEROGLYPHS);
            case AUREUM -> b(SoFEBlocks.AUREUM_GOLD_MOSAIC);
        };
    }

    BlockState[] awnings() {
        return switch (empire) {
            case PARSIVAN -> new BlockState[]{Blocks.MAGENTA_WOOL.defaultBlockState(), Blocks.CYAN_WOOL.defaultBlockState(),
                    Blocks.YELLOW_WOOL.defaultBlockState(), Blocks.PURPLE_WOOL.defaultBlockState()};
            case KHEMET -> new BlockState[]{Blocks.CYAN_WOOL.defaultBlockState(), Blocks.WHITE_WOOL.defaultBlockState(),
                    Blocks.LIGHT_BLUE_WOOL.defaultBlockState(), Blocks.YELLOW_WOOL.defaultBlockState()};
            case AUREUM -> new BlockState[]{Blocks.BLUE_WOOL.defaultBlockState(), Blocks.YELLOW_WOOL.defaultBlockState(),
                    Blocks.WHITE_WOOL.defaultBlockState(), Blocks.RED_WOOL.defaultBlockState()};
        };
    }

    /** The empire's colour for rugs, beds and banners. */
    String color() {
        return switch (empire) {
            case PARSIVAN -> "purple";
            case KHEMET -> "cyan";
            case AUREUM -> "blue";
        };
    }

    String wood() {
        return switch (empire) {
            case PARSIVAN -> "dark_oak";
            case KHEMET -> "jungle";
            case AUREUM -> "spruce";
        };
    }

    static BlockState carpet(String color) {
        return Furniture.from("minecraft", color + "_carpet", Blocks.WHITE_CARPET.defaultBlockState());
    }

    static Block banner(String color) {
        return net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", color + "_wall_banner"));
    }

    /** One house's look, with some walls corrupted (they heal with the region). */
    private Architecture.HouseStyle houseStyle() {
        BlockState[] aw = awnings();
        BlockState awning = aw[random.nextInt(aw.length)];
        boolean corrupt = random.nextInt(4) == 0;
        return switch (empire) {
            // Isfaran's houses in the concept: walls of dusky violet under stepped roofs of teal tiles, a few of white plaster
            case PARSIVAN -> new Architecture.HouseStyle(
                    corrupt ? b(SoFEBlocks.CORRUPTED_PARSIVAN_TURQUOISE_TILES) : random.nextInt(4) == 0 ? b(SoFEBlocks.PARSIVAN_WHITE_PLASTER)
                            : Blocks.PURPLE_TERRACOTTA.defaultBlockState(),
                    random.nextBoolean() ? Blocks.DARK_PRISMARINE.defaultBlockState() : b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES),
                    Blocks.PRISMARINE_BRICK_STAIRS.defaultBlockState(), Blocks.PRISMARINE_BRICK_SLAB.defaultBlockState(),
                    Blocks.DARK_OAK_DOOR.defaultBlockState(), awning, false);
            case KHEMET -> new Architecture.HouseStyle(
                    corrupt ? b(SoFEBlocks.CORRUPTED_KHEMET_CARVED_SANDSTONE) : random.nextBoolean() ? Blocks.SMOOTH_SANDSTONE.defaultBlockState()
                            : b(SoFEBlocks.KHEMET_PAINTED_LIMESTONE),
                    b(SoFEBlocks.KHEMET_CARVED_SANDSTONE), Blocks.PRISMARINE_STAIRS.defaultBlockState(), Blocks.PRISMARINE_SLAB.defaultBlockState(),
                    Blocks.JUNGLE_DOOR.defaultBlockState(), awning, false);
            case AUREUM -> new Architecture.HouseStyle(
                    corrupt ? b(SoFEBlocks.CORRUPTED_AUREUM_MARBLE_BRICKS) : random.nextBoolean() ? Blocks.CALCITE.defaultBlockState()
                            : b(SoFEBlocks.AUREUM_POLISHED_MARBLE),
                    b(SoFEBlocks.AUREUM_MARBLE_BRICKS), Blocks.BRICK_STAIRS.defaultBlockState(), Blocks.BRICK_SLAB.defaultBlockState(),
                    Blocks.SPRUCE_DOOR.defaultBlockState(), awning, true);
        };
    }

    // ------------------------------------------------------------------ the land

    /** The terrace stands at the middle height of the land it covers, never under the sea. */
    private int plateau() {
        List<Integer> heights = new ArrayList<>();
        for (int x = cx - a; x <= cx + a; x += 16) {
            for (int z = cz - a; z <= cz + a; z += 16) heights.add(SultharisBuilder.surfaceY(level, x, z));
        }
        heights.sort(Integer::compare);
        int sea = level.getSeaLevel();
        return Math.max(sea + 3, Math.min(heights.get(heights.size() / 2) + 1, sea + 30));
    }

    /** Distance out of the square of the city (negative inside). */
    private int outside(int x, int z) {
        return Math.max(Math.abs(x - cx), Math.abs(z - cz)) - a;
    }

    private void shape() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = cx - a - SHORE; x <= cx + a + SHORE; x++) {
            for (int z = cz - a - SHORE; z <= cz + a + SHORE; z++) {
                level.getChunk(x >> 4, z >> 4);
                int out = outside(x, z);
                int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                int land = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                // inside, the terrace; over the shore, a slope from the terrace to the land around, up a hill or down to
                // the sea floor (never a trench at the foot of the walls nor a cliff past it)
                int natural = Math.max(floor - 1, Math.min(land, surface - 1));
                int to = out <= 0 ? top - 1 : (int) Math.round(top - 1 + (natural - (top - 1)) * out / (double) (SHORE + 1));
                for (int y = Math.min(floor, to) - 1; y < to; y++) {
                    pos.set(x, y, z);
                    var state = level.getBlockState(pos);
                    if (y >= to - 3 || state.isAir() || !state.getFluidState().isEmpty() || state.canBeReplaced()) set(level, x, y, z, fill());
                }
                set(level, x, to, z, out <= 0 ? ground(x, z) : shoreGround(x, z));
                for (int y = to + 1; y <= Math.max(surface, to + 1) + 1 && y < top + 60; y++) set(level, x, y, z, AIR);
            }
        }
    }

    // ------------------------------------------------------------------ avenues and plaza

    int avenueHalf() {
        return empire == Empire.AUREUM ? 5 : 3;
    }

    private void avenues() {
        int half = avenueHalf();
        for (int t = -a + 2; t <= a - 2; t++) {
            for (int w = -half; w <= half; w++) {
                set(level, cx + w, top - 1, cz + t, Math.abs(w) == half ? trim() : pave(cx + w, cz + t));
                set(level, cx + t, top - 1, cz + w, Math.abs(w) == half ? trim() : pave(cx + t, cz + w));
            }
            if (Math.floorMod(t, 12) == 0 && Math.abs(t) > 18) { // lamps along the avenues
                for (int side : new int[]{-half - 1, half + 1}) {
                    lamp(cx + side, cz + t);
                    lamp(cx + t, cz + side);
                }
            }
        }
        claim(cx - half - 2, cz - a, cx + half + 2, cz + a);
        claim(cx - a, cz - half - 2, cx + a, cz + half + 2);
        // the plaza and its fountain
        int r = empire == Empire.AUREUM ? 18 : 13;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r) continue;
                set(level, cx + dx, top - 1, cz + dz, d > r - 1 ? trim() : pave(cx + dx, cz + dz));
            }
        }
        fountain(cx, cz, empire == Empire.AUREUM ? 6 : 4);
        claim(cx - r - 2, cz - r - 2, cx + r + 2, cz + r + 2);
        // stalls along the south avenue, both sides: the bazaar
        BlockState[] aw = awnings();
        int first = r + (empire == Empire.AUREUM ? 16 : 6); // in Aurelion the triumphal arch stands at the forum's edge
        for (int t = first; t < a - 24; t += 9) {
            Architecture.stall(level, cx - half - 6, top, cz + t, aw[random.nextInt(aw.length)], aw[random.nextInt(aw.length)], random);
            Architecture.stall(level, cx + half + 3, top, cz + t, aw[random.nextInt(aw.length)], aw[random.nextInt(aw.length)], random);
        }
        claim(cx - half - 8, cz + first, cx + half + 8, cz + a - 22);
    }

    void lamp(int x, int z) {
        set(level, x, top, z, wallBlock());
        set(level, x, top + 1, z, empire == Empire.KHEMET ? Blocks.SANDSTONE_WALL.defaultBlockState() : Blocks.STONE_BRICK_WALL.defaultBlockState());
        set(level, x, top + 2, z, Blocks.LANTERN.defaultBlockState());
    }

    private void fountain(int x, int z, int r) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r + 0.3) continue;
                boolean rim = d > r - 1;
                set(level, x + dx, top - 2, z + dz, trim());
                set(level, x + dx, top - 1, z + dz, rim ? trim() : Blocks.WATER.defaultBlockState());
                if (rim) set(level, x + dx, top, z + dz, empire == Empire.AUREUM ? b(SoFEBlocks.AUREUM_MARBLE_BRICK_SLAB)
                        : empire == Empire.KHEMET ? b(SoFEBlocks.KHEMET_SANDSTONE_SLAB) : b(SoFEBlocks.PARSIVAN_WHITE_PLASTER_SLAB));
            }
        }
        if (empire == Empire.AUREUM) return; // Aurelion's column of victory stands in it (Aurelion)
        for (int dy = 0; dy < 3; dy++) set(level, x, top - 1 + dy, z, trim());
        set(level, x, top + 2, z, Blocks.WATER.defaultBlockState());
    }

    // ------------------------------------------------------------------ walls, houses, roads, people

    private void walls() {
        BlockState wall = wallBlock(), band = trim();
        for (int t = -a; t <= a; t++) {
            for (int[] p : new int[][]{{cx + t, cz - a}, {cx + t, cz + a}, {cx - a, cz + t}, {cx + a, cz + t}}) {
                boolean gate = Math.abs(t) <= 3;
                for (int dy = 0; dy < WALL; dy++) {
                    set(level, p[0], top + dy, p[1], gate && dy < WALL - 2 ? AIR : dy == WALL - 2 ? band : wall);
                }
                if (Math.floorMod(t, 2) == 0) set(level, p[0], top + WALL, p[1], wall);
            }
        }
        for (int i = -a; i <= a; i += a / 3) {
            for (int[] p : new int[][]{{cx + i, cz - a}, {cx + i, cz + a}, {cx - a, cz + i}, {cx + a, cz + i}}) {
                if (Math.abs(i) <= 6) continue;
                Architecture.tower(level, p[0], top - 1, p[1], 3, WALL + 6, wall, band);
                if (empire == Empire.AUREUM) put(p[0], top + WALL + 7, p[1], Blocks.GOLD_BLOCK.defaultBlockState());
                if (empire == Empire.PARSIVAN || empire == Empire.AUREUM) {
                    put(p[0], top + WALL + 6, p[1], Blocks.SPRUCE_FENCE.defaultBlockState());
                    put(p[0], top + WALL + 7, p[1], Blocks.SPRUCE_FENCE.defaultBlockState());
                    flag(p[0], top + WALL + 8, p[1], Direction.EAST);
                }
            }
        }
        for (Direction out : new Direction[]{Direction.NORTH, Direction.EAST, Direction.WEST}) { // the south gate is the empire's own
            int gx = cx + out.getStepX() * a, gz = cz + out.getStepZ() * a;
            for (int side : new int[]{-6, 6}) {
                int x = gx + (out.getAxis() == Direction.Axis.Z ? side : 0), z = gz + (out.getAxis() == Direction.Axis.X ? side : 0);
                Architecture.tower(level, x, top - 1, z, 3, WALL + 10, wall, band);
                if (empire == Empire.PARSIVAN) Architecture.dome(level, x, top + WALL + 10, z, 3, Blocks.CYAN_TERRACOTTA.defaultBlockState(), band,
                        Blocks.GOLD_BLOCK.defaultBlockState());
                if (empire == Empire.AUREUM) hipRoof(x - 3, z - 3, x + 3, z + 3, top + WALL + 10, b(SoFEBlocks.AUREUM_ROYAL_TILE_STAIRS),
                        b(SoFEBlocks.AUREUM_GOLD_MOSAIC), 4);
                if (empire == Empire.KHEMET) set(level, x, top + WALL + 11, z, Blocks.GOLD_BLOCK.defaultBlockState());
            }
            for (int w = -3; w <= 3; w++) { // the lintel over the gate
                int x = gx + (out.getAxis() == Direction.Axis.Z ? w : 0), z = gz + (out.getAxis() == Direction.Axis.X ? w : 0);
                set(level, x, top + WALL - 2, z, band);
                set(level, x, top + WALL - 1, z, wall);
            }
        }
    }

    private boolean free(int x0, int z0, int x1, int z1) {
        for (int[] t : taken) if (x0 <= t[2] && x1 >= t[0] && z0 <= t[3] && z1 >= t[1]) return false;
        return true;
    }

    private void houses() {
        for (int x = cx - a + 4; x + PLOT <= cx + a - 4; x += PLOT) {
            for (int z = cz - a + 4; z + PLOT <= cz + a - 4; z += PLOT) {
                if (!free(x, z, x + PLOT - 1, z + PLOT - 1)) continue;
                int roll = random.nextInt(100);
                if (roll < 12) {
                    garden(x, z);
                    continue;
                }
                int w = 7 + random.nextInt(3), d = 7 + random.nextInt(3);
                Direction door = random.nextBoolean() ? Direction.NORTH : Direction.WEST;
                int height = empire == Empire.AUREUM ? 5 : 4 + random.nextInt(3);
                Architecture.HouseStyle style = houseStyle();
                int minX = x + 3, minZ = z + 3, maxX = minX + w - 1, maxZ = minZ + d - 1;
                Architecture.house(level, minX, minZ, w, d, top, height, door, style);
                switch (empire) {
                    case PARSIVAN -> { // a stepped roof of teal tiles, a violet or teal dome on the grander ones
                        int crown = hipRoof(minX, minZ, maxX, maxZ, top + height, Blocks.PRISMARINE_BRICK_STAIRS.defaultBlockState(),
                                Blocks.PRISMARINE_BRICKS.defaultBlockState(), 3);
                        if (random.nextInt(3) == 0) {
                            boolean violet = random.nextBoolean();
                            Architecture.dome(level, (minX + maxX) / 2, crown + 1, (minZ + maxZ) / 2, 2,
                                    violet ? b(SoFEBlocks.PARSIVAN_VIOLET_TILES) : Blocks.CYAN_TERRACOTTA.defaultBlockState(),
                                    b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES), Blocks.GOLD_BLOCK.defaultBlockState());
                        }
                    }
                    case KHEMET -> { // half the houses under a little pyramid roof of teal tiles, the others flat with a parapet
                        if (random.nextBoolean()) hipRoof(minX, minZ, maxX, maxZ, top + height, Blocks.PRISMARINE_STAIRS.defaultBlockState(),
                                Blocks.PRISMARINE.defaultBlockState(), 3);
                    }
                    default -> {
                    }
                }
                furnish(minX, minZ, maxX, maxZ, height, door);
                int nx = door == Direction.NORTH ? minX + w / 2 : x + 1;
                int nz = door == Direction.NORTH ? z + 1 : minZ + d / 2;
                doors.add(new int[]{nx, nz, door == Direction.NORTH ? 180 : 90});
            }
        }
    }

    /** A plot left green: a tree, flowers or a well. */
    private void garden(int x, int z) {
        tree(x + 6, z + 6);
        BlockState[] flowers = switch (empire) {
            case PARSIVAN -> new BlockState[]{Blocks.ALLIUM.defaultBlockState(), Blocks.LILAC.defaultBlockState(), Blocks.PINK_TULIP.defaultBlockState()};
            case KHEMET -> new BlockState[]{Blocks.DEAD_BUSH.defaultBlockState(), Blocks.FERN.defaultBlockState()};
            case AUREUM -> new BlockState[]{Blocks.POPPY.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState()};
        };
        for (int i = 0; i < 6; i++) {
            int fx = x + 3 + random.nextInt(8), fz = z + 3 + random.nextInt(8);
            if (level.getBlockState(new BlockPos(fx, top, fz)).isAir() && level.getBlockState(new BlockPos(fx, top - 1, fz)).is(Blocks.GRASS_BLOCK)) {
                BlockState f = flowers[random.nextInt(flowers.length)];
                if (f.is(Blocks.LILAC)) continue; // tall plants need their upper half: leave them out
                put(fx, top, fz, f);
            }
        }
    }

    private void tree(int x, int z) {
        switch (empire) {
            case KHEMET -> palm(x, z);
            case AUREUM -> { // a cypress
                for (int dy = 0; dy < 9; dy++) {
                    set(level, x, top + dy, z, dy < 2 ? Blocks.SPRUCE_LOG.defaultBlockState()
                            : Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
                    if (dy >= 2 && dy <= 5) for (Direction d : Direction.Plane.HORIZONTAL) {
                        set(level, x + d.getStepX(), top + dy, z + d.getStepZ(), Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
                    }
                }
            }
            default -> { // an azalea in flower
                for (int dy = 0; dy < 3; dy++) set(level, x, top + dy, z, Blocks.OAK_LOG.defaultBlockState());
                for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) for (int dy = 3; dy <= 4; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) > 3) continue;
                    set(level, x + dx, top + dy, z + dz, (random.nextInt(3) == 0 ? Blocks.FLOWERING_AZALEA_LEAVES : Blocks.AZALEA_LEAVES)
                            .defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
                }
            }
        }
    }

    void palm(int x, int z) {
        palm(x, top, z);
    }

    void palm(int x, int y, int z) {
        int height = 5 + random.nextInt(3);
        for (int dy = 0; dy < height; dy++) set(level, x, y + dy, z, Blocks.JUNGLE_LOG.defaultBlockState());
        BlockState leaves = Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        set(level, x, y + height, z, leaves);
        for (Direction d : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            for (int k = 1; k <= 3; k++) set(level, x + d.getStepX() * k, y + height - (k == 3 ? 1 : 0), z + d.getStepZ() * k, leaves);
        }
    }

    /**
     * The inside of a house: a rug in the empire's colours over the floor, a bed in a corner, a kitchen corner (a stove,
     * a cutting board on a counter, a jar), a table with candles and two chairs, a chest and a crate, a cabinet or
     * shelves, a plant, a loom or a cauldron for the trades, a banner over the bed. The way from the door is left clear.
     */
    private void furnish(int minX, int minZ, int maxX, int maxZ, int height, Direction door) {
        String wood = wood(), color = color();
        int x0 = minX + 1, z0 = minZ + 1, x1 = maxX - 1, z1 = maxZ - 1;
        int mx = (minX + maxX) / 2, mz = (minZ + maxZ) / 2;
        // the bed in the far corner from the door, its head against the south wall
        BlockState bed = Furniture.from("minecraft", color + "_bed", Blocks.RED_BED.defaultBlockState()).setValue(BedBlock.FACING, Direction.SOUTH);
        put(x1, top, z1, bed.setValue(BedBlock.PART, BedPart.HEAD));
        put(x1, top, z1 - 1, bed.setValue(BedBlock.PART, BedPart.FOOT));
        put(x1 - 1, top, z1, Furniture.drawer(wood, Direction.NORTH));
        put(x1 - 1, top + 1, z1, Blocks.POTTED_FERN.defaultBlockState());
        wallBanner(x1, top + 2, maxZ - 1, Direction.NORTH, banner(color));
        // the kitchen in the south-west corner
        put(x0, top, z1, Furniture.facing(Blocks.SMOKER.defaultBlockState(), Direction.EAST));
        put(x0, top, z1 - 1, Furniture.desk(wood, Direction.EAST));
        put(x0, top + 1, z1 - 1, Furniture.cuttingBoard(wood, Direction.EAST));
        if (z1 - 2 > z0 + 1 && !(door == Direction.WEST && z1 - 2 == mz)) put(x0, top, z1 - 2, Furniture.jar(wood, Direction.EAST));
        // storage in the north-east corner
        put(x1, top, z0, Furniture.facing(Blocks.CHEST.defaultBlockState(), Direction.WEST));
        put(x1, top + 1, z0, Furniture.crate(wood));
        if (door != Direction.WEST || mz != z0) put(x0, top, z0, Furniture.cabinet(wood, Direction.SOUTH));
        // a trade by the empire
        BlockState trade = switch (empire) {
            case PARSIVAN -> Blocks.LOOM.defaultBlockState();          // carpet weavers
            case KHEMET -> Blocks.WATER_CAULDRON.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3); // water from the river
            case AUREUM -> Blocks.BOOKSHELF.defaultBlockState();       // the scrolls of a citizen
        };
        if (x1 - 1 > mx + 1) put(x1 - 1, top, z0, trade);
        // the table in the middle with two chairs
        int tx = mx, tz = mz + 1;
        if (tz >= z1 - 1) tz = mz;
        put(tx, top, tz, Furniture.table(wood));
        put(tx, top + 1, tz, candles(1 + random.nextInt(3)));
        put(tx - 1, top, tz, Furniture.chair(wood, Direction.EAST));
        put(tx + 1, top, tz, Furniture.chair(wood, Direction.WEST));
        // the rug over what is left of the floor
        String border = switch (empire) {
            case PARSIVAN -> "cyan";
            case KHEMET -> "yellow";
            case AUREUM -> "yellow";
        };
        rug(x0, z0, x1, z1, top, carpet(color), carpet(border));
        // a plant by the door
        int px = door == Direction.NORTH ? mx + 1 : x0, pz = door == Direction.NORTH ? z0 : mz + 1;
        if (level.getBlockState(new BlockPos(px, top, pz)).getBlock() instanceof net.minecraft.world.level.block.CarpetBlock) {
            put(px, top, pz, empire == Empire.KHEMET ? Blocks.POTTED_CACTUS.defaultBlockState()
                    : empire == Empire.PARSIVAN ? Blocks.POTTED_ALLIUM.defaultBlockState() : Blocks.POTTED_RED_TULIP.defaultBlockState());
        }
        // clear the way in
        int ix = door == Direction.NORTH ? mx : x0, iz = door == Direction.NORTH ? z0 : mz;
        if (!(level.getBlockState(new BlockPos(ix, top, iz)).getBlock() instanceof net.minecraft.world.level.block.CarpetBlock)) {
            put(ix, top, iz, carpet(color));
        }
    }

    private void roads() {
        Roads.Style style = new Roads.Style(empire == Empire.KHEMET ? Blocks.JUNGLE_FENCE.defaultBlockState() : Blocks.SPRUCE_FENCE.defaultBlockState(),
                wallBlock(), fill(), y -> wallBlock());
        for (Direction out : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            Roads.outbound(level, cx + out.getStepX() * a, cz + out.getStepZ() * a, out, 70, top, 5, style);
        }
    }

    /** The city's people, each living at one of the houses. */
    private void people() {
        if (doors.isEmpty()) return;
        java.util.Collections.shuffle(doors, random);
        List<String> folk = FOLK.get(empire);
        for (int i = 0; i < folk.size() && i < doors.size(); i++) {
            int[] at = doors.get(i);
            StoryPlacements.spawnNpc(level, new StructurePositions.Npc(folk.get(i), "citizen", at[0], at[1], at[2], null));
        }
    }
}
