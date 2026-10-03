package com.sofe.skill.king;

import com.sofe.entity.summon.BronzeCannon;
import com.sofe.entity.summon.SummonedAlly;
import com.sofe.registry.ItemRegistry;
import com.sofe.skill.ClassMechanics;
import com.sofe.skill.ClassState;
import com.sofe.skill.Skill;
import com.sofe.skill.SkillTargeting;
import com.sofe.skill.SkillTasks;
import com.sofe.skill.data.SkillStats;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import java.util.List;

/**
 * Azhar's skills (docs/Clases.md). Decrees are zones that change the rules of a fight while one stands in
 * them; only one Decree stands at a time (the Crown of the Five Lands keeps them all). Authority comes from
 * his scepter, kills and healing.
 */
public final class KingSkills {
    public static final String STEADFAST = "steadfastness", SIEGE = "siege";

    private KingSkills() {
    }

    private static List<SummonedAlly> guard(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(SummonedAlly.class, player.getBoundingBox().inflate(64),
                a -> player.getUUID().equals(a.owner()) && a.kind() == SummonedAlly.Kind.JANISSARY);
    }

    /** Scepter Slash: a cut of the scimitar-scepter; every hit raises his Authority. */
    public static Skill.Result scepterSlash(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, s.param("range", 3.5));
        hit.target().ifPresent(t -> {
            t.invulnerableTime = 0;
            t.hurt(player.damageSources().playerAttack(player), (float) s.param("damage", 5));
            double sweep = ctx.upgradeValue("sweeping_scepter", "sweep"); // Sweeping Scepter
            if (sweep > 0) {
                for (LivingEntity e : SkillTargeting.around(player, t.position(), 2.5)) {
                    if (e == t) continue;
                    e.invulnerableTime = 0;
                    e.hurt(player.damageSources().playerAttack(player), (float) (s.param("damage", 5) * sweep));
                }
            }
            ClassMechanics.gain(player, (float) s.param("authority", 6));
            player.serverLevel().sendParticles(ParticleTypes.WAX_ON, t.getX(), t.getY() + 1, t.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
        });
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1, 1.1f);
        return Skill.Result.at(hit.point());
    }

    /** The radius and length of a Decree, with the Voice of the Throne if he has it. */
    private static double[] reach(ServerPlayer player, SkillStats s) {
        double radius = s.param("radius", 5), seconds = s.param("duration_s", 10);
        var voice = ClassState.passive(player, "voice_of_the_throne");
        if (voice.isPresent()) {
            radius *= voice.get().param("radius_multiplier", 2);
            seconds *= voice.get().param("duration_multiplier", 1.5);
        }
        return new double[]{radius, seconds};
    }

    /** Lays down a Decree (replacing the one that stood) and runs it for its time. */
    private static void decree(ServerPlayer player, String kind, Vec3 at, double radius, int ticks, boolean follow) {
        ClassState state = ClassState.of(player);
        state.decree = kind;
        state.decreeCenter = at;
        state.decreeRadius = radius;
        long until = player.level().getGameTime() + ticks;
        state.decreeUntil = until;
        ServerLevel level = player.serverLevel();
        SkillTasks.run(player, ticks, tick -> {
            if (!kind.equals(state.decree) || state.decreeUntil != until) return false; // a newer Decree stands now
            Vec3 c = follow ? player.position() : at;
            if (tick % 10 == 0) apply(player, kind, c, radius);
            if (tick % 5 == 0) ring(level, c, radius, kind.equals(SIEGE) ? ParticleTypes.FLAME : ParticleTypes.WAX_ON);
            return true;
        });
    }

    static void apply(ServerPlayer player, String kind, Vec3 c, double radius) {
        AABB box = new AABB(c, c).inflate(radius, 3, radius);
        if (kind.equals(STEADFAST)) {
            boolean iron = ClassState.rank(player, "iron_decree") > 0;
            for (LivingEntity ally : player.level().getEntitiesOfClass(LivingEntity.class, box, e -> (e instanceof Player || e instanceof com.sofe.entity.summon.Ally)
                    && e.position().distanceTo(c) <= radius)) {
                ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 0, true, false, true));
                if (iron) ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, 0, true, false, true)); // Iron Decree
            }
        } else {
            long until = player.level().getGameTime() + 30;
            for (LivingEntity e : SkillTargeting.around(player, c, radius + 2)) {
                e.getPersistentData().putLong("sofe_siege_until", until);
                double d = e.position().distanceTo(c);
                if (d > radius - 0.5) { // no one may leave: pulled back inside
                    Vec3 back = c.subtract(e.position()).normalize().scale(0.4);
                    e.setDeltaMovement(back.x, e.getDeltaMovement().y, back.z);
                    e.hurtMarked = true;
                }
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1));
            }
        }
    }

    private static void ring(ServerLevel level, Vec3 c, double radius, ParticleOptions particle) {
        int n = (int) Math.max(12, radius * 5);
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2 / n;
            level.sendParticles(particle, c.x + Math.cos(a) * radius, c.y + 0.15, c.z + Math.sin(a) * radius, 1, 0, 0, 0, 0);
        }
    }

    /** Decree of Steadfastness: a zone round the King where his allies stand harder. */
    public static Skill.Result decreeOfSteadfastness(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        double[] r = reach(player, ctx.stats());
        decree(player, STEADFAST, player.position(), r[0], (int) (r[1] * 20), false);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1, 1.2f);
        return Skill.Result.at(player.position());
    }

    /** Siege Decree: a zone where enemies cannot leave and take more from every blow. */
    public static Skill.Result siegeDecree(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        double[] r = reach(player, ctx.stats());
        Vec3 at = SkillTargeting.aim(player, ctx.stats().param("range", 16)).point();
        decree(player, SIEGE, at, r[0], (int) (r[1] * 20), false);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1, 0.8f);
        return Skill.Result.at(at);
    }

    /** Janissary Guard: soldiers of brass and aetherium come to his side. */
    public static Skill.Result janissaryGuard(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        int count = ClassState.active(ClassState.of(player).crownUntil, player) ? 6 : (int) s.param("soldiers", 2);
        guard(player).forEach(SummonedAlly::discard);
        boolean aetherium = ctx.upgrade("aetherium_guard") > 0;
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2 / count;
            int life = s.ticks("duration_s", 30);
            SummonedAlly guard = SummonedAlly.summon(player, SummonedAlly.Kind.JANISSARY, life, player.position().add(Math.cos(a) * 1.8, 0, Math.sin(a) * 1.8));
            guard.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(s.param("health", 24)); // Brass Armor
            guard.setHealth(guard.getMaxHealth());
            guard.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(s.param("guard_damage", 6)); // Scimitar Drill
            if (aetherium) guard.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, life, 0)); // Aetherium Guard
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON, SoundSource.PLAYERS, 1, 0.8f);
        return Skill.Result.at(player.position());
    }

    /** Command: every ally and guard turns on one enemy, and hits it harder. */
    public static Skill.Result command(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, s.param("range", 24));
        hit.target().ifPresent(t -> {
            int ticks = s.ticks("duration_s", 8);
            t.getPersistentData().putLong("sofe_commanded_until", player.level().getGameTime() + ticks);
            t.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0));
            for (var mob : player.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, player.getBoundingBox().inflate(32),
                    m -> m instanceof com.sofe.entity.summon.Ally a && player.getUUID().equals(a.owner()))) {
                ((com.sofe.entity.summon.Ally) mob).command(t, ticks);
            }
            SkillTargeting.beam(player.serverLevel(), SkillTargeting.origin(player), hit.point(), ParticleTypes.WAX_ON, 0.4);
            double rally = ctx.upgradeValue("rally_the_court", "speed_s"); // Rally the Court
            if (rally > 0) {
                for (Player ally : player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(12))) {
                    ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, (int) (rally * 20), 1));
                }
            }
        });
        player.level().playSound(null, player.blockPosition(), SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0).value(), SoundSource.PLAYERS, 0.6f, 1.2f);
        return Skill.Result.at(hit.point());
    }

    /** Royal Treasury: aetherium coins thrown about; whoever picks one up is healed. */
    public static Skill.Result royalTreasury(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        int coins = (int) ctx.stats().param("coins", 5);
        for (int i = 0; i < coins; i++) {
            ItemEntity coin = new ItemEntity(player.level(), player.getX(), player.getEyeY(), player.getZ(), new ItemStack(ItemRegistry.ROYAL_COIN.get()));
            double a = player.getRandom().nextDouble() * Math.PI * 2;
            coin.setDeltaMovement(Math.cos(a) * 0.25, 0.35, Math.sin(a) * 0.25);
            coin.setPickUpDelay(15);
            coin.lifespan = 20 * 20;
            coin.getPersistentData().putFloat("sofe_heal", (float) ctx.stats().param("heal", 4));
            coin.getPersistentData().putUUID("sofe_king", player.getUUID());
            coin.getPersistentData().putFloat("sofe_strength_s", (float) ctx.upgradeValue("war_chest", "strength_s")); // War Chest
            player.level().addFreshEntity(coin);
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1, 1.4f);
        return Skill.Result.at(player.position());
    }

    /** A coin of the Royal Treasury heals whoever picks it up, and is spent. */
    public static void onPickup(EntityItemPickupEvent event) {
        ItemEntity coin = event.getItem();
        if (!coin.getItem().is(ItemRegistry.ROYAL_COIN.get())) return;
        Player player = event.getEntity();
        player.heal(coin.getPersistentData().contains("sofe_heal") ? coin.getPersistentData().getFloat("sofe_heal") : 4);
        float strength = coin.getPersistentData().getFloat("sofe_strength_s");
        if (strength > 0) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, (int) (strength * 20), 0));
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.5f);
        if (coin.getPersistentData().hasUUID("sofe_king") && player.level() instanceof ServerLevel level
                && level.getPlayerByUUID(coin.getPersistentData().getUUID("sofe_king")) instanceof ServerPlayer king) {
            ClassMechanics.gain(king, 5); // healing his people raises the King's Authority
        }
        coin.discard();
        event.setCanceled(true);
    }

    /** Bronze Cannon: a siege gun set down beside him that fires at the nearest enemies. */
    public static Skill.Result bronzeCannon(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        Vec3 at = player.position().add(player.getViewVector(1f).multiply(1, 0, 1).normalize().scale(2));
        BronzeCannon.deploy(player, at, (int) s.param("shots", 5), (float) s.param("damage", 10), 1 + (int) ctx.upgradeValue("grapeshot", "pellets"));
        return Skill.Result.at(at);
    }

    /** Crown of the Five Lands: for a while every Decree stands round him at once, and his Guard grows to six. */
    public static Skill.Result crownOfTheFiveLands(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        int ticks = s.ticks("duration_s", 12);
        ClassState state = ClassState.of(player);
        state.crownUntil = player.level().getGameTime() + ticks;
        double radius = s.param("radius", 8);
        ServerLevel level = player.serverLevel();
        SkillTasks.run(player, ticks, tick -> {
            if (tick % 10 == 0) {
                apply(player, STEADFAST, player.position(), radius);
                apply(player, SIEGE, player.position(), radius);
            }
            if (tick % 5 == 0) {
                ring(level, player.position(), radius, ParticleTypes.WAX_ON);
                level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 2.4, player.getZ(), 3, 0.3, 0.1, 0.3, 0);
            }
            return true;
        });
        int guards = 6;
        guard(player).forEach(SummonedAlly::discard);
        for (int i = 0; i < guards; i++) {
            double a = i * Math.PI * 2 / guards;
            SummonedAlly.summon(player, SummonedAlly.Kind.JANISSARY, ticks + 100, player.position().add(Math.cos(a) * 2, 0, Math.sin(a) * 2));
        }
        level.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1, 0.9f);
        return Skill.Result.at(player.position());
    }
}
