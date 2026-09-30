package com.sofe.progression;

import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.player.PlayerClassData;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Turns the character sheet into game effects (docs/Clases.md, "Attributes"): more health,
 * more physical and magic damage, critical hits and dodging. Class damage differences come from
 * here too, since each class starts with 10 extra points in its primary attribute.
 */
public final class CharacterStats {
    private static final UUID HEALTH_ID = UUID.fromString("0f8c1e7a-6b3d-4b8e-9a51-2d7c4e1f9b01");

    private CharacterStats() {
    }

    public static Optional<AttributeRules.Effects> effects(Player player) {
        Optional<PlayerClass> playerClass = PlayerClassCapability.get(player).flatMap(PlayerClassData::get);
        Optional<ProgressionData> progress = ProgressionCapability.get(player);
        if (playerClass.isEmpty() || progress.isEmpty()) return Optional.empty();
        return Optional.of(ProgressionRulesManager.attributes().effects(progress.get().attributes(), playerClass.get()));
    }

    /** Applies the Vitality health bonus; called whenever the sheet or the class changes. */
    public static void applyHealth(ServerPlayer player) {
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return;
        double bonus = effects(player).map(AttributeRules.Effects::maxHealthBonus).orElse(0.0);
        health.removeModifier(HEALTH_ID);
        if (bonus > 0) {
            health.addPermanentModifier(new AttributeModifier(HEALTH_ID, "SoFE Vitality", bonus, AttributeModifier.Operation.ADDITION));
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static boolean isMagic(DamageSource source) {
        return source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC);
    }

    /** A Bearer's hits: Strength for physical damage, Intellect for magic, and a chance of a critical hit. */
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || event.getEntity() == player) return;
        effects(player).ifPresent(fx -> {
            float amount = event.getAmount();
            amount *= (float) (isMagic(event.getSource()) ? fx.magicDamageMultiplier() : fx.physicalDamageMultiplier());
            if (fx.critChance() > 0 && player.getRandom().nextDouble() < fx.critChance()) {
                amount *= (float) fx.critMultiplier();
                if (player.level() instanceof ServerLevel level) {
                    var e = event.getEntity();
                    level.sendParticles(ParticleTypes.CRIT, e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
                }
            }
            event.setAmount(amount);
        });
    }

    /** Agility gives a chance to dodge an attack completely (not falls, fire, drowning or the void). */
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
