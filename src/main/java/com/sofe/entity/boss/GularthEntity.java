package com.sofe.entity.boss;

import com.sofe.story.Sin;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;

/**
 * Gularth, the Archsin of Gluttony, in the Feast Halls under Nordrath: he devours the terrain and grows. He eats
 * the floor from under the Bearers (pits that close again after a while, so the hall is whole after the fight),
 * and every mouthful makes him bigger and hits harder, up to half again his size, and in his second phase up to
 * twice. He shrinks back when the fight resets.
 */
public class GularthEntity extends ArchsinEntity implements Scaled {
    public static final String BOSS_ID = "sofe:gularth";
    private static final EntityDataAccessor<Float> GROWTH = SynchedEntityData.defineId(GularthEntity.class, EntityDataSerializers.FLOAT);
    private static final UUID GROWTH_DAMAGE = UUID.fromString("7d0a3c2e-5b6f-4f7e-9a1d-2c3b4e5f6a81");
    public static final float GROWTH_PER_MEAL = 0.08f;

    public GularthEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 560.0).add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24).add(Attributes.ARMOR, 12.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(GROWTH, 1.0f);
    }

    @Override
    public float renderScale() {
        return entityData.get(GROWTH);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (GROWTH.equals(key)) refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(renderScale());
    }

    @Override
    public String modelName() {
        return "gularth";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public Sin sin() {
        return Sin.GLUTTONY;
    }

    @Override
    protected String introDialogue() {
        return "sofe:act4/gularth_temptation";
    }

    @Override
    protected double arenaRadius() {
        return 20;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        if (this.tickCount % (phase() >= 2 ? 140 : 220) == 0) {
            ServerPlayer target = fighters.get(random.nextInt(fighters.size()));
            devour(level, target.blockPosition().below());
            target.displayClientMessage(Component.translatable("message.sofe.gularth.devours").withStyle(ChatFormatting.DARK_RED), true);
        }
    }

    /** A mouthful of the floor: a pit two deep where a Bearer stood, closing again after eight seconds. He grows. */
    public void devour(ServerLevel level, BlockPos under) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy >= -1; dy--) {
                    BlockPos pos = under.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.1);
                    BossKit.temporary(level, pos, Blocks.AIR.defaultBlockState(), 160, this);
                }
            }
        }
        level.playSound(null, blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 2.5f, 0.3f);
        grow(GROWTH_PER_MEAL);
        heal(getMaxHealth() * 0.01f);
    }

    public void grow(float by) {
        float max = phase() >= 2 ? 2.0f : 1.5f;
        float growth = Math.min(max, renderScale() + by);
        entityData.set(GROWTH, growth);
        var damage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.removeModifier(GROWTH_DAMAGE);
            damage.addTransientModifier(new AttributeModifier(GROWTH_DAMAGE, "SoFE Gularth's girth", growth - 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @Override
    protected void onReset() {
        entityData.set(GROWTH, 1.0f);
        var damage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) damage.removeModifier(GROWTH_DAMAGE);
    }

    // --- signature attack (SoFEBossEntity.Signature): his cleaver comes down on one Bearer like a butcher's on meat, and he eats what it takes

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("gularth_chop", 30, 240, 6);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick % 4 == 0) Signatures.drawArc(level, this, signatureAim, 5, 60, net.minecraft.core.particles.ParticleTypes.CRIMSON_SPORE);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        float dealt = 0;
        for (var p : Signatures.arc(this, signatureAim, 5, 60, fighters)) {
            Signatures.strike(this, p, signatureDamage(3.0), position(), 0.8, 0);
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.HUNGER, 200, 2), this);
            dealt += signatureDamage(3.0);
        }
        heal(dealt * 0.3f);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, net.minecraft.sounds.SoundSource.HOSTILE, 1.8f, 0.6f);
    }
}
