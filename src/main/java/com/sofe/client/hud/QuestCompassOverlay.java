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
 * a gold marker toward the tracked quest's objective, the distance to it (and who to talk to there) and
 * the step to do; a step with no place shows its text alone. {@link com.sofe.client.QuestPath} draws the
 * way in the world.
 */
public final class QuestCompassOverlay {
    private static final int HALF_WIDTH = 70, TOP = 3;
    /** The widest a step's text runs before it goes on to the next line, and the height of each line. */
    private static final int STEP_WIDTH = 320, LINE_HEIGHT = 10;
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
        Font font = gui.getFont();
        int cx = width / 2;
        if (maybeTarget.isEmpty()) {
            // a step with no place (learn your first skills...): what to do is still written at the top
            trackedStep(story.get()).ifPresent(text -> drawStep(g, font, text, cx, TOP, width));
            return;
        }
        SyncStoryPacket.Target target = maybeTarget.get();

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
        if (!toCorpse && !target.npc().isEmpty()) {
            label = Component.translatable("gui.sofe.compass.talk_to", Component.translatable("npc.sofe." + target.npc()))
                    .append(" · ").append(label);
        }
        g.drawCenteredString(font, label, cx, TOP + 14, GOLD);

        if (toCorpse) {
            g.drawCenteredString(font, Component.translatable("gui.sofe.compass.corpse"), cx, TOP + 24, PARCHMENT);
            return;
        }
        trackedStep(story.get()).ifPresent(text -> drawStep(g, font, text, cx, TOP + 22, width));
    }

    /**
     * The step to do, whole: broken into lines no wider than the screen allows (a long step was cut off mid-word),
     * on a dark panel that grows with them.
     */
    private static void drawStep(GuiGraphics g, Font font, String text, int cx, int top, int screenWidth) {
        int maxWidth = Mth.clamp(screenWidth - 40, 120, STEP_WIDTH);
        var lines = font.split(Component.literal(text), maxWidth);
        int widest = 0;
        for (var line : lines) widest = Math.max(widest, font.width(line));
        int half = widest / 2 + 5, height = lines.size() * LINE_HEIGHT + 3;
        g.fill(cx - half, top, cx + half, top + height, 0x88000000);
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            g.drawString(font, line, cx - font.width(line) / 2, top + 2 + i * LINE_HEIGHT, PARCHMENT);
        }
    }

    /** The current step of the tracked quest, as written in the Journal. */
    private static Optional<String> trackedStep(SyncStoryPacket story) {
        return story.tracked().flatMap(id -> story.quests().stream().filter(q -> q.id().equals(id) && !q.completed()).findFirst())
                .map(q -> Component.translatable(QuestDefinition.stepKey(q.id(), q.step())).getString());
    }
}
