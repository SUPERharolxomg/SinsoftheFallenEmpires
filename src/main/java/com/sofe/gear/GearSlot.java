package com.sofe.gear;

import java.util.Locale;

/** Where a piece of gear is worn, which decides the affixes it can roll. */
public enum GearSlot {
    WEAPON, ARMOR, JEWELRY, TALISMAN;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static GearSlot byId(String id) {
        return valueOf(id.toUpperCase(Locale.ROOT));
    }
}
