package com.sofe.entity.npc;

import com.sofe.quest.DialogueService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A person of the story (Grand Vizier Ozhan, the Council elder...). Stands at their place, turns to
 * the player and talks through {@link DialogueService}. Cannot be hurt or pushed; never despawns.
 */
public class StoryNpcEntity extends PathfinderMob {
    private static final EntityDataAccessor<String> NPC = SynchedEntityData.defineId(StoryNpcEntity.class, EntityDataSerializers.STRING);

    public StoryNpcEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(NPC, "");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f, 1.0f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    /** The NPC id used by dialogue files, portraits and names (npc.sofe.&lt;id&gt;). */
    public String npcId() {
        return this.entityData.get(NPC);
    }

    public void setNpcId(String id) {
        this.entityData.set(NPC, id);
        setCustomName(Component.translatable("npc.sofe." + id));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || npcId().isEmpty()) return InteractionResult.PASS;
        if (!canTalkTo(player)) return InteractionResult.PASS;
        if (player instanceof ServerPlayer server) {
            getLookControl().setLookAt(player);
            swing(InteractionHand.MAIN_HAND); // the one talking gesture for now
            DialogueService.talkTo(server, npcId(), this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    /** Subclasses can refuse a player (a Bearer NPC does not talk to the player who is that Bearer). */
    protected boolean canTalkTo(Player player) {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(source, amount); // /kill still works
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("npc", npcId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("npc")) this.entityData.set(NPC, tag.getString("npc"));
    }
}
