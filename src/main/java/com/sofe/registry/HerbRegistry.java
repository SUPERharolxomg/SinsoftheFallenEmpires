package com.sofe.registry;

import com.sofe.item.Herbs;
import com.sofe.world.region.Region;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The herbs (docs/Mundo.md, W4, "Secondary materials and herbs"): Mountain Sage in Sulthari,
 * Pomegranate in Parsivan and Desert Lotus in Khemet. Each has a wild plant, seeds and a crop.
 */
public final class HerbRegistry {

    /**
     * One herb.
     *
     * @param id      e.g. "mountain_sage"
     * @param produce what the herb gives (the fruit or the leaves)
     * @param region  where the wild plant grows
     */
    public record Herb(String id, RegistryObject<Item> produce, RegistryObject<Block> wild, RegistryObject<Block> crop,
                       RegistryObject<Item> seeds, RegistryObject<Item> wildItem, Region region) {
    }

    private static final List<Herb> HERBS = new ArrayList<>();

    public static final RegistryObject<Item> MOUNTAIN_SAGE = ItemRegistry.ITEMS.register("mountain_sage", () -> new Item(new Item.Properties()));

    public static final Herb SAGE = herb("mountain_sage", MOUNTAIN_SAGE, Region.SULTHARI);
    public static final Herb POMEGRANATE = herb("pomegranate", ItemRegistry.POMEGRANATE, Region.PARSIVAN);
    public static final Herb LOTUS = herb("desert_lotus", ItemRegistry.DESERT_LOTUS, Region.KHEMET);

    private HerbRegistry() {
    }

    /** Forces the class to load so its entries reach the deferred registers; call from the mod constructor. */
    public static void init() {
    }

    public static List<Herb> all() {
        return List.copyOf(HERBS);
    }

    private static Herb herb(String id, RegistryObject<Item> produce, Region region) {
        RegistryObject<Item>[] seedsHolder = new RegistryObject[1];
        RegistryObject<Block> crop = BlockRegistry.BLOCKS.register(id + "_crop",
                () -> new Herbs.Crop(() -> seedsHolder[0].get(), BlockBehaviour.Properties.copy(Blocks.WHEAT)));
        RegistryObject<Item> seeds = ItemRegistry.ITEMS.register(id + "_seeds", () -> new ItemNameBlockItem(crop.get(), new Item.Properties()));
        seedsHolder[0] = seeds;
        RegistryObject<Block> wild = BlockRegistry.BLOCKS.register("wild_" + id,
                () -> new Herbs.Wild(BlockBehaviour.Properties.copy(Blocks.DANDELION)));
        RegistryObject<Item> wildItem = ItemRegistry.ITEMS.register("wild_" + id, () -> new BlockItem(wild.get(), new Item.Properties()));
        Herb herb = new Herb(id, produce, wild, crop, seeds, wildItem, region);
        HERBS.add(herb);
        return herb;
    }

    public static List<Item> creativeItems() {
        List<Item> items = new ArrayList<>();
        items.add(MOUNTAIN_SAGE.get());
        for (Herb h : HERBS) {
            items.add(h.seeds().get());
            items.add(h.wildItem().get());
        }
        return items;
    }
}
