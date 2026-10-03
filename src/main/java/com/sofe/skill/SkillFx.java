package com.sofe.skill;

import com.sofe.network.CastPosePacket;
import com.sofe.network.SoFENetwork;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

/**
 * How casting looks: the Bearer's arms move to a pose (both hands thrown forward to hurl a bolt, raised
 * overhead to call something down), the hand glows with the spell's element, and bolts fly from the hand to
 * where they land instead of appearing at once. The effects are seen by everyone near.
 */
public final class SkillFx {
    public enum Pose { FORWARD, OVERHEAD }

    /** The pose of each skill that has one; the others keep the plain swing. */
    private static final Map<String, Pose> POSES = Map.ofEntries(
            Map.entry("ember_verse", Pose.FORWARD), Map.entry("frost_lance", Pose.FORWARD), Map.entry("wandering_spark", Pose.FORWARD),
            Map.entry("burning_calligraphy", Pose.FORWARD), Map.entry("petal_tempest", Pose.OVERHEAD), Map.entry("starfall", Pose.OVERHEAD),
            Map.entry("written_eclipse", Pose.OVERHEAD), Map.entry("water_mirror", Pose.FORWARD),
            Map.entry("threshold_touch", Pose.FORWARD), Map.entry("burial_wraps", Pose.FORWARD), Map.entry("scarab_plague", Pose.FORWARD),
            Map.entry("scales_of_anubet", Pose.FORWARD), Map.entry("clay_warden", Pose.OVERHEAD), Map.entry("raise_the_embalmed", Pose.OVERHEAD),
            Map.entry("canopic_jars", Pose.OVERHEAD), Map.entry("boat_of_the_dead", Pose.FORWARD), Map.entry("the_great_judgment", Pose.OVERHEAD),
            Map.entry("banner_cry", Pose.OVERHEAD), Map.entry("last_one_standing", Pose.OVERHEAD),
            Map.entry("decree_of_steadfastness", Pose.OVERHEAD), Map.entry("siege_decree", Pose.FORWARD), Map.entry("command", Pose.FORWARD),
            Map.entry("royal_treasury", Pose.OVERHEAD), Map.entry("crown_of_the_five_lands", Pose.OVERHEAD), Map.entry("janissary_guard", Pose.OVERHEAD));

    /** The glow in the hand while casting, by skill. */
    private static final Map<String, ParticleOptions> GLOW = Map.ofEntries(
            Map.entry("ember_verse", ParticleTypes.FLAME), Map.entry("burning_calligraphy", ParticleTypes.FLAME),
            Map.entry("frost_lance", ParticleTypes.SNOWFLAKE), Map.entry("water_mirror", ParticleTypes.SPLASH),
            Map.entry("wandering_spark", ParticleTypes.ELECTRIC_SPARK), Map.entry("starfall", ParticleTypes.END_ROD),
            Map.entry("petal_tempest", ParticleTypes.CHERRY_LEAVES), Map.entry("written_eclipse", ParticleTypes.END_ROD));

    private SkillFx() {
    }

    /** Ticks a pose is held. */
    public static final int POSE_TICKS = 12;

    /** Moves the caster's arms for this skill (seen by everyone, the caster included) and lights the hand. */
    public static void cast(ServerPlayer player, String skill) {
        Pose pose = POSES.get(skill);
        if (pose == null) return;
        CastPosePacket packet = new CastPosePacket(player.getId(), pose.ordinal(), POSE_TICKS);
        SoFENetwork.sendTo(player, packet);
        SoFENetwork.sendToTracking(player, packet);
        ParticleOptions glow = GLOW.get(skill);
        if (glow != null) {
            ServerLevel level = player.serverLevel();
            Vec3 hand = hand(player, pose);
            SkillTasks.run(player, 6, tick -> {
                double a = tick * 1.1;
                level.sendParticles(glow, hand.x + Math.cos(a) * 0.25, hand.y + Math.sin(a) * 0.15, hand.z + Math.sin(a) * 0.25, 2, 0.02, 0.02, 0.02, 0);
                return true;
            });
        }
    }

    /** Where the casting hands are: before the chest for a throw, above the head for a call. */
    public static Vec3 hand(ServerPlayer player, Pose pose) {
        Vec3 look = player.getViewVector(1f);
        return pose == Pose.OVERHEAD ? player.position().add(0, player.getBbHeight() + 0.5, 0).add(look.scale(0.3))
                : player.getEyePosition().add(look.scale(0.8)).add(0, -0.35, 0);
    }

    /**
     * A bolt that flies from the hand to where it lands, three blocks a tick: a bright head and a trail, then a
     * burst on impact. Damage is the skill's to deal; this is only how it looks.
     */
    public static void bolt(ServerPlayer player, Vec3 from, Vec3 to, ParticleOptions head, ParticleOptions trail, ParticleOptions impact) {
        ServerLevel level = player.serverLevel();
        Vec3 path = to.subtract(from);
        double length = path.length();
        int ticks = Math.max(1, (int) Math.ceil(length / 3.0));
        SkillTasks.run(player, ticks + 1, tick -> {
            if (tick >= ticks) {
                level.sendParticles(impact, to.x, to.y, to.z, 14, 0.25, 0.25, 0.25, 0.05);
                return false;
            }
            double t0 = tick / (double) ticks, t1 = (tick + 1) / (double) ticks;
            for (double t = t0; t <= t1; t += 0.25 / Math.max(1, length)) {
                Vec3 p = from.add(path.scale(t));
                level.sendParticles(trail, p.x, p.y, p.z, 1, 0.03, 0.03, 0.03, 0);
            }
            Vec3 h = from.add(path.scale(t1));
            level.sendParticles(head, h.x, h.y, h.z, 4, 0.06, 0.06, 0.06, 0.01);
            return true;
        });
    }
}
