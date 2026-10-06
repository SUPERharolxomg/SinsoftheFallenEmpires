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

    /** A merchant screen, or a refresh of the one already open for the same merchant. */
    public static void openMerchant(com.sofe.network.OpenMerchantPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof com.sofe.client.screen.MerchantScreen open && open.npc().equals(packet.npc())) {
            open.update(packet);
        } else {
            minecraft.setScreen(new com.sofe.client.screen.MerchantScreen(packet));
        }
    }

    public static void openStation(com.sofe.crafting.StationRecipe.Kind kind) {
        Minecraft.getInstance().setScreen(new com.sofe.client.screen.StationScreen(kind));
    }

    /** A scene of the story on this player's screen (SceneService); one the client does not know ends at once. */
    public static void playScene(String scene) {
        if ("crowned_in_ash".equals(scene)) {
            Minecraft.getInstance().setScreen(new com.sofe.client.screen.CrownedInAshScreen());
        } else if ("the_ending".equals(scene)) {
            Minecraft.getInstance().setScreen(new com.sofe.client.screen.EndingScreen());
        } else {
            com.sofe.network.SoFENetwork.sendToServer(new com.sofe.network.ScenePackets.Done());
        }
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
