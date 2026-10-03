package com.sofe.skill.thief;

import com.sofe.combat.CombatCapability;
import com.sofe.gear.GearMaker;
import com.sofe.gear.LootGenerator;
import com.sofe.gear.Rarity;
import com.sofe.registry.ItemRegistry;
import com.sofe.registry.SoFEEffects;
import com.sofe.skill.ClassState;
import com.sofe.skill.Skill;
import com.sofe.skill.SkillTargeting;
import com.sofe.skill.SkillTasks;
import com.sofe.skill.data.SkillStats;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Rurik's skills (docs/Clases.md). Basic blows leave Marks on an enemy (up to 5); finishers spend them.
 * He also steals: an enemy's blessing, a potion, a few coins, now and then something rare.
 */
public final class ThiefSkills {
    public static final int MAX_MARKS = 5;
    /** Each Thief's Marks on an enemy are their own: several Thieves can mark the same enemy (a tag per Thief). */
    private static final String MARKS = "sofe_marks", ROBBED = "sofe_robbed";
    private static final int MARK_TICKS = 20 * 10;
    private static final MobEffect[] STOLEN = {MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED, MobEffects.DAMAGE_RESISTANCE,
            MobEffects.REGENERATION, MobEffects.DIG_SPEED, MobEffects.INVISIBILITY};

    private ThiefSkills() {
    }

    // ------------------------------------------------------------------------------------- Marks
    private static net.minecraft.nbt.CompoundTag mine(ServerPlayer player, LivingEntity target) {
        var all = target.getPersistentData().getCompound(MARKS);
        return all.getCompound(player.getStringUUID());
    }

    public static int marks(ServerPlayer player, LivingEntity target) {
        var tag = mine(player, target);
        if (tag.getLong("until") < player.level().getGameTime()) return 0;
        return tag.getInt("marks");
    }

    public static void addMarks(ServerPlayer player, LivingEntity target, int count) {
        int now = Math.min(MAX_MARKS, marks(player, target) + count);
        var all = target.getPersistentData().getCompound(MARKS);
        var tag = new net.minecraft.nbt.CompoundTag();
        tag.putInt("marks", now);
        tag.putLong("until", player.level().getGameTime() + MARK_TICKS);
        all.put(player.getStringUUID(), tag);
        target.getPersistentData().put(MARKS, all);
        ClassState.of(player).lastMarked = target.getUUID();
        player.serverLevel().sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + target.getBbHeight() + 0.3, target.getZ(), now, 0.2, 0.1, 0.2, 0);
        show(player, now);
    }

    /** Takes every Mark off the target and returns how many there were. */
    public static int takeMarks(ServerPlayer player, LivingEntity target) {
        int had = marks(player, target);
        var all = target.getPersistentData().getCompound(MARKS);
        all.remove(player.getStringUUID());
        target.getPersistentData().put(MARKS, all);
        show(player, 0);
        return had;
    }

    private static void show(ServerPlayer player, int marks) {
        CombatCapability.get(player).ifPresent(c -> {
            c.setMarks(marks);
            c.markDirty();
        });
    }

    /** The HUD shows the Marks on the enemy last marked; they fade with them. */
    public static void refreshMarkCounter(ServerPlayer player) {
        var id = ClassState.of(player).lastMarked;
        LivingEntity last = id != null && player.serverLevel().getEntity(id) instanceof LivingEntity e ? e : null;
        show(player, last == null || !last.isAlive() ? 0 : marks(player, last));
    }

    private static void slash(ServerPlayer player, LivingEntity target, float amount) {
        target.invulnerableTime = 0;
        target.hurt(player.damageSources().playerAttack(player), amount);
    }

    // ------------------------------------------------------------------------------------- stealing
    /** Steals from an enemy: one of its blessings, or else a potion, coins or, rarely, a piece of gear. Once per enemy. */
    public static void steal(ServerPlayer player, LivingEntity target, boolean heist) {
        if (heist) { // the Great Heist: every blow takes something
            MobEffect power = STOLEN[player.getRandom().nextInt(STOLEN.length)];
            player.addEffect(new MobEffectInstance(power, 200, 0));
            player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.5, player.getZ(), 4, 0.3, 0.3, 0.3, 0);
            if (player.getRandom().nextFloat() < 0.3f) give(player, new ItemStack(ItemRegistry.DINAR.get(), 1 + player.getRandom().nextInt(2)));
            return;
        }
        for (MobEffectInstance effect : new ArrayList<>(target.getActiveEffects())) {
            if (effect.getEffect().getCategory() == MobEffectCategory.BENEFICIAL) {
                target.removeEffect(effect.getEffect());
                player.addEffect(new MobEffectInstance(effect.getEffect(), Math.min(effect.getDuration(), 20 * 60), effect.getAmplifier()));
                tell(player, Component.translatable("message.sofe.steal.blessing", effect.getEffect().getDisplayName()));
                return;
            }
        }
        String robbed = ROBBED + "_" + player.getStringUUID(); // each Thief can rob an enemy once
        if (target.getPersistentData().getBoolean(robbed)) {
            tell(player, Component.translatable("message.sofe.steal.empty"));
            return;
        }
        target.getPersistentData().putBoolean(robbed, true);
        float roll = player.getRandom().nextFloat();
        if (roll < 0.04f) { // something rare
            LootGenerator gen = GearMaker.builder(player, GearMaker.levelOf(player)).rarity(Rarity.IMPERIAL).build();
            GearMaker.roll(gen, player.getRandom()).ifPresent(stack -> {
                give(player, stack);
                tell(player, Component.translatable("message.sofe.steal.item", stack.getHoverName()));
            });
        } else if (roll < 0.35f) {
            ItemStack potion = new ItemStack(ItemRegistry.MINOR_POMEGRANATE_ELIXIR.get());
            give(player, potion);
            tell(player, Component.translatable("message.sofe.steal.item", potion.getHoverName()));
        } else if (roll < 0.8f) {
            int coins = 2 + player.getRandom().nextInt(6);
            give(player, new ItemStack(ItemRegistry.DINAR.get(), coins));
            tell(player, Component.translatable("message.sofe.steal.coins", coins));
        } else {
            tell(player, Component.translatable("message.sofe.steal.empty"));
        }
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    private static void tell(ServerPlayer player, Component message) {
        player.displayClientMessage(message.copy().withStyle(ChatFormatting.GOLD), true);
    }

    // ------------------------------------------------------------------------------------- the skills
    /** Double Edge: two quick slashes, two Marks. */
    public static Skill.Result doubleEdge(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, s.param("range", 3.5));
        hit.target().ifPresent(t -> {
            float amount = (float) s.param("damage", 3);
            slash(player, t, amount);
            boolean third = ctx.upgrade("third_edge") > 0;
            SkillTasks.run(player, 9, tick -> {
                if ((tick == 4 || third && tick == 8) && t.isAlive()) {
                    slash(player, t, amount);
                    player.serverLevel().sendParticles(ParticleTypes.SWEEP_ATTACK, t.getX(), t.getY() + 1, t.getZ(), 1, 0, 0, 0, 0);
                }
                return true;
            });
            addMarks(player, t, (int) s.param("marks", 2) + (third ? 1 : 0));
        });
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1, 1.4f);
        return Skill.Result.at(hit.point());
    }

    /** Light Fingers: a hand in the enemy's pocket. */
    public static Skill.Result lightFingers(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, ctx.stats().param("range", 4));
        hit.target().ifPresent(t -> {
            steal(player, t, false);
            int sleight = (int) ctx.upgradeValue("sleight_of_hand", "marks"); // Sleight of Hand
            if (sleight > 0) {
                addMarks(player, t, sleight);
                slash(player, t, 2);
            }
        });
        player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.7f);
        return Skill.Result.at(hit.point());
    }

    /** Smoke Step: a smoke bomb, and he is gone; whoever hunted him loses him. */
    public static Skill.Result smokeStep(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, s.ticks("duration_s", 3), 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, s.ticks("duration_s", 3), 1, false, false, true));
        for (LivingEntity e : SkillTargeting.around(player, player.position(), 16)) {
            if (e instanceof Mob mob && mob.getTarget() == player) mob.setTarget(null);
        }
        double blind = ctx.upgradeValue("choking_smoke", "blind_s");
        if (blind > 0) { // Choking Smoke
            for (LivingEntity e : SkillTargeting.around(player, player.position(), 5)) e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (int) (blind * 20), 0));
        }
        if (ctx.upgrade("ash_cloak") > 0) player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, s.ticks("duration_s", 3), 0)); // Ash Cloak
        SkillTargeting.burst(player.serverLevel(), player.position().add(0, 1, 0), ParticleTypes.CAMPFIRE_COSY_SMOKE, 30, 0.7);
        player.level().playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1, 1.2f);
        return Skill.Result.at(player.position());
    }

    /** Cutthroat: the finisher; every Mark on the enemy adds to the cut. */
    public static Skill.Result cutthroat(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        SkillTargeting.Hit hit = SkillTargeting.aim(player, s.param("range", 3.5));
        hit.target().ifPresent(t -> {
            int marks = takeMarks(player, t);
            slash(player, t, (float) (s.param("damage", 6) * (1 + s.param("per_mark", 0.2) * marks)));
            double artery = ctx.upgradeValue("open_artery", "bleed_s"); // Open Artery
            if (artery > 0 && marks >= 3) t.addEffect(new MobEffectInstance(SoFEEffects.BLEEDING.get(), (int) (artery * 20), 1), player);
            player.serverLevel().sendParticles(ParticleTypes.DAMAGE_INDICATOR, t.getX(), t.getY() + 1, t.getZ(), 4 + marks * 2, 0.3, 0.3, 0.3, 0.1);
        });
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1, 0.8f);
        return Skill.Result.at(hit.point());
    }

    /** Fjord Snare: a rope trap on the ground; the first enemy to step in is bound and bleeds. */
    public static Skill.Result fjordSnare(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        Vec3 aim = SkillTargeting.aim(player, s.param("range", 10)).point();
        ServerLevel level = player.serverLevel();
        // Line of Snares: more traps beside the first, across the line of sight
        Vec3 side = new Vec3(-player.getViewVector(1f).z, 0, player.getViewVector(1f).x).normalize();
        int extra = (int) ctx.upgradeValue("line_of_snares", "extra_snares");
        for (int k = 0; k <= extra; k++) { // the first at the aim, then one more each side in turn
            int step = (k + 1) / 2 * (k % 2 == 0 ? -1 : 1);
            snare(player, s, level, aim.add(side.scale(step * 2.2)));
        }
        level.playSound(null, player.blockPosition(), SoundEvents.TRIPWIRE_ATTACH, SoundSource.PLAYERS, 1, 1);
        return Skill.Result.at(aim);
    }

    private static void snare(ServerPlayer player, SkillStats s, ServerLevel level, Vec3 at) {
        SkillTasks.run(player, s.ticks("duration_s", 30), tick -> {
            if (tick % 10 == 0) level.sendParticles(ParticleTypes.CRIT, at.x, at.y + 0.1, at.z, 3, 0.5, 0, 0.5, 0);
            List<LivingEntity> caught = SkillTargeting.around(player, at, 1.6);
            if (caught.isEmpty()) return true;
            LivingEntity e = caught.get(0);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, s.ticks("root_s", 3), 10));
            e.addEffect(new MobEffectInstance(SoFEEffects.BLEEDING.get(), s.ticks("bleed_s", 4), 0));
            SkillTargeting.damage(player, e, (float) s.param("damage", 4));
            level.playSound(null, e.blockPosition(), SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.PLAYERS, 1, 0.8f);
            return false; // sprung
        });
    }

    /** Throwing Axe: out along his sight and back to his hand, cutting and marking everything in its path. */
    public static Skill.Result throwingAxe(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        double range = s.param("range", 14);
        Vec3 from = SkillTargeting.origin(player);
        Vec3 look = player.getViewVector(1f);
        List<Vec3> dirs = new ArrayList<>(List.of(look));
        if (ctx.upgrade("twin_axes") > 0) dirs.add(look.yRot((float) Math.toRadians(14))); // Twin Axes
        Set<UUID> struck = new HashSet<>();
        for (Vec3 dir : dirs) SkillTasks.run(player, 16, tick -> {
            double t = tick < 8 ? tick / 8.0 : (16 - tick) / 8.0;   // out, then back
            Vec3 p = from.add(dir.scale(range * t));
            player.serverLevel().sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            for (LivingEntity e : SkillTargeting.around(player, p, 1.3)) {
                if (!struck.add(e.getUUID())) continue;
                slash(player, e, (float) s.param("damage", 5));
                addMarks(player, e, 1);
            }
            return true;
        });
        player.level().playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1, 1.3f);
        return Skill.Result.at(from.add(look.scale(range)));
    }

    /** Thousand Cuts: he leaps between the enemies around him, cutting each one. */
    public static Skill.Result thousandCuts(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        SkillStats s = ctx.stats();
        List<LivingEntity> targets = new ArrayList<>(SkillTargeting.around(player, player.position(), s.param("radius", 8))
                .stream().limit((long) s.param("targets", 6)).toList());
        if (targets.isEmpty()) return Skill.Result.at(player.position());
        Vec3 home = player.position();
        int step = Math.max(2, 20 / targets.size());
        SkillTasks.run(player, step * targets.size() + 1, tick -> {
            if (tick % step != 0) return true;
            int i = tick / step;
            if (i >= targets.size()) {
                player.teleportTo(home.x, home.y, home.z);
                return false;
            }
            LivingEntity e = targets.get(i);
            if (!e.isAlive()) return true;
            Vec3 behind = e.position().add(Vec3.directionFromRotation(0, e.getYRot()).scale(-1.2));
            player.serverLevel().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 4, 0.2, 0.2, 0.2, 0.02);
            player.teleportTo(behind.x, e.getY(), behind.z);
            slash(player, e, (float) s.param("damage", 6));
            addMarks(player, e, 1);
            player.serverLevel().sendParticles(ParticleTypes.SWEEP_ATTACK, e.getX(), e.getY() + 1, e.getZ(), 1, 0, 0, 0, 0);
            player.level().playSound(null, e.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 1.6f);
            return true;
        });
        return Skill.Result.at(player.position());
    }

    /** The Great Heist: for a while every blow steals a power from the enemy. */
    public static Skill.Result greatHeist(Skill.Context ctx) {
        ServerPlayer player = ctx.player();
        int ticks = ctx.stats().ticks("duration_s", 8);
        ClassState.of(player).heistUntil = player.level().getGameTime() + ticks;
        SkillTasks.run(player, ticks, tick -> {
            if (tick % 6 == 0) player.serverLevel().sendParticles(ParticleTypes.WAX_ON, player.getX(), player.getY() + 1, player.getZ(), 4, 0.4, 0.5, 0.4, 0);
            return true;
        });
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_CHEST_OPEN, SoundSource.PLAYERS, 1, 1.4f);
        return Skill.Result.at(player.position());
    }
}
