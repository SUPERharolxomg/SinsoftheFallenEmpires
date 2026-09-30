package com.sofe.registry;

import com.mojang.serialization.Codec;
import com.sofe.SoFEMod;
import com.sofe.world.region.AetherisBiomeSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraftforge.registries.DeferredRegister;

/** Code types used by the data-driven worldgen (the biomes and presets themselves are JSON from the data generators). */
public final class WorldgenRegistry {
    public static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, SoFEMod.MOD_ID);

    static {
        BIOME_SOURCES.register("aetheris", () -> AetherisBiomeSource.CODEC);
    }

    private WorldgenRegistry() {
    }
}
