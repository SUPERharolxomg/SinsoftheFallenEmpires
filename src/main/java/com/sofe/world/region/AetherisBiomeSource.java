package com.sofe.world.region;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Picks the biome from the region at each position (docs/Mundo.md, W1): the layout is fixed,
 * the terrain is still procedural.
 *
 * The region layout is part of this source's codec, so it is written into the world's
 * level.dat when the world is created. A mod update that changes the default layout
 * never moves the regions of an existing world (docs/Jugabilidad.md, G7).
 */
public class AetherisBiomeSource extends BiomeSource {

    private static final Codec<Region> REGION_CODEC = Codec.STRING.comapFlatMap(
            id -> Region.byId(id).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown region " + id)),
            Region::id);

    private static final Codec<RegionBounds> BOUNDS_CODEC = RecordCodecBuilder.create(i -> i.group(
            REGION_CODEC.fieldOf("region").forGetter(RegionBounds::region),
            Codec.INT.fieldOf("min_x").forGetter(RegionBounds::minX),
            Codec.INT.fieldOf("max_x").forGetter(RegionBounds::maxX),
            Codec.INT.fieldOf("min_z").forGetter(RegionBounds::minZ),
            Codec.INT.fieldOf("max_z").forGetter(RegionBounds::maxZ)
    ).apply(i, RegionBounds::new));

    /**
     * The Ashen Wastes: the outer ring of Sulthari, beyond {@code innerRadius} blocks from the
     * region's center (with a wavy edge). Optional so worlds created before it keep their biomes.
     */
    public record Wastes(Holder<Biome> biome, int innerRadius) {
        public static final Codec<Wastes> CODEC = RecordCodecBuilder.create(i -> i.group(
                Biome.CODEC.fieldOf("biome").forGetter(Wastes::biome),
                Codec.INT.fieldOf("inner_radius").forGetter(Wastes::innerRadius)
        ).apply(i, Wastes::new));
    }

    public static final Codec<AetherisBiomeSource> CODEC = RecordCodecBuilder.<AetherisBiomeSource>mapCodec(i -> i.group(
            BOUNDS_CODEC.listOf().fieldOf("regions").forGetter(s -> s.map.bounds()),
            Codec.unboundedMap(REGION_CODEC, Biome.CODEC).fieldOf("biomes").forGetter(s -> s.biomes),
            Wastes.CODEC.optionalFieldOf("ashen_wastes").forGetter(s -> s.wastes)
    ).apply(i, AetherisBiomeSource::new)).flatXmap(AetherisBiomeSource::validate, DataResult::success).codec();

    private final RegionMap map;
    private final Map<Region, Holder<Biome>> biomes;
    private final Optional<Wastes> wastes;
    private final Optional<RegionBounds> sulthari;

    /** Unchecked constructor used by the codec; {@link #validate} runs right after it. */
    private AetherisBiomeSource(List<RegionBounds> bounds, Map<Region, Holder<Biome>> biomes, Optional<Wastes> wastes) {
        this.map = new RegionMap(bounds);
        this.biomes = new EnumMap<>(biomes);
        this.wastes = wastes;
        this.sulthari = bounds.stream().filter(b -> b.region() == Region.SULTHARI).findFirst();
    }

    public AetherisBiomeSource(RegionMap map, Map<Region, Holder<Biome>> biomes, Optional<Wastes> wastes) {
        this(map.bounds(), biomes, wastes);
        validate(this).getOrThrow(false, message -> {
            throw new IllegalArgumentException(message);
        });
    }

    private static DataResult<AetherisBiomeSource> validate(AetherisBiomeSource source) {
        for (Region region : Region.values()) {
            if (!source.biomes.containsKey(region)) {
                return DataResult.error(() -> "No biome for region " + region.id());
            }
        }
        return DataResult.success(source);
    }

    public RegionMap regionMap() {
        return map;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(biomes.values().stream(), wastes.stream().map(Wastes::biome)).distinct();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        int x = QuartPos.toBlock(quartX), z = QuartPos.toBlock(quartZ);
        Region region = map.regionAt(x, z);
        if (region == Region.SULTHARI && wastes.isPresent() && sulthari.isPresent()
                && AshenWastes.contains(sulthari.get(), wastes.get().innerRadius(), x, z)) {
            return wastes.get().biome();
        }
        return biomes.get(region);
    }
}
