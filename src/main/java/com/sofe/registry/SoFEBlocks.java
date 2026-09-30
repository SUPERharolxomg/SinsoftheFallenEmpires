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

    /** @param base for stairs, slabs and walls: the full block whose texture they use */
    public record Entry(RegistryObject<Block> block, Shape shape, RegistryObject<Block> base, boolean inCreativeTab) {
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
        ENTRIES.add(new Entry(block, shape, base, inCreativeTab));
        ITEMS.add(item);
        return block;
    }

    public static List<Item> creativeItems() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < ENTRIES.size(); i++) {
            if (ENTRIES.get(i).inCreativeTab()) items.add(ITEMS.get(i).get());
        }
        return items;
    }
}
