package com.sofe.entity.npc;

import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.player.PlayerClassData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * One of the five Bearers as an NPC in Sulthari. The player who is that Bearer does not see it or
 * talk to it (they are that hero); every other player sees a fellow Bearer (docs/Anexos.md, A6).
 */
public class BearerNpcEntity extends StoryNpcEntity {
    private static final EntityDataAccessor<String> BEARER = SynchedEntityData.defineId(BearerNpcEntity.class, EntityDataSerializers.STRING);

    public BearerNpcEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BEARER, "");
    }

    public Optional<PlayerClass> bearer() {
        return PlayerClass.byId(this.entityData.get(BEARER));
    }

    public void setBearer(PlayerClass bearer) {
        this.entityData.set(BEARER, bearer.id());
        setNpcId(bearer.npcId());
    }

    @Override
    protected String layoutId() {
        return this.entityData.get(BEARER);
    }

    /** Whether this NPC is the same hero as the player (then it is hidden for them). */
    public boolean isSameHeroAs(Player player) {
        Optional<PlayerClass> theirs = PlayerClassCapability.get(player).flatMap(PlayerClassData::get);
        return theirs.isPresent() && theirs.equals(bearer());
    }

    @Override
    protected boolean canTalkTo(Player player) {
        return !isSameHeroAs(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("bearer", this.entityData.get(BEARER));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("bearer")) this.entityData.set(BEARER, tag.getString("bearer"));
    }
}
