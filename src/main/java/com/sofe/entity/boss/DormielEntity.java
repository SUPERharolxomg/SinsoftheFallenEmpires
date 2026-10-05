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
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Dormiel, the Dreamer, Broken Oath of Law V ("You shall keep watch while others sleep"), in the Khemet Catacombs.
 * The watchman who slept now makes others sleep: clouds of slumber drift over the crypt (heavy limbs, dark eyes,
 * weak blows), his touch drowses, and in his second phase his nightmares climb out of the dark.
 */
public class DormielEntity extends BrokenOathEntity {
    public static final String BOSS_ID = "sofe:dormiel";

    public DormielEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 260.0).add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26).add(Attributes.ARMOR, 8.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public String modelName() {
        return "dormiel";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public int law() {
        return 5;
    }

    @Override
    public String archsin() {
        return MorthisEntity.BOSS_ID;
    }

    @Override
    protected double arenaRadius() {
        return 18;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
        return hit;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        int every = phase() >= 2 ? 120 : 200;
        if (this.tickCount % every == 0) {
            for (int i = 0; i < Math.min(fighters.size(), phase() >= 2 ? 3 : 2); i++) slumber(level, fighters.get(i));
            level.playSound(null, blockPosition(), SoundEvents.PHANTOM_AMBIENT, SoundSource.HOSTILE, 1.4f, 0.5f);
        }
        if (phase() >= 2 && this.tickCount % 320 == 160) nightmares(level, fighters);
    }

    /** A cloud of slumber where a Bearer stands. */
    private void slumber(ServerLevel level, ServerPlayer target) {
        AreaEffectCloud cloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
        cloud.setOwner(this);
        cloud.setParticle(ParticleTypes.SPORE_BLOSSOM_AIR);
        cloud.setRadius(phase() >= 2 ? 3.5f : 2.5f);
        cloud.setDuration(140);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(0);
        cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
        cloud.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
        cloud.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1));
        level.addFreshEntity(cloud);
        target.displayClientMessage(Component.translatable("message.sofe.dormiel.slumber").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC), true);
    }

    /** His nightmares climb out of the crypt's dark. */
    private void nightmares(ServerLevel level, List<ServerPlayer> fighters) {
        BossKit.minions(level, this, com.sofe.registry.EntityRegistry.VOID_STALKER.get(), 2, 4, 5, fighters);
        level.playSound(null, blockPosition(), SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 0.8f, 1.2f);
    }

    @Override
    protected void onPhase(int newPhase, ServerLevel level, List<ServerPlayer> fighters) {
        nightmares(level, fighters);
    }

    // --- signature attack (SoFEBossEntity.Signature): he lifts his lantern: whoever stands in its cold light falls into the last sleep

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("dormiel_lantern", 30, 260, 12);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick % 4 == 0) Signatures.drawArc(level, this, signatureAim, 11, 70, net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        for (var p : Signatures.arc(this, signatureAim, 11, 70, fighters)) {
            Signatures.strike(this, p, signatureDamage(1.0), position(), 0, 0);
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 80, 5), this);
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 80, 0), this);
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 100, 1), this);
        }
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE, net.minecraft.sounds.SoundSource.HOSTILE, 1.6f, 0.6f);
    }
}
