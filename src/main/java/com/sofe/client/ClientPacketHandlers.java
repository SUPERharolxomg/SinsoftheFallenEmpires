package com.sofe.client;

import com.sofe.client.hud.RegionTitleOverlay;
import com.sofe.client.screen.ClassSelectScreen;
import com.sofe.player.PlayerClass;
import com.sofe.world.region.Region;
import net.minecraft.client.Minecraft;

import java.util.Optional;

/** Client side of the packets. Only called through DistExecutor, so servers never load it. */
public final class ClientPacketHandlers {

    private ClientPacketHandlers() {
    }

    public static void showRegionTitle(Region region, com.sofe.world.lock.RegionStatus status, boolean deep) {
        RegionTitleOverlay.show(region, status, deep);
    }

    public static void showDialogue(com.sofe.network.DialogueLinePacket line) {
        Minecraft minecraft = Minecraft.getInstance();
        if (line.isClose()) {
            if (minecraft.screen instanceof com.sofe.client.screen.DialogueScreen) minecraft.setScreen(null);
        } else if (minecraft.screen instanceof com.sofe.client.screen.DialogueScreen open) {
            open.show(line);
        } else {
            minecraft.setScreen(new com.sofe.client.screen.DialogueScreen(line));
        }
    }

    public static void openWaystones(java.util.List<com.sofe.network.OpenWaystonesPacket.Entry> entries) {
        Minecraft.getInstance().setScreen(new com.sofe.client.screen.WaystoneScreen(entries));
    }

    public static void openClassSelect() {
        if (ClientClassData.get().isEmpty()) {
            Minecraft.getInstance().setScreen(new ClassSelectScreen());
        }
    }

    public static void syncClass(Optional<PlayerClass> playerClass) {
        ClientClassData.set(playerClass);
        Minecraft minecraft = Minecraft.getInstance();
        if (playerClass.isPresent() && minecraft.screen instanceof ClassSelectScreen) {
            minecraft.setScreen(null);
        }
    }
}
