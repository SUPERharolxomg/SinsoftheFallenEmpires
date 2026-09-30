package com.sofe.progression;

import java.util.Locale;

/** The six attributes of the character sheet (docs/Clases.md, "Attributes"). */
public enum CharacterAttribute {
    STRENGTH, AGILITY, INTELLECT, WILL, CHARISMA, VITALITY;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "attribute.sofe." + id();
    }
}
