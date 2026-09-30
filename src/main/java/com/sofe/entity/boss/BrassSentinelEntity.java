package com.sofe.entity.boss;

import com.sofe.SoFEMod;
import com.sofe.mob.BossDifficulty;
import com.sofe.quest.QuestEngine;
import com.sofe.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The Brass Sentinel, boss of Act I (README, Act I): the clockwork guardian of the Great Observatory,
 * awake and obeying no one. Phase 1 is a brass golem; below half health the Void takes it and it
 * calls Void creatures. Every player who hurt it or stayed close gets the credit.
 */
public class BrassSentinelEntity extends Monster {
    public static final String BOSS_ID = "sofe:brass_sentinel";
    private static final UUID DIFFICULTY_ID = UUID.fromString("7d0a3c2e-5b6f-4f7e-9a1d-2c3b4e5f6a71");
    private static final double ARENA_RADIUS = 32;
    private static final int RESET_AFTER_TICKS = 600; // 30 s with nobody left in the fight
    private static final int SUMMON_EVERY = 400;
    private static final int SHOCKWAVE_EVERY = 160;

    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("npc.sofe.brass_sentinel"),
            BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_10);
    private final Set<UUID> participants = new HashSet<>();
    /** The last player object seen for each participant, for players the server list does not have (fake players). */
    private final java.util.Map<UUID, ServerPlayer> lastSeen = new java.util.HashMap<>();
    private boolean voidPhase;
    private int emptyTicks;

    public BrassSentinelEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 120;
        setPersistenceRequired();
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
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    /** Health and damage follow the difficulty (Easy −25%, Hard +25%). */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData data, CompoundTag tag) {
        applyDifficulty(level.getLevel().getDifficulty());
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    public void applyDifficulty(net.minecraft.world.Difficulty difficulty) {
        double bonus = BossDifficulty.multiplier(difficulty) - 1;
        for (var attribute : new net.minecraft.world.entity.ai.attributes.Attribute[]{Attributes.MAX_HEALTH, Attributes.ATTACK_DAMAGE}) {
            var instance = getAttribute(attribute);
            if (instance == null) continue;
            instance.removeModifier(DIFFICULTY_ID);
            if (bonus != 0) instance.addPermanentModifier(new AttributeModifier(DIFFICULTY_ID, "SoFE boss difficulty", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        setHealth(getMaxHealth());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) {
            if (voidPhaseClient()) {
                level().addParticle(ParticleTypes.REVERSE_PORTAL, getRandomX(0.8), getRandomY(), getRandomZ(0.8), 0, 0.05, 0);
            }
        }
    }

    private boolean voidPhaseClient() {
        return getHealth() < getMaxHealth() / 2;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossBar.setProgress(getHealth() / getMaxHealth());
        ServerLevel level = (ServerLevel) level();

        // everyone close by takes part (and sees the boss bar)
        AABB arena = getBoundingBox().inflate(ARENA_RADIUS);
        var nearby = level.getEntitiesOfClass(ServerPlayer.class, arena, p -> p.isAlive() && !p.isSpectator());
        nearby.forEach(p -> {
            participants.add(p.getUUID());
            lastSeen.put(p.getUUID(), p);
            bossBar.addPlayer(p);
        });
        for (ServerPlayer p : Set.copyOf(bossBar.getPlayers())) {
            if (!nearby.contains(p)) bossBar.removePlayer(p);
        }

        // nobody left: after 30 s the Sentinel resets (docs/Jugabilidad.md, "Boss fights")
        if (nearby.isEmpty() && getHealth() < getMaxHealth()) {
            if (++emptyTicks >= RESET_AFTER_TICKS) {
                setHealth(getMaxHealth());
                participants.clear();
                lastSeen.clear();
                voidPhase = false;
                emptyTicks = 0;
                setTarget(null);
            }
        } else {
            emptyTicks = 0;
        }

        if (!voidPhase && getHealth() < getMaxHealth() / 2) {
            voidPhase = true;
            bossBar.setColor(BossEvent.BossBarColor.PURPLE);
            level.playSound(null, blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.6f, 1.4f);
            nearby.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.brass_sentinel.void_phase"), true));
            var speed = getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.setBaseValue(0.3);
            summonVoid(level);
        }
        if (voidPhase && this.tickCount % SUMMON_EVERY == 0) summonVoid(level);
        if (voidPhase && BossDifficulty.extraMechanics(level.getDifficulty()) && this.tickCount % SHOCKWAVE_EVERY == 0) shockwave(level, nearby);
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
    private void shockwave(ServerLevel level, java.util.List<ServerPlayer> players) {
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
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof ServerPlayer player) {
            participants.add(player.getUUID());
            lastSeen.put(player.getUUID(), player);
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel level) {
            for (UUID id : participants) {
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(id); // after a respawn this is the new player
                if (player == null) player = lastSeen.get(id);
                if (player != null) QuestEngine.bossDefeated(player, BOSS_ID);
            }
            SoFEMod.LOGGER.info("The Brass Sentinel fell; {} participant(s) credited", participants.size());
        }
    }

    public Set<UUID> participants() {
        return Set.copyOf(participants);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
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

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("void_phase", voidPhase);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        voidPhase = tag.getBoolean("void_phase");
        if (hasCustomName()) bossBar.setName(getDisplayName());
    }
}
