package com.sofe.travel;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Personal Vault (docs/Jugabilidad.md, G5): every Vault block opens the same storage, and each
 * player only sees their own. 27 slots now; 54 once bought with Dinars (Sprint 5.5).
 */
public class VaultBlock extends Block {

    public VaultBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer server) {
            TravelCapability.get(server).ifPresent(travel -> {
                int rows = Math.min(6, travel.vaultRows()); // vanilla has chest menus up to 6 rows
                Container view = new VaultView(travel, rows * 9);
                MenuType<ChestMenu> type = rows > 3 ? MenuType.GENERIC_9x6 : MenuType.GENERIC_9x3;
                server.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ChestMenu(type, id, inventory, view, rows),
                        Component.translatable("container.sofe.personal_vault")));
                level.playSound(null, pos, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.6f, 1.1f);
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** The first slots of the player's vault, as a chest menu sees them. */
    private record VaultView(TravelData travel, int size) implements Container {
        @Override
        public int getContainerSize() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < size; i++) if (!getItem(i).isEmpty()) return false;
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return travel.vault().getItem(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return travel.vault().removeItem(slot, amount);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return travel.vault().removeItemNoUpdate(slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            travel.vault().setItem(slot, stack);
        }

        @Override
        public void setChanged() {
            travel.vault().setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < size; i++) travel.vault().setItem(i, ItemStack.EMPTY);
        }
    }
}
