package com.sofe.pact;

import com.sofe.config.SoFEConfig;
import com.sofe.entity.boss.SoFEBossEntity;
import com.sofe.gear.GearData;
import com.sofe.gear.GearNbt;
import com.sofe.gear.Rarity;
import com.sofe.item.Soulbound;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * The co-op rules that act in the game (docs/Anexos.md, A5 and A6):
 * <ul>
 * <li><b>Revive:</b> in a boss arena, a participant who would die is downed instead while another participant still
 * stands; an ally crouching beside them for a few seconds lifts them, else they fall when the time runs out.</li>
 * <li><b>One copy equipped:</b> two copies of the same Relic cannot be worn at once.</li>
 * <li><b>Pact only:</b> with relicBinding = pact_only, a bound Relic or Legacy piece can be picked up only by its owner
 * and their Pact.</li>
 * </ul>
 */
public final class PactHooks {
    private static final Map<UUID, Integer> REVIVING = new HashMap<>();
    private static final Set<UUID> FALLING = new HashSet<>();

    private PactHooks() {
    }

    // ------------------------------------------------------------------ downed and revive

    /** The boss fight the player takes part in, if any. */
    static Optional<SoFEBossEntity> fightOf(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(SoFEBossEntity.class, new AABB(player.blockPosition()).inflate(72),
                b -> b.isAlive() && b.isFighting() && b.participants().contains(player.getUUID())).stream().findFirst();
    }

    /** The other participants of the fight still standing (alive and not downed). */
    static List<ServerPlayer> standing(ServerPlayer player, SoFEBossEntity boss) {
        return player.serverLevel().getEntitiesOfClass(ServerPlayer.class, new AABB(boss.blockPosition()).inflate(72),
                p -> p != player && p.isAlive() && !p.isSpectator() && boss.participants().contains(p.getUUID()) && !Downed.isDowned(p));
    }

    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !SoFEConfig.SERVER.revive.get()) return;
        if (FALLING.remove(player.getUUID()) || Downed.isDowned(player)) return;
        Optional<SoFEBossEntity> boss = fightOf(player);
        if (boss.isEmpty() || standing(player, boss.get()).isEmpty()) return; // alone in the arena: the fall is real
        event.setCanceled(true);
        player.setHealth(1);
        Downed.LEFT.put(player.getUUID(), SoFEConfig.SERVER.downedSeconds.get() * 20);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 5, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false));
        player.displayClientMessage(Component.translatable("message.sofe.downed").withStyle(ChatFormatting.RED), false);
        for (ServerPlayer ally : standing(player, boss.get())) {
            ally.displayClientMessage(Component.translatable("message.sofe.downed.ally", player.getDisplayName()).withStyle(ChatFormatting.GOLD), false);
        }
    }

    /** A downed player takes no more harm (the boss's blows pass over them) and strikes no one. */
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && Downed.isDowned(player)
                && !event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
        }
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && Downed.isDowned(player)) event.setCanceled(true);
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        Integer left = Downed.LEFT.get(player.getUUID());
        if (left == null) return;
        if (player.tickCount % 20 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 5, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 4, false, false));
        }
        Optional<SoFEBossEntity> boss = fightOf(player);
        List<ServerPlayer> allies = boss.map(b -> standing(player, b)).orElse(List.of());
        // an ally crouching within two blocks lifts them
        boolean helped = allies.stream().anyMatch(a -> a.isCrouching() && a.distanceToSqr(player) <= 2.5 * 2.5);
        int need = SoFEConfig.SERVER.reviveSeconds.get() * 20;
        if (helped) {
            int done = REVIVING.merge(player.getUUID(), 1, Integer::sum);
            if (done % 10 == 0) {
                player.displayClientMessage(Component.translatable("message.sofe.reviving", Math.min(100, done * 100 / need)).withStyle(ChatFormatting.GREEN), true);
            }
            if (done >= need) {
                revive(player, allies);
                return;
            }
        } else {
            REVIVING.remove(player.getUUID());
            if (player.tickCount % 20 == 0) {
                player.displayClientMessage(Component.translatable("message.sofe.downed.left", (left + 19) / 20).withStyle(ChatFormatting.RED), true);
            }
        }
        // the fight is over (won or lost) or no one is left to help: up, or the fall
        if (boss.isEmpty()) {
            revive(player, allies);
            return;
        }
        if (allies.isEmpty() || --left <= 0) {
            fall(player);
            return;
        }
        Downed.LEFT.put(player.getUUID(), left);
    }

    private static void revive(ServerPlayer player, List<ServerPlayer> allies) {
        Downed.LEFT.remove(player.getUUID());
        REVIVING.remove(player.getUUID());
        player.setHealth(player.getMaxHealth() * 0.3f);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.WEAKNESS);
        player.removeEffect(MobEffects.GLOWING);
        player.displayClientMessage(Component.translatable("message.sofe.revived").withStyle(ChatFormatting.GREEN), false);
        allies.forEach(a -> a.displayClientMessage(Component.translatable("message.sofe.revived.ally", player.getDisplayName()).withStyle(ChatFormatting.GREEN), true));
    }

    private static void fall(ServerPlayer player) {
        Downed.LEFT.remove(player.getUUID());
        REVIVING.remove(player.getUUID());
        FALLING.add(player.getUUID());
        player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);
        FALLING.remove(player.getUUID());
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        Downed.LEFT.remove(id);
        REVIVING.remove(id);
    }

    // ------------------------------------------------------------------ Relics

    static boolean isRelic(ItemStack stack) {
        return !stack.isEmpty() && GearNbt.read(stack).map(GearData::rarity).map(r -> r == Rarity.RELIC).orElse(false);
    }

    /** Two copies of the same Relic are never worn at once: the second goes back to the pack (A6, "One copy equipped"). */
    public static void onEquip(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack worn = event.getTo();
        if (!isRelic(worn)) return;
        for (EquipmentSlot other : EquipmentSlot.values()) {
            if (other == event.getSlot()) continue;
            ItemStack o = player.getItemBySlot(other);
            if (isRelic(o) && ItemStack.isSameItem(o, worn) && sameRelic(o, worn)) {
                ItemStack back = worn.copy();
                player.setItemSlot(event.getSlot(), ItemStack.EMPTY);
                if (!player.getInventory().add(back)) player.drop(back, false);
                player.displayClientMessage(Component.translatable("message.sofe.relic.one_copy").withStyle(ChatFormatting.RED), true);
                return;
            }
        }
    }

    private static boolean sameRelic(ItemStack a, ItemStack b) {
        return GearNbt.read(a).map(GearData::relic).equals(GearNbt.read(b).map(GearData::relic));
    }

    /** With relicBinding = pact_only, a bound Relic or Legacy piece goes only to its owner and their Pact. */
    public static void onPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || Soulbound.relicPolicy() != Soulbound.BindPolicy.PACT_ONLY) return;
        ItemStack stack = event.getItem().getItem();
        boolean bindable = GearNbt.read(stack).map(GearData::rarity).map(r -> r == Rarity.RELIC || r == Rarity.LEGACY).orElse(false);
        if (!bindable) return;
        Optional<UUID> owner = GearNbt.owner(stack);
        if (owner.isEmpty() || owner.get().equals(player.getUUID())) return;
        boolean pact = PactData.get(player.server).of(owner.get()).map(p -> p.has(player.getUUID())).orElse(false);
        if (!pact) {
            event.setCanceled(true);
            if (player.tickCount % 20 == 0) player.displayClientMessage(Component.translatable("message.sofe.relic.pact_only").withStyle(ChatFormatting.RED), true);
        }
    }

    /** For GameTests. */
    public static void forget() {
        Downed.LEFT.clear();
        REVIVING.clear();
        FALLING.clear();
    }
}
