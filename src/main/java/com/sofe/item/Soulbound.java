package com.sofe.item;

import com.sofe.SoFEMod;
import com.sofe.gear.GearData;
import com.sofe.gear.GearNbt;
import com.sofe.gear.Rarity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Locale;

/**
 * Who an item belongs to (docs/Anexos.md, A6, "sofe:owner" and BindPolicy). Story items (the Bearer's
 * Flask, Codex Shards) are SOULBOUND: never dropped, never left on the corpse, kept through death.
 * Relics and Legacy pieces follow the server's policy (FREE by default).
 */
public final class Soulbound {
    /** Items that are always soulbound (data/sofe/tags/items/soulbound.json). */
    public static final TagKey<Item> SOULBOUND = TagKey.create(Registries.ITEM, SoFEMod.id("soulbound"));
    private static final String KEPT = SoFEMod.MOD_ID + ":soulbound_kept";

    public enum BindPolicy {
        FREE, PACT_ONLY, SOULBOUND;

        public static BindPolicy byId(String id) {
            return valueOf(id.toUpperCase(Locale.ROOT));
        }
    }

    private Soulbound() {
    }

    /** The policy for Relics and Legacies from the server config. */
    public static BindPolicy relicPolicy() {
        try {
            return BindPolicy.byId(com.sofe.config.SoFEConfig.SERVER.relicBinding.get());
        } catch (IllegalArgumentException e) {
            return BindPolicy.FREE;
        }
    }

    public static boolean is(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(SOULBOUND)) return true;
        return GearNbt.read(stack).map(GearData::rarity)
                .filter(r -> r == Rarity.RELIC || r == Rarity.LEGACY)
                .map(r -> relicPolicy() == BindPolicy.SOULBOUND).orElse(false);
    }

    /** A soulbound item cannot be thrown away: it goes back to the inventory. */
    public static void onToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        if (!is(stack)) return;
        Player player = event.getPlayer();
        if (player.getInventory().add(stack) || stack.isEmpty()) {   // back in the pack: the toss never happens
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("message.sofe.soulbound").withStyle(ChatFormatting.LIGHT_PURPLE), true);
            return;
        }
        // no room in the pack (a full inventory when a boss gives its Shard): it falls, but it never fades and only
        // its owner can pick it up. Putting it back here would toss it again, and again, until the server falls.
        var item = event.getEntity();
        item.setUnlimitedLifetime();
        item.setTarget(player.getUUID());
        item.setNoPickUpDelay();
        player.displayClientMessage(Component.translatable("message.sofe.soulbound.full").withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }

    /**
     * On death, before the corpse and the drops: soulbound items leave the inventory and are kept on
     * the player, to come back after the respawn.
     */
    public static void keepOnDeath(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        ListTag kept = new ListTag();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!is(stack)) continue;
            kept.add(stack.save(new CompoundTag()));
            inventory.setItem(i, ItemStack.EMPTY);
        }
        if (!kept.isEmpty()) player.getPersistentData().put(KEPT, kept);
    }

    /** The new player after a respawn gets the soulbound items back. */
    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        CompoundTag old = event.getOriginal().getPersistentData();
        if (!old.contains(KEPT)) return;
        ListTag kept = old.getList(KEPT, Tag.TAG_COMPOUND);
        for (int i = 0; i < kept.size(); i++) {
            ItemStack stack = ItemStack.of(kept.getCompound(i));
            if (!event.getEntity().getInventory().add(stack)) event.getEntity().drop(stack, false);
        }
        old.remove(KEPT);
    }
}
