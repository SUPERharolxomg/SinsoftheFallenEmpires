package com.sofe.story;

import java.util.Locale;

/** The seven sins: one per Archsin, and the temptation each Bearer carries. */
public enum Sin {
    WRATH, LUST, GREED, SLOTH, GLUTTONY, ENVY, PRIDE;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "sin.sofe." + id();
    }
}
