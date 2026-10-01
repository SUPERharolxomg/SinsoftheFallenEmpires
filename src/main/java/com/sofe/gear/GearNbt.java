package com.sofe.gear;

import com.sofe.progression.CharacterAttribute;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Gear data and ownership in an item's NBT (docs/Pociones.md; docs/Anexos.md A6, "sofe:owner").
 * A SoFE gear item without data (crafted at a table, bought) counts as Common, item level 1.
 */
public final class GearNbt {
    private static final String TAG = "sofe_gear", OWNER = "sofe_owner";

    private GearNbt() {
    }

    public static boolean isGear(ItemStack stack) {
        return stack.getItem() instanceof SoFEGear;
    }

    public static Optional<GearData> read(ItemStack stack) {
        if (!isGear(stack)) return Optional.empty();
        CompoundTag tag = stack.getTagElement(TAG);
        if (tag == null) return Optional.of(GearData.common(1));
        List<GearData.Roll> rolls = new ArrayList<>();
        ListTag list = tag.getList("affixes", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag a = list.getCompound(i);
            try {
                rolls.add(new GearData.Roll(a.getString("id"), GearStat.byId(a.getString("stat")),
                        a.contains("param") ? a.getString("param") : null, a.getInt("value")));
            } catch (IllegalArgumentException ignored) {
                // a stat removed in a later version: the rest of the item still works
            }
        }
        Rarity rarity;
        try {
            rarity = Rarity.byId(tag.getString("rarity"));
        } catch (IllegalArgumentException e) {
            rarity = Rarity.COMMON;
        }
        CharacterAttribute attribute = tag.contains("req_attr") ? CharacterAttribute.valueOf(tag.getString("req_attr").toUpperCase(Locale.ROOT)) : null;
        List<Integer> parts = new ArrayList<>();
        for (int p : tag.getIntArray("name")) parts.add(p);
        return Optional.of(new GearData(Math.max(1, tag.getInt("ilvl")), rarity, rolls, attribute, tag.getInt("req_value"),
                tag.contains("relic") ? tag.getString("relic") : null, parts));
    }

    public static void write(ItemStack stack, GearData gear) {
        CompoundTag tag = stack.getOrCreateTagElement(TAG);
        tag.putInt("ilvl", gear.itemLevel());
        tag.putString("rarity", gear.rarity().id());
        ListTag list = new ListTag();
        for (GearData.Roll r : gear.affixes()) {
            CompoundTag a = new CompoundTag();
            a.putString("id", r.affix());
            a.putString("stat", r.stat().id());
            if (r.parameter() != null) a.putString("param", r.parameter());
            a.putInt("value", r.value());
            list.add(a);
        }
        tag.put("affixes", list);
        if (gear.requiredAttribute() != null) {
            tag.putString("req_attr", gear.requiredAttribute().id());
            tag.putInt("req_value", gear.requiredValue());
        }
        if (gear.relic() != null) tag.putString("relic", gear.relic());
        if (!gear.nameParts().isEmpty()) tag.putIntArray("name", gear.nameParts().stream().mapToInt(Integer::intValue).toArray());
    }

    public static Optional<String> relic(ItemStack stack) {
        return read(stack).map(GearData::relic);
    }

    /** "Bound to &lt;name&gt;": Relics and story items carry who they belong to. */
    public static void bind(ItemStack stack, Player owner) {
        CompoundTag tag = stack.getOrCreateTagElement(OWNER);
        tag.putUUID("uuid", owner.getUUID());
        tag.putString("name", owner.getGameProfile().getName());
    }

    public static Optional<UUID> owner(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(OWNER);
        return tag != null && tag.hasUUID("uuid") ? Optional.of(tag.getUUID("uuid")) : Optional.empty();
    }

    public static Optional<String> ownerName(ItemStack stack) {
        CompoundTag tag = stack.getTagElement(OWNER);
        return tag != null && tag.contains("name") ? Optional.of(tag.getString("name")) : Optional.empty();
    }
}
