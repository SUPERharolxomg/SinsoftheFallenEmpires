package com.sofe.world.region;

import com.sofe.network.RegionEnteredPacket;
import com.sofe.network.SoFENetwork;
import com.sofe.world.SoFEWorld;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Shows "Welcome to &lt;region&gt;" when a player crosses into another region of a journey,
 * and once when they join (docs/Mundo.md, W3). Only the overworld has regions for now;
 * the Nether mapping comes with the Burning Deep.
 */
public final class RegionTitleHandler {
    private static final int CHECK_EVERY_TICKS = 10;
    private static final RegionTracker TRACKER = new RegionTracker();

    private RegionTitleHandler() {
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_EVERY_TICKS != 0 || player.level().dimension() != Level.OVERWORLD) return;

        SoFEWorld.regionMap(player.server).ifPresent(map ->
                TRACKER.update(player.getUUID(), map.regionAt(player.getBlockX(), player.getBlockZ()))
                        .ifPresent(region -> SoFENetwork.sendTo(player, new RegionEnteredPacket(region))));
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        TRACKER.forget(event.getEntity().getUUID());
    }
}
