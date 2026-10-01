package com.sofe.registry;

import com.sofe.SoFEMod;
import com.sofe.gear.GearItems;
import com.sofe.gear.GearSlot;
import com.sofe.gear.SoFETiers;
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
        return ITEMS.register(id, () -> new GearItems.Armor(material, type, new Item.Properties()));
    }

    private static RegistryObject<Item> trinket(String id, GearSlot slot) {
        return ITEMS.register(id, () -> new GearItems.Trinket(slot, new Item.Properties()));
    }

    /** Weapons and tools, held like a sword (handheld item models). */
    public static List<RegistryObject<Item>> handheld() {
        return List.of(BRASS_SCIMITAR, BRASS_DAGGER, BRASS_LONGSWORD, BRASS_STAFF, BRASS_ANKH_ROD, GLACIAL_IRON_SWORD, GLACIAL_IRON_GREATAXE,
                GLACIAL_IRON_STAFF, BRASS_PICKAXE, GLACIAL_IRON_PICKAXE, KALETH_BLADE, SERATH_FANG, VORATH_WRATH);
    }

    public static List<RegistryObject<Item>> armorPieces() {
        return List.of(BRASS_HELMET, BRASS_CHESTPLATE, BRASS_LEGGINGS, BRASS_BOOTS,
                GLACIAL_IRON_HELMET, GLACIAL_IRON_CHESTPLATE, GLACIAL_IRON_LEGGINGS, GLACIAL_IRON_BOOTS);
    }

    /** Items other than materials and blocks, for the creative tab. */
    public static List<Item> creativeItems() {
        List<Item> items = new ArrayList<>();
        handheld().forEach(i -> items.add(i.get()));
        armorPieces().forEach(i -> items.add(i.get()));
        for (RegistryObject<Item> i : List.of(BRASS_AMULET, BRASS_RING, SMALL_TALISMAN, LARGE_TALISMAN, RETURN_SCROLL, CODEX_SHARD,
                INFERNAL_EMBER, WAILING_SOUL, DINAR, BLUEPRINT, BRASS_FLASK, POMEGRANATE, DESERT_LOTUS, MINOR_POMEGRANATE_ELIXIR,
                BEARERS_TONIC, BEARERS_FLASK, DUNE_LEATHER, FROSTPELT, VOID_ASH)) {
            items.add(i.get());
        }
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
        return flat;
    }
}
