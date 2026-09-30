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
import net.minecraftforge.client.gui.overlay.ForgeGui;

import java.util.List;

/**
 * Minimal combat HUD for Sprint 2 (bottom left): the class resource bar with its icon and value,
 * the runes waiting for a constellation, and the Combat Bar slots with their cooldowns.
 * The final HUD art comes in Sprint 3.
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

        // Runes waiting for a constellation (Sorceress)
        List<Rune> runes = state.runes();
        for (int i = 0; i < runes.size(); i++) {
            ResourceLocation icon = SoFEMod.id("textures/gui/rune/" + runes.get(i).id() + ".png");
            graphics.blit(icon, MARGIN + 13 + i * 12, barY - 13, 10, 10, 0f, 0f, 16, 16, 16, 16);
        }
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
