package com.sofe.skill.sorceress;

import com.sofe.combat.Rune;
import com.sofe.skill.Skill;
import com.sofe.skill.SkillTargeting;
import com.sofe.skill.data.SkillStats;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Shirin's level-1 spells (docs/Clases.md). Each leaves a rune; three runes form a constellation. */
public final class SorceressSkills {

    private SorceressSkills() {
    }

    /** Ember Verse: a fire bolt that sets the target on fire; leaves a Fire rune. */
    public static Skill.Result emberVerse(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats stats = ctx.stats();
        ServerLevel level = player.serverLevel();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, stats.param("range", 18));

        SkillTargeting.beam(level, SkillTargeting.origin(player), hit.point(), ParticleTypes.FLAME, 0.35);
        SkillTargeting.burst(level, hit.point(), ParticleTypes.LAVA, 4, 0.2);
        hit.target().ifPresent(target -> {
            SkillTargeting.damage(player, target, (float) stats.param("damage", 5));
            target.setSecondsOnFire((int) stats.param("burn_s", 3));
        });
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.7f, 1.3f);
        return Skill.Result.withRune(hit.point(), Rune.FIRE);
    }

    /** Frost Lance: an icicle that slows the target; leaves a Frost rune. */
    public static Skill.Result frostLance(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats stats = ctx.stats();
        ServerLevel level = player.serverLevel();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, stats.param("range", 20));

        SkillTargeting.beam(level, SkillTargeting.origin(player), hit.point(), ParticleTypes.SNOWFLAKE, 0.3);
        SkillTargeting.burst(level, hit.point(), ParticleTypes.ITEM_SNOWBALL, 8, 0.25);
        hit.target().ifPresent(target -> {
            SkillTargeting.damage(player, target, (float) stats.param("damage", 6));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, stats.ticks("slow_s", 3),
                    (int) stats.param("slow_level", 2) - 1));
        });
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.8f, 1.4f);
        return Skill.Result.withRune(hit.point(), Rune.FROST);
    }

    /** Wandering Spark: lightning that hits the target and bounces to nearby enemies; leaves a Storm rune. */
    public static Skill.Result wanderingSpark(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats stats = ctx.stats();
        ServerLevel level = player.serverLevel();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, stats.param("range", 16));
        float damage = (float) stats.param("damage", 4);

        Vec3 from = SkillTargeting.origin(player);
        SkillTargeting.beam(level, from, hit.point(), ParticleTypes.ELECTRIC_SPARK, 0.25);
        if (hit.target().isPresent()) {
            List<LivingEntity> struck = new ArrayList<>();
            LivingEntity current = hit.target().get();
            SkillTargeting.damage(player, current, damage);
            struck.add(current);
            int bounces = (int) stats.param("bounces", 2);
            for (int i = 0; i < bounces; i++) {
                Vec3 at = center(current);
                LivingEntity next = SkillTargeting.around(player, at, stats.param("bounce_range", 6)).stream()
                        .filter(e -> !struck.contains(e)).findFirst().orElse(null);
                if (next == null) break;
                SkillTargeting.beam(level, at, center(next), ParticleTypes.ELECTRIC_SPARK, 0.25);
                SkillTargeting.damage(player, next, damage);
                struck.add(next);
                current = next;
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.3f, 1.8f);
        return Skill.Result.withRune(hit.point(), Rune.STORM);
    }

    static Vec3 center(LivingEntity entity) {
        return entity.position().add(0, entity.getBbHeight() / 2, 0);
    }
}
