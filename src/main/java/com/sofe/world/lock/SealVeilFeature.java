package com.sofe.world.lock;

import com.sofe.registry.SoFEBlocks;
import com.sofe.world.region.AetherisBiomeSource;
import com.sofe.world.region.RegionMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Raises the Seal Veil on the border columns of one chunk, from the bottom of the world to the
 * build limit. Runs once per chunk in every journey biome and every Nether biome (added by biome
 * modifiers); in the Nether the borders are the overworld's at 1:8, and only in a journey.
 */
public class SealVeilFeature extends Feature<NoneFeatureConfiguration> {

    public SealVeilFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RegionMap map;
        int scale = 1;
        if (context.chunkGenerator().getBiomeSource() instanceof AetherisBiomeSource source) {
            map = source.regionMap();
        } else if (level.getLevel().dimension() == net.minecraft.world.level.Level.NETHER) {
            // the Burning Deep: the same borders at 1:8 (docs/Mundo.md, W5); only in a journey
            var journey = com.sofe.world.SoFEWorld.regionMap(level.getLevel().getServer());
            if (journey.isEmpty()) return false;
            map = journey.get();
            scale = 8;
        } else {
            return false;
        }
        int chunkX = context.origin().getX() & ~15, chunkZ = context.origin().getZ() & ~15;
        BlockState veil = SoFEBlocks.SEAL_VEIL.get().defaultBlockState();
        int minY = level.getMinBuildHeight(), maxY = level.getMaxBuildHeight();
        boolean placed = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = chunkX + dx, z = chunkZ + dz;
                if (!RegionLocks.isVeilColumn(map, x, z, scale)) continue;
                for (int y = minY; y < maxY; y++) {
                    level.setBlock(pos.set(x, y, z), veil, 2);
                }
                placed = true;
            }
        }
        return placed;
    }
}
