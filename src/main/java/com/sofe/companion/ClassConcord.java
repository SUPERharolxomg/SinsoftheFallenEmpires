package com.sofe.companion;

import com.sofe.combat.CombatCapability;
import com.sofe.entity.summon.Ally;
import com.sofe.player.PlayerClass;
import com.sofe.skill.ClassState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Class Concord (docs/Anexos.md, A5): when Bearers of different classes fight together, every different class
 * among them gives each of them a small passive; all five together are the Five Pillars. Until the Pact of
 * Sprint 7.5 exists, the group is the Bearer, their companion and the other Bearers within 32 blocks. A Bearer
 * alone has no Concord. The bonuses are plain values here, so they are unit tested.
 */
public final class ClassConcord {
    public static final double RANGE = 32;
    public static final double KNIGHT_ARMOR = 0.05, NECROMANCER_LIFE_STEAL = 0.03, NECROMANCER_LAST_STAND_STEAL = 0.05,
            NECROMANCER_LAST_STAND = 0.25, SORCERESS_REGEN = 0.05,
            THIEF_CRIT = 0.03, KING_HEALING = 0.05, PILLARS_DAMAGE = 0.10;
    private static final UUID ARMOR_ID = UUID.fromString("0f6c4d2a-91e3-4a7b-8f1d-5c2e7b3a9d10");
    private static final Map<UUID, Set<PlayerClass>> CURRENT = new HashMap<>();

    private ClassConcord() {
    }

    /** The classes whose Concord applies: those of the group, only if the group is two or more. */
    public static Set<PlayerClass> concord(int members, Set<PlayerClass> classes) {
        return members < 2 || classes.isEmpty() ? EnumSet.noneOf(PlayerClass.class) : EnumSet.copyOf(classes);
    }

    public static boolean pillars(Set<PlayerClass> concord) {
        return concord.size() == PlayerClass.values().length;
    }

    public static Set<PlayerClass> of(ServerPlayer player) {
        return CURRENT.getOrDefault(player.getUUID(), Set.of());
    }

    /** Once a second: who fights beside the player, and the Concord they make. */
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % 20 != 7) return;
        Set<PlayerClass> classes = EnumSet.noneOf(PlayerClass.class);
        int members = 1;
        ClassState.classOf(player).ifPresent(classes::add);
        for (ServerPlayer other : player.serverLevel().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(RANGE), p -> p != player)) {
            members++;
            ClassState.classOf(other).ifPresent(classes::add);
        }
        for (CompanionEntity c : player.serverLevel().getEntitiesOfClass(CompanionEntity.class, player.getBoundingBox().inflate(RANGE),
                c -> player.getUUID().equals(c.owner()))) {
            members++;
            classes.add(c.bearer());
        }
        Set<PlayerClass> concord = concord(members, classes);
        Set<PlayerClass> before = CURRENT.put(player.getUUID(), concord);
        if (!concord.equals(before) && !concord.isEmpty()) {
            String names = concord.stream().map(c -> Component.translatable(c.translationKey()).getString()).collect(Collectors.joining(", "));
            player.displayClientMessage(Component.translatable(pillars(concord) ? "message.sofe.concord.pillars" : "message.sofe.concord", names)
                    .withStyle(ChatFormatting.GOLD), true);
        }
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(ARMOR_ID);
            if (concord.contains(PlayerClass.KNIGHT)) {
                armor.addTransientModifier(new AttributeModifier(ARMOR_ID, "SoFE Concord: Knight", KNIGHT_ARMOR, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
        if (concord.contains(PlayerClass.SORCERESS)) {
            CombatCapability.get(player).ifPresent(c -> c.resource().ifPresent(r -> {
                r.gain((float) (r.rules().regenPerSecond() * SORCERESS_REGEN));
                c.markDirty();
            }));
        }
    }

    /** The Necromancer's life steal, the Thief's critical blows and the Five Pillars' strength. */
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || event.getEntity() instanceof Ally) return;
        Set<PlayerClass> concord = of(player);
        if (concord.isEmpty()) return;
        float amount = event.getAmount();
        if (pillars(concord)) amount *= 1 + (float) PILLARS_DAMAGE;
        if (concord.contains(PlayerClass.THIEF) && player.getRandom().nextDouble() < THIEF_CRIT) amount *= 1.5f;
        if (concord.contains(PlayerClass.NECROMANCER)) player.heal(amount * (float) lifeSteal(player.getHealth() / player.getMaxHealth()));
        event.setAmount(amount);
    }

    /** The Necromancer's Concord: 3% of the damage dealt heals, 5% for one below a quarter of their health. */
    public static double lifeSteal(float healthFraction) {
        return healthFraction < NECROMANCER_LAST_STAND ? NECROMANCER_LAST_STAND_STEAL : NECROMANCER_LIFE_STEAL;
    }

    public static void onLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        CURRENT.remove(event.getEntity().getUUID());
    }

    /** The King's Concord: more from every healing. */
    public static void onHeal(LivingHealEvent event) {
        LivingEntity healed = event.getEntity();
        if (healed instanceof ServerPlayer player && of(player).contains(PlayerClass.KING)) event.setAmount(event.getAmount() * (1 + (float) KING_HEALING));
    }
}
