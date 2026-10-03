package com.sofe.entity.summon;

import com.sofe.registry.EntityRegistry;
import com.sofe.skill.ClassState;
import com.sofe.skill.SkillTargeting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.Locale;
import java.util.UUID;

/**
 * A Bearer's summoned ally (Sprint 6): the souls risen by the Necromancer's Great Judgment, the King's Janissary Guard, the Sorceress's ice clone. It fights for its owner (their target,
 * whoever hurts them, any monster near), never harms a player, and crumbles when its time runs out.
 */
public class SummonedAlly extends PathfinderMob implements Ally {
    public enum Kind {
        JANISSARY(24, 6, 0.32, 1.0f), ICE_CLONE(20, 0, 0, 1.0f), RISEN(16, 4, 0.3, 1.0f);

        public final double health, damage, speed;
        public final float scale;

        Kind(double health, double damage, double speed, float scale) {
            this.health = health;
            this.damage = damage;
            this.speed = speed;
            this.scale = scale;
        }

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(SummonedAlly.class, EntityDataSerializers.INT);
    /** The owner, known to the client too: the ice clone wears its caster's own skin. */
    private static final EntityDataAccessor<java.util.Optional<UUID>> OWNER = SynchedEntityData.defineId(SummonedAlly.class, EntityDataSerializers.OPTIONAL_UUID);
    private UUID owner;
    private int life = 600;
    private UUID commanded;
    private long commandedUntil;

    public SummonedAlly(EntityType<? extends SummonedAlly> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.ATTACK_DAMAGE, 5)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 24).add(Attributes.ARMOR, 4);
    }

    /** Summons an ally of this kind for its owner, beside them, for so many ticks. */
    public static SummonedAlly summon(ServerPlayer owner, Kind kind, int lifeTicks, net.minecraft.world.phys.Vec3 at) {
        SummonedAlly ally = new SummonedAlly(EntityRegistry.SUMMONED_ALLY.get(), owner.level());
        ally.setup(owner, kind, lifeTicks);
        ally.moveTo(at.x, at.y, at.z, owner.getYRot(), 0);
        owner.level().addFreshEntity(ally);
        if (owner.level() instanceof ServerLevel level) {
            level.sendParticles(kind == Kind.ICE_CLONE ? ParticleTypes.SNOWFLAKE : kind == Kind.JANISSARY ? ParticleTypes.WAX_ON : ParticleTypes.SOUL,
                    at.x, at.y + 1, at.z, 20, 0.4, 0.8, 0.4, 0.02);
        }
        return ally;
    }

    private void setup(ServerPlayer owner, Kind kind, int lifeTicks) {
        this.owner = owner.getUUID();
        entityData.set(OWNER, java.util.Optional.of(owner.getUUID()));
        this.life = lifeTicks;
        entityData.set(KIND, kind.ordinal());
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(kind.health);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(kind.damage);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(kind.speed);
        setHealth((float) kind.health);
        if (kind == Kind.JANISSARY) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            setDropChance(EquipmentSlot.MAINHAND, 0);
        }
        if (kind == Kind.ICE_CLONE) setNoAi(true);
        setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(KIND, 0);
        entityData.define(OWNER, java.util.Optional.empty());
    }

    /** The owner as the client knows it (for drawing). */
    public java.util.Optional<UUID> syncedOwner() {
        return entityData.get(OWNER);
    }

    public Kind kind() {
        return Kind.values()[Math.max(0, Math.min(Kind.values().length - 1, entityData.get(KIND)))];
    }

    @Override
    public UUID owner() {
        return owner;
    }

    public ServerPlayer ownerPlayer() {
        return ownerPlayer(this);
    }

    @Override
    public void command(LivingEntity target, int ticks) {
        commanded = target.getUUID();
        commandedUntil = level().getGameTime() + ticks;
        setTarget(target);
    }

    @Override
    public LivingEntity commanded() {
        if (commanded == null || level().getGameTime() >= commandedUntil || !(level() instanceof ServerLevel level)) return null;
        return level.getEntity(commanded) instanceof LivingEntity c && c.isAlive() ? c : null;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
        goalSelector.addGoal(3, new Ally.FollowOwner<>(this));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new Ally.OwnersFoe<>(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this, SummonedAlly.class));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, e -> e instanceof Enemy && !(e instanceof Ally)));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (--life <= 0 || owner == null) {
            expire();
            return;
        }
        if (getTarget() instanceof Ally || getTarget() instanceof Player) setTarget(null);
    }

    /** Time is up: the soul goes free. Wardens of a Heavy Heart burst; a Rite of Passage grows stronger. */
    private void expire() {
        ServerPlayer player = ownerPlayer();
        if (player != null && level() instanceof ServerLevel level) {
            Kind kind = kind();
            if (kind == Kind.RISEN) {
                com.sofe.skill.ClassMechanics.soulReleased(player);
                ClassState.passive(player, "heavy_heart").ifPresent(stats -> {
                    double radius = stats.param("radius", 3);
                    SkillTargeting.burst(level, position().add(0, 1, 0), ParticleTypes.EXPLOSION, 3, 0.5);
                    level.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.6f, 1.4f);
                    SkillTargeting.around(player, position(), radius).forEach(e -> SkillTargeting.damage(player, e, (float) stats.param("damage", 6)));
                });
            }
            level.sendParticles(kind == Kind.ICE_CLONE ? ParticleTypes.ITEM_SNOWBALL : ParticleTypes.SOUL, getX(), getY() + 1, getZ(), 15, 0.3, 0.6, 0.3, 0.02);
        }
        discard();
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        if (other instanceof Player p) return true; // never fights the Bearers
        if (other instanceof Ally a) return owner != null && owner.equals(a.owner());
        return super.isAlliedTo(other);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player || source.getEntity() instanceof Ally) return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        return hit;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putInt("Life", life);
        tag.putInt("Kind", entityData.get(KIND));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            owner = tag.getUUID("Owner");
            entityData.set(OWNER, java.util.Optional.of(owner));
        }
        life = tag.getInt("Life");
        entityData.set(KIND, tag.getInt("Kind"));
    }
}
