package com.sofe.client;

import com.sofe.SoFEMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Dev check (./gradlew runClient -PdeathCheck): in the "shots" world, the player dies, comes back (DeathSpectate),
 * then presses the skill tree key and the character key; what opens, the Bearer the client knows and the game mode are
 * written to run/deathcheck.log, then the game quits. It reproduces "after dying the skill tree and stats will not open".
 */
public final class DeathKeysCheck {
    private static int ticks;
    private static final List<String> LOG = new ArrayList<>();

    private DeathKeysCheck() {
    }

    public static boolean enabled() {
        return System.getProperty("sofe.deathCheck") != null;
    }

    /** What the server keeps for the player: level, Dinars, act. */
    private static String kept(Minecraft mc) {
        var server = mc.getSingleplayerServer();
        ServerPlayer p = server == null ? null : server.getPlayerList().getPlayer(mc.player.getUUID());
        if (p == null) return "?";
        return "level=" + com.sofe.progression.ProgressionCapability.get(p).map(g -> g.level()).orElse(-1)
                + " dinars=" + com.sofe.economy.EconomyCapability.get(p).map(e -> e.dinars()).orElse(-1L)
                + " act=" + com.sofe.story.StoryCapability.get(p).map(st -> st.act()).orElse(-1);
    }

    private static void note(Minecraft mc, String what) {
        LOG.add(String.format("t=%d %s | %s | screen=%s class=%s progress=%s mode=%s alive=%s", ticks, what, kept(mc),
                mc.screen == null ? "none" : mc.screen.getClass().getSimpleName(), ClientClassData.get(), ClientProgressData.get().isPresent(),
                mc.gameMode == null ? "?" : mc.gameMode.getPlayerMode(), mc.player != null && mc.player.isAlive()));
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        if (mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen) mc.setScreen(null);
        ticks++;
        var server = mc.getSingleplayerServer();
        switch (ticks) {
            case 150 -> { // a Bearer, standing in survival, with nothing open
                mc.setScreen(null);
                server.execute(() -> {
                    ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                    if (p == null) return;
                    com.sofe.player.PlayerClassCapability.get(p).ifPresent(c -> c.set(com.sofe.player.PlayerClass.KNIGHT));
                    com.sofe.progression.ProgressionCapability.get(p).ifPresent(g -> g.load(7, 0, 3, 5, true));
                    com.sofe.economy.EconomyCapability.get(p).ifPresent(e -> e.addDinars(500));
                    com.sofe.story.StoryCapability.get(p).ifPresent(st -> st.advanceTo(2));
                    com.sofe.player.ClassSelectionHandler.sync(p);
                    com.sofe.progression.ProgressionHandler.sync(p);
                    p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                });
            }
            case 190 -> mc.setScreen(null);
            case 200 -> note(mc, "before death");
            case 210 -> {
                KeyMapping.click(SoFEKeys.SKILL_TREE.getKey());
            }
            case 212 -> {
                note(mc, "K before death");
                mc.setScreen(null);
            }
            case 220 -> server.execute(() -> {
                ServerPlayer p = server.getPlayerList().getPlayer(mc.player.getUUID());
                if (p != null) {
                    p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                    p.hurt(p.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                }
            });
            case 300 -> note(mc, "watching");
            case 300 + 20 * 12 -> note(mc, "back after the countdown");
            case 300 + 20 * 12 + 10 -> KeyMapping.click(SoFEKeys.SKILL_TREE.getKey());
            case 300 + 20 * 12 + 12 -> {
                note(mc, "K after death");
                mc.setScreen(null);
            }
            case 300 + 20 * 12 + 20 -> KeyMapping.click(SoFEKeys.CHARACTER.getKey());
            case 300 + 20 * 12 + 22 -> {
                note(mc, "I after death");
                net.minecraft.client.Screenshot.grab(mc.gameDirectory, "deathcheck_character_sheet.png", mc.getMainRenderTarget(), m -> { });
                try {
                    Files.write(Path.of(mc.gameDirectory.getPath(), "deathcheck.log"), LOG);
                } catch (IOException e) {
                    SoFEMod.LOGGER.error("deathcheck", e);
                }
                mc.stop();
            }
            default -> {
            }
        }
    }
}
