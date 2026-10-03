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

        com.sofe.skill.SkillFx.bolt(player, com.sofe.skill.SkillFx.hand(player, com.sofe.skill.SkillFx.Pose.FORWARD), hit.point(),
                ParticleTypes.FLAME, ParticleTypes.SMALL_FLAME, ParticleTypes.LAVA);
        float damage = (float) stats.param("damage", 5);
        hit.target().ifPresent(target -> {
            SkillTargeting.damage(player, target, damage);
            target.setSecondsOnFire((int) stats.param("burn_s", 3));
            int extra = (int) ctx.upgradeValue("phoenix_verse", "extra_bolts");
            SkillTargeting.around(player, target.position(), 8).stream().filter(e -> e != target).limit(extra).forEach(e -> { // Phoenix Verse
                SkillTargeting.beam(level, center(target), center(e), ParticleTypes.FLAME, 0.35);
                SkillTargeting.damage(player, e, damage * 0.6f);
                e.setSecondsOnFire((int) stats.param("burn_s", 3));
            });
        });
        double burst = ctx.upgradeValue("cinder_burst", "burst");
        if (burst > 0) { // Cinder Burst: the verse bursts where it lands
            SkillTargeting.burst(level, hit.point(), ParticleTypes.LAVA, 8, 0.6);
            for (LivingEntity e : SkillTargeting.around(player, hit.point(), 2.2)) SkillTargeting.damage(player, e, (float) (damage * burst));
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.7f, 1.3f);
        return Skill.Result.withRune(hit.point(), Rune.FIRE);
    }

    /** Frost Lance: an icicle that slows the target; leaves a Frost rune. */
    public static Skill.Result frostLance(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats stats = ctx.stats();
        ServerLevel level = player.serverLevel();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, stats.param("range", 20));

        com.sofe.skill.SkillFx.bolt(player, com.sofe.skill.SkillFx.hand(player, com.sofe.skill.SkillFx.Pose.FORWARD), hit.point(),
                ParticleTypes.SNOWFLAKE, ParticleTypes.ITEM_SNOWBALL, ParticleTypes.ITEM_SNOWBALL);
        hit.target().ifPresent(target -> {
            List<LivingEntity> pierced = new ArrayList<>(List.of(target));
            int pierce = (int) ctx.upgradeValue("glacial_spike", "pierce");
            Vec3 dir = player.getViewVector(1f);
            for (int i = 1; i <= pierce * 3 && pierced.size() <= pierce; i++) { // Glacial Spike: on through the line
                Vec3 p = center(target).add(dir.scale(i * 1.5));
                SkillTargeting.around(player, p, 1.0).stream().filter(e -> !pierced.contains(e)).findFirst().ifPresent(pierced::add);
            }
            double shatter = ctx.upgradeValue("shatter", "shatter_bonus");
            for (LivingEntity t : pierced) {
                float amount = (float) stats.param("damage", 6);
                if (shatter > 0 && t.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) amount *= 1 + (float) shatter; // Shatter
                SkillTargeting.damage(player, t, amount);
                t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, stats.ticks("slow_s", 3), (int) stats.param("slow_level", 2) - 1));
            }
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
            double shock = ctx.upgradeValue("static_field", "shock_s");
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
            if (shock > 0) { // Static Field: everything the spark touched is stunned
                for (LivingEntity e : struck) e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (shock * 20), 9));
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.3f, 1.8f);
        return Skill.Result.withRune(hit.point(), Rune.STORM);
    }

    /** Burning Calligraphy: a line of fire written on the ground; whoever crosses it burns. */
    public static Skill.Result burningCalligraphy(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats stats = ctx.stats();
        ServerLevel level = player.serverLevel();
        Vec3 dir = player.getViewVector(1f).multiply(1, 0, 1).normalize();
        Vec3 start = player.position().add(dir.scale(1.5));
        double length = stats.param("length", 8);
        // Double Stroke: a second line across the first, through its middle
        Vec3 side = new Vec3(-dir.z, 0, dir.x);
        Vec3 mid = start.add(dir.scale(length / 2));
        List<Vec3> points = new ArrayList<>();
        for (double d = 0; d <= length; d += 0.5) points.add(start.add(dir.scale(d)));
        if (ctx.upgrade("double_stroke") > 0) {
            for (double d = -length / 2; d <= length / 2; d += 0.5) points.add(mid.add(side.scale(d)));
        }
        com.sofe.skill.SkillTasks.run(player, stats.ticks("duration_s", 6), tick -> {
            for (Vec3 p : points) {
                if (tick % 4 == 0) level.sendParticles(ParticleTypes.FLAME, p.x, p.y + 0.1, p.z, 1, 0.1, 0.05, 0.1, 0.01);
                if (tick % 10 == 0) {
                    for (LivingEntity e : SkillTargeting.around(player, p, 0.9)) {
                        SkillTargeting.damage(player, e, (float) stats.param("damage", 3));
                        e.setSecondsOnFire((int) stats.param("burn_s", 3));
                    }
                }
            }
            return true;
        });
        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
        return Skill.Result.at(start.add(dir.scale(length)));
    }

    /** Water Mirror: she steps through the water ahead and leaves an ice clone to draw the enemy. */
    public static Skill.Result waterMirror(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats stats = ctx.stats();
        Vec3 from = player.position();
        double shards = ctx.upgradeValue("mirror_shards", "damage");
        if (shards > 0) { // Mirror Shards: the water left behind bursts in ice
            SkillTargeting.burst(player.serverLevel(), from.add(0, 1, 0), ParticleTypes.SNOWFLAKE, 40, 1.2);
            for (LivingEntity e : SkillTargeting.around(player, from, 3)) {
                SkillTargeting.damage(player, e, (float) shards);
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            }
        }
        var clone = com.sofe.entity.summon.SummonedAlly.summon(player, com.sofe.entity.summon.SummonedAlly.Kind.ICE_CLONE, stats.ticks("clone_s", 5), from);
        for (LivingEntity e : SkillTargeting.around(player, from, 16)) {
            if (e instanceof net.minecraft.world.entity.Mob mob) mob.setTarget(clone);
        }
        Vec3 to = SkillTargeting.aim(player, stats.param("distance", 8)).point();
        Vec3 dir = to.subtract(player.getEyePosition()).normalize();
        Vec3 land = to.subtract(dir.scale(0.8));
        SkillTargeting.burst(player.serverLevel(), from.add(0, 1, 0), ParticleTypes.SPLASH, 30, 0.5);
        player.teleportTo(land.x, Math.max(land.y, player.getY()), land.z);
        SkillTargeting.burst(player.serverLevel(), player.position().add(0, 1, 0), ParticleTypes.SPLASH, 30, 0.5);
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.6f, 1.4f);
        return Skill.Result.at(player.position());
    }

    /** Petal Tempest: a whirl of petals and cutting wind around her. */
    public static Skill.Result petalTempest(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats stats = ctx.stats();
        ServerLevel level = player.serverLevel();
        double radius = stats.param("radius", 4);
        com.sofe.skill.SkillTasks.run(player, stats.ticks("duration_s", 4), tick -> {
            for (int i = 0; i < 6; i++) {
                double a = tick * 0.35 + i * Math.PI / 3;
                level.sendParticles(ParticleTypes.CHERRY_LEAVES, player.getX() + Math.cos(a) * radius * 0.8, player.getY() + 1 + (i % 3) * 0.4,
                        player.getZ() + Math.sin(a) * radius * 0.8, 1, 0, 0, 0, 0);
            }
            if (tick % 10 == 0) {
                for (LivingEntity e : SkillTargeting.around(player, player.position(), radius)) {
                    SkillTargeting.damage(player, e, (float) stats.param("damage", 3));
                    Vec3 away = e.position().subtract(player.position()).normalize().scale(0.3);
                    e.setDeltaMovement(e.getDeltaMovement().add(away.x, 0.1, away.z));
                    e.hurtMarked = true;
                }
            }
            return true;
        });
        level.playSound(null, player.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.5f, 1.8f);
        return Skill.Result.at(player.position());
    }

    /** Starfall: stars rain on the marked ground, one after another. */
    public static Skill.Result starfall(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats stats = ctx.stats();
        ServerLevel level = player.serverLevel();
        Vec3 at = SkillTargeting.aim(player, stats.param("range", 24)).point();
        int stars = (int) stats.param("stars", 7);
        double radius = stats.param("radius", 5);
        int every = 6;
        com.sofe.skill.SkillTasks.run(player, stars * every + 1, tick -> {
            if (tick % every != 0) return true;
            double a = player.getRandom().nextDouble() * Math.PI * 2, r = Math.sqrt(player.getRandom().nextDouble()) * radius;
            Vec3 hit = at.add(Math.cos(a) * r, 0, Math.sin(a) * r);
            SkillTargeting.beam(level, hit.add(0, 10, 0), hit, ParticleTypes.END_ROD, 0.6);
            SkillTargeting.burst(level, hit, ParticleTypes.FIREWORK, 12, 0.6);
            for (LivingEntity e : SkillTargeting.around(player, hit, stats.param("star_radius", 2.5))) {
                SkillTargeting.damage(player, e, (float) stats.param("damage", 8));
            }
            level.playSound(null, hit.x, hit.y, hit.z, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 0.7f, 0.8f);
            return true;
        });
        return Skill.Result.at(at);
    }

    /** Written Eclipse: the sky darkens; for a while every spell is free and leaves two runes. */
    public static Skill.Result writtenEclipse(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        int ticks = ctx.stats().ticks("duration_s", 10);
        com.sofe.skill.ClassState.of(player).eclipseUntil = player.level().getGameTime() + ticks;
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, ticks + 40, 0, true, false, true));
        com.sofe.skill.SkillTasks.run(player, ticks, tick -> {
            if (tick % 5 == 0) SkillTargeting.burst(player.serverLevel(), player.position().add(0, 2.5, 0), ParticleTypes.END_ROD, 6, 0.8);
            return true;
        });
        for (ServerPlayer near : player.serverLevel().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(48))) {
            near.addEffect(new MobEffectInstance(MobEffects.DARKNESS, Math.min(ticks, 60), 0, true, false, false));
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.3f, 1.6f);
        return Skill.Result.at(player.position());
    }

    static Vec3 center(LivingEntity entity) {
        return entity.position().add(0, entity.getBbHeight() / 2, 0);
    }
}
