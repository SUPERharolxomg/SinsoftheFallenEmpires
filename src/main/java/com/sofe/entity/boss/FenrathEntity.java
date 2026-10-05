package com.sofe.entity.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Fenrath, the Devourer, Broken Oath of Law VIII ("You shall share the winter bread"), in the Nordrath Caverns.
 * He shares nothing: he swallows a Bearer whole for a few seconds (dark, burning acid, held inside him; hitting him
 * hard makes him spit them out sooner) and spits pools of acid. In his second phase he swallows sooner and spits more.
 */
public class FenrathEntity extends BrokenOathEntity {
    public static final String BOSS_ID = "sofe:fenrath";
    public static final int SWALLOW_TICKS = 60;
    private ServerPlayer swallowed;
    private int inside;
    private float takenWhileFull;

    public FenrathEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26).add(Attributes.ARMOR, 10.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public String modelName() {
        return "fenrath";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public int law() {
        return 8;
    }

    @Override
    public String archsin() {
        return GularthEntity.BOSS_ID;
    }

    @Override
    protected double arenaRadius() {
        return 20;
    }

    public ServerPlayer swallowed() {
        return swallowed;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        if (swallowed != null) {
            hold(level);
            return;
        }
        int gulp = phase() >= 2 ? 220 : 340;
        if (this.tickCount % gulp == 0) {
            ServerPlayer near = fighters.stream().filter(p -> p.distanceToSqr(this) < 6 * 6).findFirst().orElse(null);
            if (near != null) swallow(level, near);
        }
        if (this.tickCount % (phase() >= 2 ? 90 : 140) == 45) {
            for (int i = 0; i < Math.min(fighters.size(), phase() >= 2 ? 3 : 2); i++) acid(level, fighters.get(i));
        }
    }

    /** Into the maw. */
    public void swallow(ServerLevel level, ServerPlayer player) {
        swallowed = player;
        inside = 0;
        takenWhileFull = 0;
        level.playSound(null, blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 2f, 0.4f);
        player.displayClientMessage(Component.translatable("message.sofe.fenrath.swallowed").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
    }

    private void hold(ServerLevel level) {
        if (!swallowed.isAlive() || swallowed.isSpectator()) {
            swallowed = null;
            return;
        }
        inside++;
        swallowed.teleportTo(getX(), getY() + 0.5, getZ());
        swallowed.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false));
        if (inside % 20 == 0) swallowed.hurt(damageSources().mobAttack(this), phase() >= 2 ? 4 : 3);
        if (inside >= SWALLOW_TICKS || takenWhileFull >= 20) spit(level);
    }

    /** Out again, thrown away, dripping. */
    private void spit(ServerLevel level) {
        Vec3 away = Vec3.directionFromRotation(0, getYRot()).scale(1.6);
        swallowed.teleportTo(getX() + away.x * 2, getY() + 1, getZ() + away.z * 2);
        swallowed.setDeltaMovement(away.x, 0.6, away.z);
        swallowed.hurtMarked = true;
        swallowed.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), this);
        level.sendParticles(ParticleTypes.ITEM_SLIME, getX(), getY() + 1.5, getZ(), 30, 0.5, 0.5, 0.5, 0.2);
        level.playSound(null, blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.HOSTILE, 2f, 0.4f);
        swallowed = null;
    }

    /** A pool of acid where a Bearer stands. */
    private void acid(ServerLevel level, ServerPlayer target) {
        AreaEffectCloud pool = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
        pool.setOwner(this);
        pool.setParticle(ParticleTypes.ITEM_SLIME);
        pool.setRadius(phase() >= 2 ? 3f : 2.2f);
        pool.setDuration(120);
        pool.setWaitTime(15);
        pool.setRadiusPerTick(0);
        pool.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1));
        level.addFreshEntity(pool);
        level.playSound(null, target.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.5f, 0.5f);
    }

    /** Blows taken while one is inside count toward spitting them out. */
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (swallowed != null && source.getEntity() == swallowed) return false; // the one inside cannot reach him
        boolean hurt = super.hurt(source, amount);
        if (hurt && swallowed != null) takenWhileFull += amount;
        return hurt;
    }

    @Override
    protected void onFightOver(ServerLevel level, boolean defeated) {
        if (swallowed != null) spit(level);
    }

    // --- signature attack (SoFEBossEntity.Signature): he crouches and pounces from afar onto his prey, all claws and teeth

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("fenrath_pounce", 18, 200, 16);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (target != null && tick % 3 == 0) Signatures.drawRing(level, target.position(), 2.5, net.minecraft.core.particles.ParticleTypes.ITEM_SLIME);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (target == null || !target.isAlive()) return;
        var d = Signatures.toward(this, target);
        moveTo(target.getX() - d.x * 1.6, target.getY(), target.getZ() - d.z * 1.6, getYRot(), getXRot());
        for (var p : Signatures.ring(target.position(), 3, fighters)) Signatures.strike(this, p, signatureDamage(2.0), position(), 1.0, 0.3);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, getX(), getY() + 0.2, getZ(), 20, 1.5, 0.1, 1.5, 0.05);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.WOLF_GROWL, net.minecraft.sounds.SoundSource.HOSTILE, 2.0f, 0.5f);
    }
}
