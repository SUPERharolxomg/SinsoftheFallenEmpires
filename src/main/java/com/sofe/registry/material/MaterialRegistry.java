package com.sofe.registry.material;

import com.sofe.registry.BlockRegistry;
import com.sofe.registry.ItemRegistry;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registers every form of every {@link Material} in one loop (Factory pattern,
 * docs/Arquitectura.md): adding a material or a form adds its block/item, model,
 * loot table and tags through the data generators.
 */
public final class MaterialRegistry {
    private static final Map<Material, Map<MaterialForm, RegistryObject<Block>>> BLOCKS = new EnumMap<>(Material.class);
    private static final Map<Material, Map<MaterialForm, RegistryObject<Item>>> ITEMS = new EnumMap<>(Material.class);
    private static boolean initialized;

    private MaterialRegistry() {
    }

    /** Adds the entries to the deferred registers; call once from the mod constructor. */
    public static void init() {
        if (initialized) return;
        initialized = true;
        for (Material material : Material.values()) {
            for (MaterialForm form : material.forms()) {
                String id = material.id(form);
                if (form.isBlock()) {
                    RegistryObject<Block> block = BlockRegistry.BLOCKS.register(id, () -> createBlock(material, form));
                    BLOCKS.computeIfAbsent(material, m -> new EnumMap<>(MaterialForm.class)).put(form, block);
                    RegistryObject<Item> item = ItemRegistry.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties()));
                    ITEMS.computeIfAbsent(material, m -> new EnumMap<>(MaterialForm.class)).put(form, item);
                } else {
                    RegistryObject<Item> item = ItemRegistry.ITEMS.register(id, () -> new Item(new Item.Properties()));
                    ITEMS.computeIfAbsent(material, m -> new EnumMap<>(MaterialForm.class)).put(form, item);
                }
            }
        }
    }

    public static Block block(Material material, MaterialForm form) {
        return entry(BLOCKS, material, form).get();
    }

    public static Item item(Material material, MaterialForm form) {
        return entry(ITEMS, material, form).get();
    }

    /** Every material item (block items included), in material and form order, for the creative tab. */
    public static List<Item> allItems() {
        List<Item> items = new ArrayList<>();
        ITEMS.values().forEach(forms -> forms.values().forEach(item -> items.add(item.get())));
        return items;
    }

    private static <T> RegistryObject<T> entry(Map<Material, Map<MaterialForm, RegistryObject<T>>> map, Material material, MaterialForm form) {
        Map<MaterialForm, RegistryObject<T>> forms = map.get(material);
        if (forms == null || !forms.containsKey(form)) {
            throw new IllegalArgumentException(material + " has no registered " + form);
        }
        return forms.get(form);
    }

    private static Block createBlock(Material material, MaterialForm form) {
        return switch (form) {
            case ORE -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_ORE), oreExperience(material));
            case DEEPSLATE_ORE -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_IRON_ORE), oreExperience(material));
            case RAW_BLOCK -> new Block(BlockBehaviour.Properties.copy(Blocks.RAW_IRON_BLOCK));
            case STORAGE_BLOCK -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK));
            default -> throw new IllegalArgumentException(form + " is not a block");
        };
    }

    /** Metal ores drop raw metal and give no XP, like iron; gem and shard ores give XP, like lapis. */
    private static IntProvider oreExperience(Material material) {
        return material.oreDrop() == MaterialForm.RAW ? ConstantInt.of(0) : UniformInt.of(2, 5);
    }
}
