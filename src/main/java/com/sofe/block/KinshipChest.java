package com.sofe.block;

import com.sofe.economy.EconomyHandler;
import com.sofe.gear.SinGem;
import com.sofe.registry.SoFEBlocks;
import com.sofe.story.StoryAct;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A Kinship Chest (docs/Anexos.md, A5): a chest at a dungeon's door that opens only for two or more Bearers standing
 * by it together. Each of them takes their own share once: Dinars by the act and, often, a rough sin gem. Never gear
 * the story needs, so playing alone misses nothing that matters.
 */
public class KinshipChest extends net.minecraft.world.level.block.Block implements EntityBlock {
    public static final int NEEDED = 2;
    public static final double REACH = 6;

    public KinshipChest(Properties properties) {
        super(properties);
    }

    /** The Bearers standing by the chest (alive, not spectating). */
    public static List<ServerPlayer> around(Level level, BlockPos pos) {
        return level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(REACH), p -> p.isAlive() && !p.isSpectator());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(player instanceof ServerPlayer server) || hand != InteractionHand.MAIN_HAND) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof Entity chest)) return InteractionResult.PASS;
        List<ServerPlayer> kin = around(level, pos);
        if (kin.size() < NEEDED) {
            server.displayClientMessage(Component.translatable("message.sofe.kinship.needs", NEEDED).withStyle(ChatFormatting.AQUA), true);
            return InteractionResult.CONSUME;
        }
        int given = 0;
        for (ServerPlayer p : kin) {
            if (!chest.opened.add(p.getUUID())) continue;
            share(p);
            given++;
        }
        chest.setChanged();
        if (given == 0) {
            server.displayClientMessage(Component.translatable("message.sofe.kinship.taken").withStyle(ChatFormatting.GRAY), true);
        } else {
            level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.8f, 1.1f);
        }
        return InteractionResult.CONSUME;
    }

    /** One Bearer's share: Dinars by the act, and two times in three a rough sin gem. */
    static void share(ServerPlayer player) {
        int act = StoryAct.of(player);
        long dinars = 60L * act + player.getRandom().nextInt(40 * act + 1);
        EconomyHandler.addDinars(player, dinars);
        EconomyHandler.sync(player);
        if (player.getRandom().nextInt(3) > 0) {
            SinGem gem = SinGem.values()[player.getRandom().nextInt(SinGem.values().length)];
            var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("sofe", gem.id(SinGem.Form.ROUGH)));
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                if (!player.getInventory().add(stack)) player.drop(stack, false);
            }
        }
        player.displayClientMessage(Component.translatable("message.sofe.kinship.share", dinars).withStyle(ChatFormatting.AQUA), false);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Entity(pos, state);
    }

    public static BlockEntityType<Entity> type() {
        return BlockEntityType.Builder.of(Entity::new, SoFEBlocks.KINSHIP_CHEST.get()).build(null);
    }

    public static class Entity extends BlockEntity {
        final Set<UUID> opened = new HashSet<>();

        public Entity(BlockPos pos, BlockState state) {
            super(SoFEBlocks.KINSHIP_CHEST_ENTITY.get(), pos, state);
        }

        public boolean openedBy(UUID player) {
            return opened.contains(player);
        }

        @Override
        protected void saveAdditional(CompoundTag tag) {
            super.saveAdditional(tag);
            ListTag list = new ListTag();
            opened.forEach(id -> list.add(NbtUtils.createUUID(id)));
            tag.put("opened", list);
        }

        @Override
        public void load(CompoundTag tag) {
            super.load(tag);
            opened.clear();
            for (Tag t : tag.getList("opened", Tag.TAG_INT_ARRAY)) opened.add(NbtUtils.loadUUID(t));
        }
    }
}
