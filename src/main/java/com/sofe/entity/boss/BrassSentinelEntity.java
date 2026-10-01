package com.sofe.entity.boss;

import com.sofe.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The Brass Sentinel, boss of Act I (README, Act I): the clockwork guardian of the Great Observatory,
 * awake and obeying no one. Phase 1 is a brass golem; below half health the Void takes it and it
 * calls Void creatures. On Hard it also sends out a ring of gears.
 */
public class BrassSentinelEntity extends SoFEBossEntity implements com.sofe.entity.SoFEAnimated {

    private final software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache animationCache =
            software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache(this);

    @Override
    public software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public String modelName() {
        return "brass_sentinel";
    }
    public static final String BOSS_ID = "sofe:brass_sentinel";
    private static final int SUMMON_EVERY = 400;
    private static final int SHOCKWAVE_EVERY = 160;

    public BrassSentinelEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level, BossEvent.BossBarColor.YELLOW);
        this.xpReward = 120;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 260.0)
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide() && getHealth() < getMaxHealth() / 2) {
            level().addParticle(ParticleTypes.REVERSE_PORTAL, getRandomX(0.8), getRandomY(), getRandomZ(0.8), 0, 0.05, 0);
        }
    }

    @Override
    protected void onPhase(int newPhase, ServerLevel level, List<ServerPlayer> fighters) {
        level.playSound(null, blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.6f, 1.4f);
        fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.brass_sentinel.void_phase"), true));
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(0.3);
        summonVoid(level);
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        if (phase() < 2) return;
        if (this.tickCount % SUMMON_EVERY == 0) summonVoid(level);
        if (hardMechanics() && this.tickCount % SHOCKWAVE_EVERY == 0) shockwave(level, fighters);
    }

    @Override
    protected void onReset() {
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(0.24);
    }

    private void summonVoid(ServerLevel level) {
        for (int i = 0; i < 3; i++) {
            var wretch = EntityRegistry.VOID_WRETCH.get().create(level);
            if (wretch == null) continue;
            double angle = random.nextDouble() * Math.PI * 2;
            wretch.moveTo(getX() + Math.cos(angle) * 4, getY(), getZ() + Math.sin(angle) * 4, random.nextFloat() * 360, 0);
            wretch.finalizeSpawn(level, level.getCurrentDifficultyAt(wretch.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
            wretch.setTarget(getTarget());
            level.addFreshEntity(wretch);
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 1, getZ(), 60, 2, 1, 2, 0.1);
    }

    /** Hard only: a ring of gears that knocks everyone back. */
    private void shockwave(ServerLevel level, List<ServerPlayer> players) {
        level.playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1f, 0.5f);
        level.sendParticles(ParticleTypes.CRIT, getX(), getY() + 0.5, getZ(), 80, 4, 0.2, 4, 0.2);
        for (ServerPlayer p : players) {
            if (p.distanceToSqr(this) > 36) continue;
            Vec3 push = p.position().subtract(position()).normalize().scale(1.4);
            p.push(push.x, 0.5, push.z);
            p.hurtMarked = true;
            p.hurt(damageSources().mobAttack(this), 4f);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) target.setDeltaMovement(target.getDeltaMovement().add(0, 0.4, 0));
        return hit;
    }
}
