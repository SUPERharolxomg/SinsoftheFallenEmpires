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

    private SoFETiers() {
    }

    /** Called during common setup: puts the tiers in their place among vanilla's. */
    public static void register() {
        TierSortingRegistry.registerTier(BRASS, SoFEMod.id("sulthari_brass"), List.of(Tiers.STONE), List.of(Tiers.IRON));
        TierSortingRegistry.registerTier(GLACIAL_IRON, SoFEMod.id("glacial_iron"), List.of(Tiers.IRON), List.of(Tiers.DIAMOND));
    }

    /** Brass and Glacial Iron armor; the texture is textures/models/armor/&lt;name&gt;_layer_1.png. */
    public enum Armor implements ArmorMaterial {
        BRASS("brass", 12, new int[]{2, 4, 5, 2}, 14, 0f, 0f, () -> MaterialRegistry.item(Material.SULTHARI_BRASS, MaterialForm.INGOT)),
        GLACIAL_IRON("glacial_iron", 22, new int[]{3, 5, 7, 3}, 10, 1.0f, 0.05f, () -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT));

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
