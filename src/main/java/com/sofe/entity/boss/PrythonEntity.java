package com.sofe.entity.boss;

import com.sofe.quest.DialogueService;
import com.sofe.story.Sin;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Prython, the Archsin of Pride, on the Celestial Spire above Sulthari: he uses the mechanics of the six before
 * (README). He wields their sins one after another, faster in his second phase, when his own Pride turns part of
 * every blow back; at the edge of defeat he offers each Bearer the rule of Aetheris for the six fragments, and they
 * must refuse.
 */
public class PrythonEntity extends ArchsinEntity {
    public static final String BOSS_ID = "sofe:prython";
    public static final float PRIDE_REFLECT = 0.2f;
    private int sinIndex;
    private final Set<UUID> offered = new HashSet<>();

    public PrythonEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 700.0).add(Attributes.ATTACK_DAMAGE, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.29).add(Attributes.ARMOR, 18.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public String modelName() {
        return "prython";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public Sin sin() {
        return Sin.PRIDE;
    }

    @Override
    protected String introDialogue() {
        return "sofe:act5/prython_temptation";
    }

    @Override
    protected double arenaRadius() {
        return 16;
    }

    public Sin currentSin() {
        return SinPowers.SIX[sinIndex % SinPowers.SIX.length];
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        if (this.tickCount % 4 == 0) SinPowers.aura(level, this, currentSin(), 6);
        if (this.tickCount % (phase() >= 2 ? 100 : 160) == 0) {
            SinPowers.use(level, this, currentSin(), fighters);
            sinIndex++;
        }
        if (getHealth() < getMaxHealth() * 0.12f) {
            for (ServerPlayer p : fighters) {
                if (offered.add(p.getUUID())) DialogueService.open(p, "sofe:act5/prythons_offer", null);
            }
        }
    }

    /** His Pride: in the second phase a fifth of every blow goes back to the one who struck. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && phase() >= 2 && source.getDirectEntity() instanceof ServerPlayer striker
                && !source.is(net.minecraft.world.damagesource.DamageTypes.THORNS)) {
            striker.hurt(damageSources().thorns(this), amount * PRIDE_REFLECT);
        }
        return hurt;
    }

    @Override
    protected void onPhase(int newPhase, ServerLevel level, List<ServerPlayer> fighters) {
        level.playSound(null, blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1f, 1.4f);
    }

    @Override
    protected void onFightOver(ServerLevel level, boolean defeated) {
        level.getEntitiesOfClass(EnvyCopy.class, getBoundingBox().inflate(48), c -> getStringUUID().equals(c.maker())).forEach(EnvyCopy::discard);
        offered.clear();
        sinIndex = 0;
    }

    // --- signature attack (SoFEBossEntity.Signature): he rises on his wings and falls like the sun on the place he marked, his sword first

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("prython_fall", 40, 280, 18);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick == 1) signatureAim = target != null ? target.position() : position();
        if (tick % 3 == 0) Signatures.drawRing(level, signatureAim, 4.5, net.minecraft.core.particles.ParticleTypes.END_ROD);
        if (tick == 12) setDeltaMovement(0, 1.4, 0);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        var at = signatureAim;
        moveTo(at.x, at.y, at.z, getYRot(), getXRot());
        for (var p : Signatures.ring(at, 4.5, fighters)) Signatures.strike(this, p, signatureDamage(2.6), at, 1.6, 0.8);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER, at.x, at.y + 0.5, at.z, 1, 0, 0, 0, 0);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, at.x, at.y + 0.5, at.z, 60, 2.5, 0.5, 2.5, 0.2);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.TRIDENT_THUNDER, net.minecraft.sounds.SoundSource.HOSTILE, 2.0f, 0.8f);
    }
}
