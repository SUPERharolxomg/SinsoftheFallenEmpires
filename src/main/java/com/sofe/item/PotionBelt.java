package com.sofe.item;

import com.sofe.gear.PlayerGear;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * The potion belt (docs/Pociones.md): a Curios slot with 4 spaces, used from the Combat Bar keys
 * (Left Alt + 7, 8, 9, 0). All potions share a 1 s cooldown.
 */
public final class PotionBelt {
    public static final int SIZE = 4;

    private PotionBelt() {
    }

    public static void drink(ServerPlayer player, int slot) {
        if (slot < 0 || slot >= SIZE) return;
        CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(curios -> curios.getStacksHandler(PlayerGear.POTION_BELT))
                .ifPresent(handler -> {
                    if (slot >= handler.getSlots()) return;
                    ItemStack stack = handler.getStacks().getStackInSlot(slot);
                    if (!(stack.getItem() instanceof ConsumableItems.Potion potion) || player.getCooldowns().isOnCooldown(potion)) return;
                    // the potion is drunk at once from the belt: no drinking animation in combat
                    ItemStack left = potion.finishUsingItem(stack, player.level(), player);
                    if (left.getItem() instanceof ConsumableItems.Potion) {
                        handler.getStacks().setStackInSlot(slot, left);
                    } else {
                        handler.getStacks().setStackInSlot(slot, ItemStack.EMPTY);
                        if (!left.isEmpty() && !player.getInventory().add(left)) player.drop(left, false);
                    }
                    player.swing(InteractionHand.MAIN_HAND, true);
                });
    }
}
