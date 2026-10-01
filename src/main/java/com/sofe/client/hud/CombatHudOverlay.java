package com.sofe.client.hud;

import com.sofe.SoFEMod;
import com.sofe.client.ClientCombatData;
import com.sofe.client.ClientProgressData;
import com.sofe.client.SoFEKeys;
import com.sofe.combat.Rune;
import com.sofe.config.SoFEConfig;
import com.sofe.network.SyncCombatPacket;
import com.sofe.player.ResourceType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.settings.KeyModifier;

import java.util.List;

/**
 * The combat HUD, bottom right, in a brass frame: health, the class resource and experience (each
 * value written inside its bar), the level, the Flask charges, the runes, Marks and souls, and the
 * Combat Bar slots with their key written inside each slot, short (Alt+1), so nothing overflows.
 * In a journey it replaces the vanilla hearts (option in the config).
 */
public final class CombatHudOverlay {
    private static final int SLOT = 24, GAP = 3, MARGIN = 6, PAD = 6;
    private static final int BAR_H = 10;
    private static final int BRASS = 0xFFB5863A, BRASS_LIGHT = 0xFFE8C77A, DARK = 0xFF1A1410, PANEL = 0xC0120E0A;

    private CombatHudOverlay() {
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null || minecraft.level == null) return;
        SyncCombatPacket state = ClientCombatData.get().orElse(null);
        if (state == null) return;

        Font font = gui.getFont();
        long now = minecraft.level.getGameTime();
        List<SyncCombatPacket.SlotCooldown> slots = state.slots();
        int slotsW = slots.size() * SLOT + (slots.size() - 1) * GAP;
        int panelW = Math.max(slotsW, 150) + PAD * 2;
        int panelH = PAD + 12 + BAR_H + 3 + BAR_H + 3 + 4 + 6 + SLOT + PAD;
        int left = width - MARGIN - panelW, top = height - MARGIN - panelH;
        int inner = left + PAD, innerW = panelW - PAD * 2;

        frame(graphics, left, top, panelW, panelH);

        // the header: level on the left, the Flask charges on the right
        ClientProgressData.get().ifPresent(progress -> {
            String level = Component.translatable("gui.sofe.hud.level", progress.level()).getString();
            if (progress.skillPoints() > 0 || progress.attributePoints() > 0) level += "  +";
            graphics.drawString(font, level, inner, top + PAD, 0xFFE8B64A, true);
        });
        com.sofe.client.ClientEconomyData.get().ifPresent(e -> {
            String flask = "⬮ " + e.flaskCharges() + "/" + e.flaskMax();
            graphics.drawString(font, flask, inner + innerW - font.width(flask), top + PAD, 0xFF7FD4E0, true);
        });
        // runes, Marks and souls between the level and the flask
        int iconsX = inner + 50, iconY = top + PAD - 1;
        List<Rune> runes = state.runes();
        for (int i = 0; i < runes.size(); i++) {
            ResourceLocation icon = SoFEMod.id("textures/gui/rune/" + runes.get(i).id() + ".png");
            graphics.blit(icon, iconsX + i * 12, iconY, 10, 10, 0f, 0f, 16, 16, 16, 16);
        }
        drawCounter(graphics, font, "mark", state.marks(), 5, iconsX, iconY);
        drawCounter(graphics, font, "soul", state.souls(), 6, iconsX, iconY);

        // health
        int y = top + PAD + 12;
        float health = minecraft.player.getHealth(), maxHealth = minecraft.player.getMaxHealth();
        float absorption = minecraft.player.getAbsorptionAmount();
        bar(graphics, font, inner, y, innerW, health / Math.max(1f, maxHealth), 0xFFC0392B, 0xFF7A1F16,
                (int) Math.ceil(health) + " / " + (int) maxHealth);
        if (absorption > 0) {
            graphics.fill(inner + 1, y + 1, inner + 1 + (int) ((innerW - 2) * Math.min(1f, absorption / Math.max(1f, maxHealth))), y + 3, 0xFFE8B64A);
        }

        // the class resource, with its icon
        y += BAR_H + 3;
        ResourceType resource = state.resource();
        float fraction = state.max() > 0 ? state.current() / state.max() : 0;
        int color = 0xFF000000 | color(resource);
        bar(graphics, font, inner + 12, y, innerW - 12, fraction, color, darker(color), (int) state.current() + " / " + state.max());
        graphics.blit(SoFEMod.id("textures/gui/hud/" + resource.id() + ".png"), inner, y, 10, 10, 0f, 0f, 16, 16, 16, 16);

        // experience, a thin bar
        y += BAR_H + 3;
        int yy = y;
        ClientProgressData.get().ifPresent(progress -> {
            float xp = progress.xpToNext() > 0 ? progress.xp() / (float) progress.xpToNext() : 1f;
            graphics.fill(inner, yy, inner + innerW, yy + 4, DARK);
            graphics.fill(inner + 1, yy + 1, inner + 1 + (int) ((innerW - 2) * Math.min(1f, xp)), yy + 3, 0xFF7BC96F);
        });

        // the Combat Bar slots, right-aligned, with their keys inside
        int slotsY = y + 4 + 6;
        int slotsX = inner + innerW - slotsW;
        for (int i = 0; i < slots.size(); i++) {
            int x = slotsX + i * (SLOT + GAP);
            SyncCombatPacket.SlotCooldown slot = slots.get(i);
            graphics.fill(x - 1, slotsY - 1, x + SLOT + 1, slotsY + SLOT + 1, BRASS);
            graphics.fill(x, slotsY, x + SLOT, slotsY + SLOT, 0xFF0C0907);
            if (!slot.skill().isEmpty()) {
                graphics.blit(SoFEMod.id("textures/gui/skill/" + slot.skill() + ".png"), x + 2, slotsY + 2, SLOT - 4, SLOT - 4, 0f, 0f, 32, 32, 32, 32);
                long remaining = slot.endTick() - now;
                if (remaining > 0 && slot.durationTicks() > 0) {
                    int covered = (int) Math.ceil((SLOT - 4) * remaining / (double) slot.durationTicks());
                    graphics.fill(x + 2, slotsY + 2 + (SLOT - 4 - covered), x + SLOT - 2, slotsY + SLOT - 2, 0xB0000000);
                    String seconds = String.valueOf((remaining + 19) / 20);
                    graphics.drawString(font, seconds, x + (SLOT - font.width(seconds)) / 2, slotsY + 6, 0xFFFFFFFF, true);
                }
            }
            keyLabel(graphics, font, shortKey(SoFEKeys.SKILLS[i]), x, slotsY, slot.skill().isEmpty() ? 0xFF7A7264 : 0xFFE6DCC8);
        }
    }

    /** A dark panel with a brass border and brass corner studs. */
    private static void frame(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, PANEL);
        g.fill(x, y, x + w, y + 1, BRASS);
        g.fill(x, y + h - 1, x + w, y + h, BRASS);
        g.fill(x, y, x + 1, y + h, BRASS);
        g.fill(x + w - 1, y, x + w, y + h, BRASS);
        g.fill(x + 2, y + 2, x + w - 2, y + 3, 0x66B5863A);
        for (int[] c : new int[][]{{x, y}, {x + w - 3, y}, {x, y + h - 3}, {x + w - 3, y + h - 3}}) {
            g.fill(c[0], c[1], c[0] + 3, c[1] + 3, BRASS_LIGHT);
        }
    }

    /** A bar with its value written in the middle of it. */
    private static void bar(GuiGraphics g, Font font, int x, int y, int w, float fraction, int fill, int shade, String text) {
        g.fill(x, y, x + w, y + BAR_H, DARK);
        int filled = (int) ((w - 2) * Math.max(0f, Math.min(1f, fraction)));
        g.fill(x + 1, y + 1, x + 1 + filled, y + BAR_H - 1, fill);
        g.fill(x + 1, y + BAR_H - 3, x + 1 + filled, y + BAR_H - 1, shade);
        g.fill(x + 1, y + 1, x + 1 + filled, y + 2, 0x40FFFFFF);
        g.drawString(font, text, x + (w - font.width(text)) / 2, y + 1, 0xFFF2E8D5, true);
    }

    /** The key, scaled down inside the bottom of the slot. */
    private static void keyLabel(GuiGraphics g, Font font, String key, int x, int y, int color) {
        float scale = Math.min(0.75f, (SLOT - 2) / (float) Math.max(1, font.width(key)));
        g.pose().pushPose();
        g.pose().translate(x + (SLOT - font.width(key) * scale) / 2f, y + SLOT - 7 * scale - 1, 200);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, key, 0, 0, color, true);
        g.pose().popPose();
    }

    /** "Alt+1" instead of "Left Alt + 1". */
    private static String shortKey(KeyMapping mapping) {
        String key = mapping.getKey().getDisplayName().getString();
        if (key.length() > 4) key = key.substring(0, 4);
        KeyModifier modifier = mapping.getKeyModifier();
        return switch (modifier) {
            case ALT -> "Alt+" + key;
            case CONTROL -> "Ctrl+" + key;
            case SHIFT -> "Shf+" + key;
            default -> key;
        };
    }

    private static int darker(int color) {
        int r = (color >> 16 & 0xFF) * 6 / 10, gr = (color >> 8 & 0xFF) * 6 / 10, b = (color & 0xFF) * 6 / 10;
        return 0xFF000000 | r << 16 | gr << 8 | b;
    }

    /** A row of icons, or one icon and a number when there are more than fit. */
    private static void drawCounter(GuiGraphics graphics, Font font, String icon, int count, int maxIcons, int x, int y) {
        if (count <= 0) return;
        ResourceLocation texture = SoFEMod.id("textures/gui/hud/" + icon + ".png");
        if (count <= maxIcons) {
            for (int i = 0; i < count; i++) graphics.blit(texture, x + i * 11, y, 10, 10, 0f, 0f, 16, 16, 16, 16);
        } else {
            graphics.blit(texture, x, y, 10, 10, 0f, 0f, 16, 16, 16, 16);
            graphics.drawString(font, "x" + count, x + 12, y + 1, 0xE6DCC8, true);
        }
    }

    /** The vanilla hearts are hidden while this HUD shows health (a Bearer in a journey). */
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) return;
        if (SoFEConfig.CLIENT.replaceHealthHud.get() && ClientCombatData.get().isPresent()) event.setCanceled(true);
    }

    private static int color(ResourceType resource) {
        return switch (resource) {
            case RESOLVE -> 0x8FB3D9;
            case ESSENCE -> 0x3FD6C4;
            case MANA -> 0x9B5BE0;
            case ENERGY -> 0xD9443B;
            case AUTHORITY -> 0xE8B64A;
        };
    }
}
