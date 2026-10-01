package com.sofe.datagen;

import com.sofe.SoFEMod;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
import com.sofe.gear.SoFETiers;
import com.sofe.registry.SoFEBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Mining tags (pickaxe + minimum tool) and the common Forge tags other mods use. */
public class SoFEBlockTagsProvider extends BlockTagsProvider {

    public SoFEBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
        super(output, lookup, SoFEMod.MOD_ID, files);
    }

    public static TagKey<Block> forgeTag(String path) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath("forge", path));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (Material material : Material.values()) {
            for (MaterialForm form : material.forms()) {
                if (!form.isBlock()) continue;
                Block block = MaterialRegistry.block(material, form);
                tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);

                if (form.isOre()) {
                    // the empire tool chain (docs/Mundo.md, W4): brass mines Glacial Iron and Star Lapis,
                    // Glacial Iron mines Solar Gold; Orichalcum and Aetherium wait for their own tiers
                    tag(switch (material) {
                        case GLACIAL_IRON, STAR_LAPIS -> SoFETiers.NEEDS_BRASS_TOOL;
                        case SOLAR_GOLD -> SoFETiers.NEEDS_GLACIAL_IRON_TOOL;
                        default -> switch (material.miningLevel()) {
                            case STONE -> BlockTags.NEEDS_STONE_TOOL;
                            case IRON -> BlockTags.NEEDS_IRON_TOOL;
                            case DIAMOND -> BlockTags.NEEDS_DIAMOND_TOOL;
                        };
                    }).add(block);
                    tag(form == MaterialForm.ORE ? Tags.Blocks.ORES_IN_GROUND_STONE : Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(block);
                    TagKey<Block> ores = forgeTag(form.forgeTag(material.id()));
                    tag(ores).add(block);
                    tag(Tags.Blocks.ORES).addTag(ores);
                } else {
                    // Storage blocks need a stone pickaxe, like vanilla iron blocks
                    tag(BlockTags.NEEDS_STONE_TOOL).add(block);
                    TagKey<Block> storage = forgeTag(form.forgeTag(material.id()));
                    tag(storage).add(block);
                    tag(Tags.Blocks.STORAGE_BLOCKS).addTag(storage);
                }
            }
        }
        for (SoFEBlocks.Entry entry : SoFEBlocks.entries()) {
            Block block = entry.block().get();
            if (block == SoFEBlocks.SEAL_VEIL.get() || block == SoFEBlocks.SEALED_GATE.get()) continue; // unbreakable
            boolean wood = block == SoFEBlocks.NORDRATH_DARK_TIMBER.get() || block == SoFEBlocks.NORDRATH_DARK_PLANKS.get()
                    || block == SoFEBlocks.CORRUPTED_NORDRATH_DARK_TIMBER.get();
            tag(wood ? BlockTags.MINEABLE_WITH_AXE : BlockTags.MINEABLE_WITH_PICKAXE).add(block);
            if (entry.oreDrop() != null) tag(BlockTags.NEEDS_IRON_TOOL).add(block);
            switch (entry.shape()) {
                case STAIRS -> tag(BlockTags.STAIRS).add(block);
                case SLAB -> tag(BlockTags.SLABS).add(block);
                case WALL -> tag(BlockTags.WALLS).add(block);
                default -> {
                }
            }
        }
        tag(BlockTags.DRAGON_IMMUNE).add(SoFEBlocks.SEAL_VEIL.get(), SoFEBlocks.SEALED_GATE.get());
        tag(BlockTags.WITHER_IMMUNE).add(SoFEBlocks.SEAL_VEIL.get(), SoFEBlocks.SEALED_GATE.get());
    }
}
