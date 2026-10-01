package com.sofe.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.BossEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;

/**
 * The boss health bar of SoFE bosses (Sprint 5): a brass frame, the boss's name in gold and a bar in
 * the boss's color with a notch every 10%. Vanilla boss bars (the Wither, the Ender Dragon) are left alone.
 */
public final class BossHealthBar {
    private static final int WIDTH = 220, BAR_H = 7;
    private static final int GOLD = 0xE8B64A, BRASS = 0xFFB5863A, DARK = 0xE0100C0A;

    private BossHealthBar() {
    }

    /** SoFE bosses name their bar with their entity name: entity.sofe.&lt;id&gt;. */
    static boolean isSoFEBoss(BossEvent event) {
        Component name = event.getName();
        return name.getContents() instanceof TranslatableContents t && t.getKey().startsWith("entity.sofe.");
    }

    public static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        BossEvent boss = event.getBossEvent();
        if (!isSoFEBoss(boss)) return;
        event.setCanceled(true);
        GuiGraphics g = event.getGuiGraphics();
        var font = Minecraft.getInstance().font;
        int x = event.getWindow().getGuiScaledWidth() / 2 - WIDTH / 2;
        int y = event.getY();

        g.drawCenteredString(font, boss.getName(), x + WIDTH / 2, y, GOLD);
        int barY = y + 10;
        g.fill(x - 2, barY - 2, x + WIDTH + 2, barY + BAR_H + 2, BRASS);
        g.fill(x - 1, barY - 1, x + WIDTH + 1, barY + BAR_H + 1, DARK);
        int filled = (int) (WIDTH * Math.max(0, Math.min(1, boss.getProgress())));
        int color = color(boss.getColor());
        g.fill(x, barY, x + filled, barY + BAR_H, color);
        g.fill(x, barY, x + filled, barY + 2, lighter(color)); // a highlight along the top
        for (int i = 1; i < 10; i++) {
            int nx = x + WIDTH * i / 10;
            g.fill(nx, barY, nx + 1, barY + BAR_H, 0x80000000);
        }
        event.setIncrement(BAR_H + 16);
    }

    private static int color(BossEvent.BossBarColor color) {
        return switch (color) {
            case RED -> 0xFFC0392B;
            case YELLOW -> 0xFFD9A441;
            case PURPLE -> 0xFF8E3FD6;
            case BLUE -> 0xFF4A7FD6;
            case GREEN -> 0xFF5FB04A;
            case PINK -> 0xFFD65C9E;
            case WHITE -> 0xFFD8D2C4;
        };
    }

    private static int lighter(int argb) {
        int r = Math.min(255, ((argb >> 16) & 0xFF) + 50), gr = Math.min(255, ((argb >> 8) & 0xFF) + 50), b = Math.min(255, (argb & 0xFF) + 50);
        return 0xFF000000 | (r << 16) | (gr << 8) | b;
    }
}
