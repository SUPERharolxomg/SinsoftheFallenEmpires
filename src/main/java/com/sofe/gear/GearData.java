package com.sofe.gear;

import com.sofe.progression.CharacterAttribute;

import java.util.List;

/**
 * What makes a piece of gear SoFE gear (docs/Pociones.md): its item level, rarity, rolled affixes, the
 * attribute it asks for, and for Relics the id of their unique effect. Saved in the item's NBT.
 *
 * @param requiredAttribute heavy weapons and armor ask for an attribute (null when none)
 * @param relic             the Relic's id (e.g. "kaleth_blade"), or null
 * @param nameParts         two indices into the generated-name lists for Imperial items, or empty
 */
public record GearData(int itemLevel, Rarity rarity, List<Roll> affixes, CharacterAttribute requiredAttribute, int requiredValue,
                       String relic, List<Integer> nameParts) {

    /** One rolled affix: which affix, its stat and parameter (kept so a removed affix file still reads), and the value. */
    public record Roll(String affix, GearStat stat, String parameter, int value) {
    }

    /** Every item needs level ≥ item level − 3. */
    public static final int LEVEL_MARGIN = 5;

    public GearData {
        affixes = List.copyOf(affixes);
        nameParts = List.copyOf(nameParts);
    }

    public static GearData common(int itemLevel) {
        return new GearData(itemLevel, Rarity.COMMON, List.of(), null, 0, null, List.of());
    }

    /** Random gear asks a few levels under its own; a unique (Relic) asks its full level, as in Diablo II. */
    public int requiredLevel() {
        if (rarity == Rarity.RELIC) return Math.max(1, itemLevel);
        return Math.max(1, itemLevel - LEVEL_MARGIN);
    }

    public GearData withAffixes(Rarity newRarity, List<Roll> rolls) {
        return new GearData(itemLevel, newRarity, rolls, requiredAttribute, requiredValue, relic, nameParts);
    }

    public GearData withRequirement(CharacterAttribute attribute, int value) {
        return new GearData(itemLevel, rarity, affixes, attribute, value, relic, nameParts);
    }

    public GearData withNameParts(List<Integer> parts) {
        return new GearData(itemLevel, rarity, affixes, requiredAttribute, requiredValue, relic, parts);
    }
}
