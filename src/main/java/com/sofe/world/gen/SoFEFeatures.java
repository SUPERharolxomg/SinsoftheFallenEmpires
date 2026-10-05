package com.sofe.world.gen;

import com.sofe.SoFEMod;
import com.sofe.datagen.SoFEBiomeTagsProvider;
import com.sofe.registry.HerbRegistry;
import com.sofe.registry.SoFEBlocks;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
import com.sofe.world.region.Region;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
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
 * Features added to existing biomes: the empire ores (Sulthari Brass, Glacial Iron, Star Lapis, Solar Gold,
 * Orichalcum, Raw Aetherium deep everywhere), Runestone and Imperial Marble by region (docs/Mundo.md, W4), the Seal Veil (every journey biome, and every Nether biome for
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

    /** The empire ores of Acts I and II and Runestone (docs/Mundo.md, W4). */
    public static final ResourceKey<ConfiguredFeature<?, ?>> BRASS_ORE_CONFIGURED = configuredKey("sulthari_brass_ore");
    public static final ResourceKey<PlacedFeature> BRASS_ORE_PLACED = placedKey("sulthari_brass_ore");
    public static final ResourceKey<BiomeModifier> ADD_BRASS_ORE = modifierKey("add_sulthari_brass_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GLACIAL_IRON_ORE_CONFIGURED = configuredKey("glacial_iron_ore");
    public static final ResourceKey<PlacedFeature> GLACIAL_IRON_ORE_PLACED = placedKey("glacial_iron_ore");
    public static final ResourceKey<BiomeModifier> ADD_GLACIAL_IRON_ORE = modifierKey("add_glacial_iron_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RUNESTONE_CONFIGURED = configuredKey("runestone");
    public static final ResourceKey<PlacedFeature> RUNESTONE_PLACED = placedKey("runestone");
    public static final ResourceKey<BiomeModifier> ADD_RUNESTONE = modifierKey("add_runestone");

    /** The empire ores of Acts III and IV, Raw Aetherium deep in every region, and Imperial Marble (docs/Mundo.md, W4). */
    public static final ResourceKey<ConfiguredFeature<?, ?>> STAR_LAPIS_ORE_CONFIGURED = configuredKey("star_lapis_ore");
    public static final ResourceKey<PlacedFeature> STAR_LAPIS_ORE_PLACED = placedKey("star_lapis_ore");
    public static final ResourceKey<BiomeModifier> ADD_STAR_LAPIS_ORE = modifierKey("add_star_lapis_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SOLAR_GOLD_ORE_CONFIGURED = configuredKey("solar_gold_ore");
    public static final ResourceKey<PlacedFeature> SOLAR_GOLD_ORE_PLACED = placedKey("solar_gold_ore");
    public static final ResourceKey<BiomeModifier> ADD_SOLAR_GOLD_ORE = modifierKey("add_solar_gold_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORICHALCUM_ORE_CONFIGURED = configuredKey("orichalcum_ore");
    public static final ResourceKey<PlacedFeature> ORICHALCUM_ORE_PLACED = placedKey("orichalcum_ore");
    public static final ResourceKey<BiomeModifier> ADD_ORICHALCUM_ORE = modifierKey("add_orichalcum_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> AETHERIUM_ORE_CONFIGURED = configuredKey("aetherium_ore");
    public static final ResourceKey<PlacedFeature> AETHERIUM_ORE_PLACED = placedKey("aetherium_ore");
    public static final ResourceKey<BiomeModifier> ADD_AETHERIUM_ORE = modifierKey("add_aetherium_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> IMPERIAL_MARBLE_CONFIGURED = configuredKey("imperial_marble");
    public static final ResourceKey<PlacedFeature> IMPERIAL_MARBLE_PLACED = placedKey("imperial_marble");
    public static final ResourceKey<BiomeModifier> ADD_IMPERIAL_MARBLE = modifierKey("add_imperial_marble");
    public static final ResourceKey<ConfiguredFeature<?, ?>> VOID_CRYSTAL_ORE_CONFIGURED = configuredKey("void_crystal_ore");
    public static final ResourceKey<PlacedFeature> VOID_CRYSTAL_ORE_PLACED = placedKey("void_crystal_ore");
    public static final ResourceKey<BiomeModifier> ADD_VOID_CRYSTAL_ORE = modifierKey("add_void_crystal_ore");

    private SoFEFeatures() {
    }

    private static BlockState block(Material material, MaterialForm form) {
        return MaterialRegistry.block(material, form).defaultBlockState();
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

    public static ResourceKey<ConfiguredFeature<?, ?>> herbConfigured(HerbRegistry.Herb herb) {
        return configuredKey("wild_" + herb.id());
    }

    public static ResourceKey<PlacedFeature> herbPlaced(HerbRegistry.Herb herb) {
        return placedKey("wild_" + herb.id());
    }

    public static void configured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        for (HerbRegistry.Herb herb : HerbRegistry.all()) {
            context.register(herbConfigured(herb), new ConfiguredFeature<>(Feature.RANDOM_PATCH,
                    net.minecraft.data.worldgen.features.FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
                            new net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration(
                                    net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider.simple(herb.wild().get())))));
        }
        var stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        var deepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        context.register(BRASS_ORE_CONFIGURED, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(stone, block(Material.SULTHARI_BRASS, MaterialForm.ORE))), 8)));
        context.register(GLACIAL_IRON_ORE_CONFIGURED, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(stone, block(Material.GLACIAL_IRON, MaterialForm.ORE)),
                OreConfiguration.target(deepslate, block(Material.GLACIAL_IRON, MaterialForm.DEEPSLATE_ORE))), 7)));
        context.register(RUNESTONE_CONFIGURED, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(stone, SoFEBlocks.RUNESTONE.get().defaultBlockState())), 33)));
        context.register(STAR_LAPIS_ORE_CONFIGURED, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(stone, block(Material.STAR_LAPIS, MaterialForm.ORE))), 4)));
        context.register(SOLAR_GOLD_ORE_CONFIGURED, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(stone, block(Material.SOLAR_GOLD, MaterialForm.ORE)),
                OreConfiguration.target(deepslate, block(Material.SOLAR_GOLD, MaterialForm.DEEPSLATE_ORE))), 5)));
        context.register(ORICHALCUM_ORE_CONFIGURED, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(deepslate, block(Material.ORICHALCUM, MaterialForm.DEEPSLATE_ORE))), 4, 0.3f)));
        context.register(AETHERIUM_ORE_CONFIGURED, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(deepslate, block(Material.AETHERIUM, MaterialForm.DEEPSLATE_ORE))), 3, 0.5f)));
        context.register(IMPERIAL_MARBLE_CONFIGURED, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(stone, SoFEBlocks.IMPERIAL_MARBLE.get().defaultBlockState())), 48)));
        context.register(VOID_CRYSTAL_ORE_CONFIGURED, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(new BlockMatchTest(Blocks.END_STONE), SoFEBlocks.VOID_CRYSTAL_ORE.get().defaultBlockState())), 5)));
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
        for (HerbRegistry.Herb herb : HerbRegistry.all()) {
            context.register(herbPlaced(herb), new PlacedFeature(configured.getOrThrow(herbConfigured(herb)), List.of(
                    net.minecraft.world.level.levelgen.placement.RarityFilter.onAverageOnceEvery(12), InSquarePlacement.spread(),
                    net.minecraft.data.worldgen.placement.PlacementUtils.HEIGHTMAP, BiomeFilter.biome())));
        }
        // no placement modifiers: it runs once per chunk and walks the chunk's columns itself
        context.register(SEAL_VEIL_PLACED, new PlacedFeature(configured.getOrThrow(SEAL_VEIL_CONFIGURED), List.of()));
        context.register(BRASS_ORE_PLACED, new PlacedFeature(configured.getOrThrow(BRASS_ORE_CONFIGURED), List.of(
                CountPlacement.of(10), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(40), VerticalAnchor.absolute(120)), BiomeFilter.biome())));
        context.register(GLACIAL_IRON_ORE_PLACED, new PlacedFeature(configured.getOrThrow(GLACIAL_IRON_ORE_CONFIGURED), List.of(
                CountPlacement.of(6), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(48)), BiomeFilter.biome())));
        context.register(RUNESTONE_PLACED, new PlacedFeature(configured.getOrThrow(RUNESTONE_CONFIGURED), List.of(
                CountPlacement.of(2), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(50), VerticalAnchor.absolute(90)), BiomeFilter.biome())));
        context.register(STAR_LAPIS_ORE_PLACED, new PlacedFeature(configured.getOrThrow(STAR_LAPIS_ORE_CONFIGURED), List.of(
                CountPlacement.of(4), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(80), VerticalAnchor.absolute(200)), BiomeFilter.biome())));
        context.register(SOLAR_GOLD_ORE_PLACED, new PlacedFeature(configured.getOrThrow(SOLAR_GOLD_ORE_CONFIGURED), List.of(
                CountPlacement.of(3), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(32)), BiomeFilter.biome())));
        context.register(ORICHALCUM_ORE_PLACED, new PlacedFeature(configured.getOrThrow(ORICHALCUM_ORE_CONFIGURED), List.of(
                CountPlacement.of(2), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-16)), BiomeFilter.biome())));
        // the Void Rifts' aetherium: rare, very deep, in every region (one vein in about eight chunks)
        context.register(AETHERIUM_ORE_PLACED, new PlacedFeature(configured.getOrThrow(AETHERIUM_ORE_CONFIGURED), List.of(
                net.minecraft.world.level.levelgen.placement.RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-40)), BiomeFilter.biome())));
        context.register(IMPERIAL_MARBLE_PLACED, new PlacedFeature(configured.getOrThrow(IMPERIAL_MARBLE_CONFIGURED), List.of(
                CountPlacement.of(3), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(50), VerticalAnchor.absolute(100)), BiomeFilter.biome())));
        context.register(VOID_CRYSTAL_ORE_PLACED, new PlacedFeature(configured.getOrThrow(VOID_CRYSTAL_ORE_CONFIGURED), List.of(
                CountPlacement.of(8), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(20), VerticalAnchor.absolute(60)), BiomeFilter.biome())));
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
        for (HerbRegistry.Herb herb : HerbRegistry.all()) {
            context.register(modifierKey("add_wild_" + herb.id()), new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                    biomes.getOrThrow(SoFEBiomeTagsProvider.regionTag(herb.region())),
                    HolderSet.direct(placed.getOrThrow(herbPlaced(herb))), GenerationStep.Decoration.VEGETAL_DECORATION));
        }
        context.register(ADD_SEAL_VEIL, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(SoFEBiomeTagsProvider.IS_AETHERIS), HolderSet.direct(placed.getOrThrow(SEAL_VEIL_PLACED)),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION));
        // in worlds that are not journeys the feature finds no region layout and places nothing
        context.register(ADD_SEAL_VEIL_NETHER, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER), HolderSet.direct(placed.getOrThrow(SEAL_VEIL_PLACED)),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION));
        context.register(ADD_BRASS_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(SoFEBiomeTagsProvider.regionTag(Region.SULTHARI)),
                HolderSet.direct(placed.getOrThrow(BRASS_ORE_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_GLACIAL_IRON_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(SoFEBiomeTagsProvider.regionTag(Region.NORDRATH)),
                HolderSet.direct(placed.getOrThrow(GLACIAL_IRON_ORE_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_RUNESTONE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(SoFEBiomeTagsProvider.regionTag(Region.NORDRATH)),
                HolderSet.direct(placed.getOrThrow(RUNESTONE_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_STAR_LAPIS_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(SoFEBiomeTagsProvider.regionTag(Region.PARSIVAN)),
                HolderSet.direct(placed.getOrThrow(STAR_LAPIS_ORE_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_SOLAR_GOLD_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(SoFEBiomeTagsProvider.regionTag(Region.KHEMET)),
                HolderSet.direct(placed.getOrThrow(SOLAR_GOLD_ORE_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_ORICHALCUM_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(SoFEBiomeTagsProvider.regionTag(Region.AUREUM)),
                HolderSet.direct(placed.getOrThrow(ORICHALCUM_ORE_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_AETHERIUM_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(SoFEBiomeTagsProvider.IS_AETHERIS),
                HolderSet.direct(placed.getOrThrow(AETHERIUM_ORE_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_IMPERIAL_MARBLE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(SoFEBiomeTagsProvider.regionTag(Region.AUREUM)),
                HolderSet.direct(placed.getOrThrow(IMPERIAL_MARBLE_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));
        // the outer islands only: the dragon's island (the_end) stays as it is
        context.register(ADD_VOID_CRYSTAL_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.END_HIGHLANDS), biomes.getOrThrow(Biomes.END_MIDLANDS), biomes.getOrThrow(Biomes.END_BARRENS),
                        biomes.getOrThrow(Biomes.SMALL_END_ISLANDS)),
                HolderSet.direct(placed.getOrThrow(VOID_CRYSTAL_ORE_PLACED)), GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_INFERNAL_EMBER_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.BASALT_DELTAS)), HolderSet.direct(placed.getOrThrow(INFERNAL_EMBER_ORE_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_WAILING_SOUL_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.SOUL_SAND_VALLEY)), HolderSet.direct(placed.getOrThrow(WAILING_SOUL_ORE_PLACED)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
    }
}
