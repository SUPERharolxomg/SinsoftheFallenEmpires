package com.sofe.entity.boss;

import com.sofe.gear.ranged.Spell;
import com.sofe.story.Sin;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Morthis, the Archsin of Sloth, in the Stagnant Marsh of Khemet: slows, and summons minions without moving. He
 * never leaves his throne on the terrace; the air round him is thick (slowness, and in his second phase heavy
 * hands too), drowsy bolts drift from him, and the marsh gives up its mummies to fight for him.
 */
public class MorthisEntity extends ArchsinEntity {
    public static final String BOSS_ID = "sofe:morthis";
    public static final double AURA = 14;

    public MorthisEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 480.0).add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.ARMOR, 14.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public String modelName() {
        return "morthis";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public Sin sin() {
        return Sin.SLOTH;
    }

    @Override
    protected String introDialogue() {
        return "sofe:act3/morthis_temptation";
    }

    @Override
    protected boolean fightsInMelee() {
        return false;
    }

    @Override
    protected double arenaRadius() {
        return 18;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        if (this.tickCount % 20 == 0) {
            for (ServerPlayer p : fighters) {
                if (p.distanceToSqr(this) > AURA * AURA) continue;
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, phase() >= 2 ? 2 : 1, false, true), this);
                if (phase() >= 2) p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 30, 1, false, true), this);
            }
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, getX(), getY() + 1, getZ(), 20, AURA / 3, 1, AURA / 3, 0);
        }
        if (this.tickCount % 30 == 15) { // whoever comes close enough to strike him is struck back
            for (ServerPlayer p : fighters) if (p.distanceToSqr(this) < 4 * 4) doHurtTarget(p);
        }
        if (this.tickCount % (phase() >= 2 ? 40 : 60) == 0) {
            ServerPlayer target = fighters.get(random.nextInt(fighters.size()));
            getLookControl().setLookAt(target);
            BossKit.bolt(level, this, target, Spell.SOUL, phase() >= 2 ? 9 : 7, 0.9f, 2);
            level.playSound(null, blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.5f, 0.5f);
        }
        if (this.tickCount % (phase() >= 2 ? 200 : 280) == 100) {
            BossKit.minions(level, this, com.sofe.registry.EntityRegistry.BOG_MUMMY.get(), 2, phase() >= 2 ? 6 : 4, 5, fighters);
            level.playSound(null, blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 1.2f, 0.4f);
            fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.morthis.risen").withStyle(ChatFormatting.DARK_GREEN), true));
        }
    }

    /** Morthis does not move: no knockback, no stepping, never pulled away from his throne. */
    @Override
    public void knockback(double strength, double x, double z) {
    }

    // --- signature attack (SoFEBossEntity.Signature): hands of mud rise from the marsh where each Bearer stands, and drag them down

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;
    private final java.util.List<net.minecraft.world.phys.Vec3> signatureMarks = new java.util.ArrayList<>();

    @Override
    protected Signature signature() {
        return new Signature("morthis_grasp", 35, 260, 20);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick == 1) {
            signatureMarks.clear();
            for (var p : fighters) signatureMarks.add(p.position());
        }
        if (tick % 4 == 0) for (var at : signatureMarks) Signatures.drawRing(level, at, 2.5, net.minecraft.core.particles.ParticleTypes.MYCELIUM);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        for (var at : signatureMarks) {
            for (var p : Signatures.ring(at, 2.5, fighters)) {
                Signatures.strike(this, p, signatureDamage(1.6), at, 0, -0.3);
                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 4), this);
            }
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.FALLING_SPORE_BLOSSOM, at.x, at.y + 1, at.z, 20, 1, 0.6, 1, 0);
        }
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.MUD_BREAK, net.minecraft.sounds.SoundSource.HOSTILE, 2.0f, 0.5f);
    }
}
