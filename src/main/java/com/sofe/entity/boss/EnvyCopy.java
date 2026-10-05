package com.sofe.entity.boss;

import com.sofe.gear.ranged.Spell;
import com.sofe.player.PlayerClass;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * A copy of a Bearer's own hero, raised by Envyris from her shadow (or by Prython and Nahrazel with her power): it wears the hero's face (the Bearer NPC's
 * skin), hunts the Bearer it copies, and fights as that class does (the Sorceress and the Necromancer from afar
 * with bolts of fire or soul; the Knight bashes and weakens, the Thief makes wounds bleed, the King's blows slow).
 * It has part of the Bearer's own health and is gone when Envyris's fight ends.
 */
public class EnvyCopy extends Monster {
    private static final EntityDataAccessor<String> BEARER = SynchedEntityData.defineId(EnvyCopy.class, EntityDataSerializers.STRING);
    public static final float HEALTH_SHARE = 0.6f;
    private String maker = "";
    private java.util.UUID copied;
    private int bolt = 30;

    public EnvyCopy(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.ATTACK_DAMAGE, 6)
                .add(Attributes.MOVEMENT_SPEED, 0.31).add(Attributes.FOLLOW_RANGE, 40).add(Attributes.ARMOR, 6);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BEARER, "knight");
    }

    public PlayerClass bearer() {
        return PlayerClass.byId(entityData.get(BEARER)).orElse(PlayerClass.KNIGHT);
    }

    public String maker() {
        return maker;
    }

    /** Fits the copy to the Bearer it copies: their hero, part of their health, a weapon of the class. */
    public void setup(LivingEntity raiser, ServerPlayer player, PlayerClass cls) {
        maker = raiser.getStringUUID();
        copied = player.getUUID();
        entityData.set(BEARER, cls.id());
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(30, player.getMaxHealth() * HEALTH_SHARE));
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6 + com.sofe.gear.PlayerGear.level(player) * 0.12);
        setHealth(getMaxHealth());
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(switch (cls) {
            case KNIGHT -> Items.IRON_SWORD;
            case NECROMANCER -> Items.BONE;
            case SORCERESS -> Items.BLAZE_ROD;
            case THIEF -> Items.IRON_AXE;
            case KING -> Items.GOLDEN_SWORD;
        }));
        if (cls == PlayerClass.KNIGHT) setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        setDropChance(EquipmentSlot.MAINHAND, 0);
        setDropChance(EquipmentSlot.OFFHAND, 0);
        setCustomName(Component.translatable("entity.sofe.envy_copy.named", Component.translatable(cls.heroKey())).withStyle(ChatFormatting.DARK_PURPLE));
        setCustomNameVisible(true);
        setTarget(player);
    }

    private boolean ranged() {
        return bearer() == PlayerClass.SORCERESS || bearer() == PlayerClass.NECROMANCER;
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
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) return;
        if (copied != null && (getTarget() == null || !getTarget().isAlive()) && level.getEntity(copied) instanceof ServerPlayer p) setTarget(p);
        if (tickCount % 10 == 0) level.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1, getZ(), 2, 0.3, 0.6, 0.3, 0.01);
        if (ranged() && getTarget() != null && --bolt <= 0) {
            getLookControl().setLookAt(getTarget());
            BossKit.bolt(level, this, getTarget(), bearer() == PlayerClass.SORCERESS ? Spell.EMBER : Spell.SOUL,
                    (float) getAttributeValue(Attributes.ATTACK_DAMAGE), 1.3f, 2);
            bolt = 30;
            if (distanceToSqr(getTarget()) < 25) getNavigation().moveTo(getX() * 2 - getTarget().getX(), getY(), getZ() * 2 - getTarget().getZ(), 1.1);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            switch (bearer()) {
                case KNIGHT -> living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0), this);
                case THIEF -> living.addEffect(new MobEffectInstance(com.sofe.registry.SoFEEffects.BLEEDING.get(), 80, 0), this);
                case KING -> living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
                default -> {
                }
            }
        }
        return hit;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Bearer", entityData.get(BEARER));
        tag.putString("Maker", maker);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(BEARER, tag.getString("Bearer"));
        maker = tag.getString("Maker");
    }
}
