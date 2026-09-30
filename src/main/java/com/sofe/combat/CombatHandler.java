package com.sofe.combat;

import com.sofe.network.SoFENetwork;
import com.sofe.network.SyncCombatPacket;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.player.PlayerClassData;
import com.sofe.skill.SkillCatalog;
import com.sofe.skill.SkillInfo;
import com.sofe.skill.data.SkillDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.List;
import java.util.Optional;

/**
 * Server side of the combat state: sets the resource rules from the player's class, regenerates
 * the resource every tick, and keeps the client's HUD up to date.
 */
public final class CombatHandler {
    /** How often a regenerating resource is resent to the client (the HUD does not need every tick). */
    private static final int SYNC_EVERY_TICKS = 10;

    private CombatHandler() {
    }

    /** Applies the class's resource rules (after joining, choosing a Bearer, respawning or /reload). */
    public static void refresh(ServerPlayer player) {
        Optional<PlayerClass> playerClass = PlayerClassCapability.get(player).flatMap(PlayerClassData::get);
        CombatCapability.get(player).ifPresent(combat -> {
            playerClass.flatMap(SkillDataManager::forClass).ifPresent(data -> combat.configure(data.resource()));
            sync(player);
        });
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) refresh(player);
    }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refresh(player);
            if (!event.isEndConquered()) {
                CombatCapability.get(player).ifPresent(CombatData::resetAfterDeath);
                sync(player);
            }
        }
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            refresh(event.getPlayer());
        } else {
            event.getPlayerList().getPlayers().forEach(CombatHandler::refresh);
        }
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        CombatCapability.get(player).ifPresent(combat -> combat.resource().ifPresent(resource -> {
            float before = resource.current();
            resource.tick();
            if (resource.current() != before && player.tickCount % SYNC_EVERY_TICKS == 0) {
                combat.markDirty();
            }
            if (combat.takeDirty()) {
                send(player, combat);
            }
        }));
    }

    public static void sync(ServerPlayer player) {
        CombatCapability.get(player).ifPresent(combat -> {
            combat.takeDirty();
            send(player, combat);
        });
    }

    private static void send(ServerPlayer player, CombatData combat) {
        Optional<PlayerClass> playerClass = PlayerClassCapability.get(player).flatMap(PlayerClassData::get);
        if (playerClass.isEmpty() || combat.resource().isEmpty()) return;
        ResourcePool resource = combat.resource().get();
        List<SyncCombatPacket.SlotCooldown> slots = SkillCatalog.defaultLoadout(playerClass.get()).stream()
                .map(SkillInfo::id)
                .map(id -> new SyncCombatPacket.SlotCooldown(id, combat.cooldowns().end(id), (int) combat.cooldowns().duration(id)))
                .toList();
        SoFENetwork.sendTo(player, new SyncCombatPacket(playerClass.get().resource(), resource.current(), resource.max(),
                combat.runes().current(), slots));
    }
}
