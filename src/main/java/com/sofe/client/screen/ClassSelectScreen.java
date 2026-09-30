package com.sofe.client.screen;

import com.sofe.SoFEMod;
import com.sofe.network.ChooseClassPacket;
import com.sofe.network.SoFENetwork;
import com.sofe.player.PlayerClass;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Choose one of the five Bearers (UC-01). The card is the Bearer's emblem and scene; the
 * player's own model is drawn live on top of it, because every player has their own skin
 * (docs/Arte.md). The choice is sent to the server, which confirms it with a sync.
 */
public class ClassSelectScreen extends Screen {
    private static final int GOLD = 0xE8B64A;
    private static final int PARCHMENT = 0xE6DCC8;
    private static final int MUTED = 0xA89F8E;
    private static final int CARD_W = 512;
    private static final int CARD_H = 640;

    private final Map<PlayerClass, Button> classButtons = new EnumMap<>(PlayerClass.class);
    private PlayerClass selected = PlayerClass.KNIGHT;
    private Button confirm;
    private boolean waiting;

    public ClassSelectScreen() {
        super(Component.translatable("gui.sofe.class_select.title"));
    }

    private static ResourceLocation card(PlayerClass c) {
        return SoFEMod.id("textures/gui/bearer/" + c.id() + "_card.png");
    }

    private static ResourceLocation resourceIcon(PlayerClass c) {
        return SoFEMod.id("textures/gui/hud/" + c.resource().id() + ".png");
    }

    // Layout, recomputed from the screen size
    private int cardHeight() {
        return Mth.clamp(this.height - 90, 120, 320);
    }

    private int cardWidth() {
        return cardHeight() * CARD_W / CARD_H;
    }

    private int cardX() {
        return this.width / 2 - cardWidth() - 12;
    }

    private int cardY() {
        return 46;
    }

    private int panelX() {
        return this.width / 2 + 12;
    }

    @Override
    protected void init() {
        classButtons.clear();
        int gap = 4;
        int size = Mth.clamp((this.width - panelX() - 12 - gap * 4) / 5, 16, 32);
        int x = panelX();
        int y = cardY() + cardHeight() - 20 - 6 - size;
        for (PlayerClass c : PlayerClass.values()) {
            classButtons.put(c, addRenderableWidget(new EmblemButton(x, y, size, c)));
            x += size + gap;
        }
        confirm = addRenderableWidget(Button.builder(Component.translatable("gui.sofe.class_select.confirm"), b -> confirmChoice())
                .bounds(panelX(), cardY() + cardHeight() - 20, size * 5 + gap * 4, 20).build());
        updateButtons();
    }

    private void select(PlayerClass c) {
        selected = c;
        updateButtons();
    }

    private void confirmChoice() {
        waiting = true;
        SoFENetwork.sendToServer(new ChooseClassPacket(selected));
        updateButtons();
    }

    private void updateButtons() {
        classButtons.forEach((c, b) -> b.active = !waiting);
        if (confirm != null) {
            confirm.active = !waiting;
            confirm.setMessage(Component.translatable(waiting ? "gui.sofe.class_select.waiting" : "gui.sofe.class_select.confirm"));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);

        graphics.drawCenteredString(this.font, this.title.copy().withStyle(ChatFormatting.BOLD), this.width / 2, 12, GOLD);
        graphics.drawCenteredString(this.font, Component.translatable("gui.sofe.class_select.subtitle"), this.width / 2, 26, MUTED);

        renderCard(graphics);
        renderDetails(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderCard(GuiGraphics graphics) {
        int x = cardX(), y = cardY(), w = cardWidth(), h = cardHeight();
        graphics.blit(card(selected), x, y, w, h, 0f, 0f, CARD_W, CARD_H, CARD_W, CARD_H);

        if (this.minecraft != null && this.minecraft.player != null) {
            // Slow sway instead of following the mouse, so the model always faces the viewer
            // The last two arguments are how far the "mouse" is from the model (like vanilla's inventory):
            // a slow sideways sway and 0 vertically, so the model turns gently and looks straight ahead
            float sway = Mth.sin((System.currentTimeMillis() % 12000L) / 12000f * Mth.TWO_PI) * 40f;
            int feetX = x + w / 2;
            int feetY = y + h - h / 10;
            int scale = h / 4;
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, feetX, feetY, scale,
                    sway, 0f, this.minecraft.player);
        }
    }

    private void renderDetails(GuiGraphics graphics) {
        int x = panelX();
        int y = cardY() + 4;
        int maxWidth = this.width - x - 12;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(2f, 2f, 1f);
        graphics.drawString(this.font, Component.translatable(selected.heroKey()), 0, 0, GOLD);
        graphics.pose().popPose();
        y += 22;

        graphics.drawString(this.font, Component.translatable(selected.translationKey()), x, y, PARCHMENT);
        y += 16;

        y = line(graphics, x, y, Component.translatable("gui.sofe.class_select.role", Component.translatable(selected.roleKey())));
        y = line(graphics, x, y, Component.translatable("gui.sofe.class_select.origin", Component.translatable(selected.origin().translationKey())));
        graphics.blit(resourceIcon(selected), x, y - 3, 12, 12, 0f, 0f, 16, 16, 16, 16);
        y = line(graphics, x + 15, y, Component.translatable("gui.sofe.class_select.resource", Component.translatable(selected.resource().translationKey())));
        y = line(graphics, x, y, Component.translatable("gui.sofe.class_select.temptation", Component.translatable(selected.temptation().translationKey())));
        y += 6;

        Component quote = Component.literal("“").append(Component.translatable(selected.quoteKey())).append("”")
                .withStyle(ChatFormatting.ITALIC);
        List<FormattedCharSequence> lines = new ArrayList<>(this.font.split(quote, maxWidth));
        for (FormattedCharSequence l : lines) {
            graphics.drawString(this.font, l, x, y, MUTED);
            y += 10;
        }
    }

    private int line(GuiGraphics graphics, int x, int y, Component text) {
        graphics.drawString(this.font, text, x, y, PARCHMENT);
        return y + 12;
    }

    /** A class button drawn as the Bearer's emblem; gold frame when selected, hero and class as tooltip. */
    private final class EmblemButton extends Button {
        private final PlayerClass playerClass;

        EmblemButton(int x, int y, int size, PlayerClass playerClass) {
            super(x, y, size, size, Component.translatable(playerClass.translationKey()), b -> select(playerClass), DEFAULT_NARRATION);
            this.playerClass = playerClass;
            setTooltip(Tooltip.create(Component.translatable(playerClass.heroKey()).append(" — ")
                    .append(Component.translatable(playerClass.translationKey()))));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean chosen = playerClass == selected;
            int border = chosen ? 0xFF000000 | GOLD : isHoveredOrFocused() ? 0xFF000000 | PARCHMENT : 0xFF3A3028;
            int thickness = chosen ? 2 : 1;
            graphics.fill(getX() - thickness, getY() - thickness, getX() + width + thickness, getY() + height + thickness, border);
            graphics.blit(emblem(playerClass), getX(), getY(), width, height, 0f, 0f, 64, 64, 64, 64);
            if (!chosen && !isHoveredOrFocused()) {
                graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x55000000); // dim the others
            }
        }
    }

    private static ResourceLocation emblem(PlayerClass c) {
        return SoFEMod.id("textures/gui/bearer/" + c.id() + "_emblem.png");
    }

    /** The shard does not let go: a journey cannot start without a Bearer. */
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
