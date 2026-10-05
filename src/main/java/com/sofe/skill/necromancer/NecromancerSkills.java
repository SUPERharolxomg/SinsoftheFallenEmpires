package com.sofe.skill.necromancer;

import com.sofe.skill.ClassState;

import com.sofe.combat.CombatData;
import com.sofe.entity.boss.SoFEBossEntity;
import com.sofe.entity.summon.SummonedAlly;
import com.sofe.skill.ClassMechanics;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ankhareth's skills (docs/Clases.md). He binds souls rather than raising the dead: enemies that die
 * near him (or that his Threshold Touch marked) leave a soul, which Clay Wardens and the Canopic Jars spend.
 */
public final class NecromancerSkills {
    public static final String THRESHOLD_TAG = "sofe_threshold";

    /** The gates of the Great Judgment that are open: whoever dies inside rises for the Necromancer. */
    private record Gate(ServerPlayer owner, Vec3 center, double radius, long until, int riseTicks) {
    }

    private static final List<Gate> GATES = new ArrayList<>();

    private NecromancerSkills() {
    }

    private static Vec3 center(LivingEntity e) {
        return e.position().add(0, e.getBbHeight() / 2, 0);
    }

    private static List<com.sofe.entity.summon.ClayGolem> wardens(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(com.sofe.entity.summon.ClayGolem.class, player.getBoundingBox().inflate(64),
                a -> player.getUUID().equals(a.owner()));
    }

    /** Marks an enemy for this Necromancer's Threshold Touch; several Necromancers can mark the same one. */
    public static void markThreshold(LivingEntity target, ServerPlayer player) {
        String marked = target.getPersistentData().getString(THRESHOLD_TAG);
        if (!marked.contains(player.getStringUUID())) {
            target.getPersistentData().putString(THRESHOLD_TAG, marked.isEmpty() ? player.getStringUUID() : marked + "," + player.getStringUUID());
        }
    }

    /** Threshold Touch: a short beam that drains life and marks the enemy to leave its soul. */
    public static Skill.Result thresholdTouch(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, s.param("range", 8));
        SkillTargeting.beam(player.serverLevel(), SkillTargeting.origin(player), hit.point(), ParticleTypes.SOUL_FIRE_FLAME, 0.3);
        hit.target().ifPresent(t -> {
            List<LivingEntity> touched = new ArrayList<>(List.of(t));
            int chain = (int) ctx.upgradeValue("devouring_beam", "chain");
            for (int i = 0; i < chain; i++) { // Devouring Beam: the touch leaps on
                LivingEntity last = touched.get(touched.size() - 1);
                SkillTargeting.around(player, last.position(), 6).stream().filter(e -> !touched.contains(e)).findFirst().ifPresent(next -> {
                    SkillTargeting.beam(player.serverLevel(), center(last), center(next), ParticleTypes.SOUL_FIRE_FLAME, 0.3);
                    touched.add(next);
                });
            }
            float amount = (float) s.param("damage", 4);
            double slow = ctx.upgradeValue("lingering_mark", "slow_s");
            for (LivingEntity e : touched) {
                SkillTargeting.damage(player, e, amount);
                player.heal(amount * (float) s.param("drain", 0.4));
                markThreshold(e, player);
                if (slow > 0) e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (slow * 20), 1));
            }
        });
        player.level().playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.7f, 1.4f);
        return Skill.Result.at(hit.point());
    }

    /** Clay Warden: a soul bound to a clay golem that fights for a while (the oldest gives way past the maximum). */
    public static Skill.Result clayWarden(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        List<com.sofe.entity.summon.ClayGolem> current = new ArrayList<>(wardens(player));
        current.sort(Comparator.comparingInt(a -> -a.tickCount));
        while (current.size() >= (int) s.param("max_wardens", 3)) current.remove(0).discard();
        Vec3 at = player.position().add(player.getViewVector(1f).multiply(1, 0, 1).normalize().scale(1.5));
        var golem = com.sofe.entity.summon.ClayGolem.summon(player, s.ticks("duration_s", 30), at, s.param("health", 40), s.param("golem_damage", 7));
        golem.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(s.param("armor", 2)); // Scarab Shell
        player.level().playSound(null, player.blockPosition(), SoundEvents.ROOTED_DIRT_PLACE, SoundSource.PLAYERS, 1, 0.6f);
        // the more Wardens stand and the stronger they are, the longer the next one takes to shape
        int standing = wardens(player).size();
        int rank = Math.max(1, ClassState.rank(player, ctx.info().id()));
        double scale = (1 + s.param("cooldown_per_warden", 0.5) * Math.max(0, standing - 1)) * (1 + s.param("cooldown_per_rank", 0.08) * (rank - 1));
        long now = player.level().getGameTime();
        int ticks = (int) Math.round(ctx.combat().cooldowns().remaining(ctx.info().id(), now) * scale);
        ctx.combat().cooldowns().start(ctx.info().id(), now, ticks);
        return Skill.Result.at(at);
    }

    /** Raise the Embalmed: the linen-wrapped dead claw out of the ground to fight for him (the oldest give way past the maximum). */
    public static Skill.Result raiseTheEmbalmed(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        List<com.sofe.entity.summon.EmbalmedDead> raised = new ArrayList<>(player.serverLevel().getEntitiesOfClass(
                com.sofe.entity.summon.EmbalmedDead.class, player.getBoundingBox().inflate(64), d -> player.getUUID().equals(d.owner())));
        raised.sort(Comparator.comparingInt(d -> -d.tickCount));
        int count = (int) s.param("count", 2), max = (int) s.param("max_dead", 6);
        while (raised.size() + count > max && !raised.isEmpty()) raised.remove(0).discard();
        Vec3 forward = player.getViewVector(1f).multiply(1, 0, 1).normalize();
        Vec3 side = new Vec3(-forward.z, 0, forward.x);
        for (int i = 0; i < count; i++) {
            Vec3 at = player.position().add(forward.scale(2)).add(side.scale((i - (count - 1) / 2.0) * 1.4));
            com.sofe.entity.summon.EmbalmedDead.raise(player, at, s.ticks("duration_s", 20), s.param("health", 16), s.param("dead_damage", 4));
        }
        return Skill.Result.at(player.position().add(forward.scale(2)));
    }

    /** Burial Wraps: linen that binds an enemy where it stands. */
    public static Skill.Result burialWraps(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, s.param("range", 14));
        SkillTargeting.beam(player.serverLevel(), SkillTargeting.origin(player), hit.point(), ParticleTypes.WHITE_ASH, 0.25);
        hit.target().ifPresent(t -> {
            int ticks = s.ticks("root_s", 3);
            t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 10));
            t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks, 0));
            t.setDeltaMovement(0, t.getDeltaMovement().y, 0);
            double mummify = ctx.upgradeValue("mummify", "damage");
            SkillTasks.run(player, ticks, tick -> {
                if (tick % 5 == 0) player.serverLevel().sendParticles(ParticleTypes.WHITE_ASH, t.getX(), t.getY() + 1, t.getZ(), 8, 0.3, 0.6, 0.3, 0);
                if (mummify > 0 && tick % 20 == 19) SkillTargeting.damage(player, t, (float) mummify); // Mummify
                return t.isAlive();
            });
        });
        player.level().playSound(null, player.blockPosition(), SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1, 0.7f);
        return Skill.Result.at(hit.point());
    }

    /** Scales of Anubet: the enemy is judged; below a quarter of its health it dies at once (bosses only take the blow). */
    public static Skill.Result scalesOfAnubet(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, s.param("range", 16));
        hit.target().ifPresent(t -> {
            ServerLevel level = player.serverLevel();
            level.sendParticles(ParticleTypes.ENCHANT, t.getX(), t.getY() + t.getBbHeight() + 0.5, t.getZ(), 30, 0.5, 0.3, 0.5, 0.5);
            if (!(t instanceof SoFEBossEntity) && t.getHealth() <= t.getMaxHealth() * s.param("threshold", 0.25)) {
                SkillTargeting.damage(player, t, t.getHealth() + 1000);
                int rites = (int) ctx.upgradeValue("final_rites", "souls_add");
                if (rites > 0) ClassMechanics.addSouls(player, rites); // Final Rites
                level.sendParticles(ParticleTypes.SOUL, t.getX(), t.getY() + 1, t.getZ(), 20, 0.3, 0.5, 0.3, 0.05);
                level.playSound(null, t.blockPosition(), SoundEvents.WARDEN_DEATH, SoundSource.PLAYERS, 0.5f, 1.6f);
            } else {
                SkillTargeting.damage(player, t, (float) s.param("damage", 8));
            }
        });
        return Skill.Result.at(hit.point());
    }

    /** Scarab Plague: a swarm that eats an enemy for a while and jumps to the next one near. */
    public static Skill.Result scarabPlague(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, s.param("range", 16));
        if (hit.target().isEmpty()) return Skill.Result.at(hit.point());
        LivingEntity[] current = {hit.target().get()};
        Set<LivingEntity> bitten = new HashSet<>();
        int jumps = (int) s.param("jumps", 3);
        int[] left = {jumps};
        SkillTasks.run(player, s.ticks("duration_s", 6), tick -> {
            LivingEntity t = current[0];
            if (t == null) return false;
            player.serverLevel().sendParticles(ParticleTypes.SQUID_INK, t.getX(), t.getY() + 0.6, t.getZ(), 4, 0.3, 0.4, 0.3, 0);
            if (tick % 20 == 0) {
                SkillTargeting.damage(player, t, (float) s.param("damage", 3));
                double locusts = ctx.upgradeValue("locust_cloud", "slow_level");
                if (locusts > 0) t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, (int) Math.min(4, locusts)));
            }
            if (!t.isAlive() || tick % 40 == 39) {
                bitten.add(t);
                LivingEntity next = left[0]-- > 0 ? SkillTargeting.around(player, t.position(), s.param("jump_range", 6)).stream()
                        .filter(e -> !bitten.contains(e)).findFirst().orElse(null) : null;
                if (next != null) SkillTargeting.beam(player.serverLevel(), center(t), center(next), ParticleTypes.SQUID_INK, 0.4);
                current[0] = next;
            }
            return true;
        });
        player.level().playSound(null, player.blockPosition(), SoundEvents.SILVERFISH_AMBIENT, SoundSource.PLAYERS, 1, 0.8f);
        return Skill.Result.at(hit.point());
    }

    /** Canopic Jars: three souls drunk to heal the Necromancer and his Wardens. */
    public static Skill.Result canopicJars(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        float heal = (float) s.param("heal", 8);
        player.heal(heal);
        for (var warden : wardens(player)) {
            warden.heal(warden.getMaxHealth());
            player.serverLevel().sendParticles(ParticleTypes.HEART, warden.getX(), warden.getY() + 2, warden.getZ(), 3, 0.3, 0.2, 0.3, 0);
        }
        for (int i = 0; i < 3; i++) ClassMechanics.soulReleased(player);
        SkillTargeting.burst(player.serverLevel(), player.position().add(0, 1, 0), ParticleTypes.SOUL, 20, 0.5);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1, 0.8f);
        return Skill.Result.at(player.position());
    }

    /** Boat of the Dead: a spectral boat crosses the field ahead, dragging every enemy in its way. */
    public static Skill.Result boatOfTheDead(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        ServerLevel level = player.serverLevel();
        Vec3 dir = player.getViewVector(1f).multiply(1, 0, 1).normalize();
        Vec3 side = new Vec3(-dir.z, 0, dir.x);
        Vec3 start = player.position();
        int ticks = s.ticks("duration_s", 3);
        double length = s.param("distance", 16), width = s.param("width", 3);
        Set<LivingEntity> struck = new HashSet<>();
        SkillTasks.run(player, ticks, tick -> {
            Vec3 bow = start.add(dir.scale(length * tick / (double) ticks));
            for (double w = -width; w <= width; w += 0.6) {
                Vec3 p = bow.add(side.scale(w));
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y + 0.4, p.z, 1, 0.05, 0.2, 0.05, 0);
            }
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(bow, bow).inflate(width, 2, width), e -> SkillTargeting.isEnemy(player, e))) {
                e.setDeltaMovement(dir.x * 0.6, 0.15, dir.z * 0.6);
                e.hurtMarked = true;
                if (struck.add(e)) SkillTargeting.damage(player, e, (float) s.param("damage", 10));
            }
            return true;
        });
        level.playSound(null, player.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.4f, 1.4f);
        return Skill.Result.at(start.add(dir.scale(length)));
    }

    /** The Great Judgment: a gate to the underworld opens; whoever dies inside rises to fight for him. */
    public static Skill.Result greatJudgment(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        Vec3 at = SkillTargeting.aim(player, s.param("range", 20)).point();
        double radius = s.param("radius", 6);
        int ticks = s.ticks("duration_s", 12);
        GATES.add(new Gate(player, at, radius, player.level().getGameTime() + ticks, s.ticks("rise_s", 20)));
        ServerLevel level = player.serverLevel();
        SkillTasks.run(player, ticks, tick -> {
            if (tick % 4 == 0) {
                for (int i = 0; i < 16; i++) {
                    double a = i * Math.PI / 8 + tick * 0.05;
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, at.x + Math.cos(a) * radius, at.y + 0.2, at.z + Math.sin(a) * radius, 1, 0, 0.05, 0, 0);
                }
                level.sendParticles(ParticleTypes.SCULK_SOUL, at.x, at.y + 0.5, at.z, 3, radius / 2, 0.2, radius / 2, 0.02);
            }
            return true;
        });
        level.playSound(null, at.x, at.y, at.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.5f, 0.6f);
        return Skill.Result.at(at);
    }

    /** Called on every death: inside an open gate of the Judgment, the dead rise for its Necromancer. */
    public static void judgmentDeath(LivingEntity dead) {
        if (GATES.isEmpty()) return;
        long now = dead.level().getGameTime();
        GATES.removeIf(g -> g.until() < now || g.owner().isRemoved());
        for (Gate gate : GATES) {
            if (gate.owner().level() == dead.level() && dead.position().distanceTo(gate.center()) <= gate.radius()) {
                SummonedAlly.summon(gate.owner(), SummonedAlly.Kind.RISEN, gate.riseTicks(), dead.position());
                return;
            }
        }
    }

    /** How many souls the Necromancer holds (for the skills that spend them). */
    public static int souls(CombatData combat) {
        return combat.souls();
    }
}
