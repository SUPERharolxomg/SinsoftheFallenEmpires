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

    /**
     * The region layout saved in this world, if it is a journey whose story can run: a journey in free mode (FreeMode)
     * answers as a world that is no journey, so the story, the locks and the lairs stop.
     */
    public static Optional<RegionMap> regionMap(MinecraftServer server) {
        return rawRegionMap(server).filter(map -> FreeMode.reason(server, map).isEmpty());
    }

    /** The region layout saved in this world, free mode or not. */
    public static Optional<RegionMap> rawRegionMap(MinecraftServer server) {
        if (server.overworld() != null && server.overworld().getChunkSource().getGenerator().getBiomeSource() instanceof AetherisBiomeSource source) {
            return Optional.of(source.regionMap());
        }
        return Optional.empty();
    }
}
