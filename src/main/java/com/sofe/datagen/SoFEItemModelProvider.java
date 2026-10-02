package com.sofe.datagen;

import com.sofe.SoFEMod;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
import com.sofe.registry.HerbRegistry;
import com.sofe.registry.ItemRegistry;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/** Flat item models with textures/item/&lt;id&gt;.png (block items are made by the block state provider). */
public class SoFEItemModelProvider extends ItemModelProvider {

    public SoFEItemModelProvider(PackOutput output, ExistingFileHelper files) {
        super(output, SoFEMod.MOD_ID, files);
    }

    @Override
    protected void registerModels() {
        for (Material material : Material.values()) {
            for (MaterialForm form : material.forms()) {
                if (!form.isBlock()) {
                    basicItem(MaterialRegistry.item(material, form));
                }
            }
        }
        ItemRegistry.flatItems().forEach(item -> basicItem(item.get()));
        basicItem(HerbRegistry.MOUNTAIN_SAGE.get());
        for (HerbRegistry.Herb herb : HerbRegistry.all()) {
            basicItem(herb.seeds().get());
            withExistingParent("wild_" + herb.id(), mcLoc("item/generated")).texture("layer0", modLoc("block/wild_" + herb.id()));
        }
        var large = ItemRegistry.large();
        ItemRegistry.handheld().forEach(item -> withExistingParent(item.getId().getPath(),
                large.contains(item) ? modLoc("item/handheld_large") : mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/" + item.getId().getPath())));
        ItemRegistry.guns().forEach(item -> withExistingParent(item.getId().getPath(), modLoc("item/handheld_gun"))
                .texture("layer0", modLoc("item/" + item.getId().getPath())));
        ItemRegistry.spawnEggs().forEach(egg -> withExistingParent(egg.getId().getPath(), mcLoc("item/template_spawn_egg")));
    }
}
