package com.sofe.datagen;

import com.sofe.SoFEMod;
import com.sofe.world.gen.SoFEBiomes;
import com.sofe.world.region.Region;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * sofe:is_aetheris and one tag per region (sofe:is_sulthari, ...). Ores and features select
 * their region through these tags (docs/Mundo.md, W4). The biomes are left out of every
 * vanilla structure tag on purpose.
 */
public class SoFEBiomeTagsProvider extends BiomeTagsProvider {
    public static final TagKey<Biome> IS_AETHERIS = TagKey.create(Registries.BIOME, SoFEMod.id("is_aetheris"));

    public SoFEBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
        super(output, lookup, SoFEMod.MOD_ID, files);
    }

    public static TagKey<Biome> regionTag(Region region) {
        return TagKey.create(Registries.BIOME, SoFEMod.id("is_" + region.id()));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        SoFEBiomes.BY_REGION.forEach((region, biome) -> {
            tag(IS_AETHERIS).add(biome);
            tag(regionTag(region)).add(biome);
        });
    }
}
