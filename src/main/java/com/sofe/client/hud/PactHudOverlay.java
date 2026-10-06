package com.sofe.client.hud;

import com.sofe.client.ClientPactData;
import com.sofe.network.SyncPactPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/**
 * The Pact's members on the left of the screen (UC-18, "the HUD shows the members' health"): each one's name, their
 * Bearer, a health bar, and whether they are near, away, offline or downed. The player's own row is left out.
 */
public final class PactHudOverlay {
    private static final int W = 112, ROW = 22, LEFT = 6, TOP = 40;
    private static final int BRASS = 0xFFB5863A, PANEL = 0xA0120E0A, RED = 0xFFB8302C, DARK_RED = 0xFF4A1412, GREY = 0xFF6E6A62;

    private PactHudOverlay() {
    }

    public static void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        var members = ClientPactData.members().stream().filter(m -> !m.uuid().equals(mc.player.getUUID())).toList();
        if (members.isEmpty()) return;
        Font font = gui.getFont();
        int y = TOP;
        g.drawString(font, Component.translatable("gui.sofe.pact.hud"), LEFT, y - 11, 0xFFE8C77A, true);
        for (SyncPactPacket.Member m : members) {
            g.fill(LEFT - 1, y - 1, LEFT + W + 1, y + ROW - 3, BRASS);
            g.fill(LEFT, y, LEFT + W, y + ROW - 4, PANEL);
            String name = (m.leader() ? "★ " : "") + m.name();
            int nameColor = !m.online() ? 0xFF8A8478 : m.downed() ? 0xFFFF6A5A : 0xFFE6DCC8;
            g.drawString(font, font.plainSubstrByWidth(name, W - 34), LEFT + 3, y + 2, nameColor, false);
            if (!m.bearer().isEmpty()) {
                String cls = Component.translatable("class.sofe." + m.bearer()).getString();
                String shortCls = font.plainSubstrByWidth(cls, 30);
                g.drawString(font, shortCls, LEFT + W - 3 - font.width(shortCls), y + 2, 0xFFB5A890, false);
            }
            int bx = LEFT + 3, by = y + 12, bw = W - 6;
            g.fill(bx, by, bx + bw, by + 5, DARK_RED);
            if (m.online()) {
                float f = Math.max(0, Math.min(1, m.health() / Math.max(1, m.maxHealth())));
                g.fill(bx, by, bx + (int) (bw * f), by + 5, m.downed() ? 0xFFFF5040 : RED);
            }
            String state = !m.online() ? "gui.sofe.pact.offline" : m.downed() ? "gui.sofe.pact.downed" : !m.near() ? "gui.sofe.pact.away" : null;
            if (state != null) {
                String s = Component.translatable(state).getString();
                g.drawString(font, s, bx + bw - font.width(s), by - 1, m.downed() ? 0xFFFF6A5A : GREY, false);
            }
            y += ROW;
        }
    }
}
