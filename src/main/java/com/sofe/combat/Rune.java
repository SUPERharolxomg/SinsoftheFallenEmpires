package com.sofe.combat;

import java.util.Locale;

/** The star runes the Sorceress's basic spells leave behind (docs/Clases.md). */
public enum Rune {
    FIRE, FROST, STORM;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "rune.sofe." + id();
    }
}
