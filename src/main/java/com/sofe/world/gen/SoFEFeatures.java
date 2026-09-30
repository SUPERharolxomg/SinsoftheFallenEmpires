package com.sofe.world.gen;

import com.sofe.SoFEMod;
import com.sofe.datagen.SoFEBiomeTagsProvider;
import com.sofe.registry.WorldgenRegistry;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/** The Seal Veil feature and the biome modifier that runs it in every journey biome. */
public final class SoFEFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SEAL_VEIL_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, SoFEMod.id("seal_veil"));
    public static final ResourceKey<PlacedFeature> SEAL_VEIL_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, SoFEMod.id("seal_veil"));
    public static final ResourceKey<BiomeModifier> ADD_SEAL_VEIL =
            ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, SoFEMod.id("add_seal_veil"));

    private SoFEFeatures() {
    }

    public static void configured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        context.register(SEAL_VEIL_CONFIGURED, new ConfiguredFeature<>(WorldgenRegistry.SEAL_VEIL.get(), NoneFeatureConfiguration.INSTANCE));
    }

    /** No placement modifiers: it runs once per chunk and walks the chunk's columns itself. */
    public static void placed(BootstapContext<PlacedFeature> context) {
        context.register(SEAL_VEIL_PLACED, new PlacedFeature(
                context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(SEAL_VEIL_CONFIGURED), List.of()));
    }

    public static void biomeModifiers(BootstapContext<BiomeModifier> context) {
        context.register(ADD_SEAL_VEIL, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                context.lookup(Registries.BIOME).getOrThrow(SoFEBiomeTagsProvider.IS_AETHERIS),
                HolderSet.direct(context.lookup(Registries.PLACED_FEATURE).getOrThrow(SEAL_VEIL_PLACED)),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION));
    }
}
