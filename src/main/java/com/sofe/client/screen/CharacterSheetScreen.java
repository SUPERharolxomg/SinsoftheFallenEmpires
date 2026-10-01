package com.sofe.client.screen;

import com.sofe.client.ClientClassData;
import com.sofe.client.ClientProgressData;
import com.sofe.network.SoFENetwork;
import com.sofe.network.SpendAttributePacket;
import com.sofe.network.SyncProgressPacket;
import com.sofe.player.PlayerClass;
import com.sofe.progression.AttributeSheet;
import com.sofe.progression.CharacterAttribute;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Diablo II style character sheet (docs/Clases.md, "Attributes"): the six attributes with a "+"
 * each while there are points to spend, and the values they produce, computed by the server.
 */
public class CharacterSheetScreen extends Screen {
    private static final int PANEL_W = 280, PANEL_H = 190;
    private static final int GOLD = 0xE8B64A, PARCHMENT = 0xE6DCC8, MUTED = 0xA89F8E, GEAR_GREEN = 0x6FD86A;
    private final Map<CharacterAttribute, Button> plus = new EnumMap<>(CharacterAttribute.class);

    public CharacterSheetScreen() {
        super(Component.translatable("key.sofe.character"));
    }

    private int left() {
        return (this.width - PANEL_W) / 2;
    }

    private int top() {
        return (this.height - PANEL_H) / 2;
    }

    private int rowY(int index) {
        return top() + 50 + index * 20;
    }

    @Override
    protected void init() {
        plus.clear();
        CharacterAttribute[] attributes = CharacterAttribute.values();
        for (int i = 0; i < attributes.length; i++) {
            CharacterAttribute a = attributes[i];
            Button button = addRenderableWidget(Button.builder(Component.literal("+"), b -> SoFENetwork.sendToServer(new SpendAttributePacket(a)))
                    .bounds(left() + 118, rowY(i) - 5, 16, 16)
                    .tooltip(Tooltip.create(Component.translatable(a.translationKey() + ".desc")))
                    .build());
            plus.put(a, button);
        }
    }

    @Override
    public void tick() {
        int points = ClientProgressData.get().map(SyncProgressPacket::attributePoints).orElse(0);
        plus.values().forEach(b -> b.visible = points > 0);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int l = left(), t = top();
        graphics.fill(l - 2, t - 2, l + PANEL_W + 2, t + PANEL_H + 2, 0xFF000000 | GOLD);
        graphics.fill(l, t, l + PANEL_W, t + PANEL_H, 0xF0181410);

        Optional<PlayerClass> playerClass = ClientClassData.get();
        Optional<SyncProgressPacket> progress = ClientProgressData.get();
        if (playerClass.isPresent() && progress.isPresent()) {
            PlayerClass c = playerClass.get();
            SyncProgressPacket p = progress.get();
            graphics.drawString(this.font, Component.translatable("gui.sofe.character.title",
                    Component.translatable(c.heroKey()), Component.translatable(c.translationKey())), l + 8, t + 8, GOLD, false);
            graphics.drawString(this.font, Component.translatable("gui.sofe.skill_tree.level", p.level()), l + 8, t + 22, PARCHMENT, false);
            graphics.drawString(this.font, Component.translatable("gui.sofe.character.points", p.attributePoints()), l + 8, t + 34,
                    p.attributePoints() > 0 ? GOLD : MUTED, false);

            CharacterAttribute[] attributes = CharacterAttribute.values();
            for (int i = 0; i < attributes.length; i++) {
                CharacterAttribute a = attributes[i];
                int base = AttributeSheet.base(a, c) + p.added(a);
                int gear = p.gearPoints(a);
                boolean primary = AttributeSheet.primary(c) == a;
                graphics.drawString(this.font, Component.translatable(a.translationKey()), l + 12, rowY(i), primary ? GOLD : PARCHMENT, false);
                // values raised by gear are green, Diablo II style; the tooltip says how much comes from gear
                graphics.drawString(this.font, String.valueOf(base + gear), l + 96, rowY(i), gear > 0 ? GEAR_GREEN : 0xFFFFFF, false);
                if (gear > 0 && mouseX >= l + 90 && mouseX < l + 116 && mouseY >= rowY(i) - 2 && mouseY < rowY(i) + 10) {
                    graphics.renderTooltip(this.font, Component.translatable("gui.sofe.character.gear", gear), mouseX, mouseY);
                }
            }

            SyncProgressPacket.Derived d = p.derived();
            int x = l + 152, y = t + 50;
            y = stat(graphics, x, y, "gui.sofe.character.health", String.format(Locale.ROOT, "%.1f", d.maxHealth()));
            y = stat(graphics, x, y, "gui.sofe.character.resource", Component.translatable(c.resource().translationKey()).getString() + " +" + d.maxResource());
            y = stat(graphics, x, y, "gui.sofe.character.physical", percent(d.physicalDamage() - 1));
            y = stat(graphics, x, y, "gui.sofe.character.magic", percent(d.magicDamage() - 1));
            y = stat(graphics, x, y, "gui.sofe.character.crit", percent(d.critChance()));
            y = stat(graphics, x, y, "gui.sofe.character.dodge", percent(d.dodgeChance()));
            y = stat(graphics, x, y, "gui.sofe.character.regen", percent(d.regen() - 1));
            long dinars = com.sofe.client.ClientEconomyData.get().map(e -> e.dinars()).orElse(0L);
            stat(graphics, x, y, "gui.sofe.character.dinars", String.valueOf(dinars));
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private int stat(GuiGraphics graphics, int x, int y, String key, String value) {
        graphics.drawString(this.font, Component.translatable(key), x, y, MUTED, false);
        graphics.drawString(this.font, value, x + 118 - this.font.width(value), y, PARCHMENT, false);
        return y + 16;
    }

    private static String percent(double fraction) {
        return String.format(Locale.ROOT, "%+.1f%%", fraction * 100);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
