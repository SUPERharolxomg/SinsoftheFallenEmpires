package com.sofe.registry;

import com.mojang.serialization.Codec;
import com.sofe.SoFEMod;
import com.sofe.world.region.AetherisBiomeSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import com.sofe.world.lock.SealVeilFeature;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

/** Code types used by the data-driven worldgen (the biomes and presets themselves are JSON from the data generators). */
public final class WorldgenRegistry {
    public static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, SoFEMod.MOD_ID);

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, SoFEMod.MOD_ID);
    public static final RegistryObject<SealVeilFeature> SEAL_VEIL = FEATURES.register("seal_veil", SealVeilFeature::new);

    static {
        BIOME_SOURCES.register("aetheris", () -> AetherisBiomeSource.CODEC);
    }

    private WorldgenRegistry() {
    }
}
