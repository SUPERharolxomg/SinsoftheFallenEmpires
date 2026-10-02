package com.sofe.gear;

import com.sofe.entity.boss.SoFEBossEntity;
import com.sofe.registry.ItemRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * The unique effects of the droppable Relics (the "uniques"): on-hit effects of the weapons, on-kill
 * effects, effects that answer blows taken, and what worn armor and jewelry grant while equipped. Boss
 * Relics (Kaleth, Serath, Vorath) keep theirs in CharacterStats. A Relic works only if its owner meets
 * its requirements.
 */
public final class RelicEffects {
    private static final int CHECK = 20, LASTS = 50;
    /** Wrappings of the Undying: how long between two saved deaths. */
    public static final long UNDYING_COOLDOWN = 20 * 60 * 5;
    public static final String UNDYING_TAG = "sofe_undying_ready_at";
    /** Jackal's Judgement executes enemies under this fraction of their health (never bosses). */
    public static final float EXECUTE_BELOW = 0.2f;

    private RelicEffects() {
    }

    /** The Relic ids the player wears (armor and jewelry) and can use. */
    public static Set<String> worn(Player player) {
        Set<String> ids = new HashSet<>();
        for (ItemStack stack : PlayerGear.equipped(player)) {
            if (stack.getItem() instanceof SoFEGear gear && gear.gearSlot() == GearSlot.WEAPON) continue;
            GearNbt.read(stack).filter(g -> g.relic() != null && PlayerGear.meets(player, g)).ifPresent(g -> ids.add(g.relic()));
        }
        return ids;
    }

    /** A melee hit with a unique weapon; returns the damage. */
    public static float onHit(ServerPlayer player, String relic, LivingEntity target, float amount) {
        switch (relic) {
            case "dunesunder" -> target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0), player);
            case "widows_kiss" -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1), player);
            case "rimetooth" -> {
                if (player.getRandom().nextFloat() < 0.2f) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 4), player);
                    target.setTicksFrozen(target.getTicksRequiredToFreeze() + 40);
                    particles(target, ParticleTypes.SNOWFLAKE, 14);
                }
            }
            case "jackals_judgement" -> {
                if (!(target instanceof SoFEBossEntity) && (target.getHealth() - amount) < target.getMaxHealth() * EXECUTE_BELOW) {
                    particles(target, ParticleTypes.SOUL, 12);
                    return Math.max(amount, target.getHealth() + 1);
                }
            }
            case "stormcaller" -> {
                if (player.getRandom().nextFloat() < 0.15f && player.level() instanceof ServerLevel level) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                    if (bolt != null) {
                        bolt.moveTo(target.getX(), target.getY(), target.getZ());
                        bolt.setVisualOnly(true);
                        level.addFreshEntity(bolt);
                    }
                    target.setSecondsOnFire(3);
                    return amount * 1.6f;
                }
            }
            default -> {
            }
        }
        return amount;
    }

    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity dead = event.getEntity();
        GearNbt.read(player.getMainHandItem()).filter(g -> g.relic() != null && PlayerGear.meets(player, g)).ifPresent(g -> {
            switch (g.relic()) {
                case "skaldbreaker" -> {
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0));
                    player.level().playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.PLAYERS, 0.4f, 1.4f);
                }
                case "greeds_chain" -> {
                    if (dead instanceof Enemy) dead.spawnAtLocation(new ItemStack(ItemRegistry.DINAR.get(), 1 + player.getRandom().nextInt(3)));
                }
                default -> {
                }
            }
        });
        if (worn(player).contains("soulkeeper_ring")) {
            player.heal(2f);
            particles(player, ParticleTypes.SOUL, 4);
        }
    }

    /** Blows taken: the Wrappings of the Undying cheat death, the Band of the Frozen Throne chills the attacker. */
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Set<String> worn = worn(player);
        if (worn.isEmpty()) return;
        if (worn.contains("frozen_throne_band") && event.getSource().getDirectEntity() instanceof LivingEntity attacker && attacker != player) {
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), player);
        }
        if (worn.contains("undying_wrappings") && event.getAmount() >= player.getHealth() + player.getAbsorptionAmount()) {
            long now = player.level().getGameTime();
            if (now >= player.getPersistentData().getLong(UNDYING_TAG)) {
                player.getPersistentData().putLong(UNDYING_TAG, now + UNDYING_COOLDOWN);
                event.setAmount(Math.max(0, player.getHealth() - 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 2));
                particles(player, ParticleTypes.TOTEM_OF_UNDYING, 30);
                player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.7f, 1.2f);
            }
        }
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level().isClientSide() || player.tickCount % CHECK != 0) return;
        Set<String> worn = worn(player);
        for (String relic : worn) {
            switch (relic) {
                case "crown_of_five_sultans" -> {
                    give(player, MobEffects.HERO_OF_THE_VILLAGE, 0);
                    if (player.tickCount % 600 == 0 && player.getAbsorptionAmount() < 4) {
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0, true, false, true));
                    }
                }
                case "white_wolf_mantle" -> {
                    give(player, MobEffects.MOVEMENT_SPEED, 0);
                    player.setTicksFrozen(0);
                }
                case "caravan_treads" -> {
                    give(player, MobEffects.MOVEMENT_SPEED, 0);
                    give(player, MobEffects.JUMP, 0);
                }
                case "blind_judge_helm" -> {
                    player.removeEffect(MobEffects.BLINDNESS);
                    player.removeEffect(MobEffects.DARKNESS);
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false, true));
                }
                case "furnace_heart" -> {
                    give(player, MobEffects.FIRE_RESISTANCE, 0);
                    if (player.isOnFire()) give(player, MobEffects.DAMAGE_BOOST, 1);
                }
                case "serpent_coil" -> player.removeEffect(MobEffects.POISON);
                case "broken_pact_sigil" -> {
                    if (player.level().isNight()) give(player, MobEffects.DAMAGE_BOOST, 0);
                }
                case "ring_of_the_last_caravan" -> {
                    if (player.tickCount % 600 == 0) player.getFoodData().eat(2, 0.5f);
                }
                case "eye_of_the_false_prophet" -> {
                    for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(14),
                            e -> e instanceof Enemy && e.isAlive())) {
                        e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false));
                    }
                }
                default -> {
                }
            }
        }
    }

    private static void give(Player player, MobEffect effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, LASTS, amplifier, true, false, true));
    }

    private static void particles(LivingEntity at, net.minecraft.core.particles.SimpleParticleType type, int count) {
        if (at.level() instanceof ServerLevel level) {
            level.sendParticles(type, at.getX(), at.getY() + at.getBbHeight() / 2, at.getZ(), count, 0.3, 0.4, 0.3, 0.05);
        }
    }
}
