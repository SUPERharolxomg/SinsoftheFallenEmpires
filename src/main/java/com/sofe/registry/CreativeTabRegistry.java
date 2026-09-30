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

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sofe"))
            .icon(() -> new ItemStack(MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)))
            .displayItems((parameters, output) -> {
                MaterialRegistry.allItems().forEach(output::accept);
                SoFEBlocks.creativeItems().forEach(output::accept);
                ItemRegistry.creativeItems().forEach(output::accept);
            })
            .build());

    private CreativeTabRegistry() {
    }
}
