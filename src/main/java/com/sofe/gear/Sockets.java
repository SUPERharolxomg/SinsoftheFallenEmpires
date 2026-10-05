package com.sofe.gear;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gem sockets (docs/Pociones.md, "Sockets: weapons and chestplates can have 0–3 sockets"). The Jeweler opens them
 * for Dinars, one at a time, and sets a cut or Oath gem in an open one. A set gem is kept as one more affix of the
 * item (its id "gem:&lt;item&gt;"), so the bonuses, the tooltip and the comparisons treat it like any other line;
 * the number of sockets opened is kept beside the affixes.
 */
public final class Sockets {
    public static final int MAX = 3;
    /** What the Jeweler asks to open the first, second and third socket (Dinars). */
    private static final long[] OPEN_COST = {250, 750, 2000};
    public static final String GEM_PREFIX = "gem:";
    private static final String TAG = "sofe_gear", SOCKETS = "sockets";

    private Sockets() {
    }

    /** Weapons and chestplates of SoFE gear take sockets. */
    public static boolean socketable(ItemStack stack) {
        if (!(stack.getItem() instanceof SoFEGear gear)) return false;
        if (gear.gearSlot() == GearSlot.WEAPON) return true;
        return stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.CHEST;
    }

    public static int opened(ItemStack stack) {
        var tag = stack.getTagElement(TAG);
        return tag == null ? 0 : tag.getInt(SOCKETS);
    }

    public static List<GearData.Roll> gems(GearData gear) {
        List<GearData.Roll> out = new ArrayList<>();
        for (GearData.Roll r : gear.affixes()) if (isGem(r)) out.add(r);
        return out;
    }

    public static boolean isGem(GearData.Roll roll) {
        return roll.affix() != null && roll.affix().startsWith(GEM_PREFIX);
    }

    public static int filled(ItemStack stack) {
        return GearNbt.read(stack).map(g -> gems(g).size()).orElse(0);
    }

    /** The Dinars the next socket costs, or empty when the item cannot take another. */
    public static Optional<Long> openCost(ItemStack stack) {
        int n = opened(stack);
        if (!socketable(stack) || n >= MAX) return Optional.empty();
        return Optional.of(OPEN_COST[n]);
    }

    /** Opens one more socket (the caller has taken the Dinars). */
    public static boolean open(ItemStack stack) {
        if (openCost(stack).isEmpty()) return false;
        if (GearNbt.read(stack).isPresent() && stack.getTagElement(TAG) == null) GearNbt.write(stack, GearData.common(1));
        stack.getOrCreateTagElement(TAG).putInt(SOCKETS, opened(stack) + 1);
        return true;
    }

    /** Sets a cut or Oath gem in an open socket; false when there is none or the gem is rough. */
    public static boolean set(ItemStack stack, SinGem gem, SinGem.Form form) {
        if (form == SinGem.Form.ROUGH || !socketable(stack)) return false;
        Optional<GearData> read = GearNbt.read(stack);
        if (read.isEmpty() || gems(read.get()).size() >= opened(stack)) return false;
        GearData gear = read.get();
        List<GearData.Roll> rolls = new ArrayList<>(gear.affixes());
        rolls.add(new GearData.Roll(GEM_PREFIX + gem.id(form), gem.stat(), null, gem.value(form)));
        GearNbt.write(stack, gear.withAffixes(gear.rarity(), rolls));
        return true;
    }
}
