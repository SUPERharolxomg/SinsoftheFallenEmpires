package com.sofe.client.screen;

import com.sofe.economy.MerchantService;
import com.sofe.economy.Prices;
import com.sofe.network.MerchantActionPacket;
import com.sofe.network.OpenMerchantPacket;
import com.sofe.network.SoFENetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * A SoFE merchant's screen (docs/Anexos.md, A4): buy what they sell today, sell to them, buy back the
 * last five things sold, and with Selim exchange emeralds and Dinars. Every action is checked by the server.
 */
public class MerchantScreen extends Screen {
    private static final int PANEL_W = 320, PANEL_H = 214, ROW = 20, ROWS = 7;
    private static final int GOLD = 0xE8B64A, PARCHMENT = 0xE6DCC8, MUTED = 0xA89F8E;

    private enum Tab { BUY, SELL, BUYBACK }

    private OpenMerchantPacket state;
    private Tab tab = Tab.BUY;
    private int scroll;

    public MerchantScreen(OpenMerchantPacket state) {
        super(Component.translatable("npc.sofe." + state.npc()));
        this.state = state;
    }

    /** The server sent the result of a trade: show the new state, same tab. */
    public void update(OpenMerchantPacket newState) {
        this.state = newState;
        rebuild();
    }

    public String npc() {
        return state.npc();
    }

    private int left() {
        return (this.width - PANEL_W) / 2;
    }

    private int top() {
        return (this.height - PANEL_H) / 2;
    }

    @Override
    protected void init() {
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        int l = left(), t = top();
        int x = l + 8;
        for (Tab each : Tab.values()) {
            Tab target = each;
            Button b = addRenderableWidget(Button.builder(Component.translatable("gui.sofe.merchant." + each.name().toLowerCase(java.util.Locale.ROOT)), btn -> {
                tab = target;
                scroll = 0;
                rebuild();
            }).bounds(x, t + 22, 70, 18).build());
            b.active = tab != each;
            x += 74;
        }
        if (state.moneyChanger()) {
            addRenderableWidget(Button.builder(Component.translatable("gui.sofe.merchant.exchange_sell", Prices.DINARS_PER_EMERALD_SOLD),
                    btn -> send(MerchantService.Action.EMERALD_TO_DINARS, 0)).bounds(l + 8, t + PANEL_H - 26, 150, 18).build());
            addRenderableWidget(Button.builder(Component.translatable("gui.sofe.merchant.exchange_buy", Prices.DINARS_PER_EMERALD_BOUGHT),
                    btn -> send(MerchantService.Action.DINARS_TO_EMERALD, 0)).bounds(l + 162, t + PANEL_H - 26, 150, 18).build());
        }
        int y = t + 46;
        switch (tab) {
            case BUY -> {
                List<OpenMerchantPacket.Offer> offers = state.offers();
                for (int i = scroll; i < Math.min(offers.size(), scroll + ROWS); i++) {
                    OpenMerchantPacket.Offer offer = offers.get(i);
                    Button b = addRenderableWidget(Button.builder(Component.translatable("gui.sofe.merchant.buy"),
                            btn -> send(MerchantService.Action.BUY, offer.index())).bounds(l + PANEL_W - 64, y, 56, 18).build());
                    b.active = offer.left() > 0 && state.dinars() >= offer.price();
                    y += ROW;
                }
            }
            case SELL, BUYBACK -> {
                List<OpenMerchantPacket.Sellable> list = tab == Tab.SELL ? state.sellables() : state.buyback();
                MerchantService.Action action = tab == Tab.SELL ? MerchantService.Action.SELL : MerchantService.Action.BUYBACK;
                for (int i = scroll; i < Math.min(list.size(), scroll + ROWS); i++) {
                    OpenMerchantPacket.Sellable s = list.get(i);
                    Button b = addRenderableWidget(Button.builder(Component.translatable("gui.sofe.merchant." + (tab == Tab.SELL ? "sell" : "buyback")),
                            btn -> send(action, s.index())).bounds(l + PANEL_W - 64, y, 56, 18).build());
                    b.active = tab == Tab.SELL || state.dinars() >= s.price();
                    y += ROW;
                }
            }
        }
    }

    private void send(MerchantService.Action action, int index) {
        SoFENetwork.sendToServer(new MerchantActionPacket(state.npc(), action, index));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int size = switch (tab) {
            case BUY -> state.offers().size();
            case SELL -> state.sellables().size();
            case BUYBACK -> state.buyback().size();
        };
        scroll = Math.max(0, Math.min(Math.max(0, size - ROWS), scroll - (int) Math.signum(delta)));
        rebuild();
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int l = left(), t = top();
        g.fill(l - 2, t - 2, l + PANEL_W + 2, t + PANEL_H + 2, 0xFF000000 | GOLD);
        g.fill(l, t, l + PANEL_W, t + PANEL_H, 0xF0181410);
        g.drawString(this.font, this.title, l + 8, t + 8, GOLD, false);
        Component dinars = Component.translatable("gui.sofe.merchant.dinars", state.dinars());
        g.drawString(this.font, dinars, l + PANEL_W - 8 - this.font.width(dinars), t + 8, GOLD, false);

        int y = t + 46;
        ItemStack hovered = ItemStack.EMPTY;
        switch (tab) {
            case BUY -> {
                for (int i = scroll; i < Math.min(state.offers().size(), scroll + ROWS); i++) {
                    OpenMerchantPacket.Offer o = state.offers().get(i);
                    hovered = row(g, o.stack(), o.price(), Component.translatable("gui.sofe.merchant.left", o.left()), l, y, mouseX, mouseY, hovered);
                    y += ROW;
                }
            }
            case SELL, BUYBACK -> {
                List<OpenMerchantPacket.Sellable> list = tab == Tab.SELL ? state.sellables() : state.buyback();
                if (list.isEmpty()) g.drawString(this.font, Component.translatable("gui.sofe.merchant.nothing_to_sell"), l + 12, y + 4, MUTED, false);
                for (int i = scroll; i < Math.min(list.size(), scroll + ROWS); i++) {
                    OpenMerchantPacket.Sellable s = list.get(i);
                    hovered = row(g, s.stack(), s.price(), null, l, y, mouseX, mouseY, hovered);
                    y += ROW;
                }
            }
        }
        super.render(g, mouseX, mouseY, partialTick);
        if (!hovered.isEmpty()) g.renderTooltip(this.font, hovered, mouseX, mouseY);
    }

    private ItemStack row(GuiGraphics g, ItemStack stack, int price, Component note, int l, int y, int mouseX, int mouseY, ItemStack hovered) {
        g.renderItem(stack, l + 10, y + 1);
        g.renderItemDecorations(this.font, stack, l + 10, y + 1);
        g.drawString(this.font, this.font.plainSubstrByWidth(stack.getHoverName().getString(), 150), l + 32, y + 2, PARCHMENT, false);
        if (note != null) g.drawString(this.font, note, l + 32, y + 11, MUTED, false);
        String priceText = price + " ◈";
        g.drawString(this.font, priceText, l + PANEL_W - 72 - this.font.width(priceText), y + 5, GOLD, false);
        if (mouseX >= l + 10 && mouseX < l + 26 && mouseY >= y + 1 && mouseY < y + 17) return stack;
        return hovered;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
