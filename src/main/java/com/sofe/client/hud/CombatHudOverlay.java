package com.sofe.client.hud;

import com.sofe.SoFEMod;
import com.sofe.client.ClientCombatData;
import com.sofe.client.ClientProgressData;
import com.sofe.client.SoFEKeys;
import com.sofe.combat.Rune;
import com.sofe.network.SyncCombatPacket;
import com.sofe.player.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.sofe.config.SoFEConfig;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

import java.util.List;

/**
 * The combat HUD (bottom left), in a brass frame: health, the class resource, level and experience,
 * the runes waiting for a constellation, the Thief's Marks, the Necromancer's souls and the Combat
 * Bar slots with their cooldowns. In a journey it replaces the vanilla hearts (option in the config).
 */
public final class CombatHudOverlay {
    private static final int SLOT = 20;
    private static final int MARGIN = 4;

    private CombatHudOverlay() {
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null || minecraft.level == null) return;
        SyncCombatPacket state = ClientCombatData.get().orElse(null);
        if (state == null) return;

        Font font = gui.getFont();
        long now = minecraft.level.getGameTime();
        int slotsY = height - SLOT - MARGIN;

        // Skill slots
        List<SyncCombatPacket.SlotCooldown> slots = state.slots();
        for (int i = 0; i < slots.size(); i++) {
            int x = MARGIN + i * (SLOT + 2);
            SyncCombatPacket.SlotCooldown slot = slots.get(i);
            graphics.fill(x, slotsY, x + SLOT, slotsY + SLOT, 0xAA000000);
            if (slot.skill().isEmpty()) {
                graphics.drawString(font, keyLabel(i), x + 1, slotsY - 8, 0x7A7264, true);
                continue;
            }
            graphics.blit(SoFEMod.id("textures/gui/skill/" + slot.skill() + ".png"), x + 2, slotsY + 2, 16, 16, 0f, 0f, 32, 32, 32, 32);
            long remaining = slot.endTick() - now;
            if (remaining > 0 && slot.durationTicks() > 0) {
                int covered = (int) Math.ceil(16 * remaining / (double) slot.durationTicks());
                graphics.fill(x + 2, slotsY + 2 + (16 - covered), x + 18, slotsY + 18, 0xB0000000);
            }
            graphics.drawString(font, keyLabel(i), x + 1, slotsY - 8, 0xC8BEAA, true);
        }

        // Resource bar with icon and value, above the slots
        int barY = slotsY - 26;
        int barX0 = MARGIN + 13, barW0 = 80;

        // Brass frame behind the whole group
        int frameTop = barY - 30, frameRight = MARGIN + slots.size() * (SLOT + 2) + 2;
        frameRight = Math.max(frameRight, barX0 + barW0 + 58);
        graphics.fill(MARGIN - 3, frameTop, frameRight, height - 1, 0x66000000);
        graphics.fill(MARGIN - 3, frameTop, frameRight, frameTop + 1, 0xCCB5863A);
        graphics.fill(MARGIN - 3, frameTop, MARGIN - 2, height - 1, 0xCCB5863A);

        // Health, above the resource bar
        int healthY = barY - 11;
        float health = minecraft.player.getHealth(), maxHealth = minecraft.player.getMaxHealth();
        float absorption = minecraft.player.getAbsorptionAmount();
        graphics.fill(MARGIN, healthY, MARGIN + 10, healthY + 7, 0xFF1A1410);
        graphics.fill(MARGIN + 2, healthY + 1, MARGIN + 8, healthY + 6, 0xFFC0392B);
        graphics.fill(barX0 - 1, healthY, barX0 + barW0 + 1, healthY + 7, 0xFF1A1410);
        graphics.fill(barX0, healthY + 1, barX0 + (int) (barW0 * Math.min(1f, health / Math.max(1f, maxHealth))), healthY + 6, 0xFFC0392B);
        if (absorption > 0) {
            graphics.fill(barX0, healthY + 1, barX0 + (int) (barW0 * Math.min(1f, absorption / Math.max(1f, maxHealth))), healthY + 3, 0xFFE8B64A);
        }
        graphics.drawString(font, (int) Math.ceil(health) + " / " + (int) maxHealth, barX0 + barW0 + 4, healthY, 0xE6DCC8, true);
        ResourceType resource = state.resource();
        graphics.blit(SoFEMod.id("textures/gui/hud/" + resource.id() + ".png"), MARGIN, barY - 2, 10, 10, 0f, 0f, 16, 16, 16, 16);
        int barX = MARGIN + 13;
        int barW = 80;
        float fraction = state.max() > 0 ? Math.min(1f, state.current() / state.max()) : 0;
        graphics.fill(barX - 1, barY, barX + barW + 1, barY + 7, 0xFF1A1410);
        graphics.fill(barX, barY + 1, barX + (int) (barW * fraction), barY + 6, 0xFF000000 | color(resource));
        graphics.drawString(font, (int) state.current() + " / " + state.max(), barX + barW + 4, barY, 0xE6DCC8, true);

        // Level and experience toward the next level, just under the resource bar
        ClientProgressData.get().ifPresent(progress -> {
            float xpFraction = progress.xpToNext() > 0 ? Math.min(1f, progress.xp() / (float) progress.xpToNext()) : 1f;
            graphics.fill(barX - 1, barY + 8, barX + barW + 1, barY + 11, 0xFF1A1410);
            graphics.fill(barX, barY + 9, barX + (int) (barW * xpFraction), barY + 10, 0xFF7BC96F);
            String level = Component.translatable("gui.sofe.hud.level", progress.level()).getString();
            if (progress.skillPoints() > 0 || progress.attributePoints() > 0) {
                level += " +";
            }
            graphics.drawString(font, level, barX + barW + 4, barY + 9, 0xE8B64A, true);
        });

        // Runes waiting for a constellation (Sorceress), Marks (Thief) and souls (Necromancer), above the health
        int iconY = barY - 24;
        List<Rune> runes = state.runes();
        for (int i = 0; i < runes.size(); i++) {
            ResourceLocation icon = SoFEMod.id("textures/gui/rune/" + runes.get(i).id() + ".png");
            graphics.blit(icon, MARGIN + 13 + i * 12, iconY, 10, 10, 0f, 0f, 16, 16, 16, 16);
        }
        // the Bearer's Flask charges, at the right of the health bar
        com.sofe.client.ClientEconomyData.get().ifPresent(e -> graphics.drawString(font, "\u2B2E " + e.flaskCharges() + "/" + e.flaskMax(),
                barX0 + barW0 + 46, healthY, 0xFF7FD4E0, true));
        drawCounter(graphics, font, "mark", state.marks(), 5, MARGIN + 13, iconY);
        drawCounter(graphics, font, "soul", state.souls(), 10, MARGIN + 13, iconY);
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

    private static String keyLabel(int slot) {
        return SoFEKeys.SKILLS[slot].getTranslatedKeyMessage().getString();
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
