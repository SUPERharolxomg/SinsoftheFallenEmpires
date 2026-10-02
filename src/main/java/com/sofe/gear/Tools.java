package com.sofe.gear;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.event.level.BlockEvent;

import java.util.List;

/** The special mining tools: the mining hammers, which break a 3x3 face, and the Gearwork Drill. */
public final class Tools {
    private static boolean breaking; // the hammer's extra blocks must not trigger the hammer again

    private Tools() {
    }

    /** Breaks the eight blocks round the one mined, across the face the player looks at. Crouch to mine one block. */
    public static class MiningHammer extends PickaxeItem {
        public MiningHammer(Tier tier, int damage, float speed, Properties properties) {
            super(tier, damage, speed, properties);
        }

        @Override
        public float getDestroySpeed(ItemStack stack, BlockState state) {
            return super.getDestroySpeed(stack, state) * 0.6f; // a heavy head swings slower
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.sofe.mining_hammer.tooltip").withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    /** A brass and gear drill: digs stone and earth alike, and fast. */
    public static class Drill extends PickaxeItem {
        public Drill(Tier tier, int damage, float speed, Properties properties) {
            super(tier, damage, speed, properties);
        }

        @Override
        public float getDestroySpeed(ItemStack stack, BlockState state) {
            if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) return getTier().getSpeed() * 1.6f;
            return super.getDestroySpeed(stack, state) * 1.6f;
        }

        @Override
        public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
            return state.is(BlockTags.MINEABLE_WITH_SHOVEL) || super.isCorrectToolForDrops(stack, state);
        }

        @Override
        public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.sofe.drill.tooltip").withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (breaking || !(event.getPlayer() instanceof ServerPlayer player) || player.isCrouching()) return;
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof MiningHammer hammer) || !hammer.isCorrectToolForDrops(held, event.getState())) return;
        HitResult hit = player.pick(player.getBlockReach(), 1, false);
        Direction face = hit instanceof BlockHitResult b && hit.getType() == HitResult.Type.BLOCK ? b.getDirection() : Direction.UP;
        BlockPos center = event.getPos();
        breaking = true;
        try {
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    if (a == 0 && b == 0) continue;
                    BlockPos pos = switch (face.getAxis()) {
                        case X -> center.offset(0, a, b);
                        case Y -> center.offset(a, 0, b);
                        case Z -> center.offset(a, b, 0);
                    };
                    BlockState state = player.level().getBlockState(pos);
                    if (state.isAir() || state.getDestroySpeed(player.level(), pos) < 0 || !hammer.isCorrectToolForDrops(held, state)) continue;
                    if (!TierSortingRegistry.isCorrectTierForDrops(hammer.getTier(), state)) continue;
                    player.gameMode.destroyBlock(pos);
                    if (held.isEmpty()) return;
                }
            }
        } finally {
            breaking = false;
        }
    }
}
