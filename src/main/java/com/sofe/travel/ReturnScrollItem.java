package com.sofe.travel;

import com.sofe.world.SoFEWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The Return Scroll (docs/Jugabilidad.md, G2): read it for 5 seconds without stopping and it takes
 * the player back to Sulthari. Also the "I am stuck" option. Sold by Yusuf (Sprint 5.5).
 */
public class ReturnScrollItem extends Item {
    public static final int CHANNEL_TICKS = 100;

    public ReturnScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return CHANNEL_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer server && !SoFEWorld.isJourney(server.server)) {
            player.displayClientMessage(Component.translatable("message.sofe.return_scroll.not_journey").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            WaystoneService.returnToSulthari(player);
            if (!player.getAbilities().instabuild) stack.shrink(1);
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.sofe.return_scroll.desc").withStyle(ChatFormatting.GRAY));
    }
}
