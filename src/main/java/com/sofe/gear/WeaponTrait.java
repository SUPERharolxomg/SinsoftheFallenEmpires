package com.sofe.gear;

import java.util.Locale;

/**
 * What makes a weapon of the arsenal fight its own way (docs/Pociones.md, "The arsenal"). Each weapon
 * has one or two traits; WeaponTraitHandler applies them when its wielder hits. The numbers live here,
 * free of Minecraft, so they can be tested.
 */
public enum WeaponTrait {
    /** Hits from farther away (the reach is set per weapon). */
    REACH,
    /** Half the damage to every other enemy close to the target. */
    SWEEP,
    /** Stops the target for a moment. */
    STUN,
    /** Slams the ground: most of the damage to every enemy around the target. */
    SLAM,
    /** The target bleeds for a few seconds. */
    BLEED,
    /** Hits beasts much harder. */
    BEAST,
    /** Hits much harder while sprinting or riding. */
    CHARGE,
    /** Cuts through leaves and plants quickly. */
    PLANTS,
    /** Part of the damage goes through armor. */
    PIERCE,
    /** Strikes again in the same swing. */
    MULTI_HIT,
    /** Chills and slows the target. */
    FROST,
    /** Heals the wielder for part of the damage. */
    LIFE_STEAL,
    /** Slows the target for longer. */
    SLOW,
    /** Pulls the target toward the wielder. */
    PULL,
    /** Throws the target into the air. */
    KNOCK_UP,
    /** Void damage that withers the target. */
    VOID,
    /** Costs the wielder health, deals far more. */
    SACRIFICE;

    public static final float SWEEP_FRACTION = 0.5f, SWEEP_RADIUS = 2.5f;
    public static final float SLAM_FRACTION = 0.6f, SLAM_RADIUS = 3.0f;
    public static final float BEAST_MULTIPLIER = 1.5f, CHARGE_MULTIPLIER = 1.6f, MULTI_HIT_MULTIPLIER = 1.4f;
    public static final float LIFE_STEAL_FRACTION = 0.15f, VOID_MULTIPLIER = 1.3f;
    public static final float SACRIFICE_MULTIPLIER = 1.5f, SACRIFICE_COST = 1.0f;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "trait.sofe." + id();
    }

    /** Armor piercing: 3% more damage per point of the target's armor, up to half again. */
    public static float pierce(float amount, double armor) {
        return amount * (1f + (float) Math.min(0.5, Math.max(0, armor) * 0.03));
    }

    /** Sacrifice never kills the wielder: it stops at one health. */
    public static float sacrificeCost(float health) {
        return Math.max(0f, Math.min(SACRIFICE_COST, health - 1f));
    }
}
