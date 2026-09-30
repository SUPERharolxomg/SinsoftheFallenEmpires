package com.sofe.world.region;

import java.util.ArrayList;
import java.util.List;

/**
 * Answers which region a block column belongs to (docs/Mundo.md, W1).
 * Anything not covered by a region is {@link Region#OCEAN}.
 */
public final class RegionMap {
    private final List<RegionBounds> bounds;

    public RegionMap(List<RegionBounds> bounds) {
        this.bounds = List.copyOf(bounds);
        List<String> problems = problems(this.bounds);
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException("Invalid region layout: " + String.join("; ", problems));
        }
    }

    /** The layout from docs/Mundo.md. New worlds store their own copy, so changing this never moves an existing world. */
    public static RegionMap defaultLayout() {
        return new RegionMap(List.of(
                new RegionBounds(Region.SULTHARI, -1500, 1500, -1500, 1500),
                new RegionBounds(Region.NORDRATH, -2500, 2500, -5800, -1500),
                new RegionBounds(Region.PARSIVAN, 1500, 5800, -1500, 1500),
                new RegionBounds(Region.KHEMET, 800, 5800, 1500, 5800),
                new RegionBounds(Region.AUREUM, -5800, -1500, -1500, 3500)
        ));
    }

    public Region regionAt(int x, int z) {
        for (RegionBounds b : bounds) {
            if (b.contains(x, z)) {
                return b.region();
            }
        }
        return Region.OCEAN;
    }

    public List<RegionBounds> bounds() {
        return bounds;
    }

    /** Overlapping rectangles and misuse of OCEAN (which is implicit). Empty when the layout is valid. */
    public static List<String> problems(List<RegionBounds> bounds) {
        List<String> problems = new ArrayList<>();
        for (int i = 0; i < bounds.size(); i++) {
            RegionBounds a = bounds.get(i);
            if (a.region() == Region.OCEAN) {
                problems.add("OCEAN is everything outside the regions and cannot have bounds");
            }
            for (int j = i + 1; j < bounds.size(); j++) {
                RegionBounds b = bounds.get(j);
                if (a.overlaps(b)) {
                    problems.add(a.region() + " overlaps " + b.region());
                }
            }
        }
        return problems;
    }
}
