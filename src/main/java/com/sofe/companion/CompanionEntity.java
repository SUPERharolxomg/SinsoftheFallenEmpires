package com.sofe.companion;

import com.sofe.entity.summon.Ally;
import com.sofe.entity.summon.EmbalmedDead;
import com.sofe.entity.summon.SummonedAlly;
import com.sofe.player.PlayerClass;
import com.sofe.quest.DialogueService;
import com.sofe.skill.SkillTargeting;
import com.sofe.world.SoFEWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * A Bearer travelling with the player (UC-22): one of the four heroes the player is not, at the player's level,
 * fighting with a few of its class skills (the Knight's Verdict, the Necromancer's draining touch and his
 * Embalmed, the Sorceress's bolts and stars, the Thief's flurry and smoke, the King's Janissaries and his
 * decree). It follows, waits when told, and speaks about where it is.
 */
public class CompanionEntity extends PathfinderMob implements Ally {
    private static final EntityDataAccessor<String> BEARER = SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.STRING);
    private UUID owner;
    private boolean staying;
    private int skillCooldown = 40, specialCooldown = 200, bolt = 30;
    private String lastRegion = "";
    private UUID commanded;
    private long commandedUntil;

    public CompanionEntity(EntityType<? extends CompanionEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.ATTACK_DAMAGE, 4)
                .add(Attributes.MOVEMENT_SPEED, 0.32).add(Attributes.FOLLOW_RANGE, 32).add(Attributes.ARMOR, 4);
    }

    /** Sets who it is and fits it to the player's level. */
    void setup(ServerPlayer player, PlayerClass bearer, int level) {
        owner = player.getUUID();
        entityData.set(BEARER, bearer.id());
        double health = 30 + 4 * level, damage = 3 + 0.35 * level;
        if (bearer == PlayerClass.KNIGHT) health *= 1.4;
        if (bearer == PlayerClass.SORCERESS || bearer == PlayerClass.NECROMANCER) health *= 0.85;
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        getAttribute(Attributes.ARMOR).setBaseValue(4 + level / 10.0 + (bearer == PlayerClass.KNIGHT ? 6 : 0));
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(bearer == PlayerClass.THIEF ? 0.36 : 0.32);
        setHealth((float) health);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(switch (bearer) {
            case KNIGHT -> Items.IRON_SWORD;
            case NECROMANCER -> Items.BONE;
            case SORCERESS -> Items.BLAZE_ROD;
            case THIEF -> Items.IRON_AXE;
            case KING -> Items.GOLDEN_SWORD;
        }));
        if (bearer == PlayerClass.KNIGHT) setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        setDropChance(EquipmentSlot.MAINHAND, 0);
        setDropChance(EquipmentSlot.OFFHAND, 0);
        setCustomName(Component.translatable(bearer.heroKey()));
        setCustomNameVisible(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BEARER, "knight");
    }

    public PlayerClass bearer() {
        return PlayerClass.byId(entityData.get(BEARER)).orElse(PlayerClass.KNIGHT);
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

    public void setStaying(boolean stay) {
        staying = stay;
        getNavigation().stop();
    }

    public boolean staying() {
        return staying;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, true) {
            @Override
            public boolean canUse() {
                return !ranged() && super.canUse();
            }
        });
        goalSelector.addGoal(3, new Ally.FollowOwner<>(this) {
            @Override
            public boolean canUse() {
                return !staying && super.canUse();
            }
        });
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new Ally.OwnersFoe<>(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this, Ally.class));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, e -> e instanceof Enemy && !(e instanceof Ally)));
    }

    /** The Sorceress and the Necromancer fight from a distance. */
    private boolean ranged() {
        return bearer() == PlayerClass.SORCERESS || bearer() == PlayerClass.NECROMANCER;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) return;
        ServerPlayer player = ownerPlayer(this);
        if (player == null) {
            if (tickCount > 100) discard(); // its Bearer left the world
            return;
        }
        if (getTarget() instanceof Player || getTarget() instanceof Ally) setTarget(null);
        if (tickCount % 100 == 0) speakOfTheRegion(player);
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) return;
        if (ranged()) keepDistance(target);
        if (--bolt <= 0 && ranged()) {
            rangedAttack(player, level, target);
            bolt = bearer() == PlayerClass.SORCERESS ? 28 : 34;
        }
        if (--skillCooldown <= 0) {
            skill(player, level, target);
            skillCooldown = 160;
        }
        if (--specialCooldown <= 0) {
            special(player, level, target);
            specialCooldown = 400;
        }
    }

    private void keepDistance(LivingEntity target) {
        double d = distanceTo(target);
        getLookControl().setLookAt(target, 30, 30);
        if (d < 5) {
            Vec3 away = position().subtract(target.position()).normalize().scale(4);
            getNavigation().moveTo(getX() + away.x, getY(), getZ() + away.z, 1.2);
        } else if (d > 12) {
            getNavigation().moveTo(target, 1.0);
        } else {
            getNavigation().stop();
        }
    }

    private void hit(ServerPlayer player, LivingEntity target, float amount) {
        target.invulnerableTime = 0;
        target.hurt(damageSources().mobAttack(this), amount);
    }

    private float power() {
        return (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private void bolt(ServerLevel level, LivingEntity target, ParticleOptions particle) {
        Vec3 from = getEyePosition(), to = target.position().add(0, target.getBbHeight() / 2, 0);
        SkillTargeting.beam(level, from, to, particle, 0.35);
    }

    /** The Sorceress hurls fire and frost in turn; the Necromancer drains life. */
    private void rangedAttack(ServerPlayer player, ServerLevel level, LivingEntity target) {
        swing(InteractionHand.MAIN_HAND);
        if (bearer() == PlayerClass.SORCERESS) {
            boolean fire = tickCount / 30 % 2 == 0;
            bolt(level, target, fire ? ParticleTypes.FLAME : ParticleTypes.SNOWFLAKE);
            hit(player, target, power() * 1.1f);
            if (fire) target.setSecondsOnFire(3);
            else target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1));
            level.playSound(null, blockPosition(), fire ? SoundEvents.BLAZE_SHOOT : SoundEvents.PLAYER_HURT_FREEZE, SoundSource.NEUTRAL, 0.6f, 1.3f);
        } else {
            bolt(level, target, ParticleTypes.SOUL_FIRE_FLAME);
            hit(player, target, power() * 0.9f);
            heal(power() * 0.4f);
            level.playSound(null, blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL, 0.6f, 1.4f);
        }
    }

    /** Each class's skill, every eight seconds. */
    private void skill(ServerPlayer player, ServerLevel level, LivingEntity target) {
        switch (bearer()) {
            case KNIGHT -> { // Verdict: a sweeping blow that stuns
                for (LivingEntity e : SkillTargeting.around(player, position(), 4)) {
                    hit(player, e, power() * 1.2f);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 9));
                }
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 1, getZ(), 6, 1.5, 0.2, 1.5, 0);
                level.playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.NEUTRAL, 0.5f, 0.8f);
            }
            case NECROMANCER -> { // Burial Wraps
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 10));
                bolt(level, target, ParticleTypes.WHITE_ASH);
            }
            case SORCERESS -> { // Wandering Spark: lightning between enemies
                LivingEntity last = target;
                for (LivingEntity e : SkillTargeting.around(player, target.position(), 6).stream().limit(3).toList()) {
                    SkillTargeting.beam(level, last.position().add(0, 1, 0), e.position().add(0, 1, 0), ParticleTypes.ELECTRIC_SPARK, 0.25);
                    hit(player, e, power());
                    last = e;
                }
            }
            case THIEF -> { // Double Edge, from behind
                Vec3 behind = target.position().add(Vec3.directionFromRotation(0, target.getYRot()).scale(-1.2));
                teleportTo(behind.x, target.getY(), behind.z);
                hit(player, target, power() * 1.5f);
                hit(player, target, power());
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1, target.getZ(), 2, 0.2, 0.2, 0.2, 0);
            }
            case KING -> { // a Decree of Steadfastness round him and his Bearer
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6), e -> e instanceof Player || e instanceof Ally)) {
                    ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 0));
                }
                level.sendParticles(ParticleTypes.WAX_ON, getX(), getY() + 0.2, getZ(), 30, 3, 0.1, 3, 0);
                level.playSound(null, blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.NEUTRAL, 0.6f, 1.2f);
            }
        }
    }

    /** A rarer, stronger skill, every twenty seconds. */
    private void special(ServerPlayer player, ServerLevel level, LivingEntity target) {
        switch (bearer()) {
            case KNIGHT -> addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2)); // Shield Wall
            case NECROMANCER -> EmbalmedDead.raise(player, position().add(1.5, 0, 0), 300, 12 + 2 * com.sofe.gear.PlayerGear.level(player), power() * 0.7);
            case SORCERESS -> { // a short Starfall
                for (int i = 0; i < 3; i++) {
                    Vec3 at = target.position().add(random.nextGaussian() * 1.5, 0, random.nextGaussian() * 1.5);
                    SkillTargeting.beam(level, at.add(0, 8, 0), at, ParticleTypes.END_ROD, 0.6);
                    SkillTargeting.around(player, at, 2.5).forEach(e -> hit(player, e, power() * 1.3f));
                }
            }
            case THIEF -> { // Smoke Step
                addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0));
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 1, getZ(), 20, 0.5, 0.6, 0.5, 0.01);
            }
            case KING -> SummonedAlly.summon(player, SummonedAlly.Kind.JANISSARY, 400, position().add(1.5, 0, 0));
        }
    }

    /** A word when the Bearer and their companion enter a new region. */
    private void speakOfTheRegion(ServerPlayer player) {
        SoFEWorld.regionMap(player.server).ifPresent(map -> {
            String region = map.regionAt(player.getBlockX(), player.getBlockZ()).id();
            if (region.equals(lastRegion)) return;
            boolean first = lastRegion.isEmpty();
            lastRegion = region;
            if (!first) say(player, "region." + region);
        });
    }

    /** What the companion says, in the chat of its Bearer. */
    void say(ServerPlayer player, String what) {
        String key = "companion.sofe." + bearer().id() + "." + what;
        if (!net.minecraft.locale.Language.getInstance().has(key) && !what.startsWith("region.")) return;
        player.sendSystemMessage(Component.translatable("message.sofe.companion.says", getDisplayName(), Component.translatable(key))
                .withStyle(ChatFormatting.AQUA));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer sp && sp.getUUID().equals(owner)) {
            DialogueService.open(sp, "sofe:companion/" + bearer().id(), getId());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void die(DamageSource source) {
        ServerPlayer player = ownerPlayer(this);
        if (player != null) {
            say(player, "down");
            Companions.lost(player);
        }
        if (level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.POOF, getX(), getY() + 1, getZ(), 20, 0.4, 0.6, 0.4, 0.02);
        }
        discard(); // not dead: beaten, it goes back to Sulthari
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
        tag.putString("Bearer", entityData.get(BEARER));
        tag.putBoolean("Staying", staying);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) owner = tag.getUUID("Owner");
        entityData.set(BEARER, tag.getString("Bearer"));
        staying = tag.getBoolean("Staying");
    }
}
