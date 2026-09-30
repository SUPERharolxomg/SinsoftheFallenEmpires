package com.sofe.datagen;

import com.sofe.SoFEMod;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
import net.minecraft.data.PackOutput;
import com.sofe.registry.SoFEBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
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
        for (SoFEBlocks.Entry entry : SoFEBlocks.entries()) {
            Block block = entry.block().get();
            String name = entry.block().getId().getPath();
            switch (entry.shape()) {
                case CUBE -> simpleBlockWithItem(block, cubeAll(block));
                case CUBE_SIDES -> simpleBlockWithItem(block, models().cubeBottomTop(name,
                        modLoc("block/" + name + "_side"), modLoc("block/" + name + "_bottom"), modLoc("block/" + name + "_top")));
                case PILLAR -> {
                    axisBlock((RotatedPillarBlock) block, modLoc("block/" + name), modLoc("block/" + name + "_end"));
                    simpleBlockItem(block, models().getExistingFile(modLoc("block/" + name)));
                }
                case STAIRS -> {
                    ResourceLocation texture = blockTexture(entry.base().get());
                    stairsBlock((StairBlock) block, texture);
                    simpleBlockItem(block, models().getExistingFile(modLoc("block/" + name)));
                }
                case SLAB -> {
                    ResourceLocation texture = blockTexture(entry.base().get());
                    slabBlock((SlabBlock) block, texture, texture);
                    simpleBlockItem(block, models().getExistingFile(modLoc("block/" + name)));
                }
                case WALL -> {
                    ResourceLocation texture = blockTexture(entry.base().get());
                    wallBlock((WallBlock) block, texture);
                    itemModels().wallInventory(name, texture);
                }
                case HAND_MADE -> simpleBlockWithItem(block, models().getExistingFile(modLoc("block/" + name)));
            }
        }
    }
}
