package com.sofe.entity.summon;

import com.sofe.registry.EntityRegistry;
import com.sofe.skill.ClassMechanics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * The Necromancer's Embalmed Dead: corpses of Khemet wrapped in linen, raised from the sand to fight for him
 * for a while (built on the vanilla husk). Their time, health and blows grow with his upgrades. When the
 * time is up they sink back into the ground and their souls go free.
 */
public class EmbalmedDead extends Husk implements Ally {
    private UUID owner;
    private int life = 400;
    private UUID commanded;
    private long commandedUntil;

    public EmbalmedDead(EntityType<? extends Husk> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static EmbalmedDead raise(ServerPlayer owner, Vec3 at, int lifeTicks, double health, double damage) {
        EmbalmedDead dead = new EmbalmedDead(EntityRegistry.EMBALMED_DEAD.get(), owner.level());
        dead.owner = owner.getUUID();
        dead.life = lifeTicks;
        dead.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        dead.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        dead.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0);
        dead.setHealth((float) health);
        dead.moveTo(at.x, at.y, at.z, owner.getYRot() + (float) (owner.getRandom().nextGaussian() * 30), 0);
        owner.level().addFreshEntity(dead);
        if (owner.level() instanceof ServerLevel level) {
            // they claw their way out of the ground
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()), at.x, at.y + 0.2, at.z, 30, 0.4, 0.1, 0.4, 0.1);
            level.sendParticles(ParticleTypes.SOUL, at.x, at.y + 1, at.z, 10, 0.3, 0.6, 0.3, 0.02);
            level.playSound(null, BlockPos.containing(at), SoundEvents.HUSK_AMBIENT, SoundSource.PLAYERS, 1, 0.7f);
        }
        return dead;
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

    /** Not a zombie of the night: none of the zombie's own goals, only the owner's. */
    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, true));
        goalSelector.addGoal(3, new Ally.FollowOwner<>(this));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new Ally.OwnersFoe<>(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this, Ally.class));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                e -> e instanceof Enemy && !(e instanceof Ally)));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (--life <= 0 || owner == null) {
            ServerPlayer player = ownerPlayer(this);
            if (player != null) ClassMechanics.soulReleased(player);
            if (level() instanceof ServerLevel level) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()), getX(), getY() + 0.5, getZ(), 25, 0.3, 0.4, 0.3, 0.1);
                level.sendParticles(ParticleTypes.SOUL, getX(), getY() + 1.2, getZ(), 8, 0.3, 0.5, 0.3, 0.02);
            }
            discard();
            return;
        }
        if (getTarget() instanceof Player || getTarget() instanceof Ally) setTarget(null);
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
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public void checkDespawn() {
        // bound to the Necromancer: they leave when their time is up, not by distance or difficulty
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
    public int getExperienceReward() {
        return 0;
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
