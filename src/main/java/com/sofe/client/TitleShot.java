package com.sofe.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraftforge.event.TickEvent;

/**
 * Dev check (-Dsofe.titleShot, scripts/pack_check.py --check title): five seconds after the first screen opens, a
 * screenshot of whatever menu the pack shows (the SoFE title screen, or another mod's over it), named after the
 * screen's class in run/titleshot.log; with a mod that decorates the vanilla title (Essential), the classic menu it
 * opens too (titleshot_classic.png); then the game quits.
 */
public final class TitleShot {
    private static int ticks;

    private TitleShot() {
    }

    /** -Dsofe.joinShot: ten seconds after joining a server, a screenshot and what the client sees (joinshot.log). */
    public static boolean joinEnabled() {
        return System.getProperty("sofe.joinShot") != null;
    }

    private static int joined;

    public static void onJoinTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        joined++;
        if (joined == 200) {
            Screenshot.grab(mc.gameDirectory, "joinshot.png", mc.getMainRenderTarget(), m -> { });
            String line = "joined " + (mc.getCurrentServer() == null ? "?" : mc.getCurrentServer().ip) + " | at " + mc.player.blockPosition().toShortString()
                    + " | screen " + (mc.screen == null ? "none" : mc.screen.getClass().getSimpleName())
                    + " | story synced " + com.sofe.client.ClientStoryData.get().isPresent() + "\n";
            try {
                java.nio.file.Files.writeString(mc.gameDirectory.toPath().resolve("joinshot.log"), line);
            } catch (java.io.IOException ignored) {
            }
        }
        if (joined == 220) mc.stop();
    }

    public static boolean enabled() {
        return System.getProperty("sofe.titleShot") != null;
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null || mc.getOverlay() != null) return;
        ticks++;
        if (ticks == 100) {
            Screenshot.grab(mc.gameDirectory, "titleshot.png", mc.getMainRenderTarget(), m -> { });
            try {
                java.nio.file.Files.writeString(mc.gameDirectory.toPath().resolve("titleshot.log"), mc.screen.getClass().getName() + "\n");
            } catch (java.io.IOException ignored) {
            }
        }
        boolean more = !TitleScreenHandler.titleMods().isEmpty();
        if (more && ticks == 110) TitleScreenHandler.openVanillaTitle(mc); // the button for Essential's menu
        if (more && ticks == 210) Screenshot.grab(mc.gameDirectory, "titleshot_classic.png", mc.getMainRenderTarget(), m -> { });
        if (ticks == (more ? 230 : 120)) mc.stop();
    }
}
