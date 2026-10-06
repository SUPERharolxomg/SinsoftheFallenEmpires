package com.sofe.world.gen;

import com.sofe.registry.SoFESounds;

import com.sofe.SoFEMod;
import com.sofe.world.region.Region;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
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
    /** Nordrath's zones (docs/Mundo.md, W1): glaciers with ice spikes, and the volcanic forges of Wrath. */
    public static final ResourceKey<Biome> NORDRATH_ICE_FIELDS = ResourceKey.create(Registries.BIOME, SoFEMod.id("nordrath_ice_fields"));
    public static final ResourceKey<Biome> NORDRATH_VOLCANIC_FORGES = ResourceKey.create(Registries.BIOME, SoFEMod.id("nordrath_volcanic_forges"));
    /** The corrupted wilderness around the city, in the outer ring of Sulthari (docs/Mundo.md, W1). */
    public static final ResourceKey<Biome> ASHEN_WASTES = ResourceKey.create(Registries.BIOME, SoFEMod.id("ashen_wastes"));

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

        context.register(SULTHARI_DESERT, withMusic(OverworldBiomes.desert(features, carvers), SoFESounds.MUSIC_SULTHARI));
        context.register(NORDRATH_TUNDRA, withMusic(OverworldBiomes.plains(features, carvers, false, true, false), SoFESounds.MUSIC_NORDRATH));
        context.register(PARSIVAN_GARDENS, withMusic(OverworldBiomes.forest(features, carvers, false, false, true), SoFESounds.MUSIC_PARSIVAN));
        context.register(KHEMET_VALLEY, withMusic(OverworldBiomes.savanna(features, carvers, false, false), SoFESounds.MUSIC_KHEMET));
        context.register(AUREUM_HILLS, withMusic(OverworldBiomes.plains(features, carvers, false, false, false), SoFESounds.MUSIC_AUREUM));
        context.register(AETHERIS_OCEAN, OverworldBiomes.ocean(features, carvers, false));
        context.register(ASHEN_WASTES, withMusic(ashenWastes(OverworldBiomes.desert(features, carvers)), SoFESounds.MUSIC_ASHEN_WASTES));
        context.register(NORDRATH_ICE_FIELDS, withMusic(OverworldBiomes.plains(features, carvers, false, true, true), SoFESounds.MUSIC_NORDRATH));
        context.register(NORDRATH_VOLCANIC_FORGES, withMusic(volcanicForges(OverworldBiomes.plains(features, carvers, false, false, false)),
                SoFESounds.MUSIC_VOLCANIC_FORGES));
    }

    /** The same biome, with the music of its region (SoFESounds, assets/sofe/sounds.json). */
    private static Biome withMusic(Biome biome, net.minecraftforge.registries.RegistryObject<net.minecraft.sounds.SoundEvent> music) {
        BiomeSpecialEffects e = biome.getSpecialEffects();
        BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .fogColor(e.getFogColor()).skyColor(e.getSkyColor()).waterColor(e.getWaterColor()).waterFogColor(e.getWaterFogColor())
                .grassColorModifier(e.getGrassColorModifier())
                .backgroundMusic(SoFESounds.regionMusic(music.getHolder().orElseThrow()));
        e.getFoliageColorOverride().ifPresent(effects::foliageColorOverride);
        e.getGrassColorOverride().ifPresent(effects::grassColorOverride);
        e.getAmbientParticleSettings().ifPresent(effects::ambientParticle);
        e.getAmbientLoopSoundEvent().ifPresent(effects::ambientLoopSound);
        e.getAmbientMoodSettings().ifPresent(effects::ambientMoodSound);
        e.getAmbientAdditionsSettings().ifPresent(effects::ambientAdditionsSound);
        var climate = biome.getModifiedClimateSettings();
        return new Biome.BiomeBuilder()
                .hasPrecipitation(climate.hasPrecipitation())
                .temperature(climate.temperature())
                .temperatureAdjustment(climate.temperatureModifier())
                .downfall(climate.downfall())
                .specialEffects(effects.build())
                .mobSpawnSettings(biome.getMobSettings())
                .generationSettings(biome.getGenerationSettings())
                .build();
    }

    /** Black rock and embers under a red sky, where Vorath's fire comes up from below. */
    private static Biome volcanicForges(Biome plains) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 80, 2, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 80, 1, 3));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 60, 1, 2));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 20, 1, 2));
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(1.6f)
                .downfall(0.0f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .fogColor(0x5A2A1E)
                        .skyColor(0x6E3226)
                        .waterColor(0x3A3A40)
                        .waterFogColor(0x201A1A)
                        .grassColorOverride(0x5E4A3A)
                        .foliageColorOverride(0x4A3A2E)
                        .ambientParticle(new AmbientParticleSettings(ParticleTypes.ASH, 0.03f))
                        .build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(plains.getGenerationSettings())
                .build();
    }

    /** The desert's terrain features under a grey sky with falling ash; only hostile creatures live here. */
    private static Biome ashenWastes(Biome desert) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HUSK, 80, 2, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 60, 1, 3));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 50, 1, 2));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(com.sofe.registry.EntityRegistry.VOID_WRETCH.get(), 70, 2, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(com.sofe.registry.EntityRegistry.VOID_STALKER.get(), 30, 1, 2));
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(2.0f)
                .downfall(0.0f)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .fogColor(0x6E6660)
                        .skyColor(0x7C736C)
                        .waterColor(0x4A4F52)
                        .waterFogColor(0x2E3133)
                        .grassColorOverride(0x7D7766)
                        .foliageColorOverride(0x6B6656)
                        .ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.025f))
                        .build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(desert.getGenerationSettings())
                .build();
    }
}
