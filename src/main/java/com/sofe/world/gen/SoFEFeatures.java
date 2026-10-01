package com.sofe.world.gen;

import com.sofe.SoFEMod;
import com.sofe.datagen.SoFEBiomeTagsProvider;
import com.sofe.registry.SoFEBlocks;
import com.sofe.registry.WorldgenRegistry;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Features added to existing biomes: the Seal Veil (every journey biome, and every Nether biome for
 * the Burning Deep at 1:8) and the Nether ores of the Burning Deep (docs/Anexos.md, A3): Infernal
 * Ember in the basalt deltas, Wailing Soul in the soul sand valleys.
 */
public final class SoFEFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SEAL_VEIL_CONFIGURED = configuredKey("seal_veil");
    public static final ResourceKey<PlacedFeature> SEAL_VEIL_PLACED = placedKey("seal_veil");
    public static final ResourceKey<BiomeModifier> ADD_SEAL_VEIL = modifierKey("add_seal_veil");
    public static final ResourceKey<BiomeModifier> ADD_SEAL_VEIL_NETHER = modifierKey("add_seal_veil_nether");

    public static final ResourceKey<ConfiguredFeature<?, ?>> INFERNAL_EMBER_ORE_CONFIGURED = configuredKey("infernal_ember_ore");
    public static final ResourceKey<PlacedFeature> INFERNAL_EMBER_ORE_PLACED = placedKey("infernal_ember_ore");
    public static final ResourceKey<BiomeModifier> ADD_INFERNAL_EMBER_ORE = modifierKey("add_infernal_ember_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> WAILING_SOUL_ORE_CONFIGURED = configuredKey("wailing_soul_ore");
    public static final ResourceKey<PlacedFeature> WAILING_SOUL_ORE_PLACED = placedKey("wailing_soul_ore");
    public static final ResourceKey<BiomeModifier> ADD_WAILING_SOUL_ORE = modifierKey("add_wailing_soul_ore");

    private SoFEFeatures() {
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configuredKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, SoFEMod.id(name));
    }

    private static ResourceKey<PlacedFeature> placedKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, SoFEMod.id(name));
    }

    private static ResourceKey<BiomeModifier> modifierKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, SoFEMod.id(name));
    }

    public static void configured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        context.register(SEAL_VEIL_CONFIGURED, new ConfiguredFeature<>(WorldgenRegistry.SEAL_VEIL.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(INFERNAL_EMBER_ORE_CONFIGURED, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(new BlockMatchTest(Blocks.BASALT), SoFEBlocks.INFERNAL_EMBER_ORE.get().defaultBlockState()),
                OreConfiguration.target(new BlockMatchTest(Blocks.BLACKSTONE), SoFEBlocks.INFERNAL_EMBER_ORE.get().defaultBlockState())), 6)));
        context.register(WAILING_SOUL_ORE_CONFIGURED, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(new BlockMatchTest(Blocks.SOUL_SOIL), SoFEBlocks.WAILING_SOUL_ORE.get().defaultBlockState()),
                OreConfiguration.target(new BlockMatchTest(Blocks.SOUL_SAND), SoFEBlocks.WAILING_SOUL_ORE.get().defaultBlockState())), 5)));
    }

    public static void placed(BootstapContext<PlacedFeature> context) {
        var configured = context.lookup(Registries.CONFIGURED_FEATURE);
        // no placement modifiers: it runs once per chunk and walks the chunk's columns itself
        context.register(SEAL_VEIL_PLACED, new PlacedFeature(configured.getOrThrow(SEAL_VEIL_CONFIGURED), List.of()));
        context.register(INFERNAL_EMBER_ORE_PLACED, new PlacedFeature(configured.getOrThrow(INFERNAL_EMBER_ORE_CONFIGURED), List.of(
                CountPlacement.of(12), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(118)), BiomeFilter.biome())));
        context.register(WAILING_SOUL_ORE_PLACED, new PlacedFeature(configured.getOrThrow(WAILING_SOUL_ORE_CONFIGURED), List.of(
                CountPlacement.of(10), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(118)), BiomeFilter.biome())));
    }

    public static void biomeModifiers(BootstapContext<BiomeModifier> context) {
        var biomes = context.lookup(Registries.BIOME);
        var placed = context.lookup(Registries.PLACED_FEATURE);
        context.register(ADD_SEAL_VEIL, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(SoFEBiomeTagsProvider.IS_AETHERIS), HolderSet.direct(placed.getOrThrow(SEAL_VEIL_PLACED)),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION));
        // in worlds that are not journeys the feature finds no region layout and places nothing
        context.register(ADD_SEAL_VEIL_NETHER, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER), HolderSet.direct(placed.getOrThrow(SEAL_VEIL_PLACED)),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION));
        context.register(ADD_INFERNAL_EMBER_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.BASALT_DELTAS)), HolderSet.direct(placed.getOrThrow(INFERNAL_EMBER_ORE_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_WAILING_SOUL_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.SOUL_SAND_VALLEY)), HolderSet.direct(placed.getOrThrow(WAILING_SOUL_ORE_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
    }
}
