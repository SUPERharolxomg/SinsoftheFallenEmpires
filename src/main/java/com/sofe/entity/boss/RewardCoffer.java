package com.sofe.entity.boss;

import com.sofe.registry.SoFEBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Reward Coffer (docs/Anexos.md, A6): when an Archsin or a Broken Oath falls, a coffer appears in
 * the arena. Each participant opens it and gets only their own loot; nothing falls on the floor, and a
 * player who was disconnected finds their share when they come back.
 */
public final class RewardCoffer {

    private RewardCoffer() {
    }

    /** Puts a player's share in the coffer at this spot, placing the coffer if there is none. */
    public static void store(ServerLevel level, BlockPos pos, UUID player, List<ItemStack> loot) {
        if (!level.getBlockState(pos).is(SoFEBlocks.REWARD_COFFER.get())) {
            level.setBlock(pos, SoFEBlocks.REWARD_COFFER.get().defaultBlockState(), 3);
        }
        if (level.getBlockEntity(pos) instanceof Entity coffer) coffer.add(player, loot);
    }

    public static class Block extends BaseEntityBlock {
        public Block(Properties properties) {
            super(properties);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new Entity(pos, state);
        }

        @Override
        public RenderShape getRenderShape(BlockState state) {
            return RenderShape.MODEL;
        }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof Entity coffer) {
                List<ItemStack> share = coffer.take(server.getUUID());
                if (share.isEmpty()) {
                    server.displayClientMessage(Component.translatable("message.sofe.coffer.empty").withStyle(ChatFormatting.GRAY), true);
                } else {
                    for (ItemStack stack : share) if (!server.getInventory().add(stack)) server.drop(stack, false);
                    server.displayClientMessage(Component.translatable("message.sofe.coffer.opened").withStyle(ChatFormatting.GOLD), true);
                    level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1f, 0.8f);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
    }

    public static class Entity extends BlockEntity {
        private final Map<UUID, List<ItemStack>> shares = new HashMap<>();

        public Entity(BlockPos pos, BlockState state) {
            super(SoFEBlocks.REWARD_COFFER_ENTITY.get(), pos, state);
        }

        public void add(UUID player, List<ItemStack> loot) {
            shares.computeIfAbsent(player, p -> new ArrayList<>()).addAll(loot);
            setChanged();
        }

        public List<ItemStack> take(UUID player) {
            List<ItemStack> share = shares.remove(player);
            setChanged();
            return share == null ? List.of() : share;
        }

        public boolean hasShare(UUID player) {
            return shares.containsKey(player);
        }

        @Override
        protected void saveAdditional(CompoundTag tag) {
            super.saveAdditional(tag);
            ListTag list = new ListTag();
            shares.forEach((player, items) -> {
                CompoundTag t = new CompoundTag();
                t.putUUID("player", player);
                ListTag stacks = new ListTag();
                items.forEach(s -> stacks.add(s.save(new CompoundTag())));
                t.put("items", stacks);
                list.add(t);
            });
            tag.put("shares", list);
        }

        @Override
        public void load(CompoundTag tag) {
            super.load(tag);
            shares.clear();
            ListTag list = tag.getList("shares", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag t = list.getCompound(i);
                List<ItemStack> items = new ArrayList<>();
                ListTag stacks = t.getList("items", Tag.TAG_COMPOUND);
                for (int j = 0; j < stacks.size(); j++) items.add(ItemStack.of(stacks.getCompound(j)));
                shares.put(t.getUUID("player"), items);
            }
        }
    }

    /** For registration: the block entity type of the coffer. */
    public static BlockEntityType<Entity> type() {
        return BlockEntityType.Builder.of(Entity::new, SoFEBlocks.REWARD_COFFER.get()).build(null);
    }
}
