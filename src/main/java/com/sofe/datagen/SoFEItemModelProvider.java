package com.sofe.datagen;

import com.sofe.SoFEMod;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
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
    }
}
