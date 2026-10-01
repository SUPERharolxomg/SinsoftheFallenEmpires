package com.sofe.client.hud;

import com.sofe.world.lock.RegionStatus;
import com.sofe.world.region.Region;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/**
 * "Welcome to" in small letters and the region name in large gold letters, fading in and out
 * near the top of the screen. Drawn by the mod instead of the vanilla title, because vanilla
 * only puts the small line below the large one.
 */
public final class RegionTitleOverlay {
    private static final long FADE_IN_MS = 600;
    private static final long HOLD_MS = 2800;
    private static final long FADE_OUT_MS = 1000;
    private static final int GOLD = 0xE8B64A;
    private static final int PARCHMENT = 0xE6DCC8;

    private static Region region;
    private static RegionStatus status = RegionStatus.NONE;
    private static boolean deep;
    private static long shownAt;

    private RegionTitleOverlay() {
    }

    public static void show(Region newRegion, RegionStatus newStatus, boolean inDeep) {
        region = newRegion;
        status = newStatus;
        deep = inDeep;
        shownAt = System.currentTimeMillis();
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        if (region == null) return;
        long elapsed = System.currentTimeMillis() - shownAt;
        if (elapsed > FADE_IN_MS + HOLD_MS + FADE_OUT_MS) {
            region = null;
            return;
        }
        float alpha = elapsed < FADE_IN_MS ? elapsed / (float) FADE_IN_MS
                : elapsed < FADE_IN_MS + HOLD_MS ? 1f
                : 1f - (elapsed - FADE_IN_MS - HOLD_MS) / (float) FADE_OUT_MS;
        int a = Mth.clamp((int) (alpha * 255), 0, 255);
        if (a < 8) return; // fonts render fully opaque below this alpha

        Font font = gui.getFont();
        int top = height / 5;
        Component welcome = Component.translatable(deep ? "gui.sofe.region_title.welcome_deep" : "gui.sofe.region_title.welcome");
        graphics.drawCenteredString(font, welcome, width / 2, top, (a << 24) | PARCHMENT);

        Component name = Component.translatable(region.translationKey());
        float scale = 2.5f;
        graphics.pose().pushPose();
        graphics.pose().translate(width / 2f, top + 12, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawCenteredString(font, name, 0, 0, (a << 24) | GOLD);
        graphics.pose().popPose();

        if (status != RegionStatus.NONE) {
            int color = status == RegionStatus.SEALED ? 0xB04A8C : 0x9FD86A;
            graphics.drawCenteredString(font, Component.translatable(status.translationKey()), width / 2, top + 12 + (int) (font.lineHeight * scale) + 4, (a << 24) | color);
        }
    }
}
