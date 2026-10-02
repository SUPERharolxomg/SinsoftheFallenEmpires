package com.sofe.registry;

import com.sofe.SoFEMod;
import com.sofe.gear.GearItems;
import com.sofe.gear.GearSlot;
import com.sofe.gear.SoFETiers;
import com.sofe.gear.ranged.RangedItems.CastMode;
import com.sofe.gear.ranged.RangedItems.TomeKind;
import com.sofe.gear.ranged.Spell;
import com.sofe.item.BlueprintItem;
import com.sofe.item.CodexShardItem;
import com.sofe.item.ConsumableItems;
import com.sofe.travel.ReturnScrollItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public final class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SoFEMod.MOD_ID);

    public static final RegistryObject<Item> RETURN_SCROLL = ITEMS.register("return_scroll",
            () -> new ReturnScrollItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

    /** The currency of the empires; picked up straight into the Wallet (docs/Pociones.md). */
    public static final RegistryObject<Item> DINAR = ITEMS.register("dinar", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CODEX_SHARD = ITEMS.register("codex_shard",
            () -> new CodexShardItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    /** Nether materials of the Burning Deep (docs/Anexos.md, A3). */
    public static final RegistryObject<Item> INFERNAL_EMBER = ITEMS.register("infernal_ember", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> WAILING_SOUL = ITEMS.register("wailing_soul", () -> new Item(new Item.Properties()));

    // --- gear (docs/Pociones.md): weapons, armor, jewelry and talismans roll affixes; pickaxes are tools
    public static final RegistryObject<Item> BRASS_SCIMITAR = sword("brass_scimitar", SoFETiers.BRASS, 3, -2.2f);
    public static final RegistryObject<Item> BRASS_DAGGER = sword("brass_dagger", SoFETiers.BRASS, 1, -1.6f);
    public static final RegistryObject<Item> BRASS_LONGSWORD = sword("brass_longsword", SoFETiers.BRASS, 4, -2.6f);
    public static final RegistryObject<Item> BRASS_STAFF = sword("brass_staff", SoFETiers.BRASS, 1, -2.8f);
    public static final RegistryObject<Item> BRASS_ANKH_ROD = sword("brass_ankh_rod", SoFETiers.BRASS, 1, -2.6f);
    public static final RegistryObject<Item> GLACIAL_IRON_SWORD = sword("glacial_iron_sword", SoFETiers.GLACIAL_IRON, 3, -2.4f);
    public static final RegistryObject<Item> GLACIAL_IRON_GREATAXE = ITEMS.register("glacial_iron_greataxe",
            () -> new GearItems.Axe(SoFETiers.GLACIAL_IRON, 6f, -3.1f, new Item.Properties()));
    public static final RegistryObject<Item> GLACIAL_IRON_STAFF = sword("glacial_iron_staff", SoFETiers.GLACIAL_IRON, 2, -2.8f);

    public static final RegistryObject<Item> BRASS_HELMET = armor("brass_helmet", SoFETiers.Armor.BRASS, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> BRASS_CHESTPLATE = armor("brass_chestplate", SoFETiers.Armor.BRASS, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> BRASS_LEGGINGS = armor("brass_leggings", SoFETiers.Armor.BRASS, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> BRASS_BOOTS = armor("brass_boots", SoFETiers.Armor.BRASS, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> GLACIAL_IRON_HELMET = armor("glacial_iron_helmet", SoFETiers.Armor.GLACIAL_IRON, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> GLACIAL_IRON_CHESTPLATE = armor("glacial_iron_chestplate", SoFETiers.Armor.GLACIAL_IRON, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> GLACIAL_IRON_LEGGINGS = armor("glacial_iron_leggings", SoFETiers.Armor.GLACIAL_IRON, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> GLACIAL_IRON_BOOTS = armor("glacial_iron_boots", SoFETiers.Armor.GLACIAL_IRON, ArmorItem.Type.BOOTS);

    public static final RegistryObject<Item> BRASS_AMULET = trinket("brass_amulet", GearSlot.JEWELRY);
    public static final RegistryObject<Item> BRASS_RING = trinket("brass_ring", GearSlot.JEWELRY);
    public static final RegistryObject<Item> SMALL_TALISMAN = trinket("small_talisman", GearSlot.TALISMAN);
    public static final RegistryObject<Item> LARGE_TALISMAN = trinket("large_talisman", GearSlot.TALISMAN);

    public static final RegistryObject<Item> BRASS_PICKAXE = ITEMS.register("brass_pickaxe",
            () -> new GearItems.Pickaxe(SoFETiers.BRASS, 1, -2.8f, new Item.Properties()));
    public static final RegistryObject<Item> GLACIAL_IRON_PICKAXE = ITEMS.register("glacial_iron_pickaxe",
            () -> new GearItems.Pickaxe(SoFETiers.GLACIAL_IRON, 1, -2.8f, new Item.Properties()));

    // --- Relics of Act II (docs/Pociones.md, "Relic"): fixed affixes and one unique effect each
    public static final RegistryObject<Item> KALETH_BLADE = ITEMS.register("kaleth_blade",
            () -> new GearItems.Sword(SoFETiers.GLACIAL_IRON, 4, -2.4f, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> SERATH_FANG = ITEMS.register("serath_fang",
            () -> new GearItems.Sword(SoFETiers.GLACIAL_IRON, 2, -1.8f, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> VORATH_WRATH = ITEMS.register("vorath_wrath",
            () -> new GearItems.Axe(SoFETiers.GLACIAL_IRON, 7f, -3.1f, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    // --- the arsenal, batch 1 (docs/Pociones.md, "The arsenal"): melee weapons with traits, and the empire shields
    public static final RegistryObject<Item> IMPERIAL_HALBERD = ITEMS.register("imperial_halberd",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 5, -3.0f, 1.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.SWEEP), new Item.Properties()));
    public static final RegistryObject<Item> CITY_HAMMER = ITEMS.register("city_hammer",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.BRASS, 5, -3.2f, 0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.STUN), new Item.Properties()));
    public static final RegistryObject<Item> WAR_HAMMER = ITEMS.register("war_hammer",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 6, -3.3f, 0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SLAM), new Item.Properties()));
    public static final RegistryObject<Item> SERRATED_DAGGER = ITEMS.register("serrated_dagger",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.BRASS, 1, -1.5f, 0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.BLEED), new Item.Properties()));
    public static final RegistryObject<Item> HUNTING_KNIFE = ITEMS.register("hunting_knife",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.BRASS, 1, -1.4f, 0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.BEAST), new Item.Properties()));
    public static final RegistryObject<Item> JOUSTING_LANCE = ITEMS.register("jousting_lance",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.BRASS, 4, -3.0f, 2.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.CHARGE), new Item.Properties()));
    public static final RegistryObject<Item> EXPLORER_MACHETE = ITEMS.register("explorer_machete",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.BRASS, 2, -2.0f, 0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.PLANTS, com.sofe.gear.WeaponTrait.BLEED), new Item.Properties()));
    public static final RegistryObject<Item> WAR_MACE = ITEMS.register("war_mace",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 4, -2.8f, 0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.PIERCE), new Item.Properties()));
    public static final RegistryObject<Item> CHAIN_SWORD = ITEMS.register("chain_sword",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 2, -2.4f, 0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.MULTI_HIT), new Item.Properties()));
    public static final RegistryObject<Item> CRYSTAL_TRIDENT = ITEMS.register("crystal_trident",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 4, -2.9f, 1.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.FROST), new Item.Properties()));
    public static final RegistryObject<Item> SHADOW_SCYTHE = ITEMS.register("shadow_scythe",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 5, -3.0f, 1.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.SWEEP, com.sofe.gear.WeaponTrait.LIFE_STEAL), new Item.Properties()));
    public static final RegistryObject<Item> BATTLE_GREATSWORD = ITEMS.register("battle_greatsword",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 7, -3.2f, 0.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.SWEEP), new Item.Properties()));
    public static final RegistryObject<Item> PHLEGM_MACE = ITEMS.register("phlegm_mace",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.BRASS, 4, -2.9f, 0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SLOW), new Item.Properties()));
    public static final RegistryObject<Item> DOUBLE_FLAIL = ITEMS.register("double_flail",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 4, -2.9f, 0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.MULTI_HIT), new Item.Properties()));
    public static final RegistryObject<Item> HOOK_BLADE = ITEMS.register("hook_blade",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.BRASS, 3, -2.2f, 0.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.PULL, com.sofe.gear.WeaponTrait.BLEED), new Item.Properties()));
    public static final RegistryObject<Item> GRAVITY_HAMMER = ITEMS.register("gravity_hammer",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 6, -3.3f, 0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.KNOCK_UP), new Item.Properties()));
    public static final RegistryObject<Item> VORTEX_DAGGER = ITEMS.register("vortex_dagger",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 2, -1.6f, 0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.VOID), new Item.Properties()));
    public static final RegistryObject<Item> OBSIDIAN_RITUAL_DAGGER = ITEMS.register("obsidian_ritual_dagger",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 2, -1.7f, 0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SACRIFICE), new Item.Properties()));
    // the arsenal, batch 2b: weapons of Nordrath, Khemet, Aureum and the Infernal relics
    public static final RegistryObject<Item> BEARDED_AXE = ITEMS.register("bearded_axe",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 6, -3.1f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SWEEP), new Item.Properties()));
    public static final RegistryObject<Item> SEAX = ITEMS.register("seax",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 2, -1.8f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.BLEED), new Item.Properties()));
    public static final RegistryObject<Item> FROST_SPEAR = ITEMS.register("frost_spear",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 4, -2.8f, 1.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.FROST), new Item.Properties()));
    public static final RegistryObject<Item> KHOPESH = ITEMS.register("khopesh",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 4, -2.5f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.BLEED, com.sofe.gear.WeaponTrait.SWEEP), new Item.Properties()));
    public static final RegistryObject<Item> SCARAB_SICKLE = ITEMS.register("scarab_sickle",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 3, -2.0f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.WEAKEN, com.sofe.gear.WeaponTrait.PLANTS), new Item.Properties()));
    public static final RegistryObject<Item> JACKAL_GLAIVE = ITEMS.register("jackal_glaive",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 5, -3.0f, 1.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.LIFE_STEAL), new Item.Properties()));
    public static final RegistryObject<Item> WAS_SCEPTRE = ITEMS.register("was_sceptre",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 3, -2.6f, 0.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.WEAKEN), new Item.Properties()));
    public static final RegistryObject<Item> GLADIUS = ITEMS.register("gladius",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 5, -2.2f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.PIERCE), new Item.Properties()));
    public static final RegistryObject<Item> LEGION_PILUM = ITEMS.register("legion_pilum",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 5, -2.9f, 2.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.CHARGE), new Item.Properties()));
    public static final RegistryObject<Item> JUSTICAR_MAUL = ITEMS.register("justicar_maul",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 8, -3.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.STUN, com.sofe.gear.WeaponTrait.SLAM), new Item.Properties()));
    public static final RegistryObject<Item> SCALE_BLADE = ITEMS.register("scale_blade",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 6, -2.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SWEEP, com.sofe.gear.WeaponTrait.LIFE_STEAL), new Item.Properties()));
    public static final RegistryObject<Item> EMBER_BLADE = ITEMS.register("ember_blade",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 6, -2.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.BURN, com.sofe.gear.WeaponTrait.SWEEP), new Item.Properties()));
    public static final RegistryObject<Item> INFERNAL_GREATAXE = ITEMS.register("infernal_greataxe",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 8, -3.3f, 0.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.BURN, com.sofe.gear.WeaponTrait.SLAM), new Item.Properties()));
    public static final RegistryObject<Item> SULTHARI_SHIELD = ITEMS.register("sulthari_shield",
            () -> new com.sofe.gear.EmpireShield(com.sofe.gear.EmpireShield.Power.SULTHARI, new Item.Properties().durability(420)));
    public static final RegistryObject<Item> NORDRATH_SHIELD = ITEMS.register("nordrath_shield",
            () -> new com.sofe.gear.EmpireShield(com.sofe.gear.EmpireShield.Power.NORDRATH, new Item.Properties().durability(520)));
    public static final RegistryObject<Item> OBSERVATORY_SHIELD = ITEMS.register("observatory_shield",
            () -> new com.sofe.gear.EmpireShield(com.sofe.gear.EmpireShield.Power.OBSERVATORY, new Item.Properties().durability(460)));
    public static final RegistryObject<Item> VOID_SHIELD = ITEMS.register("void_shield",
            () -> new com.sofe.gear.EmpireShield(com.sofe.gear.EmpireShield.Power.VOID, new Item.Properties().durability(600)));

    // --- the arsenal, batch 2: armor sets (ArmorSets gives the bonus of a full set)
    public static final RegistryObject<Item> SCOUT_HELMET = armor("scout_helmet", SoFETiers.Armor.SCOUT, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SCOUT_CHESTPLATE = armor("scout_chestplate", SoFETiers.Armor.SCOUT, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SCOUT_LEGGINGS = armor("scout_leggings", SoFETiers.Armor.SCOUT, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SCOUT_BOOTS = armor("scout_boots", SoFETiers.Armor.SCOUT, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> BRONZE_HELMET = armor("bronze_helmet", SoFETiers.Armor.BRONZE, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> BRONZE_CHESTPLATE = armor("bronze_chestplate", SoFETiers.Armor.BRONZE, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> BRONZE_LEGGINGS = armor("bronze_leggings", SoFETiers.Armor.BRONZE, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> BRONZE_BOOTS = armor("bronze_boots", SoFETiers.Armor.BRONZE, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> GEAR_HELMET = armor("gear_helmet", SoFETiers.Armor.GEAR, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> GEAR_CHESTPLATE = armor("gear_chestplate", SoFETiers.Armor.GEAR, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> GEAR_LEGGINGS = armor("gear_leggings", SoFETiers.Armor.GEAR, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> GEAR_BOOTS = armor("gear_boots", SoFETiers.Armor.GEAR, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> OBSERVATORY_HELMET = armor("observatory_helmet", SoFETiers.Armor.OBSERVATORY, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> OBSERVATORY_CHESTPLATE = armor("observatory_chestplate", SoFETiers.Armor.OBSERVATORY, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> OBSERVATORY_LEGGINGS = armor("observatory_leggings", SoFETiers.Armor.OBSERVATORY, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> OBSERVATORY_BOOTS = armor("observatory_boots", SoFETiers.Armor.OBSERVATORY, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> EMERALD_HELMET = armor("emerald_helmet", SoFETiers.Armor.EMERALD, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> EMERALD_CHESTPLATE = armor("emerald_chestplate", SoFETiers.Armor.EMERALD, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> EMERALD_LEGGINGS = armor("emerald_leggings", SoFETiers.Armor.EMERALD, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> EMERALD_BOOTS = armor("emerald_boots", SoFETiers.Armor.EMERALD, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> COBALT_HELMET = armor("cobalt_helmet", SoFETiers.Armor.COBALT, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> COBALT_CHESTPLATE = armor("cobalt_chestplate", SoFETiers.Armor.COBALT, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> COBALT_LEGGINGS = armor("cobalt_leggings", SoFETiers.Armor.COBALT, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> COBALT_BOOTS = armor("cobalt_boots", SoFETiers.Armor.COBALT, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> SOLARI_HELMET = armor("solari_helmet", SoFETiers.Armor.SOLARI, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SOLARI_CHESTPLATE = armor("solari_chestplate", SoFETiers.Armor.SOLARI, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SOLARI_LEGGINGS = armor("solari_leggings", SoFETiers.Armor.SOLARI, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SOLARI_BOOTS = armor("solari_boots", SoFETiers.Armor.SOLARI, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> CHRONOMANCER_HELMET = armor("chronomancer_helmet", SoFETiers.Armor.CHRONOMANCER, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> CHRONOMANCER_CHESTPLATE = armor("chronomancer_chestplate", SoFETiers.Armor.CHRONOMANCER, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> CHRONOMANCER_LEGGINGS = armor("chronomancer_leggings", SoFETiers.Armor.CHRONOMANCER, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> CHRONOMANCER_BOOTS = armor("chronomancer_boots", SoFETiers.Armor.CHRONOMANCER, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> VOID_HELMET = armor("void_helmet", SoFETiers.Armor.VOID, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> VOID_CHESTPLATE = armor("void_chestplate", SoFETiers.Armor.VOID, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> VOID_LEGGINGS = armor("void_leggings", SoFETiers.Armor.VOID, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> VOID_BOOTS = armor("void_boots", SoFETiers.Armor.VOID, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> BERSERKER_HELMET = armor("berserker_helmet", SoFETiers.Armor.BERSERKER, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> BERSERKER_CHESTPLATE = armor("berserker_chestplate", SoFETiers.Armor.BERSERKER, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> BERSERKER_LEGGINGS = armor("berserker_leggings", SoFETiers.Armor.BERSERKER, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> BERSERKER_BOOTS = armor("berserker_boots", SoFETiers.Armor.BERSERKER, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> SEAFARER_HELMET = armor("seafarer_helmet", SoFETiers.Armor.SEAFARER, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SEAFARER_CHESTPLATE = armor("seafarer_chestplate", SoFETiers.Armor.SEAFARER, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SEAFARER_LEGGINGS = armor("seafarer_leggings", SoFETiers.Armor.SEAFARER, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SEAFARER_BOOTS = armor("seafarer_boots", SoFETiers.Armor.SEAFARER, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> SCARAB_HELMET = armor("scarab_helmet", SoFETiers.Armor.SCARAB, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SCARAB_CHESTPLATE = armor("scarab_chestplate", SoFETiers.Armor.SCARAB, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SCARAB_LEGGINGS = armor("scarab_leggings", SoFETiers.Armor.SCARAB, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SCARAB_BOOTS = armor("scarab_boots", SoFETiers.Armor.SCARAB, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> EMBALMER_HELMET = armor("embalmer_helmet", SoFETiers.Armor.EMBALMER, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> EMBALMER_CHESTPLATE = armor("embalmer_chestplate", SoFETiers.Armor.EMBALMER, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> EMBALMER_LEGGINGS = armor("embalmer_leggings", SoFETiers.Armor.EMBALMER, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> EMBALMER_BOOTS = armor("embalmer_boots", SoFETiers.Armor.EMBALMER, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> LEGION_HELMET = armor("legion_helmet", SoFETiers.Armor.LEGION, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> LEGION_CHESTPLATE = armor("legion_chestplate", SoFETiers.Armor.LEGION, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> LEGION_LEGGINGS = armor("legion_leggings", SoFETiers.Armor.LEGION, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> LEGION_BOOTS = armor("legion_boots", SoFETiers.Armor.LEGION, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> SCALE_HELMET = armor("scale_helmet", SoFETiers.Armor.SCALE, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SCALE_CHESTPLATE = armor("scale_chestplate", SoFETiers.Armor.SCALE, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SCALE_LEGGINGS = armor("scale_leggings", SoFETiers.Armor.SCALE, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SCALE_BOOTS = armor("scale_boots", SoFETiers.Armor.SCALE, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> INFERNAL_HELMET = armor("infernal_helmet", SoFETiers.Armor.INFERNAL, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> INFERNAL_CHESTPLATE = armor("infernal_chestplate", SoFETiers.Armor.INFERNAL, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> INFERNAL_LEGGINGS = armor("infernal_leggings", SoFETiers.Armor.INFERNAL, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> INFERNAL_BOOTS = armor("infernal_boots", SoFETiers.Armor.INFERNAL, ArmorItem.Type.BOOTS);

    // droppable Relics (uniques): weapons, armor and jewelry with fixed affixes and a unique effect (data/sofe/relics)
    public static final RegistryObject<Item> DUNESUNDER = ITEMS.register("dunesunder",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.BRASS, 4, -2.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SWEEP), new Item.Properties()));
    public static final RegistryObject<Item> WIDOWS_KISS = ITEMS.register("widows_kiss",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.BRASS, 2, -1.5f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.BLEED), new Item.Properties()));
    public static final RegistryObject<Item> SKALDBREAKER = ITEMS.register("skaldbreaker",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 7, -3.2f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SWEEP), new Item.Properties()));
    public static final RegistryObject<Item> RIMETOOTH = ITEMS.register("rimetooth",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 4, -2.8f, 1.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.FROST), new Item.Properties()));
    public static final RegistryObject<Item> JACKALS_JUDGEMENT = ITEMS.register("jackals_judgement",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 5, -3.0f, 1.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH), new Item.Properties()));
    public static final RegistryObject<Item> STORMCALLER = ITEMS.register("stormcaller",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 8, -3.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.STUN), new Item.Properties()));
    public static final RegistryObject<Item> GREEDS_CHAIN = ITEMS.register("greeds_chain",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 5, -3.0f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.MULTI_HIT), new Item.Properties()));
    public static final RegistryObject<Item> CROWN_OF_FIVE_SULTANS = armor("crown_of_five_sultans", SoFETiers.Armor.CROWN_OF_FIVE_SULTANS, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> WHITE_WOLF_MANTLE = armor("white_wolf_mantle", SoFETiers.Armor.WHITE_WOLF_MANTLE, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> CARAVAN_TREADS = armor("caravan_treads", SoFETiers.Armor.CARAVAN_TREADS, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> UNDYING_WRAPPINGS = armor("undying_wrappings", SoFETiers.Armor.UNDYING_WRAPPINGS, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> BLIND_JUDGE_HELM = armor("blind_judge_helm", SoFETiers.Armor.BLIND_JUDGE_HELM, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> FURNACE_HEART = armor("furnace_heart", SoFETiers.Armor.FURNACE_HEART, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SILVER_RING = trinket("silver_ring", GearSlot.JEWELRY);
    public static final RegistryObject<Item> GLACIAL_RING = trinket("glacial_ring", GearSlot.JEWELRY);
    public static final RegistryObject<Item> STAR_LAPIS_RING = trinket("star_lapis_ring", GearSlot.JEWELRY);
    public static final RegistryObject<Item> SOLAR_GOLD_RING = trinket("solar_gold_ring", GearSlot.JEWELRY);
    public static final RegistryObject<Item> ORICHALCUM_BAND = trinket("orichalcum_band", GearSlot.JEWELRY);
    public static final RegistryObject<Item> TURQUOISE_NECKLACE = trinket("turquoise_necklace", GearSlot.JEWELRY);
    public static final RegistryObject<Item> BONE_NECKLACE = trinket("bone_necklace", GearSlot.JEWELRY);
    public static final RegistryObject<Item> SCARAB_AMULET = trinket("scarab_amulet", GearSlot.JEWELRY);
    public static final RegistryObject<Item> LAUREL_TORC = trinket("laurel_torc", GearSlot.JEWELRY);
    public static final RegistryObject<Item> AETHERIUM_PENDANT = trinket("aetherium_pendant", GearSlot.JEWELRY);
    public static final RegistryObject<Item> EYE_OF_THE_FALSE_PROPHET = trinket("eye_of_the_false_prophet", GearSlot.JEWELRY);
    public static final RegistryObject<Item> RING_OF_THE_LAST_CARAVAN = trinket("ring_of_the_last_caravan", GearSlot.JEWELRY);
    public static final RegistryObject<Item> SOULKEEPER_RING = trinket("soulkeeper_ring", GearSlot.JEWELRY);
    public static final RegistryObject<Item> FROZEN_THRONE_BAND = trinket("frozen_throne_band", GearSlot.JEWELRY);
    public static final RegistryObject<Item> SERPENT_COIL = trinket("serpent_coil", GearSlot.JEWELRY);
    public static final RegistryObject<Item> BROKEN_PACT_SIGIL = trinket("broken_pact_sigil", GearSlot.JEWELRY);
    public static final RegistryObject<Item> VEILED_WEAPON = ITEMS.register("veiled_weapon", () -> new com.sofe.gear.VeiledItem(GearSlot.WEAPON, new Item.Properties()));
    public static final RegistryObject<Item> VEILED_ARMOR = ITEMS.register("veiled_armor", () -> new com.sofe.gear.VeiledItem(GearSlot.ARMOR, new Item.Properties()));
    public static final RegistryObject<Item> VEILED_JEWELRY = ITEMS.register("veiled_jewelry", () -> new com.sofe.gear.VeiledItem(GearSlot.JEWELRY, new Item.Properties()));

    // <generated-arsenal3> by scripts/make_arsenal3_data.py from scripts/arsenal3_catalog.py
    public static final RegistryObject<Item> STAR_LAPIS_PICKAXE = ITEMS.register("star_lapis_pickaxe",
            () -> new GearItems.Pickaxe(SoFETiers.STAR_LAPIS, 1, -2.8f, new Item.Properties()));
    public static final RegistryObject<Item> SOLAR_GOLD_PICKAXE = ITEMS.register("solar_gold_pickaxe",
            () -> new GearItems.Pickaxe(SoFETiers.SOLAR_GOLD, 1, -2.8f, new Item.Properties()));
    public static final RegistryObject<Item> ORICHALCUM_PICKAXE = ITEMS.register("orichalcum_pickaxe",
            () -> new GearItems.Pickaxe(SoFETiers.ORICHALCUM, 1, -2.8f, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> AETHERIUM_PICKAXE = ITEMS.register("aetherium_pickaxe",
            () -> new GearItems.Pickaxe(SoFETiers.AETHERIUM, 1, -2.8f, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> BRASS_MINING_HAMMER = ITEMS.register("brass_mining_hammer",
            () -> new com.sofe.gear.Tools.MiningHammer(SoFETiers.BRASS, 5, -3.4f, new Item.Properties()));
    public static final RegistryObject<Item> GLACIAL_MINING_HAMMER = ITEMS.register("glacial_mining_hammer",
            () -> new com.sofe.gear.Tools.MiningHammer(SoFETiers.GLACIAL_IRON, 6, -3.4f, new Item.Properties()));
    public static final RegistryObject<Item> GEARWORK_DRILL = ITEMS.register("gearwork_drill",
            () -> new com.sofe.gear.Tools.Drill(SoFETiers.STAR_LAPIS, 1, -2.6f, new Item.Properties()));
    public static final RegistryObject<Item> BRASS_JAVELIN = ITEMS.register("brass_javelin",
            () -> new com.sofe.gear.ranged.RangedItems.Javelin(7, Spell.NONE, false, new Item.Properties().durability(200)));
    public static final RegistryObject<Item> GLACIAL_JAVELIN = ITEMS.register("glacial_javelin",
            () -> new com.sofe.gear.ranged.RangedItems.Javelin(9, Spell.FROST, false, new Item.Properties().durability(320)));
    public static final RegistryObject<Item> AETHERIUM_JAVELIN = ITEMS.register("aetherium_javelin",
            () -> new com.sofe.gear.ranged.RangedItems.Javelin(11, Spell.ARCANE, true, new Item.Properties().durability(600)));
    public static final RegistryObject<Item> BRASS_THROWING_KNIFE = ITEMS.register("brass_throwing_knife",
            () -> new com.sofe.gear.ranged.RangedItems.ThrowingKnife(4, Spell.NONE, 1, null, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> GLACIAL_THROWING_KNIFE = ITEMS.register("glacial_throwing_knife",
            () -> new com.sofe.gear.ranged.RangedItems.ThrowingKnife(5, Spell.FROST, 1, null, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> VENOM_THROWING_KNIFE = ITEMS.register("venom_throwing_knife",
            () -> new com.sofe.gear.ranged.RangedItems.ThrowingKnife(4, Spell.POISON, 1, null, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SULTHARI_RECURVE_BOW = ITEMS.register("sulthari_recurve_bow",
            () -> new com.sofe.gear.ranged.RangedItems.SoFEBow(14, 1.0f, 1.0f, Spell.NONE, new Item.Properties().durability(420)));
    public static final RegistryObject<Item> NORDRATH_LONGBOW = ITEMS.register("nordrath_longbow",
            () -> new com.sofe.gear.ranged.RangedItems.SoFEBow(30, 1.5f, 1.2f, Spell.NONE, new Item.Properties().durability(500)));
    public static final RegistryObject<Item> FROSTBITE_BOW = ITEMS.register("frostbite_bow",
            () -> new com.sofe.gear.ranged.RangedItems.SoFEBow(20, 1.1f, 1.0f, Spell.FROST, new Item.Properties().durability(520)));
    public static final RegistryObject<Item> SOLAR_BOW = ITEMS.register("solar_bow",
            () -> new com.sofe.gear.ranged.RangedItems.SoFEBow(18, 1.2f, 1.1f, Spell.EMBER, new Item.Properties().durability(700)));
    public static final RegistryObject<Item> VOID_BOW = ITEMS.register("void_bow",
            () -> new com.sofe.gear.ranged.RangedItems.SoFEBow(22, 1.3f, 1.1f, Spell.VOID, new Item.Properties().durability(800)));
    public static final RegistryObject<Item> BRASS_ARBALEST = ITEMS.register("brass_arbalest",
            () -> new com.sofe.gear.ranged.RangedItems.SoFECrossbow(1.6f, Spell.NONE, 1, new Item.Properties().durability(500)));
    public static final RegistryObject<Item> REPEATING_CROSSBOW = ITEMS.register("repeating_crossbow",
            () -> new com.sofe.gear.ranged.RangedItems.SoFECrossbow(0.9f, Spell.NONE, 3, new Item.Properties().durability(600)));
    public static final RegistryObject<Item> GLACIAL_CROSSBOW = ITEMS.register("glacial_crossbow",
            () -> new com.sofe.gear.ranged.RangedItems.SoFECrossbow(1.3f, Spell.FROST, 1, new Item.Properties().durability(650)));
    public static final RegistryObject<Item> BRASS_PISTOL = ITEMS.register("brass_pistol",
            () -> new com.sofe.gear.ranged.RangedItems.Firearm(8, 1, 1.0f, 25, null, new Item.Properties().durability(400)));
    public static final RegistryObject<Item> GEARWORK_MUSKET = ITEMS.register("gearwork_musket",
            () -> new com.sofe.gear.ranged.RangedItems.Firearm(15, 1, 0.2f, 45, null, new Item.Properties().durability(500)));
    public static final RegistryObject<Item> BLUNDERBUSS = ITEMS.register("blunderbuss",
            () -> new com.sofe.gear.ranged.RangedItems.Firearm(3.5f, 6, 8.0f, 50, null, new Item.Properties().durability(400)));
    public static final RegistryObject<Item> BRASS_CARTRIDGE = ITEMS.register("brass_cartridge",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> EMBER_STAFF = ITEMS.register("ember_staff",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.EMBER, CastMode.BOLT, 6, 20, null, new Item.Properties().durability(300)));
    public static final RegistryObject<Item> FROST_STAFF = ITEMS.register("frost_staff",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.FROST, CastMode.BOLT, 5, 20, null, new Item.Properties().durability(300)));
    public static final RegistryObject<Item> STORM_STAFF = ITEMS.register("storm_staff",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.STORM, CastMode.LIGHTNING, 9, 50, null, new Item.Properties().durability(300)));
    public static final RegistryObject<Item> VOID_STAFF = ITEMS.register("void_staff",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.VOID, CastMode.BOLT, 8, 30, null, new Item.Properties().durability(400)));
    public static final RegistryObject<Item> SOUL_STAFF = ITEMS.register("soul_staff",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.SOUL, CastMode.BEAM, 6, 30, null, new Item.Properties().durability(350)));
    public static final RegistryObject<Item> TOME_OF_EMBERS = ITEMS.register("tome_of_embers",
            () -> new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.EMBERS, 6, 200, null, new Item.Properties().durability(80)));
    public static final RegistryObject<Item> TOME_OF_FROST = ITEMS.register("tome_of_frost",
            () -> new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.FROST, 5, 200, null, new Item.Properties().durability(80)));
    public static final RegistryObject<Item> TOME_OF_WARDS = ITEMS.register("tome_of_wards",
            () -> new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.WARDS, 1, 600, null, new Item.Properties().durability(60)));
    public static final RegistryObject<Item> TOME_OF_THE_GALE = ITEMS.register("tome_of_the_gale",
            () -> new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.GALE, 1.6f, 240, null, new Item.Properties().durability(80)));
    public static final RegistryObject<Item> BOOK_OF_SOULS = ITEMS.register("book_of_souls",
            () -> new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.SOULS, 4, 300, null, new Item.Properties().durability(80)));
    public static final RegistryObject<Item> CLOCKWORK_BOMB = ITEMS.register("clockwork_bomb",
            () -> new com.sofe.gear.ranged.RangedItems.Gadget(com.sofe.entity.projectile.Bomb.Kind.CLOCKWORK, new Item.Properties()));
    public static final RegistryObject<Item> SMOKE_BOMB = ITEMS.register("smoke_bomb",
            () -> new com.sofe.gear.ranged.RangedItems.Gadget(com.sofe.entity.projectile.Bomb.Kind.SMOKE, new Item.Properties()));
    public static final RegistryObject<Item> FIRE_BOMB = ITEMS.register("fire_bomb",
            () -> new com.sofe.gear.ranged.RangedItems.Gadget(com.sofe.entity.projectile.Bomb.Kind.FIRE, new Item.Properties()));
    public static final RegistryObject<Item> OATHBLADE = ITEMS.register("oathblade",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 5, -2.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SWEEP, com.sofe.gear.WeaponTrait.HOLY), "knight", new Item.Properties()));
    public static final RegistryObject<Item> AUREUM_WARMACE = ITEMS.register("aureum_warmace",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 6, -3.0f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.STUN, com.sofe.gear.WeaponTrait.HOLY), "knight", new Item.Properties()));
    public static final RegistryObject<Item> LANCE_OF_THE_SCALE = ITEMS.register("lance_of_the_scale",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 6, -3.1f, 2.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.CHARGE, com.sofe.gear.WeaponTrait.HOLY), "knight", new Item.Properties()));
    public static final RegistryObject<Item> BONE_WAND = ITEMS.register("bone_wand",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.BONE, CastMode.BOLT, 6, 14, "necromancer", new Item.Properties().durability(250)));
    public static final RegistryObject<Item> SOUL_WAND = ITEMS.register("soul_wand",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.SOUL, CastMode.BEAM, 5, 20, "necromancer", new Item.Properties().durability(300)));
    public static final RegistryObject<Item> REAPER_SICKLE = ITEMS.register("reaper_sickle",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 4, -2.2f, 0.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.LIFE_STEAL, com.sofe.gear.WeaponTrait.WEAKEN), "necromancer", new Item.Properties()));
    public static final RegistryObject<Item> EMBER_ORB = ITEMS.register("ember_orb",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.EMBER, CastMode.BOLT, 7, 12, "sorceress", new Item.Properties().durability(300)));
    public static final RegistryObject<Item> FROST_ORB = ITEMS.register("frost_orb",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.FROST, CastMode.BOLT, 6, 12, "sorceress", new Item.Properties().durability(300)));
    public static final RegistryObject<Item> STORM_ORB = ITEMS.register("storm_orb",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.STORM, CastMode.LIGHTNING, 10, 36, "sorceress", new Item.Properties().durability(350)));
    public static final RegistryObject<Item> SHADOW_CLAWS = ITEMS.register("shadow_claws",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 3, -1.8f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.MULTI_HIT, com.sofe.gear.WeaponTrait.BLEED), "thief", new Item.Properties()));
    public static final RegistryObject<Item> VIPER_CLAWS = ITEMS.register("viper_claws",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 4, -1.8f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.MULTI_HIT, com.sofe.gear.WeaponTrait.POISON), "thief", new Item.Properties()));
    public static final RegistryObject<Item> THROWING_STARS = ITEMS.register("throwing_stars",
            () -> new com.sofe.gear.ranged.RangedItems.ThrowingKnife(3, Spell.BLEED, 3, "thief", new Item.Properties().stacksTo(32)));
    public static final RegistryObject<Item> ROYAL_SCEPTER = ITEMS.register("royal_scepter",
            () -> new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.DECREE, 0, 600, "king", new Item.Properties().durability(100)));
    public static final RegistryObject<Item> SULTAN_SABER = ITEMS.register("sultan_saber",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 5, -2.3f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SWEEP, com.sofe.gear.WeaponTrait.BLEED), "king", new Item.Properties()));
    public static final RegistryObject<Item> ROYAL_FLINTLOCK = ITEMS.register("royal_flintlock",
            () -> new com.sofe.gear.ranged.RangedItems.Firearm(12, 1, 0.3f, 30, "king", new Item.Properties().durability(500)));
    public static final RegistryObject<Item> SPELL_EMBER = ITEMS.register("spell_ember",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPELL_FROST = ITEMS.register("spell_frost",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPELL_VOID = ITEMS.register("spell_void",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPELL_SOUL = ITEMS.register("spell_soul",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPELL_BONE = ITEMS.register("spell_bone",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPELL_HOLY = ITEMS.register("spell_holy",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPELL_STORM = ITEMS.register("spell_storm",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPELL_ARCANE = ITEMS.register("spell_arcane",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LEAD_SHOT = ITEMS.register("lead_shot",
            () -> new Item(new Item.Properties()));
    /** Held like a sword (handheld and large models). */
    private static final List<RegistryObject<Item>> A3_HANDHELD = List.of(
            STAR_LAPIS_PICKAXE, SOLAR_GOLD_PICKAXE, ORICHALCUM_PICKAXE, AETHERIUM_PICKAXE, BRASS_MINING_HAMMER, GLACIAL_MINING_HAMMER,
            BRASS_JAVELIN, GLACIAL_JAVELIN, AETHERIUM_JAVELIN, BRASS_THROWING_KNIFE, GLACIAL_THROWING_KNIFE, VENOM_THROWING_KNIFE,
            EMBER_STAFF, FROST_STAFF, STORM_STAFF, VOID_STAFF, SOUL_STAFF, OATHBLADE,
            AUREUM_WARMACE, LANCE_OF_THE_SCALE, BONE_WAND, SOUL_WAND, REAPER_SICKLE, EMBER_ORB,
            FROST_ORB, STORM_ORB, SHADOW_CLAWS, VIPER_CLAWS, ROYAL_SCEPTER, SULTAN_SABER);
    /** Held bigger. */
    private static final List<RegistryObject<Item>> A3_LARGE = List.of(
            BRASS_MINING_HAMMER, GLACIAL_MINING_HAMMER, BRASS_JAVELIN, GLACIAL_JAVELIN, AETHERIUM_JAVELIN, EMBER_STAFF,
            FROST_STAFF, STORM_STAFF, VOID_STAFF, SOUL_STAFF, AUREUM_WARMACE, LANCE_OF_THE_SCALE);
    /** Held barrel forward. */
    private static final List<RegistryObject<Item>> A3_GUNS = List.of(
            GEARWORK_DRILL, BRASS_PISTOL, GEARWORK_MUSKET, BLUNDERBUSS, ROYAL_FLINTLOCK);
    /** Bows: their models pull back while drawn. */
    private static final List<RegistryObject<Item>> A3_BOWS = List.of(
            SULTHARI_RECURVE_BOW, NORDRATH_LONGBOW, FROSTBITE_BOW, SOLAR_BOW, VOID_BOW);
    /** Crossbows: their models pull and show the loaded bolt. */
    private static final List<RegistryObject<Item>> A3_CROSSBOWS = List.of(
            BRASS_ARBALEST, REPEATING_CROSSBOW, GLACIAL_CROSSBOW);
    /** Flat item models. */
    private static final List<RegistryObject<Item>> A3_FLAT = List.of(
            BRASS_CARTRIDGE, TOME_OF_EMBERS, TOME_OF_FROST, TOME_OF_WARDS, TOME_OF_THE_GALE, BOOK_OF_SOULS,
            CLOCKWORK_BOMB, SMOKE_BOMB, FIRE_BOMB, THROWING_STARS, SPELL_EMBER, SPELL_FROST,
            SPELL_VOID, SPELL_SOUL, SPELL_BONE, SPELL_HOLY, SPELL_STORM, SPELL_ARCANE,
            LEAD_SHOT);
    /** In the equipment tab besides the handheld ones. */
    private static final List<RegistryObject<Item>> A3_TAB_EXTRA = List.of(
            GEARWORK_DRILL, SULTHARI_RECURVE_BOW, NORDRATH_LONGBOW, FROSTBITE_BOW, SOLAR_BOW, VOID_BOW,
            BRASS_ARBALEST, REPEATING_CROSSBOW, GLACIAL_CROSSBOW, BRASS_PISTOL, GEARWORK_MUSKET, BLUNDERBUSS,
            BRASS_CARTRIDGE, TOME_OF_EMBERS, TOME_OF_FROST, TOME_OF_WARDS, TOME_OF_THE_GALE, BOOK_OF_SOULS,
            CLOCKWORK_BOMB, SMOKE_BOMB, FIRE_BOMB, THROWING_STARS, ROYAL_FLINTLOCK);
    // </generated-arsenal3>

    // <generated-armor> by scripts/make_armor_data.py from scripts/armor_catalog.py: the class sets
    public static final RegistryObject<Item> SENTINEL_HELMET = armor("sentinel_helmet", SoFETiers.Armor.SENTINEL, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SENTINEL_CHESTPLATE = armor("sentinel_chestplate", SoFETiers.Armor.SENTINEL, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SENTINEL_LEGGINGS = armor("sentinel_leggings", SoFETiers.Armor.SENTINEL, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SENTINEL_BOOTS = armor("sentinel_boots", SoFETiers.Armor.SENTINEL, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> DRAGONKNIGHT_HELMET = armor("dragonknight_helmet", SoFETiers.Armor.DRAGONKNIGHT, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> DRAGONKNIGHT_CHESTPLATE = armor("dragonknight_chestplate", SoFETiers.Armor.DRAGONKNIGHT, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> DRAGONKNIGHT_LEGGINGS = armor("dragonknight_leggings", SoFETiers.Armor.DRAGONKNIGHT, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> DRAGONKNIGHT_BOOTS = armor("dragonknight_boots", SoFETiers.Armor.DRAGONKNIGHT, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> WARLORD_HELMET = armor("warlord_helmet", SoFETiers.Armor.WARLORD, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> WARLORD_CHESTPLATE = armor("warlord_chestplate", SoFETiers.Armor.WARLORD, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> WARLORD_LEGGINGS = armor("warlord_leggings", SoFETiers.Armor.WARLORD, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> WARLORD_BOOTS = armor("warlord_boots", SoFETiers.Armor.WARLORD, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> GRAVEWARDEN_HELMET = armor("gravewarden_helmet", SoFETiers.Armor.GRAVEWARDEN, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> GRAVEWARDEN_CHESTPLATE = armor("gravewarden_chestplate", SoFETiers.Armor.GRAVEWARDEN, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> GRAVEWARDEN_LEGGINGS = armor("gravewarden_leggings", SoFETiers.Armor.GRAVEWARDEN, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> GRAVEWARDEN_BOOTS = armor("gravewarden_boots", SoFETiers.Armor.GRAVEWARDEN, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> BONELORD_HELMET = armor("bonelord_helmet", SoFETiers.Armor.BONELORD, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> BONELORD_CHESTPLATE = armor("bonelord_chestplate", SoFETiers.Armor.BONELORD, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> BONELORD_LEGGINGS = armor("bonelord_leggings", SoFETiers.Armor.BONELORD, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> BONELORD_BOOTS = armor("bonelord_boots", SoFETiers.Armor.BONELORD, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> ANUBIS_HELMET = armor("anubis_helmet", SoFETiers.Armor.ANUBIS, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> ANUBIS_CHESTPLATE = armor("anubis_chestplate", SoFETiers.Armor.ANUBIS, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> ANUBIS_LEGGINGS = armor("anubis_leggings", SoFETiers.Armor.ANUBIS, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> ANUBIS_BOOTS = armor("anubis_boots", SoFETiers.Armor.ANUBIS, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> PLAGUEBEARER_HELMET = armor("plaguebearer_helmet", SoFETiers.Armor.PLAGUEBEARER, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> PLAGUEBEARER_CHESTPLATE = armor("plaguebearer_chestplate", SoFETiers.Armor.PLAGUEBEARER, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> PLAGUEBEARER_LEGGINGS = armor("plaguebearer_leggings", SoFETiers.Armor.PLAGUEBEARER, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> PLAGUEBEARER_BOOTS = armor("plaguebearer_boots", SoFETiers.Armor.PLAGUEBEARER, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> LICH_HELMET = armor("lich_helmet", SoFETiers.Armor.LICH, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> LICH_CHESTPLATE = armor("lich_chestplate", SoFETiers.Armor.LICH, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> LICH_LEGGINGS = armor("lich_leggings", SoFETiers.Armor.LICH, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> LICH_BOOTS = armor("lich_boots", SoFETiers.Armor.LICH, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> MUMMY_LORD_HELMET = armor("mummy_lord_helmet", SoFETiers.Armor.MUMMY_LORD, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> MUMMY_LORD_CHESTPLATE = armor("mummy_lord_chestplate", SoFETiers.Armor.MUMMY_LORD, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> MUMMY_LORD_LEGGINGS = armor("mummy_lord_leggings", SoFETiers.Armor.MUMMY_LORD, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> MUMMY_LORD_BOOTS = armor("mummy_lord_boots", SoFETiers.Armor.MUMMY_LORD, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> SOULREAVER_HELMET = armor("soulreaver_helmet", SoFETiers.Armor.SOULREAVER, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SOULREAVER_CHESTPLATE = armor("soulreaver_chestplate", SoFETiers.Armor.SOULREAVER, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SOULREAVER_LEGGINGS = armor("soulreaver_leggings", SoFETiers.Armor.SOULREAVER, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SOULREAVER_BOOTS = armor("soulreaver_boots", SoFETiers.Armor.SOULREAVER, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> ARCHMAGE_HELMET = armor("archmage_helmet", SoFETiers.Armor.ARCHMAGE, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> ARCHMAGE_CHESTPLATE = armor("archmage_chestplate", SoFETiers.Armor.ARCHMAGE, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> ARCHMAGE_LEGGINGS = armor("archmage_leggings", SoFETiers.Armor.ARCHMAGE, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> ARCHMAGE_BOOTS = armor("archmage_boots", SoFETiers.Armor.ARCHMAGE, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> PYROMANCER_HELMET = armor("pyromancer_helmet", SoFETiers.Armor.PYROMANCER, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> PYROMANCER_CHESTPLATE = armor("pyromancer_chestplate", SoFETiers.Armor.PYROMANCER, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> PYROMANCER_LEGGINGS = armor("pyromancer_leggings", SoFETiers.Armor.PYROMANCER, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> PYROMANCER_BOOTS = armor("pyromancer_boots", SoFETiers.Armor.PYROMANCER, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> FROST_WITCH_HELMET = armor("frost_witch_helmet", SoFETiers.Armor.FROST_WITCH, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> FROST_WITCH_CHESTPLATE = armor("frost_witch_chestplate", SoFETiers.Armor.FROST_WITCH, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> FROST_WITCH_LEGGINGS = armor("frost_witch_leggings", SoFETiers.Armor.FROST_WITCH, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> FROST_WITCH_BOOTS = armor("frost_witch_boots", SoFETiers.Armor.FROST_WITCH, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> TEMPEST_HELMET = armor("tempest_helmet", SoFETiers.Armor.TEMPEST, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> TEMPEST_CHESTPLATE = armor("tempest_chestplate", SoFETiers.Armor.TEMPEST, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> TEMPEST_LEGGINGS = armor("tempest_leggings", SoFETiers.Armor.TEMPEST, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> TEMPEST_BOOTS = armor("tempest_boots", SoFETiers.Armor.TEMPEST, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> ENCHANTRESS_HELMET = armor("enchantress_helmet", SoFETiers.Armor.ENCHANTRESS, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> ENCHANTRESS_CHESTPLATE = armor("enchantress_chestplate", SoFETiers.Armor.ENCHANTRESS, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> ENCHANTRESS_LEGGINGS = armor("enchantress_leggings", SoFETiers.Armor.ENCHANTRESS, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> ENCHANTRESS_BOOTS = armor("enchantress_boots", SoFETiers.Armor.ENCHANTRESS, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> WITCH_HELMET = armor("witch_helmet", SoFETiers.Armor.WITCH, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> WITCH_CHESTPLATE = armor("witch_chestplate", SoFETiers.Armor.WITCH, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> WITCH_LEGGINGS = armor("witch_leggings", SoFETiers.Armor.WITCH, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> WITCH_BOOTS = armor("witch_boots", SoFETiers.Armor.WITCH, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> ASTRAL_SAGE_HELMET = armor("astral_sage_helmet", SoFETiers.Armor.ASTRAL_SAGE, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> ASTRAL_SAGE_CHESTPLATE = armor("astral_sage_chestplate", SoFETiers.Armor.ASTRAL_SAGE, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> ASTRAL_SAGE_LEGGINGS = armor("astral_sage_leggings", SoFETiers.Armor.ASTRAL_SAGE, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> ASTRAL_SAGE_BOOTS = armor("astral_sage_boots", SoFETiers.Armor.ASTRAL_SAGE, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> SHADOW_HELMET = armor("shadow_helmet", SoFETiers.Armor.SHADOW, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SHADOW_CHESTPLATE = armor("shadow_chestplate", SoFETiers.Armor.SHADOW, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SHADOW_LEGGINGS = armor("shadow_leggings", SoFETiers.Armor.SHADOW, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SHADOW_BOOTS = armor("shadow_boots", SoFETiers.Armor.SHADOW, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> WOLF_RAIDER_HELMET = armor("wolf_raider_helmet", SoFETiers.Armor.WOLF_RAIDER, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> WOLF_RAIDER_CHESTPLATE = armor("wolf_raider_chestplate", SoFETiers.Armor.WOLF_RAIDER, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> WOLF_RAIDER_LEGGINGS = armor("wolf_raider_leggings", SoFETiers.Armor.WOLF_RAIDER, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> WOLF_RAIDER_BOOTS = armor("wolf_raider_boots", SoFETiers.Armor.WOLF_RAIDER, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> HUNTSMAN_HELMET = armor("huntsman_helmet", SoFETiers.Armor.HUNTSMAN, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> HUNTSMAN_CHESTPLATE = armor("huntsman_chestplate", SoFETiers.Armor.HUNTSMAN, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> HUNTSMAN_LEGGINGS = armor("huntsman_leggings", SoFETiers.Armor.HUNTSMAN, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> HUNTSMAN_BOOTS = armor("huntsman_boots", SoFETiers.Armor.HUNTSMAN, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> NIGHTBLADE_HELMET = armor("nightblade_helmet", SoFETiers.Armor.NIGHTBLADE, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> NIGHTBLADE_CHESTPLATE = armor("nightblade_chestplate", SoFETiers.Armor.NIGHTBLADE, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> NIGHTBLADE_LEGGINGS = armor("nightblade_leggings", SoFETiers.Armor.NIGHTBLADE, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> NIGHTBLADE_BOOTS = armor("nightblade_boots", SoFETiers.Armor.NIGHTBLADE, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> FROST_STALKER_HELMET = armor("frost_stalker_helmet", SoFETiers.Armor.FROST_STALKER, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> FROST_STALKER_CHESTPLATE = armor("frost_stalker_chestplate", SoFETiers.Armor.FROST_STALKER, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> FROST_STALKER_LEGGINGS = armor("frost_stalker_leggings", SoFETiers.Armor.FROST_STALKER, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> FROST_STALKER_BOOTS = armor("frost_stalker_boots", SoFETiers.Armor.FROST_STALKER, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> CORSAIR_HELMET = armor("corsair_helmet", SoFETiers.Armor.CORSAIR, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> CORSAIR_CHESTPLATE = armor("corsair_chestplate", SoFETiers.Armor.CORSAIR, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> CORSAIR_LEGGINGS = armor("corsair_leggings", SoFETiers.Armor.CORSAIR, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> CORSAIR_BOOTS = armor("corsair_boots", SoFETiers.Armor.CORSAIR, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> SULTAN_HELMET = armor("sultan_helmet", SoFETiers.Armor.SULTAN, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SULTAN_CHESTPLATE = armor("sultan_chestplate", SoFETiers.Armor.SULTAN, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SULTAN_LEGGINGS = armor("sultan_leggings", SoFETiers.Armor.SULTAN, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SULTAN_BOOTS = armor("sultan_boots", SoFETiers.Armor.SULTAN, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> VIZIER_HELMET = armor("vizier_helmet", SoFETiers.Armor.VIZIER, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> VIZIER_CHESTPLATE = armor("vizier_chestplate", SoFETiers.Armor.VIZIER, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> VIZIER_LEGGINGS = armor("vizier_leggings", SoFETiers.Armor.VIZIER, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> VIZIER_BOOTS = armor("vizier_boots", SoFETiers.Armor.VIZIER, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> ROYAL_GUARD_HELMET = armor("royal_guard_helmet", SoFETiers.Armor.ROYAL_GUARD, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> ROYAL_GUARD_CHESTPLATE = armor("royal_guard_chestplate", SoFETiers.Armor.ROYAL_GUARD, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> ROYAL_GUARD_LEGGINGS = armor("royal_guard_leggings", SoFETiers.Armor.ROYAL_GUARD, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> ROYAL_GUARD_BOOTS = armor("royal_guard_boots", SoFETiers.Armor.ROYAL_GUARD, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> DESERT_EMIR_HELMET = armor("desert_emir_helmet", SoFETiers.Armor.DESERT_EMIR, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> DESERT_EMIR_CHESTPLATE = armor("desert_emir_chestplate", SoFETiers.Armor.DESERT_EMIR, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> DESERT_EMIR_LEGGINGS = armor("desert_emir_leggings", SoFETiers.Armor.DESERT_EMIR, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> DESERT_EMIR_BOOTS = armor("desert_emir_boots", SoFETiers.Armor.DESERT_EMIR, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> GOLDEN_KING_HELMET = armor("golden_king_helmet", SoFETiers.Armor.GOLDEN_KING, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> GOLDEN_KING_CHESTPLATE = armor("golden_king_chestplate", SoFETiers.Armor.GOLDEN_KING, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> GOLDEN_KING_LEGGINGS = armor("golden_king_leggings", SoFETiers.Armor.GOLDEN_KING, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> GOLDEN_KING_BOOTS = armor("golden_king_boots", SoFETiers.Armor.GOLDEN_KING, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> MIRAGE_HELMET = armor("mirage_helmet", SoFETiers.Armor.MIRAGE, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> MIRAGE_CHESTPLATE = armor("mirage_chestplate", SoFETiers.Armor.MIRAGE, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> MIRAGE_LEGGINGS = armor("mirage_leggings", SoFETiers.Armor.MIRAGE, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> MIRAGE_BOOTS = armor("mirage_boots", SoFETiers.Armor.MIRAGE, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> LION_KING_HELMET = armor("lion_king_helmet", SoFETiers.Armor.LION_KING, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> LION_KING_CHESTPLATE = armor("lion_king_chestplate", SoFETiers.Armor.LION_KING, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> LION_KING_LEGGINGS = armor("lion_king_leggings", SoFETiers.Armor.LION_KING, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> LION_KING_BOOTS = armor("lion_king_boots", SoFETiers.Armor.LION_KING, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> PEACOCK_HELMET = armor("peacock_helmet", SoFETiers.Armor.PEACOCK, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> PEACOCK_CHESTPLATE = armor("peacock_chestplate", SoFETiers.Armor.PEACOCK, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> PEACOCK_LEGGINGS = armor("peacock_leggings", SoFETiers.Armor.PEACOCK, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> PEACOCK_BOOTS = armor("peacock_boots", SoFETiers.Armor.PEACOCK, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> PHARAOH_HELMET = armor("pharaoh_helmet", SoFETiers.Armor.PHARAOH, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> PHARAOH_CHESTPLATE = armor("pharaoh_chestplate", SoFETiers.Armor.PHARAOH, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> PHARAOH_LEGGINGS = armor("pharaoh_leggings", SoFETiers.Armor.PHARAOH, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> PHARAOH_BOOTS = armor("pharaoh_boots", SoFETiers.Armor.PHARAOH, ArmorItem.Type.BOOTS);
    /** Every set piece, by class and level, for the creative tab. */
    private static final List<RegistryObject<Item>> SET_PIECES = List.of(
            BRONZE_HELMET, BRONZE_CHESTPLATE, BRONZE_LEGGINGS, BRONZE_BOOTS, SENTINEL_HELMET, SENTINEL_CHESTPLATE, SENTINEL_LEGGINGS, SENTINEL_BOOTS,
            GLACIAL_IRON_HELMET, GLACIAL_IRON_CHESTPLATE, GLACIAL_IRON_LEGGINGS, GLACIAL_IRON_BOOTS, COBALT_HELMET, COBALT_CHESTPLATE, COBALT_LEGGINGS, COBALT_BOOTS,
            WARLORD_HELMET, WARLORD_CHESTPLATE, WARLORD_LEGGINGS, WARLORD_BOOTS, SOLARI_HELMET, SOLARI_CHESTPLATE, SOLARI_LEGGINGS, SOLARI_BOOTS,
            DRAGONKNIGHT_HELMET, DRAGONKNIGHT_CHESTPLATE, DRAGONKNIGHT_LEGGINGS, DRAGONKNIGHT_BOOTS, LEGION_HELMET, LEGION_CHESTPLATE, LEGION_LEGGINGS, LEGION_BOOTS,
            SCALE_HELMET, SCALE_CHESTPLATE, SCALE_LEGGINGS, SCALE_BOOTS, INFERNAL_HELMET, INFERNAL_CHESTPLATE, INFERNAL_LEGGINGS, INFERNAL_BOOTS,
            GRAVEWARDEN_HELMET, GRAVEWARDEN_CHESTPLATE, GRAVEWARDEN_LEGGINGS, GRAVEWARDEN_BOOTS, PLAGUEBEARER_HELMET, PLAGUEBEARER_CHESTPLATE, PLAGUEBEARER_LEGGINGS, PLAGUEBEARER_BOOTS,
            SOULREAVER_HELMET, SOULREAVER_CHESTPLATE, SOULREAVER_LEGGINGS, SOULREAVER_BOOTS, BONELORD_HELMET, BONELORD_CHESTPLATE, BONELORD_LEGGINGS, BONELORD_BOOTS,
            EMBALMER_HELMET, EMBALMER_CHESTPLATE, EMBALMER_LEGGINGS, EMBALMER_BOOTS, SCARAB_HELMET, SCARAB_CHESTPLATE, SCARAB_LEGGINGS, SCARAB_BOOTS,
            MUMMY_LORD_HELMET, MUMMY_LORD_CHESTPLATE, MUMMY_LORD_LEGGINGS, MUMMY_LORD_BOOTS, VOID_HELMET, VOID_CHESTPLATE, VOID_LEGGINGS, VOID_BOOTS,
            ANUBIS_HELMET, ANUBIS_CHESTPLATE, ANUBIS_LEGGINGS, ANUBIS_BOOTS, LICH_HELMET, LICH_CHESTPLATE, LICH_LEGGINGS, LICH_BOOTS,
            WITCH_HELMET, WITCH_CHESTPLATE, WITCH_LEGGINGS, WITCH_BOOTS, ENCHANTRESS_HELMET, ENCHANTRESS_CHESTPLATE, ENCHANTRESS_LEGGINGS, ENCHANTRESS_BOOTS,
            FROST_WITCH_HELMET, FROST_WITCH_CHESTPLATE, FROST_WITCH_LEGGINGS, FROST_WITCH_BOOTS, OBSERVATORY_HELMET, OBSERVATORY_CHESTPLATE, OBSERVATORY_LEGGINGS, OBSERVATORY_BOOTS,
            EMERALD_HELMET, EMERALD_CHESTPLATE, EMERALD_LEGGINGS, EMERALD_BOOTS, PYROMANCER_HELMET, PYROMANCER_CHESTPLATE, PYROMANCER_LEGGINGS, PYROMANCER_BOOTS,
            CHRONOMANCER_HELMET, CHRONOMANCER_CHESTPLATE, CHRONOMANCER_LEGGINGS, CHRONOMANCER_BOOTS, TEMPEST_HELMET, TEMPEST_CHESTPLATE, TEMPEST_LEGGINGS, TEMPEST_BOOTS,
            ARCHMAGE_HELMET, ARCHMAGE_CHESTPLATE, ARCHMAGE_LEGGINGS, ARCHMAGE_BOOTS, ASTRAL_SAGE_HELMET, ASTRAL_SAGE_CHESTPLATE, ASTRAL_SAGE_LEGGINGS, ASTRAL_SAGE_BOOTS,
            SCOUT_HELMET, SCOUT_CHESTPLATE, SCOUT_LEGGINGS, SCOUT_BOOTS, HUNTSMAN_HELMET, HUNTSMAN_CHESTPLATE, HUNTSMAN_LEGGINGS, HUNTSMAN_BOOTS,
            GEAR_HELMET, GEAR_CHESTPLATE, GEAR_LEGGINGS, GEAR_BOOTS, SEAFARER_HELMET, SEAFARER_CHESTPLATE, SEAFARER_LEGGINGS, SEAFARER_BOOTS,
            BERSERKER_HELMET, BERSERKER_CHESTPLATE, BERSERKER_LEGGINGS, BERSERKER_BOOTS, SHADOW_HELMET, SHADOW_CHESTPLATE, SHADOW_LEGGINGS, SHADOW_BOOTS,
            WOLF_RAIDER_HELMET, WOLF_RAIDER_CHESTPLATE, WOLF_RAIDER_LEGGINGS, WOLF_RAIDER_BOOTS, FROST_STALKER_HELMET, FROST_STALKER_CHESTPLATE, FROST_STALKER_LEGGINGS, FROST_STALKER_BOOTS,
            CORSAIR_HELMET, CORSAIR_CHESTPLATE, CORSAIR_LEGGINGS, CORSAIR_BOOTS, NIGHTBLADE_HELMET, NIGHTBLADE_CHESTPLATE, NIGHTBLADE_LEGGINGS, NIGHTBLADE_BOOTS,
            BRASS_HELMET, BRASS_CHESTPLATE, BRASS_LEGGINGS, BRASS_BOOTS, DESERT_EMIR_HELMET, DESERT_EMIR_CHESTPLATE, DESERT_EMIR_LEGGINGS, DESERT_EMIR_BOOTS,
            ROYAL_GUARD_HELMET, ROYAL_GUARD_CHESTPLATE, ROYAL_GUARD_LEGGINGS, ROYAL_GUARD_BOOTS, PEACOCK_HELMET, PEACOCK_CHESTPLATE, PEACOCK_LEGGINGS, PEACOCK_BOOTS,
            VIZIER_HELMET, VIZIER_CHESTPLATE, VIZIER_LEGGINGS, VIZIER_BOOTS, MIRAGE_HELMET, MIRAGE_CHESTPLATE, MIRAGE_LEGGINGS, MIRAGE_BOOTS,
            SULTAN_HELMET, SULTAN_CHESTPLATE, SULTAN_LEGGINGS, SULTAN_BOOTS, LION_KING_HELMET, LION_KING_CHESTPLATE, LION_KING_LEGGINGS, LION_KING_BOOTS,
            PHARAOH_HELMET, PHARAOH_CHESTPLATE, PHARAOH_LEGGINGS, PHARAOH_BOOTS, GOLDEN_KING_HELMET, GOLDEN_KING_CHESTPLATE, GOLDEN_KING_LEGGINGS, GOLDEN_KING_BOOTS);
    // </generated-armor>

    // --- trade, crafting and alchemy
    public static final RegistryObject<Item> BLUEPRINT = ITEMS.register("blueprint", () -> new BlueprintItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> BRASS_FLASK = ITEMS.register("brass_flask", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> POMEGRANATE = ITEMS.register("pomegranate",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationMod(0.4f).build())));
    public static final RegistryObject<Item> DESERT_LOTUS = ITEMS.register("desert_lotus", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MINOR_POMEGRANATE_ELIXIR = ITEMS.register("minor_pomegranate_elixir",
            () -> new ConsumableItems.Elixir(new Item.Properties()));
    public static final RegistryObject<Item> BEARERS_TONIC = ITEMS.register("bearers_tonic", () -> new ConsumableItems.Tonic(new Item.Properties()));
    public static final RegistryObject<Item> BEARERS_FLASK = ITEMS.register("bearers_flask",
            () -> new ConsumableItems.Flask(new Item.Properties().rarity(Rarity.RARE)));

    // --- secondary materials of Acts I and II (docs/Anexos.md, A3)
    public static final RegistryObject<Item> DUNE_LEATHER = ITEMS.register("dune_leather", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FROSTPELT = ITEMS.register("frostpelt", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> VOID_ASH = ITEMS.register("void_ash", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> VOID_WRETCH_SPAWN_EGG = ITEMS.register("void_wretch_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.VOID_WRETCH, 0x1A0F24, 0x8E3FD6, new Item.Properties()));
    public static final RegistryObject<Item> VOID_STALKER_SPAWN_EGG = ITEMS.register("void_stalker_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.VOID_STALKER, 0x120A1A, 0xC05CFF, new Item.Properties()));
    public static final RegistryObject<Item> BRASS_SENTINEL_SPAWN_EGG = ITEMS.register("brass_sentinel_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.BRASS_SENTINEL, 0xB5863A, 0x3FD6C4, new Item.Properties()));

    public static final RegistryObject<Item> KALETH_SPAWN_EGG = ITEMS.register("kaleth_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.KALETH, 0x3A2A22, 0xFF7A1A, new Item.Properties()));
    public static final RegistryObject<Item> SERATH_SPAWN_EGG = ITEMS.register("serath_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.SERATH, 0x2A0A10, 0xB0141E, new Item.Properties()));
    public static final RegistryObject<Item> VORATH_SPAWN_EGG = ITEMS.register("vorath_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.VORATH, 0x1A0A08, 0xE0401A, new Item.Properties()));

    private ItemRegistry() {
    }

    private static RegistryObject<Item> sword(String id, Tier tier, int damage, float speed) {
        return ITEMS.register(id, () -> new GearItems.Sword(tier, damage, speed, new Item.Properties()));
    }

    private static RegistryObject<Item> armor(String id, SoFETiers.Armor material, ArmorItem.Type type) {
        // every SoFE armor is worn as its 3D model (scripts/make_armor_models.py)
        return ITEMS.register(id, () -> new com.sofe.gear.ModeledArmor(material, type, new Item.Properties()));
    }

    private static RegistryObject<Item> trinket(String id, GearSlot slot) {
        return ITEMS.register(id, () -> new GearItems.Trinket(slot, new Item.Properties()));
    }

    /** Weapons and tools, held like a sword (handheld item models). */
    public static List<RegistryObject<Item>> handheld() {
        List<RegistryObject<Item>> all = new ArrayList<>(List.of(BRASS_SCIMITAR, BRASS_DAGGER, BRASS_LONGSWORD, BRASS_STAFF, BRASS_ANKH_ROD, GLACIAL_IRON_SWORD, GLACIAL_IRON_GREATAXE,
                GLACIAL_IRON_STAFF, BRASS_PICKAXE, GLACIAL_IRON_PICKAXE, KALETH_BLADE, SERATH_FANG, VORATH_WRATH,
                IMPERIAL_HALBERD, CITY_HAMMER, WAR_HAMMER, SERRATED_DAGGER, HUNTING_KNIFE, JOUSTING_LANCE, EXPLORER_MACHETE, WAR_MACE, CHAIN_SWORD, CRYSTAL_TRIDENT, SHADOW_SCYTHE, BATTLE_GREATSWORD, PHLEGM_MACE, DOUBLE_FLAIL, HOOK_BLADE, GRAVITY_HAMMER, VORTEX_DAGGER, OBSIDIAN_RITUAL_DAGGER,
                BEARDED_AXE, SEAX, FROST_SPEAR, KHOPESH, SCARAB_SICKLE, JACKAL_GLAIVE, WAS_SCEPTRE, GLADIUS, LEGION_PILUM, JUSTICAR_MAUL, SCALE_BLADE, EMBER_BLADE, INFERNAL_GREATAXE,
                DUNESUNDER, WIDOWS_KISS, SKALDBREAKER, RIMETOOTH, JACKALS_JUDGEMENT, STORMCALLER, GREEDS_CHAIN));
        all.addAll(A3_HANDHELD);
        return all;
    }

    /** Long and heavy weapons, held bigger: polearms, great blades, hammers, flails and staves. */
    public static List<RegistryObject<Item>> large() {
        List<RegistryObject<Item>> all = new ArrayList<>(List.of(GLACIAL_IRON_GREATAXE, VORATH_WRATH, BRASS_STAFF, BRASS_ANKH_ROD, GLACIAL_IRON_STAFF, IMPERIAL_HALBERD, CITY_HAMMER,
                WAR_HAMMER, JOUSTING_LANCE, WAR_MACE, CRYSTAL_TRIDENT, SHADOW_SCYTHE, BATTLE_GREATSWORD, PHLEGM_MACE, DOUBLE_FLAIL,
                GRAVITY_HAMMER, FROST_SPEAR, JACKAL_GLAIVE, WAS_SCEPTRE, LEGION_PILUM, JUSTICAR_MAUL, INFERNAL_GREATAXE, SKALDBREAKER, RIMETOOTH, JACKALS_JUDGEMENT, STORMCALLER, GREEDS_CHAIN));
        all.addAll(A3_LARGE);
        return all;
    }

    /** Firearms and the drill: held barrel forward (item/handheld_gun). */
    public static List<RegistryObject<Item>> guns() {
        return A3_GUNS;
    }

    /** Bows, with their pull models (hand-written, not datagen's). */
    public static List<RegistryObject<Item>> bows() {
        return A3_BOWS;
    }

    /** Crossbows, with their pull and loaded models (hand-written). */
    public static List<RegistryObject<Item>> crossbows() {
        return A3_CROSSBOWS;
    }

    /** The empire shields: drawn with the vanilla shield's model. */
    public static List<RegistryObject<Item>> shields() {
        return List.of(SULTHARI_SHIELD, NORDRATH_SHIELD, OBSERVATORY_SHIELD, VOID_SHIELD);
    }

    /** Every armor piece: the set pieces by class and level, then the unique ones. */
    public static List<RegistryObject<Item>> armorPieces() {
        List<RegistryObject<Item>> all = new ArrayList<>(SET_PIECES);
        all.addAll(List.of(CROWN_OF_FIVE_SULTANS, WHITE_WOLF_MANTLE, CARAVAN_TREADS, UNDYING_WRAPPINGS, BLIND_JUDGE_HELM, FURNACE_HEART));
        return all;
    }


    /** The equipment tab: weapons and tools, armor, shields, jewelry and talismans. */
    public static List<Item> equipment() {
        List<Item> items = new ArrayList<>();
        handheld().forEach(i -> items.add(i.get()));
        armorPieces().forEach(i -> items.add(i.get()));
        shields().forEach(i -> items.add(i.get()));
        for (RegistryObject<Item> i : List.of(BRASS_AMULET, BRASS_RING, SMALL_TALISMAN, LARGE_TALISMAN)) items.add(i.get());
        jewelry().forEach(i -> items.add(i.get()));
        A3_TAB_EXTRA.forEach(i -> items.add(i.get()));
        return items;
    }

    /** The new rings, necklaces and amulets: random bases, then the unique ones. */
    public static List<RegistryObject<Item>> jewelry() {
        return List.of(SILVER_RING, GLACIAL_RING, STAR_LAPIS_RING, SOLAR_GOLD_RING, ORICHALCUM_BAND, TURQUOISE_NECKLACE, BONE_NECKLACE, SCARAB_AMULET, LAUREL_TORC, AETHERIUM_PENDANT,
                EYE_OF_THE_FALSE_PROPHET, RING_OF_THE_LAST_CARAVAN, SOULKEEPER_RING, FROZEN_THRONE_BAND, SERPENT_COIL, BROKEN_PACT_SIGIL);
    }

    /** The gambler's veiled wares. */
    public static List<RegistryObject<Item>> veiled() {
        return List.of(VEILED_WEAPON, VEILED_ARMOR, VEILED_JEWELRY);
    }

    /** The materials of monsters and the Deep, beside the ores and ingots. */
    public static List<Item> creatureMaterials() {
        return List.of(INFERNAL_EMBER.get(), WAILING_SOUL.get(), DUNE_LEATHER.get(), FROSTPELT.get(), VOID_ASH.get());
    }

    /** Potions, the Flask, food, scrolls and the story's items. */
    public static List<Item> consumables() {
        List<Item> items = new ArrayList<>();
        for (RegistryObject<Item> i : List.of(BEARERS_FLASK, BEARERS_TONIC, MINOR_POMEGRANATE_ELIXIR, BRASS_FLASK, POMEGRANATE, DESERT_LOTUS,
                RETURN_SCROLL, BLUEPRINT, CODEX_SHARD, DINAR)) {
            items.add(i.get());
        }
        veiled().forEach(i -> items.add(i.get()));
        return items;
    }

    public static List<Item> creatures() {
        List<Item> items = new ArrayList<>();
        spawnEggs().forEach(i -> items.add(i.get()));
        return items;
    }

    /** Spawn eggs use the vanilla template model. */
    public static List<RegistryObject<Item>> spawnEggs() {
        return List.of(VOID_WRETCH_SPAWN_EGG, VOID_STALKER_SPAWN_EGG, BRASS_SENTINEL_SPAWN_EGG,
                KALETH_SPAWN_EGG, SERATH_SPAWN_EGG, VORATH_SPAWN_EGG);
    }

    /** Plain items with a flat model and textures/item/&lt;id&gt;.png. */
    public static List<RegistryObject<Item>> flatItems() {
        List<RegistryObject<Item>> flat = new ArrayList<>(List.of(RETURN_SCROLL, CODEX_SHARD, INFERNAL_EMBER, WAILING_SOUL, DINAR,
                BRASS_AMULET, BRASS_RING, SMALL_TALISMAN, LARGE_TALISMAN, BLUEPRINT, BRASS_FLASK, POMEGRANATE, DESERT_LOTUS,
                MINOR_POMEGRANATE_ELIXIR, BEARERS_TONIC, BEARERS_FLASK, DUNE_LEATHER, FROSTPELT, VOID_ASH));
        flat.addAll(armorPieces());
        flat.addAll(jewelry());
        flat.addAll(veiled());
        flat.addAll(A3_FLAT);
        return flat;
    }
}
