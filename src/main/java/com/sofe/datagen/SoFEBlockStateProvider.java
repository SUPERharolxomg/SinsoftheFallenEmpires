package com.sofe.datagen;

import com.sofe.SoFEMod;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/** Block states, block models and block item models: a plain cube with textures/block/&lt;id&gt;.png. */
public class SoFEBlockStateProvider extends BlockStateProvider {

    public SoFEBlockStateProvider(PackOutput output, ExistingFileHelper files) {
        super(output, SoFEMod.MOD_ID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        for (Material material : Material.values()) {
            for (MaterialForm form : material.forms()) {
                if (form.isBlock()) {
                    Block block = MaterialRegistry.block(material, form);
                    simpleBlockWithItem(block, cubeAll(block));
                }
            }
        }
    }
}
