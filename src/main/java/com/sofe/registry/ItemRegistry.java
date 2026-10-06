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

    // <generated-uniques> by scripts/make_uniques.py from scripts/uniques_catalog.py
    public static final RegistryObject<Item> SUNDER_OF_THE_ORDER = ITEMS.register("sunder_of_the_order",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.BRASS, 3, -2.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SWEEP, com.sofe.gear.WeaponTrait.HOLY), "knight", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> VERDICT_OF_CASSIAN = ITEMS.register("verdict_of_cassian",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 4, -2.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SWEEP), "knight", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> BROKEN_BANNER_LANCE = ITEMS.register("broken_banner_lance",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.SOLAR_GOLD, 7, -3.1f, 2.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.CHARGE), "knight", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ANVIL_OF_AUREUM = ITEMS.register("anvil_of_aureum",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.ORICHALCUM, 8, -3.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.STUN, com.sofe.gear.WeaponTrait.SLAM), "knight", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> OATHKEEPER = ITEMS.register("oathkeeper",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.ORICHALCUM, 9, -3.0f, 1.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.SWEEP), "knight", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> WRATH_UNBOUND = ITEMS.register("wrath_unbound",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.AETHERIUM, 9, -3.1f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SWEEP), "knight", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> HELM_OF_THE_LAST_KNIGHT = armor("helm_of_the_last_knight", SoFETiers.Armor.HELM_OF_THE_LAST_KNIGHT, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> CROWN_OF_THE_SCALE = armor("crown_of_the_scale", SoFETiers.Armor.CROWN_OF_THE_SCALE, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> BULWARK_CUIRASS = armor("bulwark_cuirass", SoFETiers.Armor.BULWARK_CUIRASS, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> HEART_OF_THE_LEGION = armor("heart_of_the_legion", SoFETiers.Armor.HEART_OF_THE_LEGION, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> GREAVES_OF_THE_VIGIL = armor("greaves_of_the_vigil", SoFETiers.Armor.GREAVES_OF_THE_VIGIL, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> LEGS_OF_THE_UNYIELDING = armor("legs_of_the_unyielding", SoFETiers.Armor.LEGS_OF_THE_UNYIELDING, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> MARCH_OF_THE_PALADIN = armor("march_of_the_paladin", SoFETiers.Armor.MARCH_OF_THE_PALADIN, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> IRON_ROOTS = armor("iron_roots", SoFETiers.Armor.IRON_ROOTS, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> SIGNET_OF_THE_ORDER = trinket("signet_of_the_order", GearSlot.JEWELRY);
    public static final RegistryObject<Item> BAND_OF_UNBROKEN_OATHS = trinket("band_of_unbroken_oaths", GearSlot.JEWELRY);
    public static final RegistryObject<Item> MEDAL_OF_THE_SCALE = trinket("medal_of_the_scale", GearSlot.JEWELRY);
    public static final RegistryObject<Item> TEAR_OF_AUREUM = trinket("tear_of_aureum", GearSlot.JEWELRY);
    public static final RegistryObject<Item> KNIGHT_SHIELD_CHARM = trinket("knight_shield_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> KNIGHT_BANNER_CHARM = trinket("knight_banner_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> KNIGHT_WRATH_CHARM = trinket("knight_wrath_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> KNIGHT_GRAND_CHARM = trinket("knight_grand_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> WAND_OF_THE_FERRYMAN = ITEMS.register("wand_of_the_ferryman",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.BONE, CastMode.BOLT, 6, 14, "necromancer", new Item.Properties().durability(640).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> THOTH_REED = ITEMS.register("thoth_reed",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.SOUL, CastMode.BEAM, 8, 20, "necromancer", new Item.Properties().durability(1040).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> STAFF_OF_THE_NINE_GATES = ITEMS.register("staff_of_the_nine_gates",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.SOUL, CastMode.BEAM, 10, 30, "necromancer", new Item.Properties().durability(1440).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> SICKLE_OF_MORTHIS = ITEMS.register("sickle_of_morthis",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.ORICHALCUM, 7, -2.2f, 0.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.LIFE_STEAL), "necromancer", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> KHOPESH_OF_THE_EMBALMER = ITEMS.register("khopesh_of_the_embalmer",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 4, -2.5f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.BLEED, com.sofe.gear.WeaponTrait.SWEEP), "necromancer", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ANUBETS_JUDGEMENT = ITEMS.register("anubets_judgement",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.AETHERIUM, 11, -3.0f, 1.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH), "necromancer", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MASK_OF_THE_FERRYMAN = armor("mask_of_the_ferryman", SoFETiers.Armor.MASK_OF_THE_FERRYMAN, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> CROWN_OF_THE_PALE_KING = armor("crown_of_the_pale_king", SoFETiers.Armor.CROWN_OF_THE_PALE_KING, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SHROUD_OF_KHEMET = armor("shroud_of_khemet", SoFETiers.Armor.SHROUD_OF_KHEMET, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> RIBCAGE_OF_THE_LICH = armor("ribcage_of_the_lich", SoFETiers.Armor.RIBCAGE_OF_THE_LICH, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> WRAPS_OF_THE_LONG_SLEEP = armor("wraps_of_the_long_sleep", SoFETiers.Armor.WRAPS_OF_THE_LONG_SLEEP, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> GRAVE_DUST_TROUSERS = armor("grave_dust_trousers", SoFETiers.Armor.GRAVE_DUST_TROUSERS, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SANDALS_OF_THE_THRESHOLD = armor("sandals_of_the_threshold", SoFETiers.Armor.SANDALS_OF_THE_THRESHOLD, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> TREADS_OF_THE_UNDERWORLD = armor("treads_of_the_underworld", SoFETiers.Armor.TREADS_OF_THE_UNDERWORLD, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> RING_OF_BOUND_SOULS = trinket("ring_of_bound_souls", GearSlot.JEWELRY);
    public static final RegistryObject<Item> SEAL_OF_THE_HOUSE_OF_THRESHOLDS = trinket("seal_of_the_house_of_thresholds", GearSlot.JEWELRY);
    public static final RegistryObject<Item> HEART_SCARAB = trinket("heart_scarab", GearSlot.JEWELRY);
    public static final RegistryObject<Item> EYE_OF_MORTHIS = trinket("eye_of_morthis", GearSlot.JEWELRY);
    public static final RegistryObject<Item> NECRO_SCARAB_CHARM = trinket("necro_scarab_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> NECRO_ANKH_CHARM = trinket("necro_ankh_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> NECRO_JAR_CHARM = trinket("necro_jar_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> NECRO_GRAND_CHARM = trinket("necro_grand_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> LALEH_LANTERN = ITEMS.register("laleh_lantern",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.EMBER, CastMode.BOLT, 7, 12, "sorceress", new Item.Properties().durability(700).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> TEAR_OF_THE_MOON = ITEMS.register("tear_of_the_moon",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.FROST, CastMode.BOLT, 8, 12, "sorceress", new Item.Properties().durability(960).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ASTROLABE_OF_SHIRIN = ITEMS.register("astrolabe_of_shirin",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.STORM, CastMode.LIGHTNING, 10, 36, "sorceress", new Item.Properties().durability(1360).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> PEN_OF_THE_POET_KING = ITEMS.register("pen_of_the_poet_king",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.EMBER, CastMode.BOLT, 11, 20, "sorceress", new Item.Properties().durability(1640).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> STAFF_OF_WINTER_GARDENS = ITEMS.register("staff_of_winter_gardens",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.FROST, CastMode.BOLT, 13, 20, "sorceress", new Item.Properties().durability(1900).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> SKYFALL = ITEMS.register("skyfall",
            () -> new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.STORM, CastMode.LIGHTNING, 15, 50, "sorceress", new Item.Properties().durability(2300).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> VEIL_OF_LALEH = armor("veil_of_laleh", SoFETiers.Armor.VEIL_OF_LALEH, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> HAT_OF_THE_CIRCLE = armor("hat_of_the_circle", SoFETiers.Armor.HAT_OF_THE_CIRCLE, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> ROBE_OF_WRITTEN_STARS = armor("robe_of_written_stars", SoFETiers.Armor.ROBE_OF_WRITTEN_STARS, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> MANTLE_OF_THE_ECLIPSE = armor("mantle_of_the_eclipse", SoFETiers.Armor.MANTLE_OF_THE_ECLIPSE, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SILKS_OF_PARSIVAN = armor("silks_of_parsivan", SoFETiers.Armor.SILKS_OF_PARSIVAN, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> LEGGINGS_OF_THE_STORM_COURT = armor("leggings_of_the_storm_court", SoFETiers.Armor.LEGGINGS_OF_THE_STORM_COURT, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SLIPPERS_OF_THE_WATERLINE = armor("slippers_of_the_waterline", SoFETiers.Armor.SLIPPERS_OF_THE_WATERLINE, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> STEPS_OF_THE_COMET = armor("steps_of_the_comet", SoFETiers.Armor.STEPS_OF_THE_COMET, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> RING_OF_THREE_RUNES = trinket("ring_of_three_runes", GearSlot.JEWELRY);
    public static final RegistryObject<Item> BAND_OF_THE_CONSTELLATION = trinket("band_of_the_constellation", GearSlot.JEWELRY);
    public static final RegistryObject<Item> STAR_OF_PARSIVAN = trinket("star_of_parsivan", GearSlot.JEWELRY);
    public static final RegistryObject<Item> LALEHS_LOCKET = trinket("lalehs_locket", GearSlot.JEWELRY);
    public static final RegistryObject<Item> SORC_EMBER_CHARM = trinket("sorc_ember_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> SORC_FROST_CHARM = trinket("sorc_frost_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> SORC_STORM_CHARM = trinket("sorc_storm_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> SORC_GRAND_CHARM = trinket("sorc_grand_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> ASH_AND_EMBER = ITEMS.register("ash_and_ember",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.BRASS, 3, -1.8f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.MULTI_HIT), "thief", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> FANGS_OF_FENRATH = ITEMS.register("fangs_of_fenrath",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.SOLAR_GOLD, 5, -1.8f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.MULTI_HIT, com.sofe.gear.WeaponTrait.POISON), "thief", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> SMUGGLERS_SEAX = ITEMS.register("smugglers_seax",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 4, -1.8f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.BLEED), "thief", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> LAST_BREATH = ITEMS.register("last_breath",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.SOLAR_GOLD, 6, -1.5f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.BLEED), "thief", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> FJORD_WOLF_HOOK = ITEMS.register("fjord_wolf_hook",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.ORICHALCUM, 7, -2.2f, 0.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.PULL), "thief", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> GREED_INCARNATE = ITEMS.register("greed_incarnate",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.AETHERIUM, 9, -2.0f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.PLANTS), "thief", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> HOOD_OF_ASH = armor("hood_of_ash", SoFETiers.Armor.HOOD_OF_ASH, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> MASK_OF_THE_HEIST = armor("mask_of_the_heist", SoFETiers.Armor.MASK_OF_THE_HEIST, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> JERKIN_OF_MANY_POCKETS = armor("jerkin_of_many_pockets", SoFETiers.Armor.JERKIN_OF_MANY_POCKETS, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> WOLFSKIN_OF_THE_RAIDER = armor("wolfskin_of_the_raider", SoFETiers.Armor.WOLFSKIN_OF_THE_RAIDER, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> BREECHES_OF_THE_CUTPURSE = armor("breeches_of_the_cutpurse", SoFETiers.Armor.BREECHES_OF_THE_CUTPURSE, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SHADOWSTEP_LEGWRAPS = armor("shadowstep_legwraps", SoFETiers.Armor.SHADOWSTEP_LEGWRAPS, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> BOOTS_OF_THE_FJORD_RUNNER = armor("boots_of_the_fjord_runner", SoFETiers.Armor.BOOTS_OF_THE_FJORD_RUNNER, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> SILENT_SOLES = armor("silent_soles", SoFETiers.Armor.SILENT_SOLES, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> RING_OF_THE_FENCE = trinket("ring_of_the_fence", GearSlot.JEWELRY);
    public static final RegistryObject<Item> KNOT_OF_THE_GRIMSSON_CLAN = trinket("knot_of_the_grimsson_clan", GearSlot.JEWELRY);
    public static final RegistryObject<Item> WOLF_TOOTH_NECKLACE = trinket("wolf_tooth_necklace", GearSlot.JEWELRY);
    public static final RegistryObject<Item> EMBER_OF_THE_BURNED_VILLAGE = trinket("ember_of_the_burned_village", GearSlot.JEWELRY);
    public static final RegistryObject<Item> THIEF_COIN_CHARM = trinket("thief_coin_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> THIEF_KEY_CHARM = trinket("thief_key_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> THIEF_FANG_CHARM = trinket("thief_fang_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> THIEF_GRAND_CHARM = trinket("thief_grand_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> SCEPTER_OF_THE_FIRST_SULTAN = ITEMS.register("scepter_of_the_first_sultan",
            () -> new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.DECREE, 0, 500, "king", new Item.Properties().durability(200).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> AZHAR_CRESCENT = ITEMS.register("azhar_crescent",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 4, -2.3f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SWEEP, com.sofe.gear.WeaponTrait.BLEED), "king", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> VOICE_OF_THE_CANNON = ITEMS.register("voice_of_the_cannon",
            () -> new com.sofe.gear.ranged.RangedItems.Firearm(16, 1, 0.3f, 30, "king", new Item.Properties().durability(600).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> JANISSARY_OATH = ITEMS.register("janissary_oath",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 4, -2.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SWEEP), "king", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MUSKET_OF_THE_SIEGE = ITEMS.register("musket_of_the_siege",
            () -> new com.sofe.gear.ranged.RangedItems.Firearm(19, 1, 0.2f, 45, "king", new Item.Properties().durability(600).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> HALBERD_OF_FIVE_LANDS = ITEMS.register("halberd_of_five_lands",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.AETHERIUM, 11, -3.0f, 1.5, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.SWEEP), "king", new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> TURBAN_OF_THE_PEACOCK_THRONE = armor("turban_of_the_peacock_throne", SoFETiers.Armor.TURBAN_OF_THE_PEACOCK_THRONE, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> CROWN_OF_AZHAR = armor("crown_of_azhar", SoFETiers.Armor.CROWN_OF_AZHAR, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> KAFTAN_OF_THE_DIVAN = armor("kaftan_of_the_divan", SoFETiers.Armor.KAFTAN_OF_THE_DIVAN, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> ROBES_OF_THE_GOLDEN_AGE = armor("robes_of_the_golden_age", SoFETiers.Armor.ROBES_OF_THE_GOLDEN_AGE, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> TROUSERS_OF_THE_VIZIER = armor("trousers_of_the_vizier", SoFETiers.Armor.TROUSERS_OF_THE_VIZIER, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> LEGS_OF_THE_CONQUEROR = armor("legs_of_the_conqueror", SoFETiers.Armor.LEGS_OF_THE_CONQUEROR, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> SLIPPERS_OF_THE_HAREM_ROAD = armor("slippers_of_the_harem_road", SoFETiers.Armor.SLIPPERS_OF_THE_HAREM_ROAD, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> TREAD_OF_KINGS = armor("tread_of_kings", SoFETiers.Armor.TREAD_OF_KINGS, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> SIGNET_OF_TEVFIRAN = trinket("signet_of_tevfiran", GearSlot.JEWELRY);
    public static final RegistryObject<Item> RING_OF_THE_GRAND_VIZIER = trinket("ring_of_the_grand_vizier", GearSlot.JEWELRY);
    public static final RegistryObject<Item> CRESCENT_OF_SULTHARI = trinket("crescent_of_sulthari", GearSlot.JEWELRY);
    public static final RegistryObject<Item> HEART_OF_THE_THRONE = trinket("heart_of_the_throne", GearSlot.JEWELRY);
    public static final RegistryObject<Item> KING_COIN_CHARM = trinket("king_coin_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> KING_DECREE_CHARM = trinket("king_decree_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> KING_CANNON_CHARM = trinket("king_cannon_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> KING_GRAND_CHARM = trinket("king_grand_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> VOID_SHARD_CHARM = trinket("void_shard_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> DESERT_ROSE_CHARM = trinket("desert_rose_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> FROST_TOOTH_CHARM = trinket("frost_tooth_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> STORM_GLASS_CHARM = trinket("storm_glass_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> LUCKY_FEATHER_CHARM = trinket("lucky_feather_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> TRAVELER_CHARM = trinket("traveler_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> GHEED_COIN = trinket("gheed_coin", GearSlot.TALISMAN);
    public static final RegistryObject<Item> LANTERN_CHARM = trinket("lantern_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> BLOODSTONE_CHARM = trinket("bloodstone_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> HOURGLASS_CHARM = trinket("hourglass_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> COMPASS_OF_THE_CODEX = trinket("compass_of_the_codex", GearSlot.TALISMAN);
    public static final RegistryObject<Item> EMBER_HEART_CHARM = trinket("ember_heart_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> FROSTBOUND_CHARM = trinket("frostbound_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> THUNDER_IDOL_CHARM = trinket("thunder_idol_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> PILGRIM_CHARM = trinket("pilgrim_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> ANNIHILUS = trinket("annihilus", GearSlot.TALISMAN);
    public static final RegistryObject<Item> TORCH_OF_THE_BEARERS = trinket("torch_of_the_bearers", GearSlot.TALISMAN);
    public static final RegistryObject<Item> SEAL_OF_THE_EIGHTH_LOCK = trinket("seal_of_the_eighth_lock", GearSlot.TALISMAN);
    public static final RegistryObject<Item> CODEX_PAGE_CHARM = trinket("codex_page_charm", GearSlot.TALISMAN);
    public static final RegistryObject<Item> VORATH_CINDER = trinket("vorath_cinder", GearSlot.TALISMAN);
    public static final RegistryObject<Item> RING_OF_THE_CARAVANSERAI = trinket("ring_of_the_caravanserai", GearSlot.JEWELRY);
    public static final RegistryObject<Item> SERPENT_EYE_RING = trinket("serpent_eye_ring", GearSlot.JEWELRY);
    public static final RegistryObject<Item> FROST_WYRM_BAND = trinket("frost_wyrm_band", GearSlot.JEWELRY);
    public static final RegistryObject<Item> VOID_TOUCHED_RING = trinket("void_touched_ring", GearSlot.JEWELRY);
    public static final RegistryObject<Item> AMULET_OF_THE_DAWN = trinket("amulet_of_the_dawn", GearSlot.JEWELRY);
    public static final RegistryObject<Item> TIDECALLER_PENDANT = trinket("tidecaller_pendant", GearSlot.JEWELRY);
    public static final RegistryObject<Item> BONE_OF_THE_FIRST_FALLEN = trinket("bone_of_the_first_fallen", GearSlot.JEWELRY);
    public static final RegistryObject<Item> PRYTHONS_MIRROR = trinket("prythons_mirror", GearSlot.JEWELRY);
    public static final RegistryObject<Item> CARAVAN_BREAKER = ITEMS.register("caravan_breaker",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.BRASS, 4, -3.3f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SLAM), null, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> JUNGLE_OF_GLASS = ITEMS.register("jungle_of_glass",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 3, -2.0f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.PLANTS), null, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> TIDE_OF_TEETH = ITEMS.register("tide_of_teeth",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.GLACIAL_IRON, 6, -2.9f, 1.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.FROST), null, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> CHAINMASTER = ITEMS.register("chainmaster",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.SOLAR_GOLD, 5, -2.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.MULTI_HIT), null, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> HARVEST_OF_SHADOWS = ITEMS.register("harvest_of_shadows",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.ORICHALCUM, 8, -3.0f, 1.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.SWEEP), null, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> FLAIL_OF_THE_PENITENT = ITEMS.register("flail_of_the_penitent",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.ORICHALCUM, 9, -2.9f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.MULTI_HIT), null, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ECHO_OF_KALETH = ITEMS.register("echo_of_kaleth",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.AETHERIUM, 9, -2.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SWEEP), null, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> HELM_OF_THE_WANDERER = armor("helm_of_the_wanderer", SoFETiers.Armor.HELM_OF_THE_WANDERER, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> ECLIPSE_PLATE = armor("eclipse_plate", SoFETiers.Armor.ECLIPSE_PLATE, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> SANDSTRIDERS = armor("sandstriders", SoFETiers.Armor.SANDSTRIDERS, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> LEGS_OF_THE_SEVENTH_SIN = armor("legs_of_the_seventh_sin", SoFETiers.Armor.LEGS_OF_THE_SEVENTH_SIN, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> WHISPER_OF_MIRAEL = trinket("whisper_of_mirael", GearSlot.JEWELRY);
    public static final RegistryObject<Item> THESSYNS_NEEDLE = ITEMS.register("thessyns_needle",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.SOLAR_GOLD, 5, -1.5f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.BLEED), null, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> RING_OF_THE_LONG_WATCH = trinket("ring_of_the_long_watch", GearSlot.JEWELRY);
    public static final RegistryObject<Item> HEART_OF_LUXARA = trinket("heart_of_luxara", GearSlot.JEWELRY);
    public static final RegistryObject<Item> CROWN_OF_THE_STAGNANT_KING = armor("crown_of_the_stagnant_king", SoFETiers.Armor.CROWN_OF_THE_STAGNANT_KING, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> GOLDARCS_SCALES = ITEMS.register("goldarcs_scales",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.ORICHALCUM, 8, -3.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.STUN, com.sofe.gear.WeaponTrait.SLAM), null, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> NIXARAS_FALSE_RING = trinket("nixaras_false_ring", GearSlot.JEWELRY);
    public static final RegistryObject<Item> HOARD_OF_AVAROK = trinket("hoard_of_avarok", GearSlot.JEWELRY);
    public static final RegistryObject<Item> MAW_OF_FENRATH = armor("maw_of_fenrath", SoFETiers.Armor.MAW_OF_FENRATH, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> GULARTHS_CLEAVER = ITEMS.register("gularths_cleaver",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.ORICHALCUM, 9, -3.0f, 1.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.SWEEP), null, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MASK_OF_SHADEYN = armor("mask_of_shadeyn", SoFETiers.Armor.MASK_OF_SHADEYN, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> SUN_HALO_OF_SOLRATH = trinket("sun_halo_of_solrath", GearSlot.JEWELRY);
    public static final RegistryObject<Item> ASHBRINGER_OF_THE_FIRST_FALLEN = ITEMS.register("ashbringer_of_the_first_fallen",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.AETHERIUM, 9, -2.4f, 0.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.SWEEP, com.sofe.gear.WeaponTrait.HOLY), null, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ENVYRIS_BORROWED_SCYTHE = ITEMS.register("envyris_borrowed_scythe",
            () -> new com.sofe.gear.TraitWeapon(SoFETiers.AETHERIUM, 9, -3.0f, 1.0, java.util.EnumSet.of(com.sofe.gear.WeaponTrait.REACH, com.sofe.gear.WeaponTrait.SWEEP), null, new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> GRAND_TALISMAN = trinket("grand_talisman", GearSlot.TALISMAN);
    private static final List<RegistryObject<Item>> U_HANDHELD = List.of(
            SUNDER_OF_THE_ORDER, VERDICT_OF_CASSIAN, WRATH_UNBOUND, WAND_OF_THE_FERRYMAN, THOTH_REED, SICKLE_OF_MORTHIS,
            KHOPESH_OF_THE_EMBALMER, LALEH_LANTERN, TEAR_OF_THE_MOON, ASTROLABE_OF_SHIRIN, ASH_AND_EMBER, FANGS_OF_FENRATH,
            SMUGGLERS_SEAX, LAST_BREATH, FJORD_WOLF_HOOK, GREED_INCARNATE, SCEPTER_OF_THE_FIRST_SULTAN, AZHAR_CRESCENT,
            JANISSARY_OATH, JUNGLE_OF_GLASS, CHAINMASTER, ECHO_OF_KALETH, THESSYNS_NEEDLE, ASHBRINGER_OF_THE_FIRST_FALLEN);
    private static final List<RegistryObject<Item>> U_LARGE = List.of(
            BROKEN_BANNER_LANCE, ANVIL_OF_AUREUM, OATHKEEPER, STAFF_OF_THE_NINE_GATES, ANUBETS_JUDGEMENT, PEN_OF_THE_POET_KING,
            STAFF_OF_WINTER_GARDENS, SKYFALL, HALBERD_OF_FIVE_LANDS, CARAVAN_BREAKER, TIDE_OF_TEETH, HARVEST_OF_SHADOWS,
            FLAIL_OF_THE_PENITENT, GOLDARCS_SCALES, GULARTHS_CLEAVER, ENVYRIS_BORROWED_SCYTHE);
    private static final List<RegistryObject<Item>> U_GUN = List.of(
            VOICE_OF_THE_CANNON, MUSKET_OF_THE_SIEGE);
    private static final List<RegistryObject<Item>> U_ARMOR = List.of(
            HELM_OF_THE_LAST_KNIGHT, CROWN_OF_THE_SCALE, BULWARK_CUIRASS, HEART_OF_THE_LEGION, GREAVES_OF_THE_VIGIL, LEGS_OF_THE_UNYIELDING,
            MARCH_OF_THE_PALADIN, IRON_ROOTS, MASK_OF_THE_FERRYMAN, CROWN_OF_THE_PALE_KING, SHROUD_OF_KHEMET, RIBCAGE_OF_THE_LICH,
            WRAPS_OF_THE_LONG_SLEEP, GRAVE_DUST_TROUSERS, SANDALS_OF_THE_THRESHOLD, TREADS_OF_THE_UNDERWORLD, VEIL_OF_LALEH, HAT_OF_THE_CIRCLE,
            ROBE_OF_WRITTEN_STARS, MANTLE_OF_THE_ECLIPSE, SILKS_OF_PARSIVAN, LEGGINGS_OF_THE_STORM_COURT, SLIPPERS_OF_THE_WATERLINE, STEPS_OF_THE_COMET,
            HOOD_OF_ASH, MASK_OF_THE_HEIST, JERKIN_OF_MANY_POCKETS, WOLFSKIN_OF_THE_RAIDER, BREECHES_OF_THE_CUTPURSE, SHADOWSTEP_LEGWRAPS,
            BOOTS_OF_THE_FJORD_RUNNER, SILENT_SOLES, TURBAN_OF_THE_PEACOCK_THRONE, CROWN_OF_AZHAR, KAFTAN_OF_THE_DIVAN, ROBES_OF_THE_GOLDEN_AGE,
            TROUSERS_OF_THE_VIZIER, LEGS_OF_THE_CONQUEROR, SLIPPERS_OF_THE_HAREM_ROAD, TREAD_OF_KINGS, HELM_OF_THE_WANDERER, ECLIPSE_PLATE,
            SANDSTRIDERS, LEGS_OF_THE_SEVENTH_SIN, CROWN_OF_THE_STAGNANT_KING, MAW_OF_FENRATH, MASK_OF_SHADEYN);
    private static final List<RegistryObject<Item>> U_FLAT = List.of(
            SIGNET_OF_THE_ORDER, BAND_OF_UNBROKEN_OATHS, MEDAL_OF_THE_SCALE, TEAR_OF_AUREUM, KNIGHT_SHIELD_CHARM, KNIGHT_BANNER_CHARM,
            KNIGHT_WRATH_CHARM, KNIGHT_GRAND_CHARM, RING_OF_BOUND_SOULS, SEAL_OF_THE_HOUSE_OF_THRESHOLDS, HEART_SCARAB, EYE_OF_MORTHIS,
            NECRO_SCARAB_CHARM, NECRO_ANKH_CHARM, NECRO_JAR_CHARM, NECRO_GRAND_CHARM, RING_OF_THREE_RUNES, BAND_OF_THE_CONSTELLATION,
            STAR_OF_PARSIVAN, LALEHS_LOCKET, SORC_EMBER_CHARM, SORC_FROST_CHARM, SORC_STORM_CHARM, SORC_GRAND_CHARM,
            RING_OF_THE_FENCE, KNOT_OF_THE_GRIMSSON_CLAN, WOLF_TOOTH_NECKLACE, EMBER_OF_THE_BURNED_VILLAGE, THIEF_COIN_CHARM, THIEF_KEY_CHARM,
            THIEF_FANG_CHARM, THIEF_GRAND_CHARM, SIGNET_OF_TEVFIRAN, RING_OF_THE_GRAND_VIZIER, CRESCENT_OF_SULTHARI, HEART_OF_THE_THRONE,
            KING_COIN_CHARM, KING_DECREE_CHARM, KING_CANNON_CHARM, KING_GRAND_CHARM, VOID_SHARD_CHARM, DESERT_ROSE_CHARM,
            FROST_TOOTH_CHARM, STORM_GLASS_CHARM, LUCKY_FEATHER_CHARM, TRAVELER_CHARM, GHEED_COIN, LANTERN_CHARM,
            BLOODSTONE_CHARM, HOURGLASS_CHARM, COMPASS_OF_THE_CODEX, EMBER_HEART_CHARM, FROSTBOUND_CHARM, THUNDER_IDOL_CHARM,
            PILGRIM_CHARM, ANNIHILUS, TORCH_OF_THE_BEARERS, SEAL_OF_THE_EIGHTH_LOCK, CODEX_PAGE_CHARM, VORATH_CINDER,
            RING_OF_THE_CARAVANSERAI, SERPENT_EYE_RING, FROST_WYRM_BAND, VOID_TOUCHED_RING, AMULET_OF_THE_DAWN, TIDECALLER_PENDANT,
            BONE_OF_THE_FIRST_FALLEN, PRYTHONS_MIRROR, WHISPER_OF_MIRAEL, RING_OF_THE_LONG_WATCH, HEART_OF_LUXARA, NIXARAS_FALSE_RING,
            HOARD_OF_AVAROK, SUN_HALO_OF_SOLRATH, GRAND_TALISMAN);
    // </generated-uniques>

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

    // Sprint 6: the coins of the Royal Treasury, and the shape the Bronze Cannon is drawn with (neither in a tab)
    public static final RegistryObject<Item> ROYAL_COIN = ITEMS.register("royal_coin", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BRONZE_CANNON_MODEL = ITEMS.register("bronze_cannon_model", () -> new Item(new Item.Properties()));

    // --- trade, crafting and alchemy
    public static final RegistryObject<Item> BLUEPRINT = ITEMS.register("blueprint", () -> new BlueprintItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> BRASS_FLASK = ITEMS.register("brass_flask", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> POMEGRANATE = ITEMS.register("pomegranate",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationMod(0.4f).build())));
    public static final RegistryObject<Item> DESERT_LOTUS = ITEMS.register("desert_lotus", () -> new Item(new Item.Properties()));
    // <generated-potions> by scripts/make_potions.py: an elixir and a tonic per act
    public static final RegistryObject<Item> MINOR_POMEGRANATE_ELIXIR = ITEMS.register("minor_pomegranate_elixir",
            () -> new ConsumableItems.Elixir(new Item.Properties(), 20));
    public static final RegistryObject<Item> BEARERS_TONIC = ITEMS.register("bearers_tonic",
            () -> new ConsumableItems.Tonic(new Item.Properties(), 0.25f));
    public static final RegistryObject<Item> STRONG_POMEGRANATE_ELIXIR = ITEMS.register("strong_pomegranate_elixir",
            () -> new ConsumableItems.Elixir(new Item.Properties(), 35));
    public static final RegistryObject<Item> STRONG_BEARERS_TONIC = ITEMS.register("strong_bearers_tonic",
            () -> new ConsumableItems.Tonic(new Item.Properties(), 0.35f));
    public static final RegistryObject<Item> MAJOR_POMEGRANATE_ELIXIR = ITEMS.register("major_pomegranate_elixir",
            () -> new ConsumableItems.Elixir(new Item.Properties().rarity(Rarity.UNCOMMON), 50));
    public static final RegistryObject<Item> MAJOR_BEARERS_TONIC = ITEMS.register("major_bearers_tonic",
            () -> new ConsumableItems.Tonic(new Item.Properties().rarity(Rarity.UNCOMMON), 0.5f));
    public static final RegistryObject<Item> GRAND_POMEGRANATE_ELIXIR = ITEMS.register("grand_pomegranate_elixir",
            () -> new ConsumableItems.Elixir(new Item.Properties().rarity(Rarity.UNCOMMON), 75));
    public static final RegistryObject<Item> GRAND_BEARERS_TONIC = ITEMS.register("grand_bearers_tonic",
            () -> new ConsumableItems.Tonic(new Item.Properties().rarity(Rarity.UNCOMMON), 0.65f));
    public static final RegistryObject<Item> IMPERIAL_POMEGRANATE_ELIXIR = ITEMS.register("imperial_pomegranate_elixir",
            () -> new ConsumableItems.Elixir(new Item.Properties().rarity(Rarity.UNCOMMON), 100));
    public static final RegistryObject<Item> IMPERIAL_BEARERS_TONIC = ITEMS.register("imperial_bearers_tonic",
            () -> new ConsumableItems.Tonic(new Item.Properties().rarity(Rarity.UNCOMMON), 0.8f));

    /** Every elixir and tonic, weakest first. */
    public static List<RegistryObject<Item>> potions() {
        return List.of(MINOR_POMEGRANATE_ELIXIR, BEARERS_TONIC, STRONG_POMEGRANATE_ELIXIR, STRONG_BEARERS_TONIC, MAJOR_POMEGRANATE_ELIXIR, MAJOR_BEARERS_TONIC, GRAND_POMEGRANATE_ELIXIR, GRAND_BEARERS_TONIC, IMPERIAL_POMEGRANATE_ELIXIR, IMPERIAL_BEARERS_TONIC);
    }
    // </generated-potions>
    public static final RegistryObject<Item> BEARERS_FLASK = ITEMS.register("bearers_flask",
            () -> new ConsumableItems.Flask(new Item.Properties().rarity(Rarity.RARE)));

    // --- secondary materials of Acts I and II (docs/Anexos.md, A3)
    public static final RegistryObject<Item> DUNE_LEATHER = ITEMS.register("dune_leather", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FROSTPELT = ITEMS.register("frostpelt", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> VOID_ASH = ITEMS.register("void_ash", () -> new Item(new Item.Properties()));

    // --- secondary materials of Acts III to V (docs/Anexos.md, A3)
    public static final RegistryObject<Item> MOONSILK = ITEMS.register("moonsilk", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SUNREED_PAPYRUS = ITEMS.register("sunreed_papyrus", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> VOID_CRYSTAL = ITEMS.register("void_crystal",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE).fireResistant()));
    public static final RegistryObject<Item> VOID_INK = ITEMS.register("void_ink", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    /** The Sealing Quill (docs/Mundo.md, W5): forged with Void Ink, it opens the Inverted Throne; a soulbound story item. */
    public static final RegistryObject<Item> SEALING_QUILL = ITEMS.register("sealing_quill",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()) {
                @Override
                public void appendHoverText(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.Level level, List<net.minecraft.network.chat.Component> tooltip,
                                            net.minecraft.world.item.TooltipFlag flag) {
                    tooltip.add(net.minecraft.network.chat.Component.translatable("item.sofe.sealing_quill.desc").withStyle(net.minecraft.ChatFormatting.DARK_PURPLE));
                }

                @Override
                public boolean isFoil(net.minecraft.world.item.ItemStack stack) {
                    return true;
                }
            });

    /** The seven sin gems, each rough, cut and as an Oath Gem (com.sofe.gear.SinGem): "rough_wrath_ruby" and so on. */
    public static final java.util.Map<String, RegistryObject<Item>> SIN_GEMS = sinGems();

    private static java.util.Map<String, RegistryObject<Item>> sinGems() {
        java.util.Map<String, RegistryObject<Item>> gems = new java.util.LinkedHashMap<>();
        for (com.sofe.gear.SinGem gem : com.sofe.gear.SinGem.values()) {
            for (com.sofe.gear.SinGem.Form form : com.sofe.gear.SinGem.Form.values()) {
                Rarity rarity = form == com.sofe.gear.SinGem.Form.OATH ? Rarity.EPIC : form == com.sofe.gear.SinGem.Form.CUT ? Rarity.UNCOMMON : Rarity.COMMON;
                gems.put(gem.id(form), ITEMS.register(gem.id(form), () -> new Item(new Item.Properties().rarity(rarity))));
            }
        }
        return gems;
    }

    public static Item sinGem(com.sofe.gear.SinGem gem, com.sofe.gear.SinGem.Form form) {
        return SIN_GEMS.get(gem.id(form)).get();
    }

    // <generated-boss-eggs>
    public static final RegistryObject<Item> MIRAEL_SPAWN_EGG = ITEMS.register("mirael_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.MIRAEL, 0x5A2A7A, 0xE070FF, new Item.Properties()));
    public static final RegistryObject<Item> THESSYN_SPAWN_EGG = ITEMS.register("thessyn_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.THESSYN, 0x2A3A30, 0xC0E0B0, new Item.Properties()));
    public static final RegistryObject<Item> DORMIEL_SPAWN_EGG = ITEMS.register("dormiel_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.DORMIEL, 0x2A3050, 0x90C0FF, new Item.Properties()));
    public static final RegistryObject<Item> LUXARA_SPAWN_EGG = ITEMS.register("luxara_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.LUXARA, 0x7A1A5A, 0xFF80C0, new Item.Properties()));
    public static final RegistryObject<Item> MORTHIS_SPAWN_EGG = ITEMS.register("morthis_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.MORTHIS, 0x3A4A2A, 0x90FF80, new Item.Properties()));
    public static final RegistryObject<Item> GOLDARC_SPAWN_EGG = ITEMS.register("goldarc_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.GOLDARC, 0xC49232, 0x3A2A10, new Item.Properties()));
    public static final RegistryObject<Item> NIXARA_SPAWN_EGG = ITEMS.register("nixara_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.NIXARA, 0x3A3020, 0xE0B040, new Item.Properties()));
    public static final RegistryObject<Item> AVAROK_SPAWN_EGG = ITEMS.register("avarok_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.AVAROK, 0xE0B030, 0x8A1A1A, new Item.Properties()));
    public static final RegistryObject<Item> FENRATH_SPAWN_EGG = ITEMS.register("fenrath_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.FENRATH, 0x4A4038, 0x90FF60, new Item.Properties()));
    public static final RegistryObject<Item> GULARTH_SPAWN_EGG = ITEMS.register("gularth_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.GULARTH, 0x6A4A3A, 0xC04030, new Item.Properties()));
    public static final RegistryObject<Item> SHADEYN_SPAWN_EGG = ITEMS.register("shadeyn_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.SHADEYN, 0xC0C8D8, 0x2A2A3A, new Item.Properties()));
    public static final RegistryObject<Item> SOLRATH_SPAWN_EGG = ITEMS.register("solrath_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.SOLRATH, 0x4A1A6A, 0xFFD050, new Item.Properties()));
    public static final RegistryObject<Item> PRYTHON_SPAWN_EGG = ITEMS.register("prython_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.PRYTHON, 0x2A2010, 0xFFD060, new Item.Properties()));
    public static final RegistryObject<Item> NAHRAZEL_SPAWN_EGG = ITEMS.register("nahrazel_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.NAHRAZEL, 0x1A0A2A, 0xB050FF, new Item.Properties()));
    public static final RegistryObject<Item> ENVYRIS_SPAWN_EGG = ITEMS.register("envyris_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.ENVYRIS, 0x1A3A20, 0x60FF90, new Item.Properties()));

    public static List<RegistryObject<Item>> bossEggs() {
        return List.of(MIRAEL_SPAWN_EGG, THESSYN_SPAWN_EGG, DORMIEL_SPAWN_EGG, LUXARA_SPAWN_EGG, MORTHIS_SPAWN_EGG, GOLDARC_SPAWN_EGG, NIXARA_SPAWN_EGG, AVAROK_SPAWN_EGG, FENRATH_SPAWN_EGG, GULARTH_SPAWN_EGG, SHADEYN_SPAWN_EGG, SOLRATH_SPAWN_EGG, PRYTHON_SPAWN_EGG, NAHRAZEL_SPAWN_EGG, ENVYRIS_SPAWN_EGG);
    }
    // </generated-boss-eggs>
    // <generated-empire-eggs>
    public static final RegistryObject<Item> SAND_GHOUL_SPAWN_EGG = ITEMS.register("sand_ghoul_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.SAND_GHOUL, 0xA8875A, 0xFFAA28, new Item.Properties()));
    public static final RegistryObject<Item> CLOCKWORK_SCARAB_SPAWN_EGG = ITEMS.register("clockwork_scarab_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.CLOCKWORK_SCARAB, 0xB07C30, 0x3CD2C8, new Item.Properties()));
    public static final RegistryObject<Item> DRAUGR_SPAWN_EGG = ITEMS.register("draugr_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.DRAUGR, 0x4E5A64, 0x96EBFF, new Item.Properties()));
    public static final RegistryObject<Item> RIME_WOLF_SPAWN_EGG = ITEMS.register("rime_wolf_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.RIME_WOLF, 0xCEDEE8, 0x78C8F0, new Item.Properties()));
    public static final RegistryObject<Item> MIRAGE_DANCER_SPAWN_EGG = ITEMS.register("mirage_dancer_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.MIRAGE_DANCER, 0x78329A, 0xFF78DC, new Item.Properties()));
    public static final RegistryObject<Item> BOG_MUMMY_SPAWN_EGG = ITEMS.register("bog_mummy_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.BOG_MUMMY, 0x625638, 0x8CFF78, new Item.Properties()));
    public static final RegistryObject<Item> GILDED_LEGIONNAIRE_SPAWN_EGG = ITEMS.register("gilded_legionnaire_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.GILDED_LEGIONNAIRE, 0xC49232, 0x8C1A1E, new Item.Properties()));
    public static final RegistryObject<Item> GLADIATOR_SHADE_SPAWN_EGG = ITEMS.register("gladiator_shade_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.GLADIATOR_SHADE, 0x28202E, 0xD2C8FF, new Item.Properties()));
    // </generated-empire-eggs>
    public static final RegistryObject<Item> VOID_ZOMBIE_SPAWN_EGG = ITEMS.register("void_zombie_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.VOID_ZOMBIE, 0x22183A, 0xC46EFF, new Item.Properties()));
    public static final RegistryObject<Item> VOID_SKELETON_SPAWN_EGG = ITEMS.register("void_skeleton_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.VOID_SKELETON, 0x32264A, 0xE08CFF, new Item.Properties()));
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
        all.addAll(U_HANDHELD);
        all.addAll(U_LARGE);
        return all;
    }

    /** Long and heavy weapons, held bigger: polearms, great blades, hammers, flails and staves. */
    public static List<RegistryObject<Item>> large() {
        List<RegistryObject<Item>> all = new ArrayList<>(List.of(GLACIAL_IRON_GREATAXE, VORATH_WRATH, BRASS_STAFF, BRASS_ANKH_ROD, GLACIAL_IRON_STAFF, IMPERIAL_HALBERD, CITY_HAMMER,
                WAR_HAMMER, JOUSTING_LANCE, WAR_MACE, CRYSTAL_TRIDENT, SHADOW_SCYTHE, BATTLE_GREATSWORD, PHLEGM_MACE, DOUBLE_FLAIL,
                GRAVITY_HAMMER, FROST_SPEAR, JACKAL_GLAIVE, WAS_SCEPTRE, LEGION_PILUM, JUSTICAR_MAUL, INFERNAL_GREATAXE, SKALDBREAKER, RIMETOOTH, JACKALS_JUDGEMENT, STORMCALLER, GREEDS_CHAIN));
        all.addAll(A3_LARGE);
        all.addAll(U_LARGE);
        return all;
    }

    /** Firearms and the drill: held barrel forward (item/handheld_gun). */
    public static List<RegistryObject<Item>> guns() {
        List<RegistryObject<Item>> all = new ArrayList<>(A3_GUNS);
        all.addAll(U_GUN);
        return all;
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
        all.addAll(U_ARMOR);
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
        U_GUN.forEach(i -> items.add(i.get()));
        U_FLAT.forEach(i -> items.add(i.get()));
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
        List<Item> items = new ArrayList<>(List.of(INFERNAL_EMBER.get(), WAILING_SOUL.get(), DUNE_LEATHER.get(), FROSTPELT.get(), VOID_ASH.get(),
                MOONSILK.get(), SUNREED_PAPYRUS.get(), VOID_CRYSTAL.get(), VOID_INK.get()));
        SIN_GEMS.values().forEach(g -> items.add(g.get()));
        return items;
    }

    /** Potions, the Flask, food, scrolls and the story's items. */
    public static List<Item> consumables() {
        List<Item> items = new ArrayList<>();
        potions().forEach(i -> items.add(i.get()));
        for (RegistryObject<Item> i : List.of(BEARERS_FLASK, BRASS_FLASK, POMEGRANATE, DESERT_LOTUS,
                RETURN_SCROLL, BLUEPRINT, CODEX_SHARD, SEALING_QUILL, DINAR)) {
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
        List<RegistryObject<Item>> eggs = new ArrayList<>(List.of(VOID_ZOMBIE_SPAWN_EGG, VOID_SKELETON_SPAWN_EGG, VOID_WRETCH_SPAWN_EGG, VOID_STALKER_SPAWN_EGG, BRASS_SENTINEL_SPAWN_EGG,
                KALETH_SPAWN_EGG, SERATH_SPAWN_EGG, VORATH_SPAWN_EGG, SAND_GHOUL_SPAWN_EGG, CLOCKWORK_SCARAB_SPAWN_EGG, DRAUGR_SPAWN_EGG,
                RIME_WOLF_SPAWN_EGG, MIRAGE_DANCER_SPAWN_EGG, BOG_MUMMY_SPAWN_EGG, GILDED_LEGIONNAIRE_SPAWN_EGG, GLADIATOR_SHADE_SPAWN_EGG));
        eggs.addAll(bossEggs());
        return eggs;
    }

    /** Plain items with a flat model and textures/item/&lt;id&gt;.png. */
    public static List<RegistryObject<Item>> flatItems() {
        List<RegistryObject<Item>> flat = new ArrayList<>(List.of(RETURN_SCROLL, CODEX_SHARD, INFERNAL_EMBER, WAILING_SOUL, DINAR,
                BRASS_AMULET, BRASS_RING, SMALL_TALISMAN, LARGE_TALISMAN, BLUEPRINT, BRASS_FLASK, POMEGRANATE, DESERT_LOTUS,
                MINOR_POMEGRANATE_ELIXIR, BEARERS_TONIC, BEARERS_FLASK, DUNE_LEATHER, FROSTPELT, VOID_ASH,
                MOONSILK, SUNREED_PAPYRUS, VOID_CRYSTAL, VOID_INK, SEALING_QUILL));
        flat.addAll(SIN_GEMS.values());
        flat.addAll(armorPieces());
        flat.addAll(potions());
        flat.addAll(jewelry());
        flat.addAll(veiled());
        flat.addAll(A3_FLAT);
        flat.addAll(U_FLAT);
        flat.add(ROYAL_COIN);
        return flat;
    }
}
