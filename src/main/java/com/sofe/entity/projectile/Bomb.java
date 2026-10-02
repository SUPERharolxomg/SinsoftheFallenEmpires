package com.sofe.entity.projectile;

import com.sofe.registry.EntityRegistry;
import com.sofe.registry.ItemRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import java.util.Locale;

/**
 * A thrown gadget: the clockwork bomb bursts (hurting creatures, never breaking blocks), the smoke bomb
 * blinds and slows everything around it, the fire bomb sets everything around it ablaze.
 */
public class Bomb extends ThrowableItemProjectile {
    public enum Kind { CLOCKWORK, SMOKE, FIRE }

    private Kind kind = Kind.CLOCKWORK;

    public Bomb(EntityType<? extends Bomb> type, Level level) {
        super(type, level);
    }

    public Bomb(Level level, LivingEntity owner, Kind kind, ItemStack shown) {
        super(EntityRegistry.BOMB.get(), owner, level);
        this.kind = kind;
        setItem(shown.copyWithCount(1));
    }

    @Override
    protected Item getDefaultItem() {
        return ItemRegistry.CLOCKWORK_BOMB.get();
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(level() instanceof ServerLevel level)) return;
        LivingEntity owner = getOwner() instanceof LivingEntity l ? l : null;
        switch (kind) {
            case CLOCKWORK -> level.explode(this, getX(), getY(), getZ(), 2.5f, Level.ExplosionInteraction.NONE);
            case SMOKE -> {
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 0.5, getZ(), 60, 2, 1, 2, 0.02);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4), e -> e != owner)) {
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0), owner);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1), owner);
                }
                level.playSound(null, blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1, 0.6f);
            }
            case FIRE -> {
                level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.3, getZ(), 70, 1.8, 0.4, 1.8, 0.05);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.5), e -> e != owner)) {
                    e.setSecondsOnFire(6);
                    e.hurt(damageSources().onFire(), 4);
                }
                level.playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1, 0.8f);
            }
        }
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Kind", kind.name());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        try {
            kind = Kind.valueOf(tag.getString("Kind").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            kind = Kind.CLOCKWORK;
        }
    }
}
