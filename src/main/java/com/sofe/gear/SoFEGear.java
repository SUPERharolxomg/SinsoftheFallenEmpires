package com.sofe.gear;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

/**
 * A SoFE gear item: it can carry rolled affixes, an item level and a rarity. Weapons, armor, jewelry
 * and talismans implement it; its name takes the rarity's color, and Imperial items get a generated name.
 */
public interface SoFEGear {

    GearSlot gearSlot();

    /** The name in the rarity color; Imperial items use their generated name ("Vizier's Fang"). */
    static Component name(ItemStack stack, Component plain) {
        GearData gear = GearNbt.read(stack).orElse(GearData.common(1));
        MutableComponent name;
        if (gear.rarity() == Rarity.IMPERIAL && gear.nameParts().size() == 2) {
            name = Component.translatable("gear.sofe.name.format",
                    Component.translatable("gear.sofe.name.first." + gear.nameParts().get(0)),
                    Component.translatable("gear.sofe.name.second." + gear.nameParts().get(1)));
        } else {
            name = plain.copy();
        }
        return name.withStyle(Style.EMPTY.withColor(gear.rarity().color()));
    }
}
