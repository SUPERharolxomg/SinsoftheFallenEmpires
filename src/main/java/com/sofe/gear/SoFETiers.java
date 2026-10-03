package com.sofe.gear;

import com.sofe.SoFEMod;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;
import java.util.function.Supplier;

/**
 * The empires' tool tiers and armor materials (docs/Mundo.md, W4). Sulthari Brass sits between stone
 * and iron, Glacial Iron between iron and diamond, registered with Forge's TierSortingRegistry.
 */
public final class SoFETiers {
    public static final TagKey<Block> NEEDS_BRASS_TOOL = BlockTags.create(SoFEMod.id("needs_brass_tool"));
    public static final TagKey<Block> NEEDS_GLACIAL_IRON_TOOL = BlockTags.create(SoFEMod.id("needs_glacial_iron_tool"));

    public static final Tier BRASS = new ForgeTier(2, 320, 6.5f, 2.0f, 14, NEEDS_BRASS_TOOL,
            () -> Ingredient.of(MaterialRegistry.item(Material.SULTHARI_BRASS, MaterialForm.INGOT)));
    public static final Tier GLACIAL_IRON = new ForgeTier(3, 900, 7.0f, 2.5f, 12, NEEDS_GLACIAL_IRON_TOOL,
            () -> Ingredient.of(MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)));

    public static final TagKey<Block> NEEDS_STAR_LAPIS_TOOL = BlockTags.create(SoFEMod.id("needs_star_lapis_tool"));
    public static final TagKey<Block> NEEDS_SOLAR_GOLD_TOOL = BlockTags.create(SoFEMod.id("needs_solar_gold_tool"));
    public static final TagKey<Block> NEEDS_ORICHALCUM_TOOL = BlockTags.create(SoFEMod.id("needs_orichalcum_tool"));
    public static final TagKey<Block> NEEDS_AETHERIUM_TOOL = BlockTags.create(SoFEMod.id("needs_aetherium_tool"));
    /** Star Lapis: a fast, enchantable gem tool between Glacial Iron and diamond. */
    public static final Tier STAR_LAPIS = new ForgeTier(3, 700, 10.0f, 2.5f, 22, NEEDS_STAR_LAPIS_TOOL,
            () -> Ingredient.of(MaterialRegistry.item(Material.STAR_LAPIS, MaterialForm.GEM)));
    /** Solar Gold, Orichalcum and Aetherium: the tiers of Acts III to V, above diamond. */
    public static final Tier SOLAR_GOLD = new ForgeTier(4, 1600, 9.0f, 3.5f, 20, NEEDS_SOLAR_GOLD_TOOL,
            () -> Ingredient.of(MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)));
    public static final Tier ORICHALCUM = new ForgeTier(4, 2200, 9.5f, 4.0f, 15, NEEDS_ORICHALCUM_TOOL,
            () -> Ingredient.of(MaterialRegistry.item(Material.ORICHALCUM, MaterialForm.INGOT)));
    public static final Tier AETHERIUM = new ForgeTier(5, 3000, 11.0f, 5.0f, 18, NEEDS_AETHERIUM_TOOL,
            () -> Ingredient.of(MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)));

    private SoFETiers() {
    }

    /** Called during common setup: puts the tiers in their place among vanilla's. */
    public static void register() {
        TierSortingRegistry.registerTier(BRASS, SoFEMod.id("sulthari_brass"), List.of(Tiers.STONE), List.of(Tiers.IRON));
        TierSortingRegistry.registerTier(GLACIAL_IRON, SoFEMod.id("glacial_iron"), List.of(Tiers.IRON), List.of(Tiers.DIAMOND));
        TierSortingRegistry.registerTier(STAR_LAPIS, SoFEMod.id("star_lapis"), List.of(GLACIAL_IRON), List.of(Tiers.DIAMOND));
        TierSortingRegistry.registerTier(SOLAR_GOLD, SoFEMod.id("solar_gold"), List.of(Tiers.DIAMOND), List.of(Tiers.NETHERITE));
        TierSortingRegistry.registerTier(ORICHALCUM, SoFEMod.id("orichalcum"), List.of(Tiers.NETHERITE), List.of());
        TierSortingRegistry.registerTier(AETHERIUM, SoFEMod.id("aetherium"), List.of(ORICHALCUM), List.of());
    }

    /** Brass and Glacial Iron armor; the texture is textures/models/armor/&lt;name&gt;_layer_1.png. */
    public enum Armor implements ArmorMaterial {
        BRASS("brass", 12, new int[]{2, 4, 5, 2}, 14, 0f, 0f, () -> MaterialRegistry.item(Material.SULTHARI_BRASS, MaterialForm.INGOT)),
        GLACIAL_IRON("glacial_iron", 22, new int[]{3, 5, 7, 3}, 10, 1.0f, 0.05f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        // the arsenal, batch 2: armor sets with a bonus when the four pieces are worn (ArmorSets)
        SCOUT("scout", 10, new int[]{1, 3, 4, 1}, 15, 0f, 0f, () -> com.sofe.registry.ItemRegistry.DUNE_LEATHER.get()),
        BRONZE("bronze", 14, new int[]{2, 4, 5, 2}, 12, 0f, 0f, () -> MaterialRegistry.item(Material.SULTHARI_BRASS, MaterialForm.INGOT)),
        GEAR("gear", 16, new int[]{2, 5, 6, 2}, 12, 0.5f, 0f, () -> MaterialRegistry.item(Material.SULTHARI_BRASS, MaterialForm.INGOT)),
        OBSERVATORY("observatory", 20, new int[]{2, 5, 7, 2}, 14, 1.0f, 0f, () -> MaterialRegistry.item(Material.SULTHARI_BRASS, MaterialForm.INGOT)),
        EMERALD("emerald", 24, new int[]{3, 5, 7, 3}, 12, 1.5f, 0f, () -> MaterialRegistry.item(Material.ORICHALCUM, MaterialForm.INGOT)),
        COBALT("cobalt", 28, new int[]{3, 6, 8, 3}, 10, 2.5f, 0.1f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        SOLARI("solari", 26, new int[]{3, 6, 8, 3}, 16, 2.0f, 0.05f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        CHRONOMANCER("chronomancer", 18, new int[]{2, 4, 6, 2}, 20, 0.5f, 0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        VOID("void", 30, new int[]{3, 6, 8, 3}, 12, 3.0f, 0.1f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        // batch 2b: the sets of Nordrath (Act II), Khemet (Act III), Aureum (Act IV) and the Infernal relic set
        BERSERKER("berserker", 22, new int[]{2, 5, 7, 2}, 10, 1.0f, 0.05f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        SEAFARER("seafarer", 18, new int[]{2, 4, 6, 2}, 14, 0f, 0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        SCARAB("scarab", 26, new int[]{3, 5, 7, 3}, 16, 1.5f, 0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        EMBALMER("embalmer", 20, new int[]{2, 4, 6, 2}, 20, 0.5f, 0f, () -> com.sofe.registry.ItemRegistry.DUNE_LEATHER.get()),
        LEGION("legion", 30, new int[]{3, 6, 8, 3}, 12, 2.0f, 0.1f, () -> MaterialRegistry.item(Material.ORICHALCUM, MaterialForm.INGOT)),
        SCALE("scale", 32, new int[]{3, 6, 8, 3}, 14, 2.5f, 0.1f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        INFERNAL("infernal", 34, new int[]{3, 6, 8, 3}, 15, 3.0f, 0.1f, () -> com.sofe.registry.ItemRegistry.INFERNAL_EMBER.get()),
        // the unique armor pieces (droppable Relics): one material each, for its own worn look
        CROWN_OF_FIVE_SULTANS("crown_of_five_sultans", 26, new int[]{3, 6, 8, 3}, 18, 2.0f, 0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        WHITE_WOLF_MANTLE("white_wolf_mantle", 24, new int[]{3, 6, 8, 3}, 12, 2.0f, 0.05f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        CARAVAN_TREADS("caravan_treads", 20, new int[]{2, 5, 6, 2}, 15, 1.0f, 0f, () -> com.sofe.registry.ItemRegistry.DUNE_LEATHER.get()),
        UNDYING_WRAPPINGS("undying_wrappings", 26, new int[]{3, 6, 8, 3}, 18, 1.5f, 0f, () -> com.sofe.registry.ItemRegistry.DUNE_LEATHER.get()),
        BLIND_JUDGE_HELM("blind_judge_helm", 30, new int[]{3, 6, 8, 3}, 14, 2.5f, 0.1f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        FURNACE_HEART("furnace_heart", 32, new int[]{3, 6, 8, 3}, 14, 3.0f, 0.1f, () -> com.sofe.registry.ItemRegistry.INFERNAL_EMBER.get()),
        // <generated-unique-armor> by scripts/make_uniques.py: one material per unique armor piece
        HELM_OF_THE_LAST_KNIGHT("helm_of_the_last_knight", 23, new int[]{3, 5, 7, 3}, 10, 1.08f, 0.0f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        CROWN_OF_THE_SCALE("crown_of_the_scale", 40, new int[]{3, 7, 9, 3}, 10, 2.92f, 0.1f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        BULWARK_CUIRASS("bulwark_cuirass", 26, new int[]{3, 5, 7, 3}, 10, 1.32f, 0.0f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        HEART_OF_THE_LEGION("heart_of_the_legion", 49, new int[]{4, 8, 10, 4}, 10, 4.0f, 0.1f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        GREAVES_OF_THE_VIGIL("greaves_of_the_vigil", 28, new int[]{3, 5, 7, 3}, 10, 1.6f, 0.0f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        LEGS_OF_THE_UNYIELDING("legs_of_the_unyielding", 43, new int[]{4, 8, 10, 4}, 10, 3.32f, 0.1f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        MARCH_OF_THE_PALADIN("march_of_the_paladin", 24, new int[]{3, 5, 7, 3}, 10, 1.2f, 0.0f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        IRON_ROOTS("iron_roots", 41, new int[]{3, 7, 9, 3}, 10, 3.08f, 0.1f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        MASK_OF_THE_FERRYMAN("mask_of_the_ferryman", 18, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        CROWN_OF_THE_PALE_KING("crown_of_the_pale_king", 45, new int[]{2, 6, 8, 2}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        SHROUD_OF_KHEMET("shroud_of_khemet", 23, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        RIBCAGE_OF_THE_LICH("ribcage_of_the_lich", 40, new int[]{2, 6, 8, 2}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        WRAPS_OF_THE_LONG_SLEEP("wraps_of_the_long_sleep", 25, new int[]{1, 4, 6, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        GRAVE_DUST_TROUSERS("grave_dust_trousers", 36, new int[]{1, 5, 7, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        SANDALS_OF_THE_THRESHOLD("sandals_of_the_threshold", 19, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        TREADS_OF_THE_UNDERWORLD("treads_of_the_underworld", 39, new int[]{2, 6, 8, 2}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        VEIL_OF_LALEH("veil_of_laleh", 18, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        HAT_OF_THE_CIRCLE("hat_of_the_circle", 36, new int[]{1, 5, 7, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        ROBE_OF_WRITTEN_STARS("robe_of_written_stars", 24, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        MANTLE_OF_THE_ECLIPSE("mantle_of_the_eclipse", 45, new int[]{2, 6, 8, 2}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        SILKS_OF_PARSIVAN("silks_of_parsivan", 20, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        LEGGINGS_OF_THE_STORM_COURT("leggings_of_the_storm_court", 34, new int[]{1, 5, 7, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        SLIPPERS_OF_THE_WATERLINE("slippers_of_the_waterline", 19, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        STEPS_OF_THE_COMET("steps_of_the_comet", 39, new int[]{2, 6, 8, 2}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        HOOD_OF_ASH("hood_of_ash", 17, new int[]{1, 4, 5, 1}, 14, 0.4f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        MASK_OF_THE_HEIST("mask_of_the_heist", 37, new int[]{2, 6, 8, 2}, 14, 1.54f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        JERKIN_OF_MANY_POCKETS("jerkin_of_many_pockets", 22, new int[]{2, 4, 6, 2}, 14, 0.66f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        WOLFSKIN_OF_THE_RAIDER("wolfskin_of_the_raider", 45, new int[]{3, 7, 9, 3}, 14, 2.0f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        BREECHES_OF_THE_CUTPURSE("breeches_of_the_cutpurse", 20, new int[]{2, 4, 6, 2}, 14, 0.6f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        SHADOWSTEP_LEGWRAPS("shadowstep_legwraps", 34, new int[]{2, 6, 8, 2}, 14, 1.4f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        BOOTS_OF_THE_FJORD_RUNNER("boots_of_the_fjord_runner", 18, new int[]{2, 4, 6, 2}, 14, 0.46f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        SILENT_SOLES("silent_soles", 38, new int[]{2, 6, 8, 2}, 14, 1.6f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        TURBAN_OF_THE_PEACOCK_THRONE("turban_of_the_peacock_throne", 18, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        CROWN_OF_AZHAR("crown_of_azhar", 38, new int[]{1, 5, 7, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        KAFTAN_OF_THE_DIVAN("kaftan_of_the_divan", 23, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        ROBES_OF_THE_GOLDEN_AGE("robes_of_the_golden_age", 45, new int[]{2, 6, 8, 2}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        TROUSERS_OF_THE_VIZIER("trousers_of_the_vizier", 20, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        LEGS_OF_THE_CONQUEROR("legs_of_the_conqueror", 36, new int[]{1, 5, 7, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        SLIPPERS_OF_THE_HAREM_ROAD("slippers_of_the_harem_road", 19, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        TREAD_OF_KINGS("tread_of_kings", 39, new int[]{2, 6, 8, 2}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        HELM_OF_THE_WANDERER("helm_of_the_wanderer", 16, new int[]{1, 4, 5, 1}, 14, 0.34f, 0.0f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        ECLIPSE_PLATE("eclipse_plate", 37, new int[]{3, 7, 9, 3}, 10, 2.68f, 0.1f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        SANDSTRIDERS("sandstriders", 22, new int[]{2, 4, 6, 2}, 14, 0.66f, 0.0f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        LEGS_OF_THE_SEVENTH_SIN("legs_of_the_seventh_sin", 45, new int[]{4, 8, 10, 4}, 10, 3.6f, 0.1f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        // </generated-unique-armor>
        // <generated-armor> by scripts/make_armor_data.py from scripts/armor_catalog.py: the class sets
        SENTINEL("sentinel", 20, new int[]{2, 5, 6, 2}, 10, 0.64f, 0.0f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        DRAGONKNIGHT("dragonknight", 35, new int[]{3, 6, 8, 3}, 10, 2.4f, 0.1f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        WARLORD("warlord", 28, new int[]{3, 5, 7, 3}, 10, 1.6f, 0.0f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)),
        GRAVEWARDEN("gravewarden", 15, new int[]{1, 3, 4, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        BONELORD("bonelord", 24, new int[]{2, 4, 6, 2}, 14, 0.8f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        ANUBIS("anubis", 39, new int[]{3, 7, 9, 3}, 14, 1.68f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        PLAGUEBEARER("plaguebearer", 20, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        LICH("lich", 43, new int[]{2, 6, 8, 2}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        MUMMY_LORD("mummy_lord", 31, new int[]{2, 5, 7, 2}, 14, 1.2f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        SOULREAVER("soulreaver", 23, new int[]{2, 4, 6, 2}, 14, 0.72f, 0.0f, () -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)),
        ARCHMAGE("archmage", 39, new int[]{2, 6, 8, 2}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        PYROMANCER("pyromancer", 24, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        FROST_WITCH("frost_witch", 18, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        TEMPEST("tempest", 31, new int[]{1, 4, 6, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        ENCHANTRESS("enchantress", 16, new int[]{1, 3, 4, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        WITCH("witch", 12, new int[]{1, 3, 4, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        ASTRAL_SAGE("astral_sage", 43, new int[]{2, 6, 8, 2}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)),
        SHADOW("shadow", 23, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        WOLF_RAIDER("wolf_raider", 24, new int[]{2, 4, 6, 2}, 14, 0.8f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        HUNTSMAN("huntsman", 15, new int[]{1, 3, 4, 1}, 18, 0.0f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        NIGHTBLADE("nightblade", 36, new int[]{1, 5, 7, 1}, 18, 0.0f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        FROST_STALKER("frost_stalker", 28, new int[]{2, 5, 7, 2}, 14, 1.0f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        CORSAIR("corsair", 31, new int[]{1, 4, 6, 1}, 18, 0.0f, 0.0f, () -> com.sofe.registry.ItemRegistry.FROSTPELT.get()),
        SULTAN("sultan", 31, new int[]{1, 4, 6, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        VIZIER("vizier", 20, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        ROYAL_GUARD("royal_guard", 16, new int[]{1, 4, 5, 1}, 14, 0.32f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        DESERT_EMIR("desert_emir", 15, new int[]{1, 3, 4, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        GOLDEN_KING("golden_king", 43, new int[]{3, 7, 9, 3}, 14, 1.88f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        MIRAGE("mirage", 24, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        LION_KING("lion_king", 36, new int[]{2, 6, 8, 2}, 14, 1.48f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        PEACOCK("peacock", 18, new int[]{1, 3, 5, 1}, 18, 0.0f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)),
        PHARAOH("pharaoh", 39, new int[]{3, 7, 9, 3}, 14, 1.68f, 0.0f, () -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT));
        // </generated-armor>

        private static final int[] DURABILITY = {13, 15, 16, 11}; // boots, leggings, chestplate, helmet (vanilla order)
        private final String name;
        private final int durabilityMultiplier;
        private final int[] protection; // boots, leggings, chestplate, helmet
        private final int enchantability;
        private final float toughness, knockbackResistance;
        private final Supplier<net.minecraft.world.item.Item> repair;

        Armor(String name, int durabilityMultiplier, int[] protection, int enchantability, float toughness, float knockbackResistance,
              Supplier<net.minecraft.world.item.Item> repair) {
            this.name = name;
            this.durabilityMultiplier = durabilityMultiplier;
            this.protection = protection;
            this.enchantability = enchantability;
            this.toughness = toughness;
            this.knockbackResistance = knockbackResistance;
            this.repair = repair;
        }

        private static int index(ArmorItem.Type type) {
            return switch (type) {
                case BOOTS -> 0;
                case LEGGINGS -> 1;
                case CHESTPLATE -> 2;
                case HELMET -> 3;
            };
        }

        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return DURABILITY[index(type)] * durabilityMultiplier;
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return protection[index(type)];
        }

        @Override
        public int getEnchantmentValue() {
            return enchantability;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_IRON;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(repair.get());
        }

        @Override
        public String getName() {
            return SoFEMod.MOD_ID + ":" + name;
        }

        @Override
        public float getToughness() {
            return toughness;
        }

        @Override
        public float getKnockbackResistance() {
            return knockbackResistance;
        }
    }
}
