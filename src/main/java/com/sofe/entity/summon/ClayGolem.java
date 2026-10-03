package com.sofe.entity.summon;

import com.sofe.registry.EntityRegistry;
import com.sofe.skill.ClassMechanics;
import com.sofe.skill.ClassState;
import com.sofe.skill.SkillTargeting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * The Necromancer's Clay Warden: a canopic automaton of baked clay with a gold-painted face, built on the
 * vanilla iron golem (its body, long arms and swing), smaller and bound to a soul for a while. When the
 * soul goes free it crumbles; with Heavy Heart it bursts.
 */
public class ClayGolem extends IronGolem implements Ally {
    private UUID owner;
    private int life = 600;
    private UUID commanded;
    private long commandedUntil;

    public ClayGolem(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
    }

    public static ClayGolem summon(ServerPlayer owner, int lifeTicks, Vec3 at, double health, double damage) {
        ClayGolem golem = new ClayGolem(EntityRegistry.CLAY_GOLEM.get(), owner.level());
        golem.owner = owner.getUUID();
        golem.life = lifeTicks;
        golem.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        golem.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        golem.setHealth((float) health);
        golem.setPlayerCreated(true);
        golem.moveTo(at.x, at.y, at.z, owner.getYRot(), 0);
        owner.level().addFreshEntity(golem);
        if (owner.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SOUL, at.x, at.y + 1, at.z, 24, 0.5, 0.9, 0.5, 0.02);
            level.playSound(null, golem.blockPosition(), SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS, 0.8f, 0.7f);
        }
        return golem;
    }

    @Override
    public UUID owner() {
        return owner;
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
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(3, new Ally.FollowOwner<>(this));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new Ally.OwnersFoe<>(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this, ClayGolem.class, SummonedAlly.class));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                e -> e instanceof Enemy && !(e instanceof Ally)));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (--life <= 0 || owner == null) {
            expire();
            return;
        }
        if (getTarget() instanceof Player || getTarget() instanceof Ally) setTarget(null);
    }

    /** The soul goes free: Rite of Passage grows, and with Heavy Heart the clay bursts. */
    private void expire() {
        ServerPlayer player = ownerPlayer(this);
        if (player != null && level() instanceof ServerLevel level) {
            ClassMechanics.soulReleased(player);
            ClassState.passive(player, "heavy_heart").ifPresent(stats -> {
                SkillTargeting.burst(level, position().add(0, 1, 0), ParticleTypes.EXPLOSION, 3, 0.6);
                level.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.6f, 1.2f);
                SkillTargeting.around(player, position(), stats.param("radius", 3))
                        .forEach(e -> SkillTargeting.damage(player, e, (float) stats.param("damage", 6)));
            });
            level.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.2, getZ(), 18, 0.4, 0.8, 0.4, 0.02);
            level.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,
                    net.minecraft.world.level.block.Blocks.TERRACOTTA.defaultBlockState()), getX(), getY() + 1, getZ(), 30, 0.5, 0.8, 0.5, 0.1);
        }
        discard();
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        if (other instanceof Player) return true;
        if (other instanceof Ally a) return owner != null && owner.equals(a.owner());
        return super.isAlliedTo(other);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player || source.getEntity() instanceof Ally) return false;
        return super.hurt(source, amount);
    }

    @Override
    public void startPersistentAngerTimer() {
        // a bound soul holds no grudges
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) owner = tag.getUUID("Owner");
        life = tag.getInt("Life");
    }
}
