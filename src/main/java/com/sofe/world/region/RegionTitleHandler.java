package com.sofe.world.region;

import com.sofe.network.RegionEnteredPacket;
import com.sofe.network.SoFENetwork;
import com.sofe.quest.QuestEngine;
import com.sofe.quest.QuestEvent;
import com.sofe.world.SoFEWorld;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Shows "Welcome to &lt;region&gt;" when a player crosses into another region of a journey,
 * and once when they join (docs/Mundo.md, W3). In the Nether it names the region above at 1:8,
 * as "the Burning Deep beneath" it.
 */
public final class RegionTitleHandler {
    private static final int CHECK_EVERY_TICKS = 10;
    private static final RegionTracker TRACKER = new RegionTracker();

    private RegionTitleHandler() {
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % CHECK_EVERY_TICKS != 0) return;
        Level level = player.level();
        if (level.dimension() != Level.OVERWORLD && level.dimension() != Level.NETHER) return;
        boolean deep = level.dimension() == Level.NETHER; // the Burning Deep beneath the region, at 1:8
        int scale = com.sofe.world.lock.LockAccess.scale(level);

        SoFEWorld.regionMap(player.server).ifPresent(map ->
                TRACKER.update(player.getUUID(), map.regionAt(player.getBlockX() * scale, player.getBlockZ() * scale))
                        .ifPresent(region -> {
                            SoFENetwork.sendTo(player, new RegionEnteredPacket(region, com.sofe.story.StoryCapability.get(player)
                                    .map(story -> com.sofe.world.lock.RegionStatus.of(region, story)).orElse(com.sofe.world.lock.RegionStatus.NONE), deep));
                            if (!deep) QuestEngine.event(player, new QuestEvent.EnteredRegion(region.id()));
                            com.sofe.story.SoFEAdvancements.award(player, deep ? "story/burning_deep" : "story/enter_" + region.id());
                        }));
    }

    /** A new dimension shows the title again. */
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        TRACKER.forget(event.getEntity().getUUID());
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        TRACKER.forget(event.getEntity().getUUID());
    }
}
