package com.sofe.entity.boss;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The shapes of the bosses' signature attacks (SoFEBossEntity.Signature): who stands in a frontal arc, on a line,
 * in a ring, and the particles that draw those shapes on the floor during the wind-up, so the Bearers can see
 * where the blow will land and step out of it.
 */
public final class Signatures {
    private Signatures() {
    }

    /** The direction the boss faces, flat. */
    public static Vec3 facing(LivingEntity boss) {
        return Vec3.directionFromRotation(0, boss.getYRot()).normalize();
    }

    /** The direction from the boss to a target, flat (or where it faces when there is none). */
    public static Vec3 toward(LivingEntity boss, LivingEntity target) {
        if (target == null) return facing(boss);
        Vec3 d = new Vec3(target.getX() - boss.getX(), 0, target.getZ() - boss.getZ());
        return d.lengthSqr() < 1e-4 ? facing(boss) : d.normalize();
    }

    /** Fighters within radius, inside a cone of the given width (degrees) round the direction. */
    public static List<ServerPlayer> arc(LivingEntity boss, Vec3 direction, double radius, double degrees, List<ServerPlayer> fighters) {
        List<ServerPlayer> hit = new ArrayList<>();
        double cos = Math.cos(Math.toRadians(degrees / 2));
        for (ServerPlayer p : fighters) {
            Vec3 d = new Vec3(p.getX() - boss.getX(), 0, p.getZ() - boss.getZ());
            if (d.lengthSqr() > radius * radius || Math.abs(p.getY() - boss.getY()) > 4) continue;
            if (d.lengthSqr() < 1 || d.normalize().dot(direction) >= cos) hit.add(p);
        }
        return hit;
    }

    /** Fighters within half a width of the segment from start along direction for length. */
    public static List<ServerPlayer> line(Vec3 start, Vec3 direction, double length, double width, List<ServerPlayer> fighters) {
        List<ServerPlayer> hit = new ArrayList<>();
        for (ServerPlayer p : fighters) {
            Vec3 rel = new Vec3(p.getX() - start.x, 0, p.getZ() - start.z);
            double along = rel.dot(direction);
            if (along < -1 || along > length || Math.abs(p.getY() - start.y) > 4) continue;
            if (rel.subtract(direction.scale(along)).length() <= width / 2) hit.add(p);
        }
        return hit;
    }

    /** Fighters within radius of a point (and not far above or below it). */
    public static List<ServerPlayer> ring(Vec3 center, double radius, List<ServerPlayer> fighters) {
        List<ServerPlayer> hit = new ArrayList<>();
        for (ServerPlayer p : fighters) {
            if (p.position().distanceToSqr(center.x, p.getY(), center.z) <= radius * radius && Math.abs(p.getY() - center.y) <= 4) hit.add(p);
        }
        return hit;
    }

    /** Draws a frontal arc on the floor. */
    public static void drawArc(ServerLevel level, LivingEntity boss, Vec3 direction, double radius, double degrees, ParticleOptions mote) {
        double base = Math.atan2(direction.z, direction.x);
        for (double r = 1.5; r <= radius; r += 1.5) {
            int steps = (int) Math.max(4, r * Math.toRadians(degrees));
            for (int i = 0; i <= steps; i++) {
                double a = base + Math.toRadians(-degrees / 2 + degrees * i / steps);
                level.sendParticles(mote, boss.getX() + Math.cos(a) * r, boss.getY() + 0.2, boss.getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
            }
        }
    }

    /** Draws a line on the floor. */
    public static void drawLine(ServerLevel level, Vec3 start, Vec3 direction, double length, ParticleOptions mote) {
        for (double t = 0; t <= length; t += 0.8) {
            level.sendParticles(mote, start.x + direction.x * t, start.y + 0.2, start.z + direction.z * t, 1, 0.1, 0, 0.1, 0);
        }
    }

    /** Draws a ring on the floor. */
    public static void drawRing(ServerLevel level, Vec3 center, double radius, ParticleOptions mote) {
        int steps = (int) Math.max(12, radius * 6);
        for (int i = 0; i < steps; i++) {
            double a = Math.PI * 2 * i / steps;
            level.sendParticles(mote, center.x + Math.cos(a) * radius, center.y + 0.2, center.z + Math.sin(a) * radius, 1, 0, 0, 0, 0);
        }
    }

    /** Hurts a Bearer with the boss's blow and throws them back from a point. */
    public static void strike(SoFEBossEntity boss, ServerPlayer p, float damage, Vec3 from, double knock, double lift) {
        p.hurt(boss.damageSources().mobAttack(boss), damage);
        Vec3 away = new Vec3(p.getX() - from.x, 0, p.getZ() - from.z);
        if (away.lengthSqr() > 1e-4 && knock > 0) away = away.normalize().scale(knock);
        else away = Vec3.ZERO;
        p.push(away.x, lift, away.z);
        p.hurtMarked = true;
    }
}
