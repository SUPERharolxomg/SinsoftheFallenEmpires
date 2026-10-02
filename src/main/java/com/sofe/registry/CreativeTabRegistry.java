package com.sofe.registry;

import com.sofe.SoFEMod;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class CreativeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SoFEMod.MOD_ID);

    /** Weapons, tools, armor, shields and jewelry: everything a Bearer wears or wields. */
    public static final RegistryObject<CreativeModeTab> EQUIPMENT = TABS.register("equipment", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sofe.equipment"))
            .icon(() -> new ItemStack(ItemRegistry.KALETH_BLADE.get()))
            .displayItems((parameters, output) -> ItemRegistry.equipment().forEach(output::accept))
            .build());

    /** Ores, raw metals, ingots, gems, storage blocks and the materials of monsters. */
    public static final RegistryObject<CreativeModeTab> MATERIALS = TABS.register("materials", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sofe.materials"))
            .icon(() -> new ItemStack(MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)))
            .withTabsBefore(EQUIPMENT.getId())
            .displayItems((parameters, output) -> {
                MaterialRegistry.allItems().forEach(output::accept);
                ItemRegistry.creatureMaterials().forEach(output::accept);
            })
            .build());

    /** The building blocks of the empires, the stations and the story's blocks. */
    public static final RegistryObject<CreativeModeTab> BLOCKS = TABS.register("blocks", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sofe.blocks"))
            .icon(() -> new ItemStack(SoFEBlocks.CLAN_BANNER_ITEM.get()))
            .withTabsBefore(MATERIALS.getId())
            .displayItems((parameters, output) -> SoFEBlocks.creativeItems().forEach(output::accept))
            .build());

    /** Potions, food, herbs and seeds, scrolls, Blueprints and the story's items. */
    public static final RegistryObject<CreativeModeTab> CONSUMABLES = TABS.register("consumables", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sofe.consumables"))
            .icon(() -> new ItemStack(ItemRegistry.BEARERS_FLASK.get()))
            .withTabsBefore(BLOCKS.getId())
            .displayItems((parameters, output) -> {
                ItemRegistry.consumables().forEach(output::accept);
                HerbRegistry.creativeItems().forEach(output::accept);
            })
            .build());

    /** The spawn eggs of the creatures and bosses. */
    public static final RegistryObject<CreativeModeTab> CREATURES = TABS.register("creatures", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sofe.creatures"))
            .icon(() -> new ItemStack(ItemRegistry.VORATH_SPAWN_EGG.get()))
            .withTabsBefore(CONSUMABLES.getId())
            .displayItems((parameters, output) -> ItemRegistry.creatures().forEach(output::accept))
            .build());

    private CreativeTabRegistry() {
    }
}
