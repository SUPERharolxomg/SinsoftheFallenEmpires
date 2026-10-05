package com.sofe.entity.boss;

import com.sofe.gear.ranged.Spell;
import com.sofe.player.PlayerClass;
import com.sofe.skill.ClassState;
import com.sofe.story.Sin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/**
 * The powers of the six Archsins, for those who wield them all: Prython (Pride, "uses the mechanics of the six
 * before") and Nahrazel in his second phase ("all seven sins at once"). Each is a short, readable version of its
 * Archsin's own: Wrath's ring of fire, Lust's pull, Sloth's weight, Greed's coins, Gluttony's hunger for the floor,
 * Envy's copy of a Bearer's hero. Every power is told to the Bearers, in the colour of its sin.
 */
public final class SinPowers {

    private SinPowers() {
    }

    public static int color(Sin sin) {
        return switch (sin) {
            case WRATH -> 0xFF4020;
            case LUST -> 0xFF60C0;
            case GREED -> 0xFFD040;
            case SLOTH -> 0x80FF80;
            case GLUTTONY -> 0xC04030;
            case ENVY -> 0x40FF90;
            case PRIDE -> 0xB0A0FF;
        };
    }

    /** A ring of motes of the sin's colour round the one who wields it. */
    public static void aura(ServerLevel level, SoFEBossEntity boss, Sin sin, int count) {
        int c = color(sin);
        var dust = new DustParticleOptions(new Vector3f((c >> 16 & 255) / 255f, (c >> 8 & 255) / 255f, (c & 255) / 255f), 2f);
        for (int i = 0; i < count; i++) {
            double a = (boss.tickCount * 0.08) + i * Math.PI * 2 / count;
            level.sendParticles(dust, boss.getX() + Math.cos(a) * 2.5, boss.getY() + boss.getBbHeight() * 0.6, boss.getZ() + Math.sin(a) * 2.5, 1, 0, 0, 0, 0);
        }
    }

    public static void use(ServerLevel level, SoFEBossEntity boss, Sin sin, List<ServerPlayer> fighters) {
        if (fighters.isEmpty()) return;
        ServerPlayer one = fighters.get(boss.getRandom().nextInt(fighters.size()));
        switch (sin) {
            case WRATH -> {
                level.sendParticles(ParticleTypes.FLAME, boss.getX(), boss.getY() + 0.5, boss.getZ(), 120, 4, 0.2, 4, 0.08);
                for (ServerPlayer p : fighters) {
                    if (p.distanceToSqr(boss) > 7 * 7) continue;
                    p.setSecondsOnFire(4);
                    p.hurt(boss.damageSources().mobAttack(boss), 6);
                }
                level.playSound(null, boss.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2f, 0.5f);
            }
            case LUST -> {
                Vec3 pull = boss.position().subtract(one.position()).normalize().scale(1.4);
                one.setDeltaMovement(pull.x, 0.4, pull.z);
                one.hurtMarked = true;
                one.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), boss);
                level.sendParticles(ParticleTypes.HEART, one.getX(), one.getY() + 2, one.getZ(), 6, 0.3, 0.3, 0.3, 0);
            }
            case SLOTH -> {
                for (ServerPlayer p : fighters) {
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2), boss);
                    p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 80, 1), boss);
                }
                level.playSound(null, boss.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 2f, 0.4f);
            }
            case GREED -> {
                for (ServerPlayer p : fighters) {
                    for (int i = 0; i < 3; i++) BossKit.bolt(level, boss, p, Spell.HOLY, 5, 1.4f, 8);
                }
                level.playSound(null, boss.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.HOSTILE, 2f, 1.8f);
            }
            case GLUTTONY -> {
                BlockPos under = one.blockPosition().below();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        for (int dy = 0; dy >= -1; dy--) {
                            BlockPos pos = under.offset(dx, dy, dz);
                            if (!level.getBlockState(pos).isAir()) BossKit.temporary(level, pos, Blocks.AIR.defaultBlockState(), 140, boss);
                        }
                    }
                }
                boss.heal(boss.getMaxHealth() * 0.01f);
                level.playSound(null, boss.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 2.5f, 0.3f);
            }
            case ENVY -> {
                PlayerClass cls = ClassState.classOf(one).orElse(PlayerClass.KNIGHT);
                EnvyCopy copy = com.sofe.registry.EntityRegistry.ENVY_COPY.get().create(level);
                if (copy != null) {
                    copy.setup(boss, one, cls);
                    copy.moveTo(boss.getX() + 2, boss.getY(), boss.getZ() + 2, 0, 0);
                    level.addFreshEntity(copy);
                }
            }
            case PRIDE -> {
            }
        }
        int c = color(sin);
        for (ServerPlayer p : fighters) {
            p.displayClientMessage(Component.translatable("message.sofe.sin_power." + sin.id()).withStyle(s -> s.withColor(c).withBold(true)), true);
        }
    }

    /** The six whose powers Pride wields. */
    public static final Sin[] SIX = {Sin.WRATH, Sin.LUST, Sin.SLOTH, Sin.GREED, Sin.GLUTTONY, Sin.ENVY};
}
