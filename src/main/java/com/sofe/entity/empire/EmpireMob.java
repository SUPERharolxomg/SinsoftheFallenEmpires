package com.sofe.entity.empire;

import com.sofe.entity.SoFEAnimated;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * The creatures of the empires, two for each of Acts I to IV (scripts/make_empire_mobs.py draws them). They
 * spawn only in their own region, which opens only in its act, so each act has enemies no other has. Each
 * {@link Kind} has its numbers and its tricks.
 */
public class EmpireMob extends Monster implements SoFEAnimated {

    /** What each creature can do beside biting and hitting. */
    public enum Trick {
        /** Springs at its target from a few blocks away. */
        LEAP,
        /** Its hits leave hunger. */
        HUNGER,
        /** Its hits freeze: slowness. */
        FROST,
        /** Its hits weaken and slow. */
        WITHERING_WRAPS,
        /** Its hits leave nausea. */
        DAZZLE,
        /** Appears behind its target now and then. */
        BLINK,
        /** Fades from sight for a moment now and then. */
        FLICKER,
        /** Takes half the damage from blows in front of it. */
        SHIELD,
        /** Sets fire to nothing, but burns away in fire: takes double fire damage. */
        FLAMMABLE
    }

    public enum Kind {
        SAND_GHOUL(26, 4, 0.30, 2, 0, 7, EnumSet.of(Trick.LEAP, Trick.HUNGER)),
        CLOCKWORK_SCARAB(10, 2, 0.34, 4, 0, 3, EnumSet.noneOf(Trick.class)),
        DRAUGR(38, 6, 0.24, 6, 0.4, 10, EnumSet.of(Trick.FROST)),
        RIME_WOLF(24, 5, 0.36, 2, 0.1, 8, EnumSet.of(Trick.LEAP, Trick.FROST)),
        MIRAGE_DANCER(30, 5, 0.30, 2, 0, 10, EnumSet.of(Trick.BLINK, Trick.DAZZLE)),
        BOG_MUMMY(48, 6, 0.20, 4, 0.5, 11, EnumSet.of(Trick.WITHERING_WRAPS, Trick.FLAMMABLE)),
        GILDED_LEGIONNAIRE(56, 8, 0.25, 10, 0.6, 14, EnumSet.of(Trick.SHIELD)),
        GLADIATOR_SHADE(44, 9, 0.32, 4, 0.2, 14, EnumSet.of(Trick.FLICKER, Trick.LEAP));

        public final double health, damage, speed, armor, knockbackResistance;
        public final int xp;
        public final Set<Trick> tricks;

        Kind(double health, double damage, double speed, double armor, double knockbackResistance, int xp, Set<Trick> tricks) {
            this.health = health;
            this.damage = damage;
            this.speed = speed;
            this.armor = armor;
            this.knockbackResistance = knockbackResistance;
            this.xp = xp;
            this.tricks = tricks;
        }

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public AttributeSupplier.Builder attributes() {
            return Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, health)
                    .add(Attributes.ATTACK_DAMAGE, damage)
                    .add(Attributes.MOVEMENT_SPEED, speed)
                    .add(Attributes.ARMOR, armor)
                    .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance)
                    .add(Attributes.FOLLOW_RANGE, 32.0);
        }
    }

    private final software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache animationCache =
            software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache(this);
    private final Kind kind;
    private int trickCooldown;

    public EmpireMob(EntityType<? extends Monster> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        this.xpReward = kind.xp;
        // the goals are registered by Mob's constructor, before the kind is known: the leap is added here
        if (kind.tricks.contains(Trick.LEAP)) this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.45f));
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public String modelName() {
        return kind.id();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            if (kind.tricks.contains(Trick.HUNGER)) living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 140, 0), this);
            if (kind.tricks.contains(Trick.FROST)) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
                living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 40, living.getTicksFrozen() + 60));
            }
            if (kind.tricks.contains(Trick.WITHERING_WRAPS)) {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), this);
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0), this);
            }
            if (kind.tricks.contains(Trick.DAZZLE) && random.nextInt(3) == 0) living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0), this);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (kind.tricks.contains(Trick.SHIELD) && source.getDirectEntity() != null && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) {
            Vec3 to = source.getDirectEntity().position().subtract(position()).normalize();
            Vec3 facing = Vec3.directionFromRotation(0, getYRot());
            if (to.x * facing.x + to.z * facing.z > 0.3) { // in front: the scutum takes half
                amount *= 0.5f;
                level().playSound(null, blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 0.8f, 1.0f);
            }
        }
        if (kind.tricks.contains(Trick.FLAMMABLE) && source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) amount *= 2;
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel level) || trickCooldown-- > 0) return;
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;
        double distance = distanceToSqr(target);
        if (kind.tricks.contains(Trick.BLINK) && distance > 9 && distance < 400 && random.nextInt(4) == 0) {
            Vec3 behind = target.position().subtract(Vec3.directionFromRotation(0, target.getYRot()).scale(2));
            level.sendParticles(ParticleTypes.WITCH, getX(), getY() + 1, getZ(), 16, 0.3, 0.6, 0.3, 0.02);
            if (randomTeleport(behind.x, target.getY(), behind.z, true)) {
                level.playSound(null, blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.HOSTILE, 1f, 1.2f);
            }
            trickCooldown = 100;
        } else if (kind.tricks.contains(Trick.FLICKER) && distance < 256 && random.nextInt(3) == 0) {
            addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 30, 0, false, false));
            level.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1, getZ(), 20, 0.3, 0.6, 0.3, 0.02);
            trickCooldown = 120;
        } else {
            trickCooldown = 20;
        }
    }
}
