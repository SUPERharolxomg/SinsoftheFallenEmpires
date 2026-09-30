package com.sofe.client.screen;

import com.sofe.network.OpenWaystonesPacket;
import com.sofe.network.SoFENetwork;
import com.sofe.network.TravelPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** The Waystone screen (docs/Jugabilidad.md, G2): the player's Waystones; travel is free. */
public class WaystoneScreen extends Screen {
    private static final int GOLD = 0xE8B64A, MUTED = 0xA89F8E;
    private static final int ROW = 22, PER_PAGE = 8;
    private final List<OpenWaystonesPacket.Entry> entries;
    private int page;

    public WaystoneScreen(List<OpenWaystonesPacket.Entry> entries) {
        super(Component.translatable("gui.sofe.waystone.title"));
        this.entries = entries;
    }

    private static Component name(OpenWaystonesPacket.Entry e) {
        return e.nameKey().isEmpty() ? Component.translatable("waystone.sofe.unnamed", e.pos().getX(), e.pos().getZ())
                : Component.translatable(e.nameKey());
    }

    @Override
    protected void init() {
        clearWidgets();
        int w = 220, x = (this.width - w) / 2, y = this.height / 2 - PER_PAGE * ROW / 2;
        int from = page * PER_PAGE;
        for (int i = from; i < Math.min(entries.size(), from + PER_PAGE); i++) {
            OpenWaystonesPacket.Entry e = entries.get(i);
            Button b = addRenderableWidget(Button.builder(name(e), btn -> {
                SoFENetwork.sendToServer(new TravelPacket(e.pos()));
                onClose();
            }).bounds(x, y + (i - from) * ROW, w, 20).build());
            b.active = !e.here() && e.reachable();
            if (e.here()) b.setTooltip(Tooltip.create(Component.translatable("gui.sofe.waystone.here")));
            else if (!e.reachable()) b.setTooltip(Tooltip.create(Component.translatable("gui.sofe.waystone.unreachable")));
        }
        if (entries.size() > PER_PAGE) {
            int pages = (entries.size() + PER_PAGE - 1) / PER_PAGE;
            addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                page = (page + pages - 1) % pages;
                init();
            }).bounds(x, y + PER_PAGE * ROW + 4, 20, 20).build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                page = (page + 1) % pages;
                init();
            }).bounds(x + w - 20, y + PER_PAGE * ROW + 4, 20, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int top = this.height / 2 - PER_PAGE * ROW / 2;
        g.drawCenteredString(this.font, this.title, this.width / 2, top - 20, GOLD);
        if (entries.size() <= 1) {
            g.drawCenteredString(this.font, Component.translatable("gui.sofe.waystone.only_one"), this.width / 2, top - 8, MUTED);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
