package com.sofe.entity.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Mirael, the Whisperer, Broken Oath of Law III ("You shall not take a heart that is not given to you"), in the
 * Parsivan Baths. She takes hearts by deceit: she fades and comes back behind a Bearer, her touch dizzies, and her
 * whisper fills the bath with confusion and dark. In her second phase two illusions of her dance with her.
 */
public class MiraelEntity extends BrokenOathEntity {
    public static final String BOSS_ID = "sofe:mirael";

    public MiraelEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 240.0).add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.31).add(Attributes.ARMOR, 6.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public String modelName() {
        return "mirael";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public int law() {
        return 3;
    }

    @Override
    public String archsin() {
        return LuxaraEntity.BOSS_ID;
    }

    @Override
    protected double arenaRadius() {
        return 16;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0), this);
        return hit;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        int fade = phase() >= 2 ? 120 : 180;
        if (this.tickCount % fade == 0) fadeAndReturn(level, fighters);
        if (this.tickCount % 240 == 120) whisper(level, fighters);
        if (phase() >= 2 && this.tickCount % 300 == 0) {
            BossKit.minions(level, this, com.sofe.registry.EntityRegistry.MIRAGE_DANCER.get(), 2, 2, 4, fighters)
                    .forEach(m -> m.setCustomName(getDisplayName().copy().withStyle(ChatFormatting.LIGHT_PURPLE)));
        }
    }

    /** She fades from sight and comes back behind one of them. */
    private void fadeAndReturn(ServerLevel level, List<ServerPlayer> fighters) {
        ServerPlayer target = fighters.get(random.nextInt(fighters.size()));
        level.sendParticles(ParticleTypes.WITCH, getX(), getY() + 1, getZ(), 30, 0.4, 0.8, 0.4, 0.05);
        addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, phase() >= 2 ? 60 : 40, 0, false, false));
        Vec3 behind = target.position().subtract(Vec3.directionFromRotation(0, target.getYRot()).scale(2));
        randomTeleport(behind.x, target.getY(), behind.z, true);
        setTarget(target);
        level.playSound(null, blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.HOSTILE, 1.2f, 0.9f);
    }

    /** The whisper: confusion and a moment of dark for everyone near her. */
    private void whisper(ServerLevel level, List<ServerPlayer> fighters) {
        level.playSound(null, blockPosition(), SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundSource.HOSTILE, 1.5f, 0.5f);
        for (ServerPlayer p : fighters) {
            if (p.distanceToSqr(this) > 12 * 12) continue;
            p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0), this);
            p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0), this);
            p.displayClientMessage(Component.translatable("message.sofe.mirael.whisper").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC), true);
        }
    }

    @Override
    protected void onPhase(int newPhase, ServerLevel level, List<ServerPlayer> fighters) {
        BossKit.minions(level, this, com.sofe.registry.EntityRegistry.MIRAGE_DANCER.get(), 2, 2, 4, fighters)
                .forEach(m -> m.setCustomName(getDisplayName().copy().withStyle(ChatFormatting.LIGHT_PURPLE)));
    }

    // --- signature attack (SoFEBossEntity.Signature): her veils lash out round her and draw every Bearer in, blind

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("mirael_veil", 25, 240, 10);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick % 4 == 0) Signatures.drawRing(level, position(), 9, net.minecraft.core.particles.ParticleTypes.WITCH);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        for (var p : Signatures.ring(position(), 9, fighters)) {
            Signatures.strike(this, p, signatureDamage(1.2), position(), -1.3, 0.2);
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 60, 0), this);
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, 100, 0), this);
        }
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.ELYTRA_FLYING, net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.4f);
    }
}
