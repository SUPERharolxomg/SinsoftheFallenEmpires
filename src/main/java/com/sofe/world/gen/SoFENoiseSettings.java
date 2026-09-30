package com.sofe.world.gen;

import com.sofe.SoFEMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;

/**
 * Terrain of the journey: vanilla overworld terrain with one extra surface rule.
 * Vanilla surface rules only give sand to the vanilla desert biome, so the Sulthari
 * desert needs its own rule (sand on top, sandstone below) or it would be grass.
 */
public final class SoFENoiseSettings {
    public static final ResourceKey<NoiseGeneratorSettings> AETHERIS =
            ResourceKey.create(Registries.NOISE_SETTINGS, SoFEMod.id("aetheris"));

    private SoFENoiseSettings() {
    }

    // disableMobGeneration() is deprecated by Mojang but still required by the record constructor; copied as is
    @SuppressWarnings("deprecation")
    public static void bootstrap(BootstapContext<NoiseGeneratorSettings> context) {
        NoiseGeneratorSettings vanilla = NoiseGeneratorSettings.overworld(context, false, false);

        SurfaceRules.RuleSource desert = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(SoFEBiomes.SULTHARI_DESERT),
                SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.state(Blocks.SAND.defaultBlockState())),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.state(Blocks.SANDSTONE.defaultBlockState())))));

        context.register(AETHERIS, new NoiseGeneratorSettings(
                vanilla.noiseSettings(),
                vanilla.defaultBlock(),
                vanilla.defaultFluid(),
                vanilla.noiseRouter(),
                SurfaceRules.sequence(desert, vanilla.surfaceRule()),
                vanilla.spawnTarget(),
                vanilla.seaLevel(),
                vanilla.disableMobGeneration(),
                vanilla.aquifersEnabled(),
                vanilla.oreVeinsEnabled(),
                vanilla.useLegacyRandomSource()));
    }
}
