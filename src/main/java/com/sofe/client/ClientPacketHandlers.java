package com.sofe.client;

import com.sofe.client.hud.RegionTitleOverlay;
import com.sofe.world.region.Region;

/** Client side of the packets. Only called through DistExecutor, so servers never load it. */
public final class ClientPacketHandlers {

    private ClientPacketHandlers() {
    }

    public static void showRegionTitle(Region region) {
        RegionTitleOverlay.show(region);
    }
}
