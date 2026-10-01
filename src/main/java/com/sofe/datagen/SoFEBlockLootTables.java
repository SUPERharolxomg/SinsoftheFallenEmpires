package com.sofe.datagen;

import com.sofe.registry.BlockRegistry;
import com.sofe.registry.SoFEBlocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

/** Ores drop their raw metal, gem or shard (Fortune applies; Silk Touch drops the ore itself); other blocks drop themselves. */
public class SoFEBlockLootTables extends BlockLootSubProvider {

    public SoFEBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        for (Material material : Material.values()) {
            for (MaterialForm form : material.forms()) {
                if (!form.isBlock()) continue;
                Block block = MaterialRegistry.block(material, form);
                if (form.isOre()) {
                    add(block, createOreDrop(block, MaterialRegistry.item(material, material.oreDrop())));
                } else {
                    dropSelf(block);
                }
            }
        }
        for (SoFEBlocks.Entry entry : SoFEBlocks.entries()) {
            Block block = entry.block().get();
            if (block.getLootTable().equals(BuiltInLootTables.EMPTY)) continue; // the Seal Veil drops nothing
            if (entry.oreDrop() != null) {
                add(block, createOreDrop(block, entry.oreDrop().get()));
            } else if (entry.shape() == SoFEBlocks.Shape.SLAB) {
                add(block, createSlabItemTable(block));
            } else {
                dropSelf(block);
            }
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BlockRegistry.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
