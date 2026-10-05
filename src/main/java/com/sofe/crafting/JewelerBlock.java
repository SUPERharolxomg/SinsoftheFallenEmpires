package com.sofe.crafting;

import com.sofe.economy.EconomyCapability;
import com.sofe.economy.EconomyHandler;
import com.sofe.gear.SinGem;
import com.sofe.gear.Sockets;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The Jeweler (docs/Pociones.md, "Forges and crafting": Kerem the cutter): with empty hands it lists its cuts (rough
 * gem and Dinars into a cut gem); with a weapon or chestplate in hand it works on its sockets: sneaking opens the
 * next socket for Dinars, and a cut or Oath gem in the other hand is set in an open socket.
 */
public class JewelerBlock extends StationBlock {
    public JewelerBlock(Properties properties) {
        super(StationRecipe.Kind.JEWELER, properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack gear = player.getMainHandItem();
        if (hand != InteractionHand.MAIN_HAND || !Sockets.socketable(gear)) return super.use(state, level, pos, player, hand, hit);
        if (player instanceof ServerPlayer server) work(server, gear, player.getOffhandItem(), pos);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** Opens a socket (sneaking) or sets the gem held in the other hand. */
    public static void work(ServerPlayer player, ItemStack gear, ItemStack offhand, BlockPos pos) {
        if (player.isShiftKeyDown()) {
            var cost = Sockets.openCost(gear);
            if (cost.isEmpty()) {
                tell(player, Component.translatable("message.sofe.jeweler.full", Sockets.MAX), ChatFormatting.RED);
                return;
            }
            var wallet = EconomyCapability.get(player).orElse(null);
            if (wallet == null || !wallet.spend(cost.get())) {
                tell(player, Component.translatable("message.sofe.jeweler.no_dinars", cost.get()), ChatFormatting.RED);
                return;
            }
            Sockets.open(gear);
            EconomyHandler.sync(player);
            player.level().playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.6f, 1.6f);
            tell(player, Component.translatable("message.sofe.jeweler.opened", Sockets.opened(gear), Sockets.MAX, cost.get()), ChatFormatting.GOLD);
            return;
        }
        var found = SinGem.of(String.valueOf(ForgeRegistries.ITEMS.getKey(offhand.getItem())));
        if (found.isEmpty()) {
            tell(player, Component.translatable("message.sofe.jeweler.how", Sockets.filled(gear), Sockets.opened(gear)), ChatFormatting.GRAY);
            return;
        }
        if (found.get().form() == SinGem.Form.ROUGH) {
            tell(player, Component.translatable("message.sofe.jeweler.rough"), ChatFormatting.RED);
            return;
        }
        if (!Sockets.set(gear, found.get().gem(), found.get().form())) {
            tell(player, Component.translatable("message.sofe.jeweler.no_socket"), ChatFormatting.RED);
            return;
        }
        offhand.shrink(1);
        player.level().playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1f, 1.2f);
        tell(player, Component.translatable("message.sofe.jeweler.set", offhand.isEmpty() ? Component.translatable("item.sofe." + found.get().gem().id(found.get().form()))
                : offhand.getHoverName()), ChatFormatting.GOLD);
    }

    private static void tell(ServerPlayer player, Component message, ChatFormatting color) {
        player.displayClientMessage(message.copy().withStyle(color), true);
    }
}
