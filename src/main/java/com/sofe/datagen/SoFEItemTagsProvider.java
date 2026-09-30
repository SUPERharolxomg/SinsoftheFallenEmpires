package com.sofe.datagen;

import com.sofe.SoFEMod;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Block item tags copied from the block tags, plus forge:ingots/..., forge:raw_materials/..., etc. */
public class SoFEItemTagsProvider extends ItemTagsProvider {

    public SoFEItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper files) {
        super(output, lookup, blockTags, SoFEMod.MOD_ID, files);
    }

    private static TagKey<Item> forgeTag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", path));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (Material material : Material.values()) {
            for (MaterialForm form : material.forms()) {
                String path = form.forgeTag(material.id());
                if (path == null) continue;
                if (form.isBlock()) {
                    copy(SoFEBlockTagsProvider.forgeTag(path), forgeTag(path));
                    copy(SoFEBlockTagsProvider.forgeTag(form.forgeTagFolder()), forgeTag(form.forgeTagFolder()));
                } else {
                    TagKey<Item> tag = forgeTag(path);
                    tag(tag).add(MaterialRegistry.item(material, form));
                    tag(forgeTag(form.forgeTagFolder())).addTag(tag);
                }
            }
        }
    }
}
