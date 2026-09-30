package com.sofe.datagen;

import com.sofe.SoFEMod;
import com.sofe.world.gen.SoFEBiomes;
import com.sofe.world.gen.SoFENoiseSettings;
import com.sofe.world.gen.SoFEWorldPresets;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Writes the journey's biomes, terrain settings and world preset as datapack JSON. */
public class SoFEWorldgenProvider extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.BIOME, SoFEBiomes::bootstrap)
            .add(Registries.NOISE_SETTINGS, SoFENoiseSettings::bootstrap)
            .add(Registries.WORLD_PRESET, SoFEWorldPresets::bootstrap);

    public SoFEWorldgenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, BUILDER, Set.of(SoFEMod.MOD_ID));
    }
}
