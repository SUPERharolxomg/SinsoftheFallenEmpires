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
            () -> new WaystoneBlock(BlockBehaviour.Properties.copy(Blocks.LODESTONE).lightLevel(s -> 10).noOcclusion()));
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
    public static final RegistryObject<Block> NORDRATH_IRON_BRAZIER = cube("nordrath_iron_brazier",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).lightLevel(s -> 15)));
    public static final RegistryObject<Block> CORRUPTED_NORDRATH_RUNESTONE_BRICKS = cube("corrupted_nordrath_runestone_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS).mapColor(MapColor.COLOR_BLACK)));
    public static final RegistryObject<Block> CORRUPTED_NORDRATH_DARK_TIMBER = register("corrupted_nordrath_dark_timber", Shape.PILLAR, null, true,
            () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_STEM).lightLevel(s -> 6)));

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

    // --- Sprint 5.5: crafting stations, the Reward Coffer and Runestone
    public static final RegistryObject<Block> RUNESTONE = cube("runestone",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(MapColor.COLOR_LIGHT_GRAY)));
    public static final RegistryObject<Block> IMPERIAL_FORGE = register("imperial_forge", Shape.CUBE_SIDES, null, true,
            () -> new com.sofe.crafting.StationBlock(com.sofe.crafting.StationRecipe.Kind.IMPERIAL_FORGE,
                    BlockBehaviour.Properties.copy(Blocks.ANVIL).sound(SoundType.ANVIL).lightLevel(s -> 6)));
    public static final RegistryObject<Block> ALEMBIC = register("alembic", Shape.CUBE_SIDES, null, true,
            () -> new com.sofe.crafting.StationBlock(com.sofe.crafting.StationRecipe.Kind.ALEMBIC,
                    BlockBehaviour.Properties.copy(Blocks.BREWING_STAND).strength(2.0f).noOcclusion()));
    public static final RegistryObject<Block> REWARD_COFFER = register("reward_coffer", Shape.CUBE_SIDES, null, false,
            () -> new com.sofe.entity.boss.RewardCoffer.Block(BlockBehaviour.Properties.copy(Blocks.CHEST)
                    .strength(-1.0f, 3_600_000.0f).noLootTable().lightLevel(s -> 8)));

    public static final net.minecraftforge.registries.DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITIES =
            net.minecraftforge.registries.DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.BLOCK_ENTITY_TYPES, com.sofe.SoFEMod.MOD_ID);
    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<com.sofe.entity.boss.RewardCoffer.Entity>> REWARD_COFFER_ENTITY =
            BLOCK_ENTITIES.register("reward_coffer", com.sofe.entity.boss.RewardCoffer::type);

    /** The great banner of the clans: hand-written model and blockstate, so it is not in ENTRIES (no datagen). */
    public static final RegistryObject<Block> CLAN_BANNER = BlockRegistry.BLOCKS.register("clan_banner",
            () -> new com.sofe.block.ClanBanner(BlockBehaviour.Properties.copy(Blocks.RED_BANNER).noOcclusion().noLootTable()));
    public static final RegistryObject<Item> CLAN_BANNER_ITEM = ItemRegistry.ITEMS.register("clan_banner",
            () -> new BlockItem(CLAN_BANNER.get(), new Item.Properties()));
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
        return items;
    }
}
