package com.sofe.world.gen;

import com.sofe.SoFEMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.Noises;
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

        // Ashen Wastes: coarse dirt and gravel over sandstone, with patches of smooth basalt
        SurfaceRules.RuleSource wastes = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(SoFEBiomes.ASHEN_WASTES),
                SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.25),
                                        SurfaceRules.state(Blocks.SMOOTH_BASALT.defaultBlockState())),
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.GRAVEL, 0.0),
                                        SurfaceRules.state(Blocks.GRAVEL.defaultBlockState())),
                                SurfaceRules.state(Blocks.COARSE_DIRT.defaultBlockState()))),
                        SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.state(Blocks.SANDSTONE.defaultBlockState())))));

        context.register(AETHERIS, new NoiseGeneratorSettings(
                vanilla.noiseSettings(),
                vanilla.defaultBlock(),
                vanilla.defaultFluid(),
                vanilla.noiseRouter(),
                SurfaceRules.sequence(desert, wastes, vanilla.surfaceRule()),
                vanilla.spawnTarget(),
                vanilla.seaLevel(),
                vanilla.disableMobGeneration(),
                vanilla.aquifersEnabled(),
                vanilla.oreVeinsEnabled(),
                vanilla.useLegacyRandomSource()));
    }
}
