package com.sofe.gear;

import java.util.Locale;

/**
 * Item rarities (docs/Pociones.md, "Rarities, affixes and slots"). The names are SoFE's own. Each has a
 * name color, a loot beam color and how many random affixes it rolls.
 */
public enum Rarity {
    COMMON(0xFFFFFF, 0, 0, -1),
    TEMPERED(0x5B8CFF, 1, 2, 0x5B8CFF),
    IMPERIAL(0xF2D24B, 3, 4, 0xF2D24B),
    /** Fixed affixes and one unique effect; never rolled at random. */
    RELIC(0xD9A441, 0, 0, 0xFFB030),
    /** Fixed affixes and a set bonus; never rolled at random. */
    LEGACY(0x4FD06A, 0, 0, 0x4FD06A);

    private final int color;
    private final int minAffixes, maxAffixes;
    private final int beamColor;

    Rarity(int color, int minAffixes, int maxAffixes, int beamColor) {
        this.color = color;
        this.minAffixes = minAffixes;
        this.maxAffixes = maxAffixes;
        this.beamColor = beamColor;
    }

    /** RGB of the item name. */
    public int color() {
        return color;
    }

    public int minAffixes() {
        return minAffixes;
    }

    public int maxAffixes() {
        return maxAffixes;
    }

    /** Whether items of this rarity show a loot beam on the ground. */
    public boolean hasBeam() {
        return beamColor >= 0;
    }

    public int beamColor() {
        return beamColor;
    }

    /** Rarities the loot generator can roll; Relics and Legacies come from fixed definitions. */
    public boolean isRandom() {
        return this == COMMON || this == TEMPERED || this == IMPERIAL;
    }

    public boolean atLeast(Rarity other) {
        return ordinal() >= other.ordinal();
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "rarity.sofe." + id();
    }

    public static Rarity byId(String id) {
        return valueOf(id.toUpperCase(Locale.ROOT));
    }
}
