package com.sofe.entity;

import com.sofe.death.CorpseRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;

import java.util.Optional;
import java.util.UUID;

/**
 * The Bearer's body (docs/Jugabilidad.md, G1, Diablo II style): lies where the player fell with
 * everything they carried. Only its owner can take it back, into the same slots. Nothing destroys
 * it and it never despawns.
 */
public class BearerCorpseEntity extends Entity {
    /** 36 inventory slots, 4 armor slots and the offhand, in {@link Inventory} order. */
    public static final int SLOTS = 41;
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(BearerCorpseEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> OWNER_NAME = SynchedEntityData.defineId(BearerCorpseEntity.class, EntityDataSerializers.STRING);

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

    public BearerCorpseEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER, Optional.empty());
        this.entityData.define(OWNER_NAME, "");
    }

    public Optional<UUID> owner() {
        return this.entityData.get(OWNER);
    }

    public String ownerName() {
        return this.entityData.get(OWNER_NAME);
    }

    /** Moves everything the player carries onto this body. */
    public void takeFrom(Player player) {
        this.entityData.set(OWNER, Optional.of(player.getUUID()));
        this.entityData.set(OWNER_NAME, player.getGameProfile().getName());
        setCustomName(Component.translatable("entity.sofe.bearer_corpse.of", player.getGameProfile().getName()));
        Inventory inventory = player.getInventory();
        for (int i = 0; i < SLOTS && i < inventory.getContainerSize(); i++) {
            items.set(i, inventory.getItem(i));
            inventory.setItem(i, ItemStack.EMPTY);
        }
    }

    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    public int itemCount() {
        return (int) items.stream().filter(s -> !s.isEmpty()).count();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!(player instanceof ServerPlayer server)) return InteractionResult.SUCCESS;
        if (!owner().map(player.getUUID()::equals).orElse(false)) {
            player.displayClientMessage(Component.translatable("message.sofe.corpse.not_yours", ownerName()).withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.CONSUME;
        }
        giveBack(server);
        return InteractionResult.CONSUME;
    }

    /** Same slot when it is free, otherwise anywhere in the inventory; what does not fit stays here. */
    public void giveBack(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;
            if (i < inventory.getContainerSize() && inventory.getItem(i).isEmpty()) {
                inventory.setItem(i, stack);
                items.set(i, ItemStack.EMPTY);
            } else if (inventory.add(stack)) {
                items.set(i, ItemStack.EMPTY);
            } else {
                items.set(i, stack); // add() shrank what fit
            }
        }
        level().playSound(null, blockPosition(), SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 1f, 0.9f);
        if (isEmpty()) {
            CorpseRegistry.get(player.server).remove(player.getUUID(), getUUID());
            discard();
            player.displayClientMessage(Component.translatable("message.sofe.corpse.recovered").withStyle(ChatFormatting.GOLD), true);
        } else {
            player.displayClientMessage(Component.translatable("message.sofe.corpse.partly", itemCount()).withStyle(ChatFormatting.YELLOW), true);
        }
        com.sofe.quest.QuestEngine.sync(player); // the compass stops pointing at a recovered body
    }

    @Override
    public void tick() {
        super.tick();
        if (!isNoGravity()) {
            setDeltaMovement(getDeltaMovement().add(0, -0.04, 0));
        }
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().multiply(0.5, 0.98, 0.5));
        if (onGround()) setDeltaMovement(getDeltaMovement().multiply(1, 0, 1));
        if (getY() < level().getMinBuildHeight() + 1) { // never lost in the void
            setPos(getX(), level().getMinBuildHeight() + 1, getZ());
            setNoGravity(true);
            setDeltaMovement(0, 0, 0);
        }
    }

    // Nothing destroys it: not lava, explosions, mobs or pistons.
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean shouldBeSaved() {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        owner().ifPresent(o -> tag.putUUID("owner", o));
        tag.putString("owner_name", ownerName());
        ListTag list = new ListTag();
        for (int i = 0; i < SLOTS; i++) {
            if (items.get(i).isEmpty()) continue;
            CompoundTag item = items.get(i).save(new CompoundTag());
            item.putByte("slot", (byte) i);
            list.add(item);
        }
        tag.put("items", list);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("owner")) this.entityData.set(OWNER, Optional.of(tag.getUUID("owner")));
        this.entityData.set(OWNER_NAME, tag.getString("owner_name"));
        ListTag list = tag.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag item = list.getCompound(i);
            int slot = item.getByte("slot") & 0xFF;
            if (slot < SLOTS) items.set(slot, ItemStack.of(item));
        }
    }

    /** For GameTests: a corpse and its owner in one call. */
    public static BearerCorpseEntity spawnFor(ServerPlayer player, ServerLevel level, double x, double y, double z) {
        BearerCorpseEntity corpse = new BearerCorpseEntity(com.sofe.registry.EntityRegistry.BEARER_CORPSE.get(), level);
        corpse.setPos(x, y, z);
        corpse.takeFrom(player);
        level.addFreshEntity(corpse);
        CorpseRegistry.get(level.getServer()).add(player.getUUID(), corpse.getUUID(), level.dimension(), corpse.blockPosition());
        return corpse;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        return true;
    }
}
