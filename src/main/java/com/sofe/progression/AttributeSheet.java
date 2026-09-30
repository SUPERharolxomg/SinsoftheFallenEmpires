package com.sofe.progression;

import com.sofe.player.PlayerClass;

import java.util.EnumMap;
import java.util.Map;

/**
 * Points the player has put into each attribute. The shown value is the class base (10, or 20
 * for the class's primary attribute) plus these points.
 */
public final class AttributeSheet {
    public static final int BASE = 10;
    public static final int PRIMARY_BONUS = 10;

    private final Map<CharacterAttribute, Integer> added = new EnumMap<>(CharacterAttribute.class);

    public static CharacterAttribute primary(PlayerClass playerClass) {
        return switch (playerClass) {
            case KNIGHT -> CharacterAttribute.STRENGTH;
            case NECROMANCER -> CharacterAttribute.WILL;
            case SORCERESS -> CharacterAttribute.INTELLECT;
            case THIEF -> CharacterAttribute.AGILITY;
            case KING -> CharacterAttribute.CHARISMA;
        };
    }

    public static int base(CharacterAttribute attribute, PlayerClass playerClass) {
        return BASE + (primary(playerClass) == attribute ? PRIMARY_BONUS : 0);
    }

    public int added(CharacterAttribute attribute) {
        return added.getOrDefault(attribute, 0);
    }

    public int value(CharacterAttribute attribute, PlayerClass playerClass) {
        return base(attribute, playerClass) + added(attribute);
    }

    /** Points above the plain base of 10, which is what the effects count. */
    public int aboveBase(CharacterAttribute attribute, PlayerClass playerClass) {
        return value(attribute, playerClass) - BASE;
    }

    public void add(CharacterAttribute attribute) {
        added.merge(attribute, 1, Integer::sum);
    }

    public Map<CharacterAttribute, Integer> addedPoints() {
        return Map.copyOf(added);
    }

    public int reset() {
        int spent = added.values().stream().mapToInt(Integer::intValue).sum();
        added.clear();
        return spent;
    }

    public void load(Map<CharacterAttribute, Integer> saved) {
        added.clear();
        saved.forEach((a, v) -> added.put(a, Math.max(0, v)));
    }
}
