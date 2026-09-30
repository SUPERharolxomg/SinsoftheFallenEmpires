package com.sofe.client.hud;

import com.sofe.client.ClientStoryData;
import com.sofe.config.SoFEConfig;
import com.sofe.network.SyncStoryPacket;
import com.sofe.quest.QuestDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.gui.overlay.ForgeGui;

import java.util.Optional;

/**
 * The Quest Compass (docs/Mundo.md, W3): a strip at the top of the screen with the cardinal points,
 * a gold marker toward the tracked quest's objective and the distance to it.
 */
public final class QuestCompassOverlay {
    private static final int HALF_WIDTH = 70, TOP = 3;
    private static final float VIEW_DEGREES = 90f; // the strip shows 90 degrees to each side
    private static final int GOLD = 0xE8B64A, PARCHMENT = 0xE6DCC8, MUTED = 0x8A8275;

    private QuestCompassOverlay() {
    }

    public static void render(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.screen != null || !SoFEConfig.CLIENT.showQuestCompass.get()) return;
        if (mc.player.level().dimension() != Level.OVERWORLD) return;
        Optional<SyncStoryPacket> story = ClientStoryData.get();
        if (story.isEmpty()) return;
        boolean toCorpse = story.get().corpse().isPresent();
        Optional<SyncStoryPacket.Target> maybeTarget = toCorpse ? story.get().corpse() : story.get().compass();
        if (maybeTarget.isEmpty()) return;
        SyncStoryPacket.Target target = maybeTarget.get();

        Font font = gui.getFont();
        int cx = width / 2;
        float yaw = Mth.wrapDegrees(mc.player.getViewYRot(partialTick));
        g.fill(cx - HALF_WIDTH - 2, TOP, cx + HALF_WIDTH + 2, TOP + 11, 0x88000000);
        g.fill(cx - HALF_WIDTH - 2, TOP + 11, cx + HALF_WIDTH + 2, TOP + 12, 0xAA000000 | GOLD);

        // Minecraft yaw: 0 = south, 90 = west, 180 = north, -90 = east
        String[] names = {"S", "W", "N", "E"};
        for (int i = 0; i < 4; i++) {
            float offset = Mth.wrapDegrees(i * 90f - yaw);
            if (Math.abs(offset) <= VIEW_DEGREES) {
                int x = cx + Math.round(offset / VIEW_DEGREES * HALF_WIDTH);
                g.drawCenteredString(font, names[i], x, TOP + 2, names[i].equals("N") ? PARCHMENT : MUTED);
            }
        }

        double dx = target.x() + 0.5 - mc.player.getX(), dz = target.z() + 0.5 - mc.player.getZ();
        float bearing = (float) Math.toDegrees(Math.atan2(-dx, dz)); // same convention as the yaw
        float offset = Mth.wrapDegrees(bearing - yaw);
        int distance = (int) Math.sqrt(dx * dx + dz * dz);
        String marker;
        int mx;
        if (offset < -VIEW_DEGREES) {
            marker = "◀";
            mx = cx - HALF_WIDTH;
        } else if (offset > VIEW_DEGREES) {
            marker = "▶";
            mx = cx + HALF_WIDTH;
        } else {
            marker = "◆";
            mx = cx + Math.round(offset / VIEW_DEGREES * HALF_WIDTH);
        }
        g.drawCenteredString(font, marker, mx, TOP + 2, GOLD);
        Component label = Component.translatable("gui.sofe.compass.distance", distance);
        g.drawCenteredString(font, label, cx, TOP + 14, GOLD);

        if (toCorpse) {
            g.drawCenteredString(font, Component.translatable("gui.sofe.compass.corpse"), cx, TOP + 24, PARCHMENT);
            return;
        }
        story.get().tracked().flatMap(id -> story.get().quests().stream().filter(q -> q.id().equals(id)).findFirst())
                .ifPresent(q -> {
                    Component step = Component.translatable(QuestDefinition.stepKey(q.id(), q.step()));
                    String text = font.plainSubstrByWidth(step.getString(), 220);
                    g.drawCenteredString(font, text, cx, TOP + 24, PARCHMENT);
                });
    }
}
