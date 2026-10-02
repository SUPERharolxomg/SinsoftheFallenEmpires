package com.sofe.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Locale;

/** A shield of one of the empires: it blocks like any shield, and has the power of its empire when it does. */
public class EmpireShield extends ShieldItem {
    /** The Nordrath shield returns this much of the blocked damage to the attacker. */
    public static final float REFLECT = 0.3f;

    public enum Power {
        /** Slows the attacker. */
        SULTHARI,
        /** Returns part of the blow to the attacker. */
        NORDRATH,
        /** Heals the defender a little. */
        OBSERVATORY,
        /** Weakens and withers the attacker. */
        VOID;

        public String translationKey() {
            return "shield.sofe." + name().toLowerCase(Locale.ROOT);
        }
    }

    private final Power power;

    public EmpireShield(Power power, Properties properties) {
        super(properties);
        this.power = power;
    }

    public Power power() {
        return power;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(power.translationKey()).withStyle(ChatFormatting.DARK_AQUA));
    }

    /** Drawn with the vanilla shield's model and the empire's texture. */
    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return com.sofe.client.render.EmpireShieldRenderer.INSTANCE;
            }
        });
    }

    /** The empire shields are named, not "Shield": ShieldItem would add the banner colour to the name. */
    @Override
    public String getDescriptionId(ItemStack stack) {
        return getDescriptionId();
    }
}
