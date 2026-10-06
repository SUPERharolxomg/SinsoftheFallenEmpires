package com.sofe.registry;

import com.sofe.travel.VaultBlock;
import com.sofe.travel.WaystoneBlock;
import com.sofe.world.lock.SealVeilBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Blocks that are not material forms: the Seal Veil and the Sulthari building set
 * (docs/Mundo.md, W6). Each entry says how the data generators build its model.
 */
public final class SoFEBlocks {

    /** How the data generators make the block state and model. */
    public enum Shape { CUBE, CUBE_SIDES, STAIRS, SLAB, WALL, PILLAR, HAND_MADE }

    /**
     * @param base    for stairs, slabs and walls: the full block whose texture they use
     * @param oreDrop for ores: the item they drop (Fortune applies, Silk Touch drops the ore); null otherwise
     */
    public record Entry(RegistryObject<Block> block, Shape shape, RegistryObject<Block> base, boolean inCreativeTab,
                        Supplier<? extends net.minecraft.world.item.Item> oreDrop) {
    }

    // declared before the blocks: their static initializers add to these lists
    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final List<RegistryObject<Item>> ITEMS = new ArrayList<>();

    public static final RegistryObject<Block> SEAL_VEIL = register("seal_veil", Shape.HAND_MADE, null, false,
            () -> new SealVeilBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0f, 3_600_000.0f)
                    .noLootTable()
                    .noOcclusion()
                    .lightLevel(s -> 4)
                    .sound(SoundType.AMETHYST)
                    .pushReaction(PushReaction.BLOCK)
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false)));

    // --- travel and storage (docs/Jugabilidad.md, G2 and G5) ---
    public static final RegistryObject<Block> WAYSTONE = register("waystone", Shape.HAND_MADE, null, true,
            // story waystones stand wherever the journey put them, in a city or out in the wild: no one can break them
            () -> new WaystoneBlock(BlockBehaviour.Properties.copy(Blocks.LODESTONE).strength(-1f, 3600000f).lightLevel(s -> 10).noOcclusion()));
    public static final RegistryObject<Block> PERSONAL_VAULT = register("personal_vault", Shape.CUBE_SIDES, null, true,
            () -> new VaultBlock(BlockBehaviour.Properties.copy(Blocks.ENDER_CHEST)));

    // --- Sulthari building set (intact) ---
    public static final RegistryObject<Block> SULTHARI_SANDSTONE_BRICKS = cube("sulthari_sandstone_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE).strength(1.5f, 6.0f)));
    public static final RegistryObject<Block> SULTHARI_SANDSTONE_BRICK_STAIRS = register("sulthari_sandstone_brick_stairs", Shape.STAIRS,
            SULTHARI_SANDSTONE_BRICKS, true, () -> new StairBlock(() -> SULTHARI_SANDSTONE_BRICKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE).strength(1.5f, 6.0f)));
    public static final RegistryObject<Block> SULTHARI_SANDSTONE_BRICK_SLAB = register("sulthari_sandstone_brick_slab", Shape.SLAB,
            SULTHARI_SANDSTONE_BRICKS, true, () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE).strength(1.5f, 6.0f)));
    public static final RegistryObject<Block> SULTHARI_SANDSTONE_BRICK_WALL = register("sulthari_sandstone_brick_wall", Shape.WALL,
            SULTHARI_SANDSTONE_BRICKS, true, () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE).strength(1.5f, 6.0f).forceSolidOn()));
    public static final RegistryObject<Block> SULTHARI_BRASS_PLATING = cube("sulthari_brass_plating",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.GOLD)));
    public static final RegistryObject<Block> SULTHARI_BRASS_TRIM = register("sulthari_brass_trim", Shape.PILLAR, null, true,
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK).mapColor(MapColor.GOLD)));
    public static final RegistryObject<Block> SULTHARI_GLAZED_TILES = cube("sulthari_glazed_tiles",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.BLUE_GLAZED_TERRACOTTA).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final RegistryObject<Block> SULTHARI_AETHERIUM_LAMP = cube("sulthari_aetherium_lamp",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SEA_LANTERN).mapColor(MapColor.COLOR_CYAN).lightLevel(s -> 15)));

    // --- Nordrath building set (docs/Mundo.md, W6): intact and corrupted
    public static final RegistryObject<Block> NORDRATH_RUNESTONE_BRICKS = cube("nordrath_runestone_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS).mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryObject<Block> NORDRATH_RUNESTONE_BRICK_STAIRS = register("nordrath_runestone_brick_stairs", Shape.STAIRS,
            NORDRATH_RUNESTONE_BRICKS, true, () -> new StairBlock(() -> NORDRATH_RUNESTONE_BRICKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
    public static final RegistryObject<Block> NORDRATH_RUNESTONE_BRICK_SLAB = register("nordrath_runestone_brick_slab", Shape.SLAB,
            NORDRATH_RUNESTONE_BRICKS, true, () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
    public static final RegistryObject<Block> NORDRATH_RUNESTONE_BRICK_WALL = register("nordrath_runestone_brick_wall", Shape.WALL,
            NORDRATH_RUNESTONE_BRICKS, true, () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS).forceSolidOn()));
    public static final RegistryObject<Block> NORDRATH_DARK_TIMBER = register("nordrath_dark_timber", Shape.PILLAR, null, true,
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_LOG)));
    public static final RegistryObject<Block> NORDRATH_DARK_PLANKS = cube("nordrath_dark_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_PLANKS)));
    public static final RegistryObject<Block> NORDRATH_IRON_BRAZIER = register("nordrath_iron_brazier", Shape.HAND_MADE, null, true,
            () -> new com.sofe.block.Brazier(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).lightLevel(s -> 15).noOcclusion()));
    public static final RegistryObject<Block> CORRUPTED_NORDRATH_RUNESTONE_BRICKS = cube("corrupted_nordrath_runestone_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS).mapColor(MapColor.COLOR_BLACK)));
    public static final RegistryObject<Block> CORRUPTED_NORDRATH_DARK_TIMBER = register("corrupted_nordrath_dark_timber", Shape.PILLAR, null, true,
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_STEM).lightLevel(s -> 6)));

    // <generated-empire-blocks> by scripts/make_empire_blocks.py: Parsivan, Khemet and Aureum, intact and corrupted
    public static final RegistryObject<Block> PARSIVAN_TURQUOISE_TILES = cube("parsivan_turquoise_tiles", () -> new Block(BlockBehaviour.Properties.copy(Blocks.CYAN_TERRACOTTA).mapColor(MapColor.COLOR_CYAN)));
    public static final RegistryObject<Block> PARSIVAN_WHITE_PLASTER = cube("parsivan_white_plaster", () -> new Block(BlockBehaviour.Properties.copy(Blocks.SMOOTH_QUARTZ).mapColor(MapColor.SNOW)));
    public static final RegistryObject<Block> PARSIVAN_LAPIS_MOSAIC = cube("parsivan_lapis_mosaic", () -> new Block(BlockBehaviour.Properties.copy(Blocks.LAPIS_BLOCK).mapColor(MapColor.LAPIS)));
    public static final RegistryObject<Block> CORRUPTED_PARSIVAN_TURQUOISE_TILES = cube("corrupted_parsivan_turquoise_tiles", () -> new Block(BlockBehaviour.Properties.copy(Blocks.CYAN_TERRACOTTA).mapColor(MapColor.COLOR_GRAY)));
    public static final RegistryObject<Block> CORRUPTED_PARSIVAN_WHITE_PLASTER = cube("corrupted_parsivan_white_plaster", () -> new Block(BlockBehaviour.Properties.copy(Blocks.SMOOTH_QUARTZ).mapColor(MapColor.COLOR_GREEN)));
    public static final RegistryObject<Block> KHEMET_CARVED_SANDSTONE = cube("khemet_carved_sandstone", () -> new Block(BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE).mapColor(MapColor.SAND)));
    public static final RegistryObject<Block> KHEMET_PAINTED_LIMESTONE = cube("khemet_painted_limestone", () -> new Block(BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.TERRACOTTA_WHITE)));
    public static final RegistryObject<Block> KHEMET_GOLD_HIEROGLYPHS = cube("khemet_gold_hieroglyphs", () -> new Block(BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE).mapColor(MapColor.GOLD)));
    public static final RegistryObject<Block> KHEMET_OBELISK = register("khemet_obelisk", Shape.PILLAR, null, true,
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE).mapColor(MapColor.SAND)));
    public static final RegistryObject<Block> CORRUPTED_KHEMET_CARVED_SANDSTONE = cube("corrupted_khemet_carved_sandstone", () -> new Block(BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE).mapColor(MapColor.DIRT)));
    public static final RegistryObject<Block> CORRUPTED_KHEMET_PAINTED_LIMESTONE = cube("corrupted_khemet_painted_limestone", () -> new Block(BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.COLOR_BROWN)));
    public static final RegistryObject<Block> IMPERIAL_MARBLE = cube("imperial_marble", () -> new Block(BlockBehaviour.Properties.copy(Blocks.CALCITE).mapColor(MapColor.QUARTZ)));
    public static final RegistryObject<Block> AUREUM_POLISHED_MARBLE = cube("aureum_polished_marble", () -> new Block(BlockBehaviour.Properties.copy(Blocks.POLISHED_DIORITE).mapColor(MapColor.QUARTZ)));
    public static final RegistryObject<Block> AUREUM_MARBLE_BRICKS = cube("aureum_marble_bricks", () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS).mapColor(MapColor.QUARTZ)));
    public static final RegistryObject<Block> AUREUM_MARBLE_PILLAR = register("aureum_marble_pillar", Shape.PILLAR, null, true,
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.QUARTZ_PILLAR).mapColor(MapColor.QUARTZ)));
    public static final RegistryObject<Block> AUREUM_GOLD_MOSAIC = cube("aureum_gold_mosaic", () -> new Block(BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK).mapColor(MapColor.GOLD)));
    public static final RegistryObject<Block> AUREUM_ROYAL_TILES = cube("aureum_royal_tiles", () -> new Block(BlockBehaviour.Properties.copy(Blocks.BLUE_TERRACOTTA).mapColor(MapColor.COLOR_BLUE)));
    public static final RegistryObject<Block> PARSIVAN_VIOLET_TILES = cube("parsivan_violet_tiles", () -> new Block(BlockBehaviour.Properties.copy(Blocks.PURPLE_TERRACOTTA).mapColor(MapColor.COLOR_PURPLE)));
    public static final RegistryObject<Block> CORRUPTED_AUREUM_MARBLE_BRICKS = cube("corrupted_aureum_marble_bricks", () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS).mapColor(MapColor.COLOR_BLACK)));
    public static final RegistryObject<Block> CORRUPTED_AUREUM_MARBLE_PILLAR = register("corrupted_aureum_marble_pillar", Shape.PILLAR, null, true,
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.QUARTZ_PILLAR).mapColor(MapColor.COLOR_BLACK)));
    public static final RegistryObject<Block> PARSIVAN_WHITE_PLASTER_STAIRS = register("parsivan_white_plaster_stairs", Shape.STAIRS, PARSIVAN_WHITE_PLASTER, true,
            () -> new StairBlock(() -> PARSIVAN_WHITE_PLASTER.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SMOOTH_QUARTZ)));
    public static final RegistryObject<Block> PARSIVAN_WHITE_PLASTER_SLAB = register("parsivan_white_plaster_slab", Shape.SLAB, PARSIVAN_WHITE_PLASTER, true,
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SMOOTH_QUARTZ)));
    public static final RegistryObject<Block> PARSIVAN_WHITE_PLASTER_WALL = register("parsivan_white_plaster_wall", Shape.WALL, PARSIVAN_WHITE_PLASTER, true,
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.SMOOTH_QUARTZ).forceSolidOn()));
    public static final RegistryObject<Block> KHEMET_SANDSTONE_STAIRS = register("khemet_sandstone_stairs", Shape.STAIRS, KHEMET_CARVED_SANDSTONE, true,
            () -> new StairBlock(() -> KHEMET_CARVED_SANDSTONE.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE)));
    public static final RegistryObject<Block> KHEMET_SANDSTONE_SLAB = register("khemet_sandstone_slab", Shape.SLAB, KHEMET_CARVED_SANDSTONE, true,
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE)));
    public static final RegistryObject<Block> KHEMET_SANDSTONE_WALL = register("khemet_sandstone_wall", Shape.WALL, KHEMET_CARVED_SANDSTONE, true,
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.CUT_SANDSTONE).forceSolidOn()));
    public static final RegistryObject<Block> AUREUM_MARBLE_BRICK_STAIRS = register("aureum_marble_brick_stairs", Shape.STAIRS, AUREUM_MARBLE_BRICKS, true,
            () -> new StairBlock(() -> AUREUM_MARBLE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
    public static final RegistryObject<Block> AUREUM_MARBLE_BRICK_SLAB = register("aureum_marble_brick_slab", Shape.SLAB, AUREUM_MARBLE_BRICKS, true,
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS)));
    public static final RegistryObject<Block> AUREUM_MARBLE_BRICK_WALL = register("aureum_marble_brick_wall", Shape.WALL, AUREUM_MARBLE_BRICKS, true,
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BRICKS).forceSolidOn()));
    public static final RegistryObject<Block> AUREUM_ROYAL_TILE_STAIRS = register("aureum_royal_tile_stairs", Shape.STAIRS, AUREUM_ROYAL_TILES, true,
            () -> new StairBlock(() -> AUREUM_ROYAL_TILES.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.BLUE_TERRACOTTA)));
    public static final RegistryObject<Block> AUREUM_ROYAL_TILE_SLAB = register("aureum_royal_tile_slab", Shape.SLAB, AUREUM_ROYAL_TILES, true,
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BLUE_TERRACOTTA)));
    public static final RegistryObject<Block> AUREUM_ROYAL_TILE_WALL = register("aureum_royal_tile_wall", Shape.WALL, AUREUM_ROYAL_TILES, true,
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.BLUE_TERRACOTTA).forceSolidOn()));
    public static final RegistryObject<Block> PARSIVAN_VIOLET_TILE_STAIRS = register("parsivan_violet_tile_stairs", Shape.STAIRS, PARSIVAN_VIOLET_TILES, true,
            () -> new StairBlock(() -> PARSIVAN_VIOLET_TILES.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.PURPLE_TERRACOTTA)));
    public static final RegistryObject<Block> PARSIVAN_VIOLET_TILE_SLAB = register("parsivan_violet_tile_slab", Shape.SLAB, PARSIVAN_VIOLET_TILES, true,
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.PURPLE_TERRACOTTA)));
    public static final RegistryObject<Block> PARSIVAN_VIOLET_TILE_WALL = register("parsivan_violet_tile_wall", Shape.WALL, PARSIVAN_VIOLET_TILES, true,
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.PURPLE_TERRACOTTA).forceSolidOn()));
    // </generated-empire-blocks>

    // --- Sealed Gates (docs/Mundo.md, W2): dungeon and arena doors with their own condition
    public static final RegistryObject<Block> SEALED_GATE = register("sealed_gate", Shape.HAND_MADE, null, false,
            () -> new com.sofe.world.lock.SealedGateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0f, 3_600_000.0f).noLootTable().lightLevel(s -> 7).sound(SoundType.AMETHYST)
                    .pushReaction(PushReaction.BLOCK).isValidSpawn((state, level, pos, type) -> false)));

    // --- the Burning Deep (docs/Anexos.md, A3)
    public static final RegistryObject<Block> INFERNAL_EMBER_ORE = ore("infernal_ember_ore",
            () -> new net.minecraft.world.level.block.DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE)
                    .mapColor(MapColor.COLOR_ORANGE).lightLevel(s -> 5), net.minecraft.util.valueproviders.UniformInt.of(2, 5)),
            ItemRegistry.INFERNAL_EMBER);
    public static final RegistryObject<Block> WAILING_SOUL_ORE = ore("wailing_soul_ore",
            () -> new net.minecraft.world.level.block.DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_QUARTZ_ORE)
                    .mapColor(MapColor.COLOR_CYAN).lightLevel(s -> 3), net.minecraft.util.valueproviders.UniformInt.of(2, 5)),
            ItemRegistry.WAILING_SOUL);
    // --- the Outer Void (docs/Mundo.md: Void Crystal in end stone veins on the outer islands, y 20 to 60)
    public static final RegistryObject<Block> VOID_CRYSTAL_ORE = ore("void_crystal_ore",
            () -> new net.minecraft.world.level.block.DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.END_STONE)
                    .strength(4.5f, 9f).requiresCorrectToolForDrops().mapColor(MapColor.COLOR_PURPLE).lightLevel(s -> 6),
                    net.minecraft.util.valueproviders.UniformInt.of(4, 8)),
            ItemRegistry.VOID_CRYSTAL);

    // --- Sprint 5.5: crafting stations, the Reward Coffer and Runestone
    public static final RegistryObject<Block> RUNESTONE = cube("runestone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_LIGHT_GRAY)));
    public static final RegistryObject<Block> IMPERIAL_FORGE = register("imperial_forge", Shape.HAND_MADE, null, true,
            () -> new com.sofe.crafting.StationBlock(com.sofe.crafting.StationRecipe.Kind.IMPERIAL_FORGE,
                    BlockBehaviour.Properties.copy(Blocks.ANVIL).sound(SoundType.ANVIL).lightLevel(s -> 11).noOcclusion()));
    public static final RegistryObject<Block> ALEMBIC = register("alembic", Shape.CUBE_SIDES, null, true,
            () -> new com.sofe.crafting.StationBlock(com.sofe.crafting.StationRecipe.Kind.ALEMBIC,
                    BlockBehaviour.Properties.copy(Blocks.BREWING_STAND).strength(2.0f).noOcclusion()));
    // --- Sprint 7: the Jeweler, the Purifier and the Tempering Anvil (docs/Pociones.md, "Forges and crafting")
    public static final RegistryObject<Block> JEWELER = register("jeweler", Shape.CUBE_SIDES, null, true,
            () -> new com.sofe.crafting.JewelerBlock(BlockBehaviour.Properties.copy(Blocks.SMITHING_TABLE).lightLevel(s -> 5).noOcclusion()));
    public static final RegistryObject<Block> PURIFIER = register("purifier", Shape.CUBE_SIDES, null, true,
            () -> new com.sofe.crafting.StationBlock(com.sofe.crafting.StationRecipe.Kind.PURIFIER,
                    BlockBehaviour.Properties.copy(Blocks.BLAST_FURNACE).lightLevel(s -> 9).noOcclusion()));
    public static final RegistryObject<Block> TEMPERING_ANVIL = register("tempering_anvil", Shape.CUBE_SIDES, null, true,
            () -> new com.sofe.crafting.TemperingAnvilBlock(BlockBehaviour.Properties.copy(Blocks.ANVIL).sound(SoundType.ANVIL).noOcclusion()));
    /** A chest at a dungeon's door that opens for two or more Bearers together (docs/Anexos.md, A5). */
    public static final RegistryObject<Block> KINSHIP_CHEST = register("kinship_chest", Shape.CUBE_SIDES, null, false,
            () -> new com.sofe.block.KinshipChest(BlockBehaviour.Properties.copy(Blocks.CHEST)
                    .strength(-1.0f, 3_600_000.0f).noLootTable().lightLevel(s -> 6)));
    public static final RegistryObject<Block> REWARD_COFFER = register("reward_coffer", Shape.CUBE_SIDES, null, false,
            () -> new com.sofe.entity.boss.RewardCoffer.Block(BlockBehaviour.Properties.copy(Blocks.CHEST)
                    .strength(-1.0f, 3_600_000.0f).noLootTable().lightLevel(s -> 8)));

    public static final net.minecraftforge.registries.DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITIES =
            net.minecraftforge.registries.DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.BLOCK_ENTITY_TYPES, com.sofe.SoFEMod.MOD_ID);
    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<com.sofe.block.KinshipChest.Entity>> KINSHIP_CHEST_ENTITY =
            BLOCK_ENTITIES.register("kinship_chest", com.sofe.block.KinshipChest::type);
    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<com.sofe.entity.boss.RewardCoffer.Entity>> REWARD_COFFER_ENTITY =
            BLOCK_ENTITIES.register("reward_coffer", com.sofe.entity.boss.RewardCoffer::type);

    /** The great banner of the clans: hand-written model and blockstate, so it is not in ENTRIES (no datagen). */
    public static final RegistryObject<Block> CLAN_BANNER = BlockRegistry.BLOCKS.register("clan_banner",
            () -> new com.sofe.block.ClanBanner(BlockBehaviour.Properties.copy(Blocks.RED_BANNER).noOcclusion().noLootTable()));
    public static final RegistryObject<Item> CLAN_BANNER_ITEM = ItemRegistry.ITEMS.register("clan_banner",
            () -> new BlockItem(CLAN_BANNER.get(), new Item.Properties()));
    /** The great banners of the empires, the same block with each empire's cloth. */
    public static final RegistryObject<Block> PARSIVAN_BANNER = BlockRegistry.BLOCKS.register("parsivan_banner",
            () -> new com.sofe.block.ClanBanner(BlockBehaviour.Properties.copy(Blocks.PURPLE_BANNER).noOcclusion().noLootTable(), "parsivan_banner"));
    public static final RegistryObject<Item> PARSIVAN_BANNER_ITEM = ItemRegistry.ITEMS.register("parsivan_banner",
            () -> new BlockItem(PARSIVAN_BANNER.get(), new Item.Properties()));
    public static final RegistryObject<Block> KHEMET_BANNER = BlockRegistry.BLOCKS.register("khemet_banner",
            () -> new com.sofe.block.ClanBanner(BlockBehaviour.Properties.copy(Blocks.BLUE_BANNER).noOcclusion().noLootTable(), "khemet_banner"));
    public static final RegistryObject<Item> KHEMET_BANNER_ITEM = ItemRegistry.ITEMS.register("khemet_banner",
            () -> new BlockItem(KHEMET_BANNER.get(), new Item.Properties()));
    public static final RegistryObject<Block> AUREUM_BANNER = BlockRegistry.BLOCKS.register("aureum_banner",
            () -> new com.sofe.block.ClanBanner(BlockBehaviour.Properties.copy(Blocks.BLUE_BANNER).noOcclusion().noLootTable(), "aureum_banner"));
    public static final RegistryObject<Item> AUREUM_BANNER_ITEM = ItemRegistry.ITEMS.register("aureum_banner",
            () -> new BlockItem(AUREUM_BANNER.get(), new Item.Properties()));
    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<com.sofe.block.ClanBanner.Entity>> CLAN_BANNER_ENTITY =
            BLOCK_ENTITIES.register("clan_banner", com.sofe.block.ClanBanner::type);

    private static RegistryObject<Block> ore(String id, Supplier<Block> factory, Supplier<? extends net.minecraft.world.item.Item> drop) {
        RegistryObject<Block> block = register(id, Shape.CUBE, null, true, factory);
        Entry plain = ENTRIES.remove(ENTRIES.size() - 1);
        ENTRIES.add(new Entry(plain.block(), plain.shape(), null, true, drop));
        return block;
    }

    private SoFEBlocks() {
    }

    /** Forces the class to load so its entries reach the deferred registers; call from the mod constructor. */
    public static void init() {
    }

    public static List<Entry> entries() {
        return List.copyOf(ENTRIES);
    }

    private static RegistryObject<Block> cube(String id, Supplier<Block> factory) {
        return register(id, Shape.CUBE, null, true, factory);
    }

    private static RegistryObject<Block> register(String id, Shape shape, RegistryObject<Block> base, boolean inCreativeTab, Supplier<Block> factory) {
        RegistryObject<Block> block = BlockRegistry.BLOCKS.register(id, factory);
        RegistryObject<Item> item = ItemRegistry.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
        ENTRIES.add(new Entry(block, shape, base, inCreativeTab, null));
        ITEMS.add(item);
        return block;
    }

    public static List<Item> creativeItems() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < ENTRIES.size(); i++) {
            if (ENTRIES.get(i).inCreativeTab()) items.add(ITEMS.get(i).get());
        }
        items.add(CLAN_BANNER_ITEM.get());
        items.add(PARSIVAN_BANNER_ITEM.get());
        items.add(KHEMET_BANNER_ITEM.get());
        items.add(AUREUM_BANNER_ITEM.get());
        return items;
    }
}
