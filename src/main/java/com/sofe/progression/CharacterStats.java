package com.sofe.progression;

import com.sofe.entity.VoidCreature;
import com.sofe.gear.GearBonuses;
import com.sofe.gear.GearNbt;
import com.sofe.gear.GearStat;
import com.sofe.gear.PlayerGear;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.player.PlayerClassData;
import com.sofe.player.ResourceType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Turns the character sheet and the gear into game effects (docs/Clases.md, "Attributes";
 * docs/Pociones.md, "Affixes that raise the character"): health, armor, physical and magic damage,
 * critical hits, dodging, elemental damage, life steal, resistances and the Relics' unique effects.
 */
public final class CharacterStats {
    private static final UUID HEALTH_ID = UUID.fromString("0f8c1e7a-6b3d-4b8e-9a51-2d7c4e1f9b01");
    private static final UUID ARMOR_ID = UUID.fromString("0f8c1e7a-6b3d-4b8e-9a51-2d7c4e1f9b02");

    private CharacterStats() {
    }

    /** The character's effects: attributes from the sheet and gear, plus the gear's own bonuses. */
    public static Optional<AttributeRules.Effects> effects(Player player) {
        Optional<PlayerClass> playerClass = PlayerClassCapability.get(player).flatMap(PlayerClassData::get);
        Optional<ProgressionData> progress = ProgressionCapability.get(player);
        if (playerClass.isEmpty() || progress.isEmpty()) return Optional.empty();
        AttributeRules rules = ProgressionRulesManager.attributes();
        GearBonuses gear = PlayerGear.bonuses(player);
        AttributeRules.Effects base = rules.effects(progress.get().attributes(), playerClass.get(), gear.attributes());
        if (gear.isEmpty()) return Optional.of(base);
        int[] resource = base.maxResourceBonus().clone();
        for (ResourceType type : ResourceType.values()) resource[type.ordinal()] += (int) gear.get(GearStat.MAX_RESOURCE);
        return Optional.of(new AttributeRules.Effects(
                base.physicalDamageMultiplier() + gear.fraction(GearStat.PHYSICAL_DAMAGE),
                base.magicDamageMultiplier() + gear.fraction(GearStat.MAGIC_DAMAGE),
                Math.min(1, base.critChance() + gear.fraction(GearStat.CRIT_CHANCE)),
                base.critMultiplier() + gear.fraction(GearStat.CRIT_DAMAGE),
                Math.min(rules.dodgeCap(), base.dodgeChance() + gear.fraction(GearStat.DODGE)),
                base.maxHealthBonus() + gear.get(GearStat.MAX_HEALTH),
                base.regenMultiplier() * (1 + gear.fraction(GearStat.RESOURCE_REGEN)),
                resource));
    }

    /** Applies the Vitality and gear health bonus and the gear's armor; called whenever the sheet, class or gear changes. */
    public static void applyHealth(ServerPlayer player) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return;
        double bonus = effects(player).map(AttributeRules.Effects::maxHealthBonus).orElse(0.0);
        health.removeModifier(HEALTH_ID);
        if (bonus > 0) {
            health.addPermanentModifier(new AttributeModifier(HEALTH_ID, "SoFE Vitality", bonus, AttributeModifier.Operation.ADDITION));
        }
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(ARMOR_ID);
            double gearArmor = PlayerGear.bonuses(player).get(GearStat.ARMOR);
            if (gearArmor > 0) armor.addTransientModifier(new AttributeModifier(ARMOR_ID, "SoFE gear armor", gearArmor, AttributeModifier.Operation.ADDITION));
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static boolean isMagic(DamageSource source) {
        return source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC);
    }

    /**
     * A Bearer's hits: Strength for physical damage, Intellect for magic, a chance of a critical hit,
     * the gear's elemental damage, life steal and the Relics' effects. Then the defender's resistances.
     */
    public static void onHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player && event.getEntity() != player) {
            effects(player).ifPresent(fx -> event.setAmount(attack(player, event.getEntity(), event.getSource(), event.getAmount(), fx)));
        }
        if (event.getEntity() instanceof ServerPlayer defender) {
            event.setAmount(resist(defender, event.getSource(), event.getAmount()));
        }
    }

    private static float attack(ServerPlayer player, LivingEntity target, DamageSource source, float amount, AttributeRules.Effects fx) {
        amount *= (float) (isMagic(source) ? fx.magicDamageMultiplier() : fx.physicalDamageMultiplier());
        GearBonuses gear = PlayerGear.bonuses(player);
        boolean melee = source.getDirectEntity() == player;
        if (melee) {
            amount += (float) (gear.get(GearStat.FIRE_DAMAGE) + gear.get(GearStat.FROST_DAMAGE) + gear.get(GearStat.STORM_DAMAGE));
            if (gear.get(GearStat.FIRE_DAMAGE) > 0) target.setSecondsOnFire(2);
            if (gear.get(GearStat.FROST_DAMAGE) > 0) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), player);
            amount = relicEffects(player, target, amount);
        }
        if (fx.critChance() > 0 && player.getRandom().nextDouble() < fx.critChance()) {
            amount *= (float) fx.critMultiplier();
            if (player.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
            }
        }
        double lifeSteal = gear.fraction(GearStat.LIFE_STEAL);
        if (melee && lifeSteal > 0) player.heal((float) (Math.min(amount, target.getHealth()) * lifeSteal));
        return amount;
    }

    /** Kaleth's Blade burns the slowed and stunned; Vorath's Wrath hits harder the more hearts are missing. */
    private static float relicEffects(ServerPlayer player, LivingEntity target, float amount) {
        Optional<String> relic = GearNbt.relic(player.getMainHandItem());
        if (relic.isEmpty() || !PlayerGear.meets(player, GearNbt.read(player.getMainHandItem()).orElseThrow())) return amount;
        switch (relic.get()) {
            case "kaleth_blade" -> {
                if (target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                    target.setSecondsOnFire(5);
                    return amount * 1.3f;
                }
            }
            case "vorath_wrath" -> {
                float missingHearts = (player.getMaxHealth() - player.getHealth()) / 2f;
                return amount * (1 + 0.03f * missingHearts);
            }
            default -> {
            }
        }
        return amount;
    }

    /** Resistances from gear cut fire, frost, storm and Void damage, at most by 75%. */
    private static float resist(ServerPlayer defender, DamageSource source, float amount) {
        GearBonuses gear = PlayerGear.bonuses(defender);
        if (gear.isEmpty()) return amount;
        GearStat stat = null;
        if (source.is(DamageTypeTags.IS_FIRE)) stat = GearStat.FIRE_RESISTANCE;
        else if (source.is(DamageTypeTags.IS_FREEZING)) stat = GearStat.FROST_RESISTANCE;
        else if (source.is(DamageTypeTags.IS_LIGHTNING)) stat = GearStat.STORM_RESISTANCE;
        else if (source.getEntity() instanceof VoidCreature) stat = GearStat.VOID_RESISTANCE;
        return stat == null ? amount : amount * (float) (1 - gear.fraction(stat));
    }

    /** Serath's Fang: each kill restores two hearts. */
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (GearNbt.relic(player.getMainHandItem()).filter("serath_fang"::equals).isPresent()) player.heal(4f);
    }

    /** Agility (and gear) gives a chance to dodge an attack completely (not falls, fire, drowning or the void). */
    public static void onAttacked(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        DamageSource source = event.getSource();
        if (source.getEntity() == null || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        effects(player).ifPresent(fx -> {
            if (fx.dodgeChance() > 0 && player.getRandom().nextDouble() < fx.dodgeChance()) {
                event.setCanceled(true);
                player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.PLAYERS, 0.8f, 1.4f);
                player.displayClientMessage(Component.translatable("message.sofe.dodged").withStyle(ChatFormatting.GRAY), true);
            }
        });
    }
}
