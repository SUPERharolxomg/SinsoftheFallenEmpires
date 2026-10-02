package com.sofe.gear;

import com.sofe.gear.GearNbt;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

/** The item classes of SoFE gear: weapons, armor, pickaxes, jewelry and talismans. */
public final class GearItems {

    private GearItems() {
    }

    public static class Sword extends SwordItem implements SoFEGear {
        public Sword(Tier tier, int damage, float speed, Properties properties) {
            super(tier, damage, speed, properties);
        }

        @Override
        public GearSlot gearSlot() {
            return GearSlot.WEAPON;
        }

        @Override
        public Component getName(ItemStack stack) {
            return SoFEGear.name(stack, super.getName(stack));
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return GearNbt.read(stack).map(g -> g.rarity() == Rarity.RELIC).orElse(false) || super.isFoil(stack);
        }
    }

    public static class Axe extends AxeItem implements SoFEGear {
        public Axe(Tier tier, float damage, float speed, Properties properties) {
            super(tier, damage, speed, properties);
        }

        @Override
        public GearSlot gearSlot() {
            return GearSlot.WEAPON;
        }

        @Override
        public Component getName(ItemStack stack) {
            return SoFEGear.name(stack, super.getName(stack));
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return GearNbt.read(stack).map(g -> g.rarity() == Rarity.RELIC).orElse(false) || super.isFoil(stack);
        }
    }

    public static class Armor extends ArmorItem implements SoFEGear {
        public Armor(ArmorMaterial material, Type type, Properties properties) {
            super(material, type, properties);
        }

        @Override
        public GearSlot gearSlot() {
            return GearSlot.ARMOR;
        }

        @Override
        public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, java.util.List<Component> tooltip,
                                    net.minecraft.world.item.TooltipFlag flag) {
            super.appendHoverText(stack, level, tooltip, flag);
            if (getMaterial() instanceof SoFETiers.Armor set && ArmorSets.bonusKey(set) != null) {
                tooltip.add(Component.translatable(ArmorSets.bonusKey(set)).withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
            }
        }

        @Override
        public Component getName(ItemStack stack) {
            return SoFEGear.name(stack, super.getName(stack));
        }
    }

    /** Pickaxes are tools, not loot: they have tiers but never roll affixes. */
    public static class Pickaxe extends PickaxeItem {
        public Pickaxe(Tier tier, int damage, float speed, Properties properties) {
            super(tier, damage, speed, properties);
        }
    }

    /** Amulets, rings (Curios necklace and ring slots) and talismans (the Talisman Pouch). */
    public static class Trinket extends Item implements SoFEGear {
        private final GearSlot slot;

        public Trinket(GearSlot slot, Properties properties) {
            super(properties.stacksTo(1));
            this.slot = slot;
        }

        @Override
        public GearSlot gearSlot() {
            return slot;
        }

        @Override
        public Component getName(ItemStack stack) {
            return SoFEGear.name(stack, super.getName(stack));
        }
    }
}
