package com.sofe.client.hud;

import com.sofe.network.InvasionHudPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/**
 * The Void's invasion in the top right corner of the screen: its name, the wave, a bar of the fallen and their count
 * ("Wave 2/5", "14/80"), or the next wave coming. It stands below the effect icons when there are any.
 */
public final class InvasionHudOverlay {
    private static final int W = 128, H = 38, RIGHT = 6, TOP = 6;
    private static final int VIOLET = 0xFF8A4FD8, PANEL = 0xB0120A1A, BAR_BACK = 0xFF2A1840, BAR = 0xFFB070FF, GOLD = 0xFFE8C77A;
    private static InvasionHudPacket current = InvasionHudPacket.NONE;

    private InvasionHudOverlay() {
    }

    public static void show(InvasionHudPacket packet) {
        current = packet;
    }

    public static void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        InvasionHudPacket c = current;
        if (mc.options.hideGui || mc.player == null || c.waves() == 0) return;
        Font font = gui.getFont();
        int x = width - W - RIGHT, y = TOP + (mc.player.getActiveEffects().isEmpty() ? 0 : 50);
        g.fill(x - 1, y - 1, x + W + 1, y + H + 1, VIOLET);
        g.fill(x, y, x + W, y + H, PANEL);
        g.drawString(font, Component.translatable("invasion.sofe.hud.title"), x + 4, y + 3, GOLD, true);
        Component line = c.waiting() ? Component.translatable("invasion.sofe.hud.coming", c.wave(), c.waves())
                : Component.translatable("invasion.sofe.hud.wave", c.wave(), c.waves());
        g.drawString(font, line, x + 4, y + 14, 0xFFE6DCF8, false);
        int barY = y + 26, filled = c.total() == 0 ? 0 : (int) ((W - 8) * Math.min(1f, c.fallen() / (float) c.total()));
        g.fill(x + 4, barY, x + W - 4, barY + 7, BAR_BACK);
        g.fill(x + 4, barY, x + 4 + filled, barY + 7, BAR);
        String count = Math.min(c.fallen(), c.total()) + "/" + c.total();
        g.drawString(font, count, x + W - 4 - font.width(count), y + 14, GOLD, false);
    }
}
