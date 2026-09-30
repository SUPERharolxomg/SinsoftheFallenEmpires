package com.sofe.player;

import com.sofe.story.Sin;
import com.sofe.world.region.Region;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * The five Bearers (docs/Clases.md). Names, roles and quotes live in the lang files
 * under the keys built here, never in code.
 */
public enum PlayerClass {
    KNIGHT(ResourceType.RESOLVE, Sin.WRATH, Region.AUREUM),
    NECROMANCER(ResourceType.ESSENCE, Sin.SLOTH, Region.KHEMET),
    SORCERESS(ResourceType.MANA, Sin.LUST, Region.PARSIVAN),
    THIEF(ResourceType.ENERGY, Sin.GREED, Region.NORDRATH),
    KING(ResourceType.AUTHORITY, Sin.PRIDE, Region.SULTHARI);

    private final ResourceType resource;
    private final Sin temptation; // used in dialogue and in the Envyris fight
    private final Region origin;

    PlayerClass(ResourceType resource, Sin temptation, Region origin) {
        this.resource = resource;
        this.temptation = temptation;
        this.origin = origin;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public ResourceType resource() {
        return resource;
    }

    public Sin temptation() {
        return temptation;
    }

    public Region origin() {
        return origin;
    }

    /** Class name, e.g. "Knight". */
    public String translationKey() {
        return "class.sofe." + id();
    }

    /** Hero name, e.g. "Cassian Dravo". */
    public String heroKey() {
        return translationKey() + ".hero";
    }

    /** The hero as an NPC and a dialogue speaker: cassian, ankhareth, shirin, rurik, azhar. */
    public String npcId() {
        return switch (this) {
            case KNIGHT -> "cassian";
            case NECROMANCER -> "ankhareth";
            case SORCERESS -> "shirin";
            case THIEF -> "rurik";
            case KING -> "azhar";
        };
    }

    public String roleKey() {
        return translationKey() + ".role";
    }

    public String quoteKey() {
        return translationKey() + ".quote";
    }

    public static Optional<PlayerClass> byId(String id) {
        return Arrays.stream(values()).filter(c -> c.id().equals(id)).findFirst();
    }
}
