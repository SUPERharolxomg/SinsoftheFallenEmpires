package com.sofe.story;

import com.sofe.world.region.Region;

import java.util.List;
import java.util.Map;

/**
 * The fates each region can end with (docs/Jugabilidad.md, "Fates and epilogues"). A fate is saved
 * as one of these ids; its text is the lang key fate.sofe.&lt;region&gt;.&lt;fate&gt;.
 */
public final class RegionFates {
    private static final Map<Region, List<String>> FATES = Map.of(
            Region.SULTHARI, List.of("bazaar", "observatory"),
            Region.NORDRATH, List.of("peace", "war"),
            Region.PARSIVAN, List.of("awakened", "asleep"),
            Region.KHEMET, List.of("rest", "guardians"),
            Region.AUREUM, List.of("law", "shared"));

    private RegionFates() {
    }

    public static List<String> of(Region region) {
        return FATES.getOrDefault(region, List.of());
    }

    public static boolean isValid(Region region, String fate) {
        return of(region).contains(fate);
    }

    /** Regions the player has opened whose fate is still to be chosen (shown in the Journal). */
    public static List<Region> open(StoryProgress story) {
        return java.util.Arrays.stream(Region.values())
                .filter(r -> !of(r).isEmpty() && story.hasUnlocked(r) && story.fate(r).isEmpty())
                .toList();
    }

    public static String translationKey(Region region, String fate) {
        return "fate.sofe." + region.id() + "." + fate;
    }
}
