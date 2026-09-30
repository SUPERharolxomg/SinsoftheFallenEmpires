package com.sofe.client;

import com.sofe.network.SyncStoryPacket;
import com.sofe.world.lock.LockAccess;
import com.sofe.world.region.RegionBounds;
import com.sofe.world.region.RegionMap;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Optional;

/** The client side of the locks: the synced layout plus the act and bypass flag from the story sync. */
public final class ClientLockData implements LockAccess.ClientView {
    private static final ClientLockData INSTANCE = new ClientLockData();
    private static volatile RegionMap map;

    private ClientLockData() {
    }

    public static void install() {
        LockAccess.setClientView(INSTANCE);
    }

    public static void setLayout(List<RegionBounds> bounds) {
        map = bounds.isEmpty() ? null : new RegionMap(bounds);
        redrawVeil();
    }

    public static void clear() {
        map = null;
    }

    /** The Veil's color and collision follow the act: redraw the chunks when it changes. */
    static void storyChanged(Optional<SyncStoryPacket> before, SyncStoryPacket after) {
        if (before.isEmpty() || before.get().act() != after.act() || before.get().bypassLocks() != after.bypassLocks()) {
            redrawVeil();
        }
    }

    private static void redrawVeil() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.levelRenderer != null && minecraft.level != null) minecraft.levelRenderer.allChanged();
    }

    @Override
    public Optional<RegionMap> map() {
        return Optional.ofNullable(map);
    }

    @Override
    public int act() {
        return ClientStoryData.get().map(SyncStoryPacket::act).orElse(1);
    }

    @Override
    public boolean bypass() {
        return ClientStoryData.get().map(SyncStoryPacket::bypassLocks).orElse(false);
    }
}
