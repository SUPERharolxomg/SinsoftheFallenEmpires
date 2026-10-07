package com.sofe.client;

import com.sofe.network.SyncStoryPacket;
import com.sofe.quest.QuestEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;

/**
 * The Night of the Eclipse as the Bearer lives it (the first steps of Act I, data/sofe/quests/act1_eclipse.json):
 * until the Void is driven out of the lower district, night has fallen over Sulthari with the dark disc of the new
 * moon overhead, the air is thick with the Void's violet haze and ash and its sparks drift down through the cracks
 * in the sky. Only this player's view changes: the server's time, its mobs and the other players' skies do not.
 */
public final class EclipseSky {
    /** The steps of the first quest under the eclipse: learning the first skill and driving out the Void (0-based). */
    private static final int LAST_ECLIPSE_STEP = 1;
    /** Midnight under a new moon (phase 4 of 8), the moon a dark disc. */
    private static final long ECLIPSE_TIME = 4 * 24000L + 18000L;
    private static final float HAZE_R = 0.20f, HAZE_G = 0.07f, HAZE_B = 0.27f, HAZE = 0.75f;
    private static final float FOG_FAR = 72f;

    private EclipseSky() {
    }

    /** Whether this player is still under the eclipse. */
    public static boolean active() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.level.dimension() != Level.OVERWORLD) return false;
        return ClientStoryData.get().flatMap(story -> story.quests().stream()
                .filter(q -> q.id().equals(QuestEngine.FIRST_QUEST)).findFirst())
                .map(q -> !q.completed() && q.step() <= LAST_ECLIPSE_STEP).orElse(false);
    }

    /** The night held over the city (the server sends its own time each second; it is set again every tick). */
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !active()) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        level.setDayTime(ECLIPSE_TIME);
        if (mc.isPaused() || mc.player == null) return;
        var random = level.getRandom();
        for (int i = 0; i < 3; i++) { // ash and the Void's sparks falling through the cracks in the sky
            double x = mc.player.getX() + (random.nextDouble() - 0.5) * 32, z = mc.player.getZ() + (random.nextDouble() - 0.5) * 32;
            double y = mc.player.getY() + 4 + random.nextDouble() * 10;
            level.addParticle(random.nextInt(4) == 0 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.WHITE_ASH, x, y, z, 0, -0.04, 0);
        }
    }

    /** The Void's violet haze over the horizon and in the fog. */
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (!active()) return;
        event.setRed(Mth.lerp(HAZE, event.getRed(), HAZE_R));
        event.setGreen(Mth.lerp(HAZE, event.getGreen(), HAZE_G));
        event.setBlue(Mth.lerp(HAZE, event.getBlue(), HAZE_B));
    }

    /** The haze closes in a little: the far districts fade into it. */
    public static void onFog(ViewportEvent.RenderFog event) {
        if (!active() || event.getMode() != net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN) return;
        if (event.getFarPlaneDistance() <= FOG_FAR) return;
        event.setFarPlaneDistance(FOG_FAR);
        event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), FOG_FAR * 0.25f));
        event.setCanceled(true);
    }
}
