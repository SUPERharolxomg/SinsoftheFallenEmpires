package com.sofe.gear;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sofe.entity.boss.SoFEBossEntity;
import com.sofe.entity.summon.EmbalmedDead;
import com.sofe.player.PlayerClass;
import com.sofe.registry.ItemRegistry;
import com.sofe.registry.SoFEEffects;
import com.sofe.skill.ClassMechanics;
import com.sofe.skill.ClassState;
import com.sofe.skill.SkillTargeting;
import com.sofe.skill.thief.ThiefSkills;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * The data-driven effects of the uniques (data/sofe/relics/&lt;id&gt;.json, "effects"), written by
 * scripts/make_uniques.py. Each effect says when it acts ("on": hit, kill, hurt, worn) and what it does
 * ("do"). On-hit effects come from the weapon in hand; the others from everything worn, held, or in the
 * Curios slots. A unique of another class, or whose requirements are not met, does nothing.
 */
public final class UniqueEffects {
    private static final String CHEAT_DEATH = "sofe_unique_cheat_death";

    private UniqueEffects() {
    }

    private record Active(GearDataManager.Relic relic, boolean weapon) {
    }

    /** The uniques the player has equipped and can use. */
    private static List<Active> active(ServerPlayer player) {
        List<Active> list = new ArrayList<>();
        String cls = ClassState.classOf(player).map(PlayerClass::id).orElse(null);
        ItemStack main = player.getMainHandItem();
        for (ItemStack stack : PlayerGear.equipped(player)) {
            GearNbt.read(stack).filter(g -> g.relic() != null && PlayerGear.meets(player, g)).ifPresent(g ->
                    GearDataManager.relic(g.relic()).filter(r -> r.suits(cls) && !r.effects().isEmpty())
                            .ifPresent(r -> list.add(new Active(r, stack == main))));
        }
        return list;
    }

    private static double num(JsonObject e, String key, double fallback) {
        return e.has(key) ? e.get(key).getAsDouble() : fallback;
    }

    private static int ticks(JsonObject e, String key, double fallbackSeconds) {
        return (int) Math.round(num(e, key, fallbackSeconds) * 20);
    }

    private static boolean chance(ServerPlayer player, JsonObject e) {
        return !e.has("chance") || player.getRandom().nextDouble() < e.get("chance").getAsDouble();
    }

    private static MobEffect effect(JsonObject e) {
        return ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(e.get("effect").getAsString()));
    }

    // ------------------------------------------------------------------------------------------ blows
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;
        if (victim instanceof ServerPlayer defender && event.getSource().getEntity() instanceof LivingEntity attacker && attacker != defender
                && !event.getSource().is(DamageTypes.THORNS)) {
            for (Active a : active(defender)) for (JsonObject e : a.relic().effects()) if (on(e, "hurt")) hurt(event, defender, attacker, e);
        }
        if (victim instanceof ServerPlayer defender) cheatDeath(event, defender);
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != victim && !(victim instanceof net.minecraft.world.entity.player.Player)) {
            float amount = event.getAmount();
            for (Active a : active(attacker)) {
                if (!a.weapon()) continue;
                for (JsonObject e : a.relic().effects()) if (on(e, "hit")) amount = hit(attacker, victim, amount, e);
            }
            event.setAmount(amount);
        }
    }

    private static boolean on(JsonObject e, String when) {
        return e.get("on").getAsString().equals(when);
    }

    private static float hit(ServerPlayer player, LivingEntity target, float amount, JsonObject e) {
        if (!chance(player, e) && !e.get("do").getAsString().equals("execute")) return amount;
        ServerLevel level = player.serverLevel();
        switch (e.get("do").getAsString()) {
            case "ignite" -> target.setSecondsOnFire((int) num(e, "s", 3));
            case "slow" -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks(e, "s", 2), (int) num(e, "lvl", 1)), player);
            case "poison" -> target.addEffect(new MobEffectInstance(MobEffects.POISON, ticks(e, "s", 3), (int) num(e, "lvl", 0)), player);
            case "wither" -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, ticks(e, "s", 3), 0), player);
            case "weaken" -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ticks(e, "s", 4), 0), player);
            case "bleed" -> target.addEffect(new MobEffectInstance(SoFEEffects.BLEEDING.get(), ticks(e, "s", 4), 0), player);
            case "blind" -> target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, ticks(e, "s", 2), 0), player);
            case "stun" -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks(e, "s", 1), 9), player);
            case "freeze" -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks(e, "s", 2), 4), player);
                target.setTicksFrozen(target.getTicksRequiredToFreeze() + ticks(e, "s", 2));
                level.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1, target.getZ(), 10, 0.3, 0.4, 0.3, 0.02);
            }
            case "lightning" -> {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(target.getX(), target.getY(), target.getZ());
                    bolt.setVisualOnly(true);
                    level.addFreshEntity(bolt);
                }
                return amount * (float) num(e, "mult", 1.5);
            }
            case "execute" -> {
                if (!(target instanceof SoFEBossEntity) && target.getHealth() - amount < target.getMaxHealth() * num(e, "below", 0.15)) {
                    level.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY() + 1, target.getZ(), 12, 0.3, 0.5, 0.3, 0.05);
                    return Math.max(amount, target.getHealth() + 1);
                }
            }
            case "lifesteal" -> player.heal(amount * (float) num(e, "frac", 0.1));
            case "vs_undead" -> {
                if (target.getMobType() == MobType.UNDEAD) return amount * (float) num(e, "mult", 1.5);
            }
            case "vs_beast" -> {
                if (target instanceof Animal) return amount * (float) num(e, "mult", 1.5);
            }
            case "vs_monster" -> {
                if (target instanceof Enemy) return amount * (float) num(e, "mult", 1.2);
            }
            case "knockup" -> {
                target.setDeltaMovement(target.getDeltaMovement().add(0, num(e, "v", 0.7), 0));
                target.hurtMarked = true;
            }
            case "chain" -> {
                int left = (int) num(e, "targets", 2);
                for (LivingEntity next : SkillTargeting.around(player, target.position(), 6)) {
                    if (next == target || left-- <= 0) continue;
                    SkillTargeting.beam(level, center(target), center(next), ParticleTypes.ELECTRIC_SPARK, 0.3);
                    SkillTargeting.damage(player, next, amount * (float) num(e, "frac", 0.5));
                }
            }
            case "marks" -> ThiefSkills.addMarks(player, target, (int) num(e, "n", 1));
            case "resource" -> ClassMechanics.gain(player, (float) num(e, "amount", 3));
            case "soul" -> ClassMechanics.addSouls(player, (int) num(e, "n", 1));
            default -> {
            }
        }
        return amount;
    }

    private static Vec3 center(LivingEntity e) {
        return e.position().add(0, e.getBbHeight() / 2, 0);
    }

    private static void hurt(LivingHurtEvent event, ServerPlayer player, LivingEntity attacker, JsonObject e) {
        if (!chance(player, e)) return;
        switch (e.get("do").getAsString()) {
            case "thorns" -> attacker.hurt(player.damageSources().thorns(player), event.getAmount() * (float) num(e, "frac", 0.2));
            case "ignite" -> attacker.setSecondsOnFire((int) num(e, "s", 3));
            case "slow" -> attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks(e, "s", 3), (int) num(e, "lvl", 1)), player);
            case "poison" -> attacker.addEffect(new MobEffectInstance(MobEffects.POISON, ticks(e, "s", 3), 0), player);
            case "wither" -> attacker.addEffect(new MobEffectInstance(MobEffects.WITHER, ticks(e, "s", 3), 0), player);
            case "knockback" -> attacker.knockback(num(e, "v", 0.8), player.getX() - attacker.getX(), player.getZ() - attacker.getZ());
            case "reduce" -> event.setAmount(event.getAmount() * (1 - (float) num(e, "frac", 0.1)));
            case "effect" -> {
                MobEffect fx = effect(e);
                if (fx != null) player.addEffect(new MobEffectInstance(fx, ticks(e, "s", 4), (int) num(e, "amp", 0)));
            }
            default -> {
            }
        }
    }

    private static void cheatDeath(LivingHurtEvent event, ServerPlayer player) {
        if (event.getAmount() < player.getHealth() + player.getAbsorptionAmount()) return;
        long now = player.level().getGameTime();
        for (Active a : active(player)) {
            for (JsonObject e : a.relic().effects()) {
                if (!on(e, "hurt") || !e.get("do").getAsString().equals("cheat_death")) continue;
                if (now < player.getPersistentData().getLong(CHEAT_DEATH)) return;
                player.getPersistentData().putLong(CHEAT_DEATH, now + ticks(e, "cooldown_s", 300));
                event.setAmount(Math.max(0, player.getHealth() - 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 2));
                player.serverLevel().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 30, 0.4, 0.6, 0.4, 0.3);
                player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.7f, 1.2f);
                return;
            }
        }
    }

    // ------------------------------------------------------------------------------------------ kills
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || event.getEntity() instanceof net.minecraft.world.entity.player.Player) return;
        LivingEntity dead = event.getEntity();
        for (Active a : active(player)) {
            for (JsonObject e : a.relic().effects()) {
                if (!on(e, "kill") || !chance(player, e)) continue;
                switch (e.get("do").getAsString()) {
                    case "heal" -> player.heal((float) num(e, "amount", 2));
                    case "effect" -> {
                        MobEffect fx = effect(e);
                        if (fx != null) player.addEffect(new MobEffectInstance(fx, ticks(e, "s", 5), (int) num(e, "amp", 0)));
                    }
                    case "dinars" -> dead.spawnAtLocation(new ItemStack(ItemRegistry.DINAR.get(), (int) num(e, "n", 1)));
                    case "souls" -> ClassMechanics.addSouls(player, (int) num(e, "n", 1));
                    case "resource" -> ClassMechanics.gain(player, (float) num(e, "amount", 10));
                    case "explode" -> {
                        player.serverLevel().sendParticles(ParticleTypes.EXPLOSION, dead.getX(), dead.getY() + 1, dead.getZ(), 2, 0.3, 0.3, 0.3, 0);
                        for (LivingEntity near : SkillTargeting.around(player, dead.position(), num(e, "radius", 3))) {
                            SkillTargeting.damage(player, near, (float) num(e, "damage", 5));
                        }
                    }
                    case "raise" -> EmbalmedDead.raise(player, dead.position(), ticks(e, "s", 15), num(e, "health", 16), num(e, "damage", 4));
                    default -> {
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------ worn
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;
        for (Active a : active(player)) {
            for (JsonObject e : a.relic().effects()) {
                if (!on(e, "worn")) continue;
                switch (e.get("do").getAsString()) {
                    case "effect" -> {
                        MobEffect fx = effect(e);
                        if (fx != null && holds(player, e.has("when") ? e.get("when").getAsString() : "always")) {
                            player.addEffect(new MobEffectInstance(fx, fx == MobEffects.NIGHT_VISION ? 260 : 50, (int) num(e, "amp", 0), true, false, true));
                        }
                    }
                    case "cleanse" -> {
                        for (JsonElement id : e.getAsJsonArray("effects")) {
                            MobEffect fx = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(id.getAsString()));
                            if (fx != null) player.removeEffect(fx);
                        }
                    }
                    case "resource" -> ClassMechanics.gain(player, (float) num(e, "per_s", 1));
                    case "heal" -> {
                        if (player.tickCount % ticks(e, "every_s", 5) < 20) player.heal((float) num(e, "amount", 1));
                    }
                    case "absorption" -> {
                        if (player.tickCount % ticks(e, "every_s", 30) < 20 && player.getAbsorptionAmount() < 4) {
                            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ticks(e, "every_s", 30), 0, true, false, true));
                        }
                    }
                    case "aura" -> {
                        for (LivingEntity near : SkillTargeting.around(player, player.position(), num(e, "radius", 4))) {
                            SkillTargeting.damage(player, near, (float) num(e, "damage", 1));
                            String kind = e.has("kind") ? e.get("kind").getAsString() : "";
                            if (kind.equals("fire")) near.setSecondsOnFire(2);
                            if (kind.equals("frost")) near.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1));
                        }
                    }
                    case "reveal" -> {
                        for (LivingEntity near : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(num(e, "radius", 14)),
                                x -> x instanceof Enemy && x.isAlive())) {
                            near.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false));
                        }
                    }
                    case "unfreeze" -> player.setTicksFrozen(0);
                    default -> {
                    }
                }
            }
        }
    }

    private static boolean holds(ServerPlayer player, String when) {
        return switch (when) {
            case "low_health" -> player.getHealth() < player.getMaxHealth() / 2;
            case "crouching" -> player.isCrouching();
            case "water" -> player.isInWater();
            case "night" -> player.level().isNight();
            case "day" -> player.level().isDay();
            default -> true;
        };
    }
}
