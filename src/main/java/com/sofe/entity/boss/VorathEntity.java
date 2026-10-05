package com.sofe.entity.boss;

import com.sofe.story.Sin;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

/**
 * Vorath, Archsin of Wrath, at the Burning Citadel (README, Act II). Every wound feeds his rage: the
 * angrier he is, the harder and faster he hits. At half health the arena shrinks: a ring of fire
 * closes in and burns whoever stays outside it. Placeholder GeckoLib model until the Blockbench one.
 */
public class VorathEntity extends ArchsinEntity implements GeoEntity {
    public static final String BOSS_ID = "sofe:vorath";
    public static final int MAX_RAGE_STACKS = 5;
    private static final float RAGE_PER_STACK = 25f;
    private static final UUID RAGE_ID = UUID.fromString("9a3e6c1b-2d4f-4b8a-8f1e-3c5d7e9f1a21");
    private static final double MIN_RING = 8;
    private static final EntityDataAccessor<Integer> RAGE = SynchedEntityData.defineId(VorathEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.vorath.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.vorath.walk");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.vorath.attack");
    private static final RawAnimation SIGNATURE = RawAnimation.begin().thenPlay("animation.vorath.signature");

    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private float rage;
    private double ring = -1;

    public VorathEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 400;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 420.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(RAGE, 0);
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public Sin sin() {
        return Sin.WRATH;
    }

    @Override
    protected String introDialogue() {
        return "sofe:act2/vorath_temptation";
    }

    @Override
    protected double arenaRadius() {
        return 26;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 20.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    /** Rage stacks, 0 to 5: each adds 10% damage and 5% speed. */
    public int rageStacks() {
        return this.entityData.get(RAGE);
    }

    /** The current radius of the ring of fire, or -1 before phase 2. */
    public double ringRadius() {
        return ring;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide()) {
            rage += amount;
            updateRage();
        }
        return hurt;
    }

    private void updateRage() {
        int stacks = Math.min(MAX_RAGE_STACKS, (int) (rage / RAGE_PER_STACK));
        if (stacks == rageStacks()) return;
        this.entityData.set(RAGE, stacks);
        var damage = getAttribute(Attributes.ATTACK_DAMAGE);
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (damage != null) {
            damage.removeModifier(RAGE_ID);
            if (stacks > 0) damage.addTransientModifier(new AttributeModifier(RAGE_ID, "Vorath rage", 0.10 * stacks, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        if (speed != null) {
            speed.removeModifier(RAGE_ID);
            if (stacks > 0) speed.addTransientModifier(new AttributeModifier(RAGE_ID, "Vorath rage", 0.05 * stacks, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) target.setSecondsOnFire(5);
        return hit;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        if (this.tickCount % 20 == 0 && rage > 0) { // rage cools down slowly
            rage = Math.max(0, rage - 2f);
            updateRage();
        }
        if (this.tickCount % 100 == 0) fireSlam(level, fighters);
        if (phase() >= 2) shrinkArena(level, fighters);
    }

    /** A blow to the ground: a burst of fire around him. */
    private void fireSlam(ServerLevel level, List<ServerPlayer> fighters) {
        level.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.8f, 0.7f);
        level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.3, getZ(), 120, 4, 0.2, 4, 0.08);
        for (ServerPlayer p : fighters) {
            if (p.distanceToSqr(this) > 25) continue;
            p.hurt(damageSources().mobAttack(this), 5f + rageStacks());
            p.setSecondsOnFire(4);
        }
    }

    /** The ring of fire closes in by one block every two seconds, down to 8 blocks. */
    private void shrinkArena(ServerLevel level, List<ServerPlayer> fighters) {
        Vec3 center = Vec3.atBottomCenterOf(arenaCenterPos());
        if (ring < 0) ring = arenaRadius();
        if (this.tickCount % 40 == 0 && ring > MIN_RING) ring -= 1;
        if (this.tickCount % 4 == 0) {
            for (int i = 0; i < 48; i++) {
                double a = i * Math.PI * 2 / 48;
                level.sendParticles(ParticleTypes.FLAME, center.x + Math.cos(a) * ring, center.y + 0.3, center.z + Math.sin(a) * ring, 1, 0, 0.3, 0, 0.01);
            }
        }
        if (this.tickCount % 20 == 0) {
            for (ServerPlayer p : fighters) {
                double dx = p.getX() - center.x, dz = p.getZ() - center.z;
                if (dx * dx + dz * dz > ring * ring) {
                    p.hurt(damageSources().inFire(), 3f);
                    p.setSecondsOnFire(3);
                }
            }
        }
    }

    @Override
    protected void onPhase(int newPhase, ServerLevel level, List<ServerPlayer> fighters) {
        ring = arenaRadius();
        level.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1f, 0.6f);
        fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.vorath.phase2").withStyle(ChatFormatting.RED), true));
    }

    @Override
    protected void onReset() {
        rage = 0;
        ring = -1;
        updateRage();
    }

    // --- GeckoLib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            if (signing()) return state.setAndContinue(SIGNATURE);
            if (this.swinging) return state.setAndContinue(ATTACK);
            return state.setAndContinue(state.isMoving() ? WALK : IDLE);
        }));
    }

    @Override
    public String modelName() {
        return "vorath";
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide() && rageStacks() > 0 && random.nextInt(6 - Math.min(5, rageStacks())) == 0) {
            level().addParticle(ParticleTypes.LAVA, getRandomX(0.6), getRandomY(), getRandomZ(0.6), 0, 0, 0);
        }
    }

    /** Unused but required by the GeckoLib contract on some versions. */
    @SuppressWarnings("unused")
    private PlayState idle() {
        return PlayState.CONTINUE;
    }

    // --- signature attack (SoFEBossEntity.Signature): his great axe splits the ground: a line of lava erupts from the wound

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("vorath_splitter", 30, 220, 12);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick % 3 == 0) Signatures.drawLine(level, position(), signatureAim, 12, net.minecraft.core.particles.ParticleTypes.FLAME);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        for (var p : Signatures.line(position(), signatureAim, 12, 3, fighters)) {
            Signatures.strike(this, p, signatureDamage(2.4), position(), 0.6, 0.9);
            p.setSecondsOnFire(6);
        }
        for (double t = 1; t <= 12; t += 1.5) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.LAVA, getX() + signatureAim.x * t, getY() + 0.3, getZ() + signatureAim.z * t, 6, 0.4, 0.2, 0.4, 0);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, getX() + signatureAim.x * t, getY() + 0.3, getZ() + signatureAim.z * t, 1, 0, 0, 0, 0);
        }
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.HOSTILE, 1.6f, 0.6f);
    }
}
