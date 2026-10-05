package com.sofe.entity.boss;

import com.sofe.registry.SoFEBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Reward Coffers (docs/Anexos.md, A6): when a boss falls, every Bearer who fought it gets a coffer of their own,
 * set in a ring round the middle of the arena, with the loot rolled for them alone. Only its owner can open it; once
 * opened it is gone, and a coffer nobody opens fades away after thirty minutes. Bearers who were online but not in
 * the fight get nothing.
 */
public final class RewardCoffer {
    /** How long an unopened coffer waits: thirty minutes. */
    public static final long LIFETIME_TICKS = 30L * 60 * 20;
    /** How far from the middle of the arena the ring of coffers stands. */
    public static final int RING = 3;

    private RewardCoffer() {
    }

    /**
     * Places one participant's coffer: the n-th of the ring round the middle, standing on the floor there.
     * Returns where it stands.
     */
    public static BlockPos place(ServerLevel level, BlockPos middle, int index, int count, ServerPlayer owner, List<ItemStack> loot) {
        double angle = 2 * Math.PI * index / Math.max(1, count);
        int r = count <= 1 ? 0 : RING;
        BlockPos at = middle.offset((int) Math.round(Math.cos(angle) * r), 0, (int) Math.round(Math.sin(angle) * r));
        at = standingSpot(level, at);
        level.setBlock(at, SoFEBlocks.REWARD_COFFER.get().defaultBlockState(), 3);
        if (level.getBlockEntity(at) instanceof Entity coffer) coffer.fill(owner.getUUID(), owner.getGameProfile().getName(), loot, level.getGameTime());
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, at.getX() + 0.5, at.getY() + 1, at.getZ() + 0.5, 20, 0.3, 0.5, 0.3, 0.2);
        return at;
    }

    /** The first free block at or above the spot (or a little below, when it floats). */
    private static BlockPos standingSpot(ServerLevel level, BlockPos at) {
        BlockPos pos = at;
        for (int i = 0; i < 6 && !level.getBlockState(pos.below()).isSolid() && level.getBlockState(pos.below()).isAir(); i++) pos = pos.below();
        while (!level.getBlockState(pos).isAir() && pos.getY() < level.getMaxBuildHeight() - 1) pos = pos.above();
        return pos;
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
            if (player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof Entity coffer) open(server, (ServerLevel) level, pos, coffer);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        @Override
        public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
            return level.isClientSide() ? null : createTickerHelper(type, SoFEBlocks.REWARD_COFFER_ENTITY.get(), (lvl, pos, st, coffer) -> coffer.tick(lvl, pos));
        }
    }

    /** The owner takes everything and the coffer is gone; anyone else is told whose it is. */
    public static boolean open(ServerPlayer player, ServerLevel level, BlockPos pos, Entity coffer) {
        if (!coffer.isOwner(player.getUUID())) {
            player.displayClientMessage(Component.translatable("message.sofe.coffer.not_yours", coffer.ownerName()).withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        for (ItemStack stack : coffer.takeAll()) if (!player.getInventory().add(stack)) player.drop(stack, false);
        player.displayClientMessage(Component.translatable("message.sofe.coffer.opened").withStyle(ChatFormatting.GOLD), true);
        level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1f, 0.8f);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        return true;
    }

    public static class Entity extends BlockEntity {
        private UUID owner;
        private String ownerName = "";
        private final List<ItemStack> loot = new ArrayList<>();
        private long placedAt;

        public Entity(BlockPos pos, BlockState state) {
            super(SoFEBlocks.REWARD_COFFER_ENTITY.get(), pos, state);
        }

        public void fill(UUID owner, String name, List<ItemStack> items, long gameTime) {
            this.owner = owner;
            this.ownerName = name;
            this.loot.clear();
            this.loot.addAll(items);
            this.placedAt = gameTime;
            setChanged();
        }

        public boolean isOwner(UUID player) {
            return owner != null && owner.equals(player);
        }

        public String ownerName() {
            return ownerName;
        }

        public List<ItemStack> takeAll() {
            List<ItemStack> out = List.copyOf(loot);
            loot.clear();
            setChanged();
            return out;
        }

        public boolean expired(long gameTime) {
            return gameTime - placedAt >= LIFETIME_TICKS;
        }

        /** An unopened coffer fades after its time (checked once a second while its land is loaded). */
        void tick(Level level, BlockPos pos) {
            if (level.getGameTime() % 20 != 0 || !expired(level.getGameTime())) return;
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.02);
                ServerPlayer p = owner == null ? null : server.getServer().getPlayerList().getPlayer(owner);
                if (p != null) {
                    p.displayClientMessage(Component.translatable("message.sofe.coffer.faded").withStyle(ChatFormatting.GRAY), false);
                }
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }

        @Override
        protected void saveAdditional(CompoundTag tag) {
            super.saveAdditional(tag);
            if (owner != null) tag.putUUID("owner", owner);
            tag.putString("owner_name", ownerName);
            tag.putLong("placed_at", placedAt);
            ListTag stacks = new ListTag();
            loot.forEach(s -> stacks.add(s.save(new CompoundTag())));
            tag.put("items", stacks);
        }

        @Override
        public void load(CompoundTag tag) {
            super.load(tag);
            owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
            ownerName = tag.getString("owner_name");
            placedAt = tag.getLong("placed_at");
            loot.clear();
            ListTag stacks = tag.getList("items", Tag.TAG_COMPOUND);
            for (int j = 0; j < stacks.size(); j++) loot.add(ItemStack.of(stacks.getCompound(j)));
        }
    }

    /** For registration: the block entity type of the coffer. */
    public static BlockEntityType<Entity> type() {
        return BlockEntityType.Builder.of(Entity::new, SoFEBlocks.REWARD_COFFER.get()).build(null);
    }
}
