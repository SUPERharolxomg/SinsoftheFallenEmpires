package com.sofe.entity.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import org.joml.Vector3f;

import java.util.List;

/**
 * Serath, the Blood Maiden, Broken Oath of Law II ("You shall honor the blood of your fathers"), in
 * the Nordrath Arena. She drinks the blood she spills: every hit heals her, and the pools of blood
 * she leaves hurt the Bearers and heal her while she stands in them.
 */
public class SerathEntity extends BrokenOathEntity {
    public static final String BOSS_ID = "sofe:serath";
    private static final DustParticleOptions BLOOD = new DustParticleOptions(new Vector3f(0.6f, 0.02f, 0.05f), 1.4f);
    private static final float POOL_RADIUS = 2.5f;

    public SerathEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public String modelName() {
        return "serath";
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.MOVEMENT_SPEED, 0.31)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public int law() {
        return 2;
    }

    @Override
    public String archsin() {
        return VorathEntity.BOSS_ID;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, true));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    /** Life steal: half of the damage dealt, all of it in phase 2. */
    public float lifeSteal() {
        return phase() >= 2 ? 1.0f : 0.5f;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float before = target instanceof LivingEntity living ? living.getHealth() : 0;
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            float dealt = Math.max(0, before - living.getHealth());
            heal(dealt * lifeSteal());
            if (level() instanceof ServerLevel level) level.sendParticles(BLOOD, getX(), getY() + 1.2, getZ(), 12, 0.3, 0.4, 0.3, 0);
        }
        return hit;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        int every = phase() >= 2 ? 70 : 120;
        if (this.tickCount % every == 0) {
            for (int i = 0; i < Math.min(3, fighters.size()); i++) bloodPool(level, fighters.get(i));
            level.playSound(null, blockPosition(), SoundEvents.HONEY_BLOCK_BREAK, SoundSource.HOSTILE, 1.2f, 0.5f);
        }
        // standing in her own blood heals her
        if (this.tickCount % 20 == 0 && !level.getEntitiesOfClass(AreaEffectCloud.class, getBoundingBox().inflate(POOL_RADIUS),
                c -> c.getOwner() == this).isEmpty()) {
            heal(phase() >= 2 ? 3f : 2f);
        }
    }

    private void bloodPool(ServerLevel level, ServerPlayer target) {
        AreaEffectCloud pool = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
        pool.setOwner(this);
        pool.setParticle(BLOOD);
        pool.setRadius(POOL_RADIUS);
        pool.setDuration(160);
        pool.setWaitTime(15);
        pool.setRadiusPerTick(0);
        pool.addEffect(new MobEffectInstance(MobEffects.WITHER, 50, 0));
        pool.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
        level.addFreshEntity(pool);
    }

    @Override
    protected void onPhase(int newPhase, ServerLevel level, List<ServerPlayer> fighters) {
        level.playSound(null, blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1f, 1.4f);
        fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.serath.phase2").withStyle(ChatFormatting.DARK_RED), true));
    }

    // --- signature attack (SoFEBossEntity.Signature): her two bone blades: she leaps on her prey and cuts it three times, drinking what she spills

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("serath_flurry", 20, 200, 12);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (target != null && tick % 3 == 0) level.sendParticles(new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.8f, 0.05f, 0.1f), 1.6f), target.getX(), target.getY() + 0.1, target.getZ(), 12, 0.6, 0, 0.6, 0);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (target == null || !target.isAlive()) return;
        var d = Signatures.toward(this, target);
        moveTo(target.getX() - d.x * 1.4, target.getY(), target.getZ() - d.z * 1.4, getYRot(), getXRot());
        float dealt = signatureDamage(2.4f);
        if (target instanceof net.minecraft.server.level.ServerPlayer p) Signatures.strike(this, p, dealt, position(), 0.4, 0.1);
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 80, 1), this);
        heal(dealt * 0.5f);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1, target.getZ(), 3, 0.4, 0.4, 0.4, 0);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, net.minecraft.sounds.SoundSource.HOSTILE, 1.4f, 0.8f);
    }
}
