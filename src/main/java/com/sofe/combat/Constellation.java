package com.sofe.combat;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** What three runes make (docs/Clases.md, Sorceress). */
public enum Constellation {
    STEAM_BURST, SOLAR_FLARE, WINTERS_GRASP, TEMPEST_CROWN, LESSER_CONSTELLATION;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "constellation.sofe." + id();
    }

    /** The constellation for three runes, in any order. */
    public static Constellation of(List<Rune> runes) {
        if (runes.size() != 3) {
            throw new IllegalArgumentException("a constellation needs 3 runes, got " + runes.size());
        }
        Map<Rune, Integer> counts = new EnumMap<>(Rune.class);
        runes.forEach(r -> counts.merge(r, 1, Integer::sum));
        if (counts.size() == 3) return STEAM_BURST;
        if (counts.size() == 2) return LESSER_CONSTELLATION;
        return switch (runes.get(0)) {
            case FIRE -> SOLAR_FLARE;
            case FROST -> WINTERS_GRASP;
            case STORM -> TEMPEST_CROWN;
        };
    }

    /** For a lesser constellation: the rune that appears twice, which sets its element. */
    public static Rune majority(List<Rune> runes) {
        Map<Rune, Integer> counts = new EnumMap<>(Rune.class);
        runes.forEach(r -> counts.merge(r, 1, Integer::sum));
        return counts.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
    }
}
