package com.sofe.player;

import java.util.Locale;

/** The resource each class spends on its skills. */
public enum ResourceType {
    RESOLVE, ESSENCE, MANA, ENERGY, AUTHORITY;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "resource.sofe." + id();
    }
}
