package com.sofe.world.region;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** The regions of Aetheris (docs/Mundo.md, W1). */
public enum Region {
    SULTHARI(1),
    NORDRATH(2),
    PARSIVAN(3),
    KHEMET(3),
    AUREUM(4),
    /** Everything outside the five empires; never unlocked. */
    OCEAN(0);

    private final int opensAtAct;

    Region(int opensAtAct) {
        this.opensAtAct = opensAtAct;
    }

    /** Act in which the region opens; 0 means it never opens. */
    public int opensAtAct() {
        return opensAtAct;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "region.sofe." + id();
    }

    public static Optional<Region> byId(String id) {
        return Arrays.stream(values()).filter(r -> r.id().equals(id)).findFirst();
    }
}
