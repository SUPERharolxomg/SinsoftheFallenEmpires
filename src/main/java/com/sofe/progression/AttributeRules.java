package com.sofe.progression;

import com.google.gson.JsonObject;
import com.sofe.player.PlayerClass;
import com.sofe.player.ResourceType;

/**
 * What each attribute point does, from data/sofe/attributes.json (docs/Clases.md, "Attributes").
 * Effects count the points above the base of 10, so a new character has vanilla health and the
 * class's primary attribute already gives its edge.
 */
public record AttributeRules(double physicalDamage, double critChance, double critMultiplier, double dodgeChance, double dodgeCap,
                             double magicDamage, double maxMana, double resourceRegen, double maxEssence, double maxResolve,
                             double maxAuthority, double decreeStrength, double maxHealth) {

    public static final AttributeRules DEFAULT = new AttributeRules(0.01, 0.005, 1.5, 0.003, 0.30,
            0.01, 1, 0.02, 1, 1, 1, 0.01, 0.5);

    /** The combined effect of a character's attributes. */
    public record Effects(double physicalDamageMultiplier, double magicDamageMultiplier, double critChance, double critMultiplier,
                          double dodgeChance, double maxHealthBonus, double regenMultiplier, int[] maxResourceBonus) {

        public int maxResourceBonus(ResourceType type) {
            return maxResourceBonus[type.ordinal()];
        }
    }

    public Effects effects(AttributeSheet sheet, PlayerClass playerClass) {
        int str = sheet.aboveBase(CharacterAttribute.STRENGTH, playerClass);
        int agi = sheet.aboveBase(CharacterAttribute.AGILITY, playerClass);
        int intel = sheet.aboveBase(CharacterAttribute.INTELLECT, playerClass);
        int will = sheet.aboveBase(CharacterAttribute.WILL, playerClass);
        int cha = sheet.aboveBase(CharacterAttribute.CHARISMA, playerClass);
        int vit = sheet.aboveBase(CharacterAttribute.VITALITY, playerClass);

        int[] resource = new int[ResourceType.values().length];
        resource[ResourceType.MANA.ordinal()] = (int) Math.round(intel * maxMana);
        resource[ResourceType.ESSENCE.ordinal()] = (int) Math.round(will * maxEssence);
        resource[ResourceType.RESOLVE.ordinal()] = (int) Math.round(will * maxResolve);
        resource[ResourceType.AUTHORITY.ordinal()] = (int) Math.round(cha * maxAuthority);

        return new Effects(
                1 + str * physicalDamage,
                1 + intel * magicDamage,
                Math.min(1, agi * critChance),
                critMultiplier,
                Math.min(dodgeCap, agi * dodgeChance),
                vit * maxHealth,
                1 + will * resourceRegen,
                resource);
    }

    public static AttributeRules parse(JsonObject json) {
        AttributeRules d = DEFAULT;
        return new AttributeRules(
                get(json, "strength", "physical_damage", d.physicalDamage),
                get(json, "agility", "crit_chance", d.critChance),
                get(json, "agility", "crit_multiplier", d.critMultiplier),
                get(json, "agility", "dodge_chance", d.dodgeChance),
                get(json, "agility", "dodge_cap", d.dodgeCap),
                get(json, "intellect", "magic_damage", d.magicDamage),
                get(json, "intellect", "max_mana", d.maxMana),
                get(json, "will", "resource_regen", d.resourceRegen),
                get(json, "will", "max_essence", d.maxEssence),
                get(json, "will", "max_resolve", d.maxResolve),
                get(json, "charisma", "max_authority", d.maxAuthority),
                get(json, "charisma", "decree_strength", d.decreeStrength),
                get(json, "vitality", "max_health", d.maxHealth));
    }

    private static double get(JsonObject json, String attribute, String key, double fallback) {
        if (!json.has(attribute) || !json.getAsJsonObject(attribute).has(key)) return fallback;
        return json.getAsJsonObject(attribute).get(key).getAsDouble();
    }
}
