package com.sofe.world.region;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Remembers the last region of each player and reports when they cross into another one. */
public final class RegionTracker {
    private final Map<UUID, Region> lastRegion = new HashMap<>();

    /**
     * @return the new region when the player just entered it (including the first check after
     * joining), or empty when they are still in the same region
     */
    public Optional<Region> update(UUID player, Region current) {
        Region previous = lastRegion.put(player, current);
        return previous == current ? Optional.empty() : Optional.of(current);
    }

    public void forget(UUID player) {
        lastRegion.remove(player);
    }
}
