package com.sofe.entity.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Kaleth, the Burning Blade, Broken Oath of Law I ("You shall not raise your sword against one who
 * surrenders"), in the Nordrath Forge. The inverted Law: he stuns everyone around him and strikes
 * the stunned for far more. Fire and thrown blades keep the fight moving.
 */
public class KalethEntity extends BrokenOathEntity {
    public static final String BOSS_ID = "sofe:kaleth";
    public static final float EXECUTION_MULTIPLIER = 2.5f;
    private static final int STUN_TICKS = 50;
    private final Map<UUID, Long> stunnedUntil = new HashMap<>();

    public KalethEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public String modelName() {
        return "kaleth";
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 220.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public int law() {
        return 1;
    }

    @Override
    public String archsin() {
        return VorathEntity.BOSS_ID;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, true));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    @Override
    protected void populateDefaultEquipmentSlots(net.minecraft.util.RandomSource random, DifficultyInstance difficulty) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, DifficultyInstance difficulty,
                                                                  net.minecraft.world.entity.MobSpawnType reason, net.minecraft.world.entity.SpawnGroupData data, net.minecraft.nbt.CompoundTag tag) {
        populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        setDropChance(EquipmentSlot.MAINHAND, 0f);
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    /** Stuns a target for a moment: slow, weak, and open to the execution strike. */
    public void stun(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, STUN_TICKS, 4, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, STUN_TICKS, 1, false, true));
        stunnedUntil.put(target.getUUID(), level().getGameTime() + STUN_TICKS);
    }

    public boolean isStunned(LivingEntity target) {
        Long until = stunnedUntil.get(target.getUUID());
        return until != null && level().getGameTime() < until;
    }

    /** The Law turned upside down: a stunned target takes the execution strike. */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean stunned = target instanceof LivingEntity living && isStunned(living);
        var damage = getAttribute(Attributes.ATTACK_DAMAGE);
        double base = damage == null ? 0 : damage.getBaseValue();
        if (stunned && damage != null) damage.setBaseValue(base * EXECUTION_MULTIPLIER);
        boolean hit = super.doHurtTarget(target);
        if (stunned && damage != null) damage.setBaseValue(base);
        if (hit) {
            target.setSecondsOnFire(4);
            if (stunned && target instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable("message.sofe.kaleth.execution").withStyle(ChatFormatting.DARK_RED), true);
                stunnedUntil.remove(player.getUUID());
            }
        }
        return hit;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        int throwEvery = phase() >= 2 ? 50 : 80;
        if (this.tickCount % throwEvery == 0) throwBlades(level, fighters, phase() >= 2 ? 5 : 3);
        if (this.tickCount % 200 == 100) stunningSlam(level, fighters);
    }

    @Override
    protected void onPhase(int newPhase, ServerLevel level, List<ServerPlayer> fighters) {
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(0.33);
        level.playSound(null, blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1f, 0.5f);
        fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.kaleth.phase2").withStyle(ChatFormatting.GOLD), true));
    }

    @Override
    protected void onReset() {
        stunnedUntil.clear();
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(0.28);
    }

    /** Burning blades thrown at the fighters (fire charges as placeholders). */
    private void throwBlades(ServerLevel level, List<ServerPlayer> fighters, int count) {
        for (int i = 0; i < count && !fighters.isEmpty(); i++) {
            ServerPlayer target = fighters.get(i % fighters.size());
            Vec3 from = getEyePosition();
            Vec3 aim = target.getEyePosition().subtract(from).add(random.nextGaussian() * 0.6, 0, random.nextGaussian() * 0.6);
            SmallFireball blade = new SmallFireball(level, this, aim.x, aim.y, aim.z);
            blade.setPos(from.x, from.y, from.z);
            level.addFreshEntity(blade);
        }
        level.playSound(null, blockPosition(), SoundEvents.TRIDENT_THROW, SoundSource.HOSTILE, 1f, 0.7f);
    }

    /** The forge hammer comes down: everyone close is stunned for a moment. */
    private void stunningSlam(ServerLevel level, List<ServerPlayer> fighters) {
        level.playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.2f, 0.6f);
        level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 0.2, getZ(), 60, 3, 0.1, 3, 0.05);
        for (ServerPlayer p : fighters) {
            if (p.distanceToSqr(this) > 7 * 7) continue;
            stun(p);
            p.displayClientMessage(Component.translatable("message.sofe.kaleth.stunned").withStyle(ChatFormatting.RED), true);
        }
    }
}
