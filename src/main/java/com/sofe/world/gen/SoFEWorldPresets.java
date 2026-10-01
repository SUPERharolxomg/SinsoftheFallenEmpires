package com.sofe.world.gen;

import com.sofe.SoFEMod;
import com.sofe.world.region.AetherisBiomeSource;
import com.sofe.world.region.Region;
import com.sofe.world.region.RegionMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The sofe:aetheris world preset: the journey. Overworld with the fixed region layout;
 * vanilla Nether and End (they open with the story, docs/Mundo.md W5).
 */
public final class SoFEWorldPresets {
    public static final ResourceKey<WorldPreset> AETHERIS = ResourceKey.create(Registries.WORLD_PRESET, SoFEMod.id("aetheris"));
    /** The city and its desert plateau fill the middle of Sulthari; the Ashen Wastes start about here. */
    public static final int ASHEN_WASTES_INNER_RADIUS = 800;

    private SoFEWorldPresets() {
    }

    /** Nordrath: the Burning Citadel and the Forge stand on volcanic ground, glaciers to the west and east. */
    private static List<AetherisBiomeSource.Zone> zones(HolderGetter<Biome> biomes) {
        var volcanic = biomes.getOrThrow(SoFEBiomes.NORDRATH_VOLCANIC_FORGES);
        var ice = biomes.getOrThrow(SoFEBiomes.NORDRATH_ICE_FIELDS);
        return List.of(
                new AetherisBiomeSource.Zone(volcanic, 0, -4500, 560),
                new AetherisBiomeSource.Zone(volcanic, -1200, -3000, 280),
                new AetherisBiomeSource.Zone(ice, -1800, -4200, 650),
                new AetherisBiomeSource.Zone(ice, 1800, -4000, 600));
    }

    public static void bootstrap(BootstapContext<WorldPreset> context) {
        HolderGetter<DimensionType> dimensionTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noise = context.lookup(Registries.NOISE_SETTINGS);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<MultiNoiseBiomeSourceParameterList> parameterLists = context.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);

        Map<Region, Holder<Biome>> regionBiomes = new EnumMap<>(Region.class);
        SoFEBiomes.BY_REGION.forEach((region, key) -> regionBiomes.put(region, biomes.getOrThrow(key)));

        LevelStem overworld = new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                new NoiseBasedChunkGenerator(new AetherisBiomeSource(RegionMap.defaultLayout(), regionBiomes,
                        Optional.of(new AetherisBiomeSource.Wastes(biomes.getOrThrow(SoFEBiomes.ASHEN_WASTES), ASHEN_WASTES_INNER_RADIUS)),
                        zones(biomes)),
                        noise.getOrThrow(SoFENoiseSettings.AETHERIS)));
        LevelStem nether = new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.NETHER),
                new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.createFromPreset(parameterLists.getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER)),
                        noise.getOrThrow(NoiseGeneratorSettings.NETHER)));
        LevelStem end = new LevelStem(dimensionTypes.getOrThrow(BuiltinDimensionTypes.END),
                new NoiseBasedChunkGenerator(TheEndBiomeSource.create(biomes), noise.getOrThrow(NoiseGeneratorSettings.END)));

        context.register(AETHERIS, new WorldPreset(Map.of(
                LevelStem.OVERWORLD, overworld,
                LevelStem.NETHER, nether,
                LevelStem.END, end)));
    }
}
