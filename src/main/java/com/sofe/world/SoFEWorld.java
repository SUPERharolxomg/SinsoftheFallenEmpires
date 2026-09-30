package com.sofe.world;

import com.sofe.world.region.AetherisBiomeSource;
import com.sofe.world.region.RegionMap;
import net.minecraft.server.MinecraftServer;

import java.util.Optional;

/** Questions about the current world that the rest of the mod asks. */
public final class SoFEWorld {

    private SoFEWorld() {
    }

    /**
     * Whether this world is a SoFE journey (created with the sofe:aetheris preset). The story,
     * locks and Bearer selection only run in journeys; other worlds use free mode.
     */
    public static boolean isJourney(MinecraftServer server) {
        return regionMap(server).isPresent();
    }

    /** The region layout saved in this world, if it is a journey. */
    public static Optional<RegionMap> regionMap(MinecraftServer server) {
        if (server.overworld().getChunkSource().getGenerator().getBiomeSource() instanceof AetherisBiomeSource source) {
            return Optional.of(source.regionMap());
        }
        return Optional.empty();
    }
}
