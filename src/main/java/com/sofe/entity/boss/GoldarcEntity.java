package com.sofe.entity.boss;

import com.sofe.gear.ranged.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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

import java.util.List;

/**
 * Goldarc, the Coinlord, Broken Oath of Law VI ("You shall not hoard what another needs"), in the Aureum Treasury.
 * He hoards even blows: volleys of gold coins fly at the Bearers, and now and then he raises a ward of gold that
 * swallows damage until it breaks. In his second phase the volleys and the ward grow.
 */
public class GoldarcEntity extends BrokenOathEntity {
    public static final String BOSS_ID = "sofe:goldarc";
    private float ward;

    public GoldarcEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 280.0).add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ARMOR, 14.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    public String modelName() {
        return "goldarc";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public int law() {
        return 6;
    }

    @Override
    public String archsin() {
        return AvarokEntity.BOSS_ID;
    }

    @Override
    protected double arenaRadius() {
        return 16;
    }

    public float ward() {
        return ward;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        int volley = phase() >= 2 ? 50 : 80;
        if (this.tickCount % volley == 0) {
            for (ServerPlayer p : fighters) {
                for (int i = 0; i < (phase() >= 2 ? 3 : 2); i++) BossKit.bolt(level, this, p, Spell.HOLY, phase() >= 2 ? 6 : 4, 1.4f, 6);
            }
            level.playSound(null, blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.HOSTILE, 1.2f, 1.6f);
        }
        if (this.tickCount % 300 == 150 && ward <= 0) {
            ward = phase() >= 2 ? 40 : 25;
            level.playSound(null, blockPosition(), SoundEvents.ARMOR_EQUIP_GOLD, SoundSource.HOSTILE, 1.5f, 0.7f);
            fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.goldarc.ward").withStyle(ChatFormatting.GOLD), true));
        }
        if (ward > 0 && this.tickCount % 5 == 0) {
            level.sendParticles(ParticleTypes.WAX_ON, getX(), getY() + 1, getZ(), 6, 0.7, 1, 0.7, 0.02);
        }
    }

    /** The ward of gold takes the damage first. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (ward > 0 && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            float taken = Math.min(ward, amount);
            ward -= taken;
            amount -= taken;
            if (ward <= 0 && level() instanceof ServerLevel level) {
                level.playSound(null, blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.HOSTILE, 1.5f, 0.6f);
            }
            if (amount <= 0) return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void onReset() {
        ward = 0;
    }

    // --- signature attack (SoFEBossEntity.Signature): he lowers his shield of coins and charges in a straight line, trampling all in his way

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("goldarc_charge", 20, 220, 14);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick % 3 == 0) Signatures.drawLine(level, position(), signatureAim, 12, net.minecraft.core.particles.ParticleTypes.WAX_ON);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        var start = position();
        for (var p : Signatures.line(start, signatureAim, 12, 2.6, fighters)) Signatures.strike(this, p, signatureDamage(2.0), start, 2.0, 0.5);
        double run = 11;
        if (target != null) run = Math.min(run, Math.max(0, start.distanceTo(target.position()) - 1.5));
        moveTo(start.x + signatureAim.x * run, start.y, start.z + signatureAim.z * run, getYRot(), getXRot());
        Signatures.drawLine(level, start, signatureAim, run, net.minecraft.core.particles.ParticleTypes.CRIT);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.RAVAGER_ATTACK, net.minecraft.sounds.SoundSource.HOSTILE, 1.6f, 0.8f);
    }
}
