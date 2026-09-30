package com.sofe.world.lock;

import com.sofe.story.StoryProgress;
import com.sofe.world.region.Region;

import java.util.Map;

/** What the region title adds after the name (docs/Mundo.md, W3): "— Sealed" or "— Liberated". */
public enum RegionStatus {
    NONE, SEALED, LIBERATED;

    /** The Archsin whose fall liberates each region. Sulthari is the hub and is never sealed. */
    private static final Map<Region, String> LIBERATED_BY = Map.of(
            Region.NORDRATH, "sofe:vorath",
            Region.PARSIVAN, "sofe:luxara",
            Region.KHEMET, "sofe:morthis",
            Region.AUREUM, "sofe:envyris");

    public static RegionStatus of(Region region, StoryProgress story) {
        if (region == Region.OCEAN) return NONE;
        if (!story.hasUnlocked(region)) return SEALED;
        String boss = LIBERATED_BY.get(region);
        return boss != null && story.hasDefeated(boss) ? LIBERATED : NONE;
    }

    public String translationKey() {
        return "gui.sofe.region_title." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
