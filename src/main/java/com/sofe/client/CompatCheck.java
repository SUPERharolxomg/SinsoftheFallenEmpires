package com.sofe.client;

import com.sofe.SoFEMod;
import com.sofe.config.SoFEConfig;
import com.sofe.world.SoFEWorld;
import com.sofe.world.lock.LockAccess;
import com.sofe.world.lock.RegionLocks;
import com.sofe.world.region.RegionMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Dev check (-Dsofe.compatCheck, scripts/pack_check.py --check compat): in a journey, a Bearer in survival and with no
 * operator's pass is moved into a sealed region every way the pack allows: the vanilla teleport, and, when FTB
 * Essentials is installed, a home set inside Nordrath, a random teleport, /back and /spawn. After each the Bearer must
 * stand in an open region again (RegionEnforcer). run/compatcheck.log says what happened, then the game quits.
 */
public final class CompatCheck {
    private static final int WAIT = 80;
    private static final List<String> LOG = new ArrayList<>();
    private static final List<Step> STEPS = new ArrayList<>();
    private static int ticks, step, waitUntil;

    private record Step(String name, java.util.function.Consumer<ServerPlayer> act) {
    }

    private CompatCheck() {
    }

    public static boolean enabled() {
        return System.getProperty("sofe.compatCheck") != null;
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
        mc.options.pauseOnLostFocus = false;
        if (mc.screen != null) mc.setScreen(null);
        MinecraftServer server = mc.getSingleplayerServer();
        ticks++;
        if (ticks == 150) server.execute(() -> setUp(server, player(server, mc)));
        if (ticks < 220) return;
        if (step > 0 && ticks == waitUntil - WAIT + 10) {
            String name = STEPS.get(step - 1).name();
            server.execute(() -> report(server, player(server, mc), name + " | landed"));
        }
        if (ticks < waitUntil) return;
        if (step > 0 && ticks == waitUntil) {
            String name = STEPS.get(step - 1).name();
            server.execute(() -> report(server, player(server, mc), name + " | 4 s later"));
        }
        if (step < STEPS.size()) {
            Step s = STEPS.get(step++);
            server.execute(() -> s.act().accept(player(server, mc)));
            waitUntil = ticks + WAIT;
            return;
        }
        if (step == STEPS.size() && ticks >= waitUntil + 10) {
            Screenshot.grab(mc.gameDirectory, "compatcheck.png", mc.getMainRenderTarget(), m -> { });
            try {
                Files.write(Path.of(mc.gameDirectory.getPath(), "compatcheck.log"), LOG);
            } catch (IOException e) {
                SoFEMod.LOGGER.error("compatcheck", e);
            }
            step++;
            mc.stop();
        }
    }

    private static ServerPlayer player(MinecraftServer server, Minecraft mc) {
        return server.getPlayerList().getPlayer(mc.player.getUUID());
    }

    private static void setUp(MinecraftServer server, ServerPlayer p) {
        if (p == null) return;
        SoFEConfig.SERVER.opsBypass.set(false); // a host is an operator: the check plays as any other Bearer
        p.setGameMode(GameType.SURVIVAL);
        p.setInvulnerable(true);
        ModList mods = ModList.get();
        LOG.add("mods: essential=" + mods.isLoaded("essential") + " ftbessentials=" + mods.isLoaded("ftbessentials")
                + " ftblibrary=" + mods.isLoaded("ftblibrary") + " | act " + LockAccess.act(p) + " | bypass " + LockAccess.bypasses(p));
        STEPS.add(new Step("vanilla teleport into Nordrath", q -> to(q, 0, -3000)));
        if (mods.isLoaded("ftbessentials")) {
            STEPS.add(new Step("sethome in Nordrath (creative, then back home in survival)", q -> {
                q.setGameMode(GameType.CREATIVE);
                to(q, 0, -3000);
                run(server, q, "sethome compat_north");
                to(q, 0, 12);
                q.setGameMode(GameType.SURVIVAL);
            }));
            STEPS.add(new Step("/home compat_north", q -> run(server, q, "home compat_north")));
            STEPS.add(new Step("/rtp", q -> run(server, q, "rtp")));
            STEPS.add(new Step("/back", q -> run(server, q, "back")));
            STEPS.add(new Step("/spawn", q -> run(server, q, "spawn")));
        }
    }

    private static void to(ServerPlayer p, int x, int z) {
        ServerLevel level = p.serverLevel();
        level.getChunk(x >> 4, z >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) + 1;
        p.teleportTo(level, x + 0.5, y, z + 0.5, p.getYRot(), p.getXRot());
    }

    private static void run(MinecraftServer server, ServerPlayer p, String command) {
        int result = server.getCommands().performPrefixedCommand(p.createCommandSourceStack(), command);
        LOG.add("  ran /" + command + " -> " + result);
    }

    /** Where the Bearer stands after a step, and whether they may stand there. */
    private static void report(MinecraftServer server, ServerPlayer p, String name) {
        if (p == null) return;
        RegionMap map = SoFEWorld.regionMap(server).orElse(null);
        int x = p.getBlockX(), z = p.getBlockZ();
        boolean allowed = map == null || RegionLocks.canBeAt(map, x, z, LockAccess.act(p));
        LOG.add(name + ": at " + x + ", " + p.getBlockY() + ", " + z + " in " + (map == null ? "?" : map.regionAt(x, z).id())
                + " | allowed there: " + allowed + (allowed || name.endsWith("landed") ? "" : " (FAIL: the seal did not hold)"));
    }
}
