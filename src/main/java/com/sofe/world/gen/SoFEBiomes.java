package com.sofe.world.gen;

import com.sofe.SoFEMod;
import com.sofe.world.region.Region;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.EnumMap;
import java.util.Map;

/**
 * One placeholder biome per region, built from vanilla biomes so they look right until
 * Sprint 4 and later add the real empire biomes. They are SoFE biomes (not the vanilla
 * ones) on purpose: they are in none of the vanilla structure tags, so no strongholds,
 * villages or other vanilla structures generate in a journey.
 */
public final class SoFEBiomes {
    public static final Map<Region, ResourceKey<Biome>> BY_REGION = new EnumMap<>(Region.class);

    public static final ResourceKey<Biome> SULTHARI_DESERT = key(Region.SULTHARI, "sulthari_desert");
    public static final ResourceKey<Biome> NORDRATH_TUNDRA = key(Region.NORDRATH, "nordrath_tundra");
    public static final ResourceKey<Biome> PARSIVAN_GARDENS = key(Region.PARSIVAN, "parsivan_gardens");
    public static final ResourceKey<Biome> KHEMET_VALLEY = key(Region.KHEMET, "khemet_valley");
    public static final ResourceKey<Biome> AUREUM_HILLS = key(Region.AUREUM, "aureum_hills");
    public static final ResourceKey<Biome> AETHERIS_OCEAN = key(Region.OCEAN, "aetheris_ocean");

    private SoFEBiomes() {
    }

    private static ResourceKey<Biome> key(Region region, String name) {
        ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, SoFEMod.id(name));
        BY_REGION.put(region, key);
        return key;
    }

    public static void bootstrap(BootstapContext<Biome> context) {
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);

        context.register(SULTHARI_DESERT, OverworldBiomes.desert(features, carvers));
        context.register(NORDRATH_TUNDRA, OverworldBiomes.plains(features, carvers, false, true, false));
        context.register(PARSIVAN_GARDENS, OverworldBiomes.forest(features, carvers, false, false, true));
        context.register(KHEMET_VALLEY, OverworldBiomes.savanna(features, carvers, false, false));
        context.register(AUREUM_HILLS, OverworldBiomes.plains(features, carvers, false, false, false));
        context.register(AETHERIS_OCEAN, OverworldBiomes.ocean(features, carvers, false));
    }
}
