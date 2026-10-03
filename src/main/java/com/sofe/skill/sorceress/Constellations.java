package com.sofe.skill.sorceress;

import com.sofe.combat.Constellation;
import com.sofe.combat.Rune;
import com.sofe.skill.SkillTargeting;
import com.sofe.skill.data.ClassSkillData;
import com.sofe.skill.data.SkillStats;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** The extra effect when the Sorceress completes three runes (docs/Clases.md). Values from sorceress.json. */
public final class Constellations {

    private Constellations() {
    }

    public static void trigger(ServerPlayer player, Constellation constellation, List<Rune> runes, Vec3 at, ClassSkillData data) {
        SkillStats stats = data.constellation(constellation.id()).orElse(new SkillStats(0, 0, java.util.Map.of()));
        ServerLevel level = player.serverLevel();
        // Arcane Poetry: three different runes write a stronger verse
        double poetry = runes.stream().distinct().count() == runes.size()
                ? com.sofe.skill.ClassState.passive(player, "arcane_poetry").map(p -> p.param("damage_bonus", 0.2)).orElse(0.0) : 0;
        float damage = (float) (stats.param("damage", 3) * (1 + poetry));
        // Sky Map: a completed constellation gives Mana back
        com.sofe.skill.ClassState.passive(player, "sky_map").ifPresent(p -> com.sofe.skill.ClassMechanics.gain(player, (float) p.param("mana_refund", 20)));
        double radius = stats.param("radius", 3);
        List<LivingEntity> targets = SkillTargeting.around(player, at, radius);

        // The constellation itself: a ring of stars over the impact point
        SkillTargeting.burst(level, at.add(0, 1, 0), ParticleTypes.END_ROD, 24, 0.6);

        switch (constellation) {
            case STEAM_BURST -> {
                SkillTargeting.burst(level, at, ParticleTypes.CLOUD, 40, radius / 2);
                int stun = stats.ticks("stun_s", 1.5);
                targets.forEach(t -> {
                    SkillTargeting.damage(player, t, damage);
                    t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, stun, 9)); // stunned
                    t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, stun, 1));
                });
                level.playSound(null, at.x, at.y, at.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1f, 0.8f);
            }
            case SOLAR_FLARE -> {
                SkillTargeting.burst(level, at, ParticleTypes.FLAME, 50, radius / 2);
                int burn = (int) stats.param("burn_s", 4);
                targets.forEach(t -> {
                    SkillTargeting.damage(player, t, damage);
                    t.setSecondsOnFire(burn);
                });
                level.playSound(null, at.x, at.y, at.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1f, 0.9f);
            }
            case WINTERS_GRASP -> {
                SkillTargeting.burst(level, at, ParticleTypes.SNOWFLAKE, 50, radius / 2);
                int slow = stats.ticks("slow_s", 4);
                int amplifier = (int) stats.param("slow_level", 4) - 1;
                targets.forEach(t -> {
                    SkillTargeting.damage(player, t, damage);
                    t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slow, amplifier));
                });
                level.playSound(null, at.x, at.y, at.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1f, 0.7f);
            }
            case TEMPEST_CROWN -> {
                int max = (int) stats.param("targets", 5);
                targets.stream().limit(max).forEach(t -> {
                    SkillTargeting.beam(level, at, SorceressSkills.center(t), ParticleTypes.ELECTRIC_SPARK, 0.25);
                    SkillTargeting.damage(player, t, damage);
                });
                level.playSound(null, at.x, at.y, at.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.4f, 1.6f);
            }
            case LESSER_CONSTELLATION -> {
                SkillTargeting.burst(level, at, particleFor(Constellation.majority(runes)), 25, radius / 2);
                targets.forEach(t -> SkillTargeting.damage(player, t, damage));
                level.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1f, 1.2f);
            }
        }
        player.displayClientMessage(Component.translatable("message.sofe.constellation",
                Component.translatable(constellation.translationKey())), true);
    }

    private static ParticleOptions particleFor(Rune rune) {
        return switch (rune) {
            case FIRE -> ParticleTypes.FLAME;
            case FROST -> ParticleTypes.SNOWFLAKE;
            case STORM -> ParticleTypes.ELECTRIC_SPARK;
        };
    }
}
