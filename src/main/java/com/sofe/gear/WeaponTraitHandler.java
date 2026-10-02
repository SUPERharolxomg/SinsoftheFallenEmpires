package com.sofe.gear;

import com.sofe.registry.SoFEEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;

/**
 * Applies the traits of the arsenal's weapons when their wielder strikes in melee, and the powers of
 * the empire shields when they block. Registered after CharacterStats, so the traits work on the
 * damage the Bearer's attributes and gear have already shaped.
 */
public final class WeaponTraitHandler {
    /** Set while the handler deals its own splash hits, so they do not trigger traits again. */
    private static boolean splashing;

    private WeaponTraitHandler() {
    }

    public static void onHurt(LivingHurtEvent event) {
        if (splashing || !(event.getSource().getEntity() instanceof Player player)) return;
        if (event.getSource().getDirectEntity() != player || !(player.getMainHandItem().getItem() instanceof TraitWeapon weapon)) return;
        LivingEntity target = event.getEntity();
        if (!com.sofe.gear.ranged.ClassBound.allows(player, weapon.requiredClass())) { // a class weapon in the wrong hands
            event.setAmount(Math.min(event.getAmount(), 1.0f));
            return;
        }
        float amount = event.getAmount();
        if (weapon.has(WeaponTrait.HOLY) && target.getMobType() == net.minecraft.world.entity.MobType.UNDEAD) amount *= 1.5f;
        if (weapon.has(WeaponTrait.PIERCE)) amount = WeaponTrait.pierce(amount, target.getArmorValue());
        if (weapon.has(WeaponTrait.BEAST) && target instanceof Animal) amount *= WeaponTrait.BEAST_MULTIPLIER;
        if (weapon.has(WeaponTrait.CHARGE) && (player.isSprinting() || player.isPassenger())) {
            amount *= WeaponTrait.CHARGE_MULTIPLIER;
            target.knockback(1.0, player.getX() - target.getX(), player.getZ() - target.getZ());
        }
        if (weapon.has(WeaponTrait.MULTI_HIT)) {
            amount *= WeaponTrait.MULTI_HIT_MULTIPLIER;
            particles(target, ParticleTypes.SWEEP_ATTACK, 2);
        }
        if (weapon.has(WeaponTrait.VOID)) {
            amount *= WeaponTrait.VOID_MULTIPLIER;
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0), player);
            particles(target, ParticleTypes.PORTAL, 12);
        }
        if (weapon.has(WeaponTrait.SACRIFICE)) {
            float cost = WeaponTrait.sacrificeCost(player.getHealth());
            if (cost > 0) {
                player.setHealth(player.getHealth() - cost);
                amount *= WeaponTrait.SACRIFICE_MULTIPLIER;
                particles(target, ParticleTypes.DAMAGE_INDICATOR, 6);
            }
        }
        event.setAmount(amount);

        if (weapon.has(WeaponTrait.STUN)) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 4), player);
        if (weapon.has(WeaponTrait.SLOW)) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1), player);
        if (weapon.has(WeaponTrait.FROST)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0), player);
            target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 60, target.getTicksFrozen() + 80));
            particles(target, ParticleTypes.SNOWFLAKE, 10);
        }
        if (weapon.has(WeaponTrait.BURN)) target.setSecondsOnFire(4);
        if (weapon.has(WeaponTrait.HOLY)) player.heal(0.5f);
        if (weapon.has(WeaponTrait.POISON)) target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), player);
        if (weapon.has(WeaponTrait.WEAKEN)) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), player);
        if (weapon.has(WeaponTrait.BLEED)) target.addEffect(new MobEffectInstance(SoFEEffects.BLEEDING.get(), 80, 0), player);
        if (weapon.has(WeaponTrait.LIFE_STEAL)) player.heal(Math.min(amount, target.getHealth()) * WeaponTrait.LIFE_STEAL_FRACTION);
        if (weapon.has(WeaponTrait.PULL)) {
            Vec3 toward = player.position().subtract(target.position()).normalize().scale(0.6);
            target.setDeltaMovement(target.getDeltaMovement().add(toward.x, 0.15, toward.z));
            target.hurtMarked = true;
        }
        if (weapon.has(WeaponTrait.KNOCK_UP)) {
            target.setDeltaMovement(target.getDeltaMovement().add(0, 0.8, 0));
            target.hurtMarked = true;
            particles(target, ParticleTypes.CLOUD, 8);
        }
        if (weapon.has(WeaponTrait.SWEEP)) splash(player, target, amount * WeaponTrait.SWEEP_FRACTION, WeaponTrait.SWEEP_RADIUS, false);
        if (weapon.has(WeaponTrait.SLAM)) splash(player, target, amount * WeaponTrait.SLAM_FRACTION, WeaponTrait.SLAM_RADIUS, true);
    }

    /** Hits every hostile creature near the target, never players, NPCs or animals. */
    private static void splash(Player player, LivingEntity target, float amount, double radius, boolean slam) {
        splashing = true;
        try {
            for (LivingEntity other : target.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(radius),
                    e -> e != target && e != player && e instanceof Enemy && e.isAlive())) {
                other.hurt(player.damageSources().playerAttack(player), amount);
            }
        } finally {
            splashing = false;
        }
        if (slam) {
            particles(target, ParticleTypes.EXPLOSION, 1);
            target.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5f, 1.4f);
        } else {
            particles(target, ParticleTypes.SWEEP_ATTACK, 1);
        }
    }

    private static void particles(LivingEntity target, net.minecraft.core.particles.SimpleParticleType type, int count) {
        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(type, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), count, 0.4, 0.3, 0.4, 0.05);
        }
    }

    /** The power of an empire shield, when it blocks a hit from a living attacker. */
    public static void onBlock(ShieldBlockEvent event) {
        LivingEntity defender = event.getEntity();
        if (!(defender.getUseItem().getItem() instanceof EmpireShield shield)) return;
        LivingEntity attacker = event.getDamageSource().getEntity() instanceof LivingEntity l ? l : null;
        switch (shield.power()) {
            case SULTHARI -> {
                if (attacker != null) attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), defender);
            }
            case NORDRATH -> {
                if (attacker != null && attacker != defender) {
                    splashing = true;
                    try {
                        attacker.hurt(defender.damageSources().thorns(defender), event.getBlockedDamage() * EmpireShield.REFLECT);
                    } finally {
                        splashing = false;
                    }
                }
            }
            case OBSERVATORY -> defender.heal(1.0f);
            case VOID -> {
                if (attacker != null) {
                    attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), defender);
                    attacker.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0), defender);
                }
            }
        }
    }
}
