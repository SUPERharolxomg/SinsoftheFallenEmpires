package com.sofe.datagen;

import com.sofe.registry.BlockRegistry;
import com.sofe.registry.HerbRegistry;
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
        // herbs: a ripe crop gives its produce and 0-3 extra seeds; a wild plant gives produce and maybe seeds
        for (HerbRegistry.Herb herb : HerbRegistry.all()) {
            var ripe = net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition.hasBlockStateProperties(herb.crop().get())
                    .setProperties(net.minecraft.advancements.critereon.StatePropertiesPredicate.Builder.properties()
                            .hasProperty(net.minecraft.world.level.block.CropBlock.AGE, 7));
            add(herb.crop().get(), createCropDrops(herb.crop().get(), herb.produce().get(), herb.seeds().get(), ripe));
            add(herb.wild().get(), net.minecraft.world.level.storage.loot.LootTable.lootTable()
                    .withPool(net.minecraft.world.level.storage.loot.LootPool.lootPool()
                            .add(net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem(herb.produce().get())
                                    .apply(net.minecraft.world.level.storage.loot.functions.SetItemCountFunction.setCount(
                                            net.minecraft.world.level.storage.loot.providers.number.UniformGenerator.between(1, 2)))))
                    .withPool(net.minecraft.world.level.storage.loot.LootPool.lootPool()
                            .add(net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem(herb.seeds().get())
                                    .when(net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance(0.6f)))));
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
