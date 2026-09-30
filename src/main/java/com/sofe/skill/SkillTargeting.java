package com.sofe.skill;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Aiming, area searches, damage and particle lines shared by the skills. */
public final class SkillTargeting {

    private SkillTargeting() {
    }

    public record Hit(Optional<LivingEntity> target, Vec3 point) {
    }

    /**
     * Enemies a Bearer's skills can hit: living, not a player (co-op, no friendly fire), not
     * allied or tamed by the caster.
     */
    public static boolean isEnemy(ServerPlayer caster, Entity entity) {
        return entity instanceof LivingEntity living
                && living.isAlive()
                && entity != caster
                && !(entity instanceof Player)
                && !entity.isSpectator()
                && !living.isAlliedTo(caster);
    }

    /** The first enemy along the player's view, stopped by blocks; the point is where it lands. */
    public static Hit aim(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1f);
        Vec3 end = eye.add(look.scale(range));
        var block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) {
            end = block.getLocation();
        }
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1);
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(player.level(), player, eye, end, box, e -> isEnemy(player, e), 0.3f);
        if (entity != null && entity.getEntity() instanceof LivingEntity living) {
            return new Hit(Optional.of(living), living.position().add(0, living.getBbHeight() / 2, 0));
        }
        return new Hit(Optional.empty(), end);
    }

    /** Enemies within the radius, closest first. */
    public static List<LivingEntity> around(ServerPlayer caster, Vec3 center, double radius) {
        return caster.level().getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius),
                        e -> isEnemy(caster, e) && e.position().distanceTo(center) <= radius)
                .stream()
                .sorted(Comparator.comparingDouble(e -> e.position().distanceToSqr(center)))
                .toList();
    }

    /** Magic damage credited to the player. Resets hit immunity so a spell and its constellation both land. */
    public static void damage(ServerPlayer caster, LivingEntity target, float amount) {
        target.invulnerableTime = 0;
        target.hurt(caster.damageSources().indirectMagic(caster, caster), amount);
    }

    /** A line of particles, seen by everyone nearby. */
    public static void beam(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle, double spacing) {
        Vec3 step = to.subtract(from);
        int points = Math.max(1, (int) (step.length() / spacing));
        for (int i = 0; i <= points; i++) {
            Vec3 p = from.add(step.scale(i / (double) points));
            level.sendParticles(particle, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0);
        }
    }

    public static void burst(ServerLevel level, Vec3 at, ParticleOptions particle, int count, double spread) {
        level.sendParticles(particle, at.x, at.y, at.z, count, spread, spread, spread, 0.02);
    }

    /** Where spells start: a little in front of the player's eyes. */
    public static Vec3 origin(ServerPlayer player) {
        return player.getEyePosition().add(player.getViewVector(1f).scale(0.6)).add(0, -0.2, 0);
    }
}
