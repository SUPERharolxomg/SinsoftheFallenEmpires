package com.sofe.skill.knight;

import com.sofe.skill.ClassState;
import com.sofe.skill.Skill;
import com.sofe.skill.SkillTargeting;
import com.sofe.skill.SkillTasks;
import com.sofe.skill.data.SkillStats;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/** Cassian's skills (docs/Clases.md): strike and charge, shield and protect, banner and verdict. */
public final class KnightSkills {
    public static final String BANNER_TAG = "sofe_banner_until";

    private KnightSkills() {
    }

    /** Physical damage from the Knight's own hand, so Strength and gear count. */
    private static void strike(ServerPlayer player, LivingEntity target, float amount) {
        target.invulnerableTime = 0;
        target.hurt(player.damageSources().playerAttack(player), amount);
    }

    /** Scale Strike: the more health the enemy has over you, the harder it lands. */
    public static Skill.Result scaleStrike(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, s.param("range", 4));
        hit.target().ifPresent(t -> {
            double over = Math.max(0, (t.getHealth() - player.getHealth()) / Math.max(1, player.getHealth()));
            float amount = (float) (s.param("damage", 6) * (1 + Math.min(s.param("max_bonus", 1.5), over)));
            if (t.getHealth() < t.getMaxHealth() * 0.3f) amount *= 1 + (float) ctx.upgradeValue("executioners_edge", "execute_bonus");
            strike(player, t, amount);
            if (s.param("bleed_s", 0) > 0) t.addEffect(new MobEffectInstance(com.sofe.registry.SoFEEffects.BLEEDING.get(), s.ticks("bleed_s", 0), 0), player);
            player.serverLevel().sendParticles(ParticleTypes.SWEEP_ATTACK, t.getX(), t.getY() + 1, t.getZ(), 1, 0, 0, 0, 0);
            player.serverLevel().sendParticles(ParticleTypes.WAX_OFF, t.getX(), t.getY() + 1, t.getZ(), 10, 0.3, 0.4, 0.3, 0.05);
        });
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1, 0.9f);
        return Skill.Result.at(hit.point());
    }

    /** Shield Wall: everything from the front is blocked for a moment. */
    public static Skill.Result shieldWall(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        ClassState.of(player).shieldWallUntil = player.level().getGameTime() + ctx.stats().ticks("duration_s", 2);
        player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1, 0.7f);
        int ticks = ctx.stats().ticks("duration_s", 2);
        double bash = ctx.upgradeValue("shield_bash", "damage");
        SkillTasks.run(player, ticks, tick -> {
            if (tick % 4 == 0) {
                Vec3 front = player.position().add(player.getViewVector(1f).multiply(1, 0, 1).normalize().scale(0.9));
                player.serverLevel().sendParticles(ParticleTypes.WAX_OFF, front.x, front.y + 1, front.z, 6, 0.4, 0.5, 0.4, 0);
            }
            if (bash > 0 && tick == ticks - 1) { // Shield Bash: the wall comes down on whoever stands before it
                Vec3 dir = player.getViewVector(1f).multiply(1, 0, 1).normalize();
                Vec3 at = player.position().add(dir.scale(1.8));
                for (LivingEntity e : SkillTargeting.around(player, at, 2.2)) {
                    strike(player, e, (float) bash);
                    e.knockback(1.0, -dir.x, -dir.z);
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 9));
                }
                player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.8f, 1.2f);
            }
            return true;
        });
        return Skill.Result.at(player.position());
    }

    /** Banner Cry: every enemy near turns on the Knight, and carries the banner's mark for the Verdict. */
    public static Skill.Result bannerCry(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        long until = player.level().getGameTime() + s.ticks("mark_s", 8);
        for (LivingEntity e : SkillTargeting.around(player, player.position(), s.param("radius", 8))) {
            if (e instanceof Mob mob) mob.setTarget(player);
            e.getPersistentData().putLong(BANNER_TAG, until);
            player.serverLevel().sendParticles(ParticleTypes.ANGRY_VILLAGER, e.getX(), e.getY() + e.getBbHeight() + 0.3, e.getZ(), 1, 0, 0, 0, 0);
        }
        int taunted = SkillTargeting.around(player, player.position(), s.param("radius", 8)).size();
        double resolve = ctx.upgradeValue("martyrs_resolve", "resolve_per_enemy");
        if (resolve > 0) com.sofe.skill.ClassMechanics.gain(player, (float) (resolve * taunted));
        double rally = ctx.upgradeValue("rallying_call", "strength_s");
        if (rally > 0) {
            for (net.minecraft.world.entity.player.Player ally : player.level().getEntitiesOfClass(net.minecraft.world.entity.player.Player.class,
                    player.getBoundingBox().inflate(s.param("radius", 8)))) {
                ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, (int) (rally * 20), 0));
            }
        }
        SkillTargeting.burst(player.serverLevel(), player.position().add(0, 2.2, 0), ParticleTypes.WAX_ON, 30, 0.6);
        player.level().playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 0.6f, 1.3f);
        return Skill.Result.at(player.position());
    }

    /** Legionary Charge: a rush in a straight line, knocking down whatever stands in it. */
    public static Skill.Result legionaryCharge(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        Vec3 dir = player.getViewVector(1f).multiply(1, 0, 1).normalize();
        int ticks = s.ticks("duration_s", 0.5);
        double speed = s.param("distance", 8) / ticks;
        Set<LivingEntity> hit = new HashSet<>();
        double trample = ctx.upgradeValue("trampling_charge", "damage");
        SkillTasks.run(player, ticks, tick -> {
            if (trample > 0 && tick == ticks - 1) { // Trampling Charge: the stop shakes the ground
                for (LivingEntity e : SkillTargeting.around(player, player.position(), 3)) {
                    strike(player, e, (float) trample);
                    e.setDeltaMovement(e.getDeltaMovement().add(0, 0.4, 0));
                    e.hurtMarked = true;
                }
                player.serverLevel().sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.3, player.getZ(), 2, 0.5, 0, 0.5, 0);
            }
            player.setDeltaMovement(dir.x * speed, player.getDeltaMovement().y, dir.z * speed);
            player.hurtMarked = true;
            for (LivingEntity e : SkillTargeting.around(player, player.position(), 1.8)) {
                if (!hit.add(e)) continue;
                strike(player, e, (float) s.param("damage", 6));
                e.knockback(1.2, -dir.x, -dir.z);
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, s.ticks("knockdown_s", 1.5), 5));
            }
            player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.2, player.getZ(), 3, 0.2, 0, 0.2, 0.01);
            return true;
        });
        player.level().playSound(null, player.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.5f, 1.4f);
        return Skill.Result.at(player.position());
    }

    /** Verdict: a sweeping strike round the Knight; those under his banner are stunned. */
    public static Skill.Result verdict(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        for (LivingEntity e : SkillTargeting.around(player, player.position(), s.param("radius", 4))) {
            strike(player, e, (float) s.param("damage", 8));
            if (e.getPersistentData().getLong(BANNER_TAG) > now) {
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, s.ticks("stun_s", 2), 9));
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, s.ticks("stun_s", 2), 2));
                level.sendParticles(ParticleTypes.FLASH, e.getX(), e.getY() + 1, e.getZ(), 1, 0, 0, 0, 0);
            }
        }
        for (int i = 0; i < 24; i++) {
            double a = i * Math.PI / 12;
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + Math.cos(a) * 2.5, player.getY() + 1, player.getZ() + Math.sin(a) * 2.5, 1, 0, 0, 0, 0);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.7f, 0.7f);
        double banner = ctx.upgradeValue("war_banner", "banner_s");
        if (banner > 0) { // War Banner: a standard planted where the Verdict fell
            Vec3 at = player.position();
            SkillTasks.run(player, (int) (banner * 20), tick -> {
                if (tick % 10 == 0) {
                    level.sendParticles(ParticleTypes.WAX_ON, at.x, at.y + 2.5, at.z, 4, 0.1, 0.6, 0.1, 0);
                    for (LivingEntity e : SkillTargeting.around(player, at, 4)) e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 0));
                }
                return true;
            });
        }
        return Skill.Result.at(player.position());
    }

    /** Protector's Oath: for a while, part of what hurts the Knight's allies falls on him instead. */
    public static Skill.Result protectorsOath(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        ClassState.of(player).oathUntil = player.level().getGameTime() + ctx.stats().ticks("duration_s", 6);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ctx.stats().ticks("duration_s", 6), 0));
        SkillTargeting.burst(player.serverLevel(), player.position().add(0, 1, 0), ParticleTypes.TOTEM_OF_UNDYING, 20, 0.6);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8f, 1.2f);
        return Skill.Result.at(player.position());
    }

    /** Living Rampart: a wall of light before the Knight that stops every arrow and bolt. */
    public static Skill.Result livingRampart(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        Vec3 dir = player.getViewVector(1f).multiply(1, 0, 1).normalize();
        Vec3 side = new Vec3(-dir.z, 0, dir.x);
        Vec3 mid = player.position().add(dir.scale(2.5));
        double half = s.param("width", 5) / 2;
        Vec3 a = mid.add(side.scale(half)), b = mid.subtract(side.scale(half));
        AABB box = new AABB(a, b.add(0, 3, 0)).inflate(0.6);
        ServerLevel level = player.serverLevel();
        SkillTasks.run(player, s.ticks("duration_s", 8), tick -> {
            for (Projectile p : level.getEntitiesOfClass(Projectile.class, box, p -> !(p.getOwner() instanceof net.minecraft.world.entity.player.Player))) {
                level.sendParticles(ParticleTypes.CRIT, p.getX(), p.getY(), p.getZ(), 6, 0.1, 0.1, 0.1, 0.1);
                p.discard();
            }
            if (tick % 5 == 0) {
                for (double t = 0; t <= 1; t += 0.1) {
                    Vec3 p = a.add(b.subtract(a).scale(t));
                    level.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 0.3 + (tick % 15) / 5.0, p.z, 1, 0, 0.3, 0, 0);
                }
            }
            return true;
        });
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.1f);
        return Skill.Result.at(mid);
    }

    /** Last One Standing: for a while nothing can bring the Knight below a last heartbeat, and his blows heal the line. */
    public static Skill.Result lastOneStanding(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        int ticks = ctx.stats().ticks("duration_s", 10);
        ClassState.of(player).lastStandUntil = player.level().getGameTime() + ticks;
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, true, false, true));
        SkillTasks.run(player, ticks, tick -> {
            if (tick % 10 == 0) SkillTargeting.burst(player.serverLevel(), player.position().add(0, 1.2, 0), ParticleTypes.WAX_ON, 8, 0.5);
            return true;
        });
        player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8f, 0.8f);
        return Skill.Result.at(player.position());
    }
}
