package com.sofe.client.screen;

import com.sofe.client.ClientStoryData;
import com.sofe.network.SoFENetwork;
import com.sofe.network.SyncStoryPacket;
import com.sofe.network.TrackQuestPacket;
import com.sofe.quest.QuestDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The Journal (key U, docs/Jugabilidad.md, G3): the current act, active quests by type, completed
 * quests, the fates chosen so far and the choices still open. A quest can be tracked for the Quest Compass.
 */
public class JournalScreen extends Screen {
    private static final int PANEL_W = 340, PANEL_H = 210, LIST_W = 130, ROW_H = 12;
    private static final int GOLD = 0xE8B64A, PARCHMENT = 0xE6DCC8, MUTED = 0xA89F8E, DONE = 0x7FA06A;

    private String selected;
    private Button trackButton;

    public JournalScreen() {
        super(Component.translatable("key.sofe.journal"));
    }

    private int left() {
        return (this.width - PANEL_W) / 2;
    }

    private int top() {
        return (this.height - PANEL_H) / 2;
    }

    @Override
    protected void init() {
        trackButton = addRenderableWidget(Button.builder(Component.translatable("gui.sofe.journal.track"), b -> {
            if (selected != null) SoFENetwork.sendToServer(new TrackQuestPacket(selected));
        }).bounds(left() + PANEL_W - 78, top() + PANEL_H - 24, 70, 18).build());
        if (selected == null) {
            selected = ClientStoryData.get().flatMap(SyncStoryPacket::tracked).orElse(null);
        }
    }

    /** The rows of the quest list: headers (null quest) and quests. */
    private record Row(Component text, SyncStoryPacket.Quest quest) {
    }

    private List<Row> rows(SyncStoryPacket story) {
        List<Row> rows = new ArrayList<>();
        for (String type : List.of("main", "bearer", "side")) {
            List<SyncStoryPacket.Quest> active = story.quests().stream().filter(q -> q.type().equals(type) && !q.completed()).toList();
            if (active.isEmpty()) continue;
            rows.add(new Row(Component.translatable("quest.sofe.type." + type).withStyle(ChatFormatting.GOLD), null));
            active.forEach(q -> rows.add(new Row(Component.translatable(QuestDefinition.translationKey(q.id())), q)));
        }
        List<SyncStoryPacket.Quest> completed = story.quests().stream().filter(SyncStoryPacket.Quest::completed).toList();
        if (!completed.isEmpty()) {
            rows.add(new Row(Component.translatable("gui.sofe.journal.completed").withStyle(ChatFormatting.GOLD), null));
            completed.forEach(q -> rows.add(new Row(Component.translatable(QuestDefinition.translationKey(q.id())), q)));
        }
        return rows;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Optional<SyncStoryPacket> story = ClientStoryData.get();
        if (story.isPresent() && mouseX >= left() + 8 && mouseX < left() + 8 + LIST_W) {
            List<Row> rows = rows(story.get());
            int index = (int) ((mouseY - (top() + 30)) / ROW_H);
            if (mouseY >= top() + 30 && index >= 0 && index < rows.size() && rows.get(index).quest() != null) {
                selected = rows.get(index).quest().id();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int l = left(), t = top();
        g.fill(l - 2, t - 2, l + PANEL_W + 2, t + PANEL_H + 2, 0xFF000000 | GOLD);
        g.fill(l, t, l + PANEL_W, t + PANEL_H, 0xF0181410);
        g.fill(l + LIST_W + 12, t + 26, l + LIST_W + 13, t + PANEL_H - 8, 0x60E8B64A);

        Optional<SyncStoryPacket> maybe = ClientStoryData.get();
        if (maybe.isEmpty()) {
            g.drawCenteredString(this.font, Component.translatable("gui.sofe.journal.empty"), l + PANEL_W / 2, t + PANEL_H / 2, MUTED);
            trackButton.visible = false;
            super.render(g, mouseX, mouseY, partialTick);
            return;
        }
        SyncStoryPacket story = maybe.get();
        g.drawString(this.font, Component.translatable("gui.sofe.journal.title", Component.translatable("act.sofe." + story.act())), l + 8, t + 9, GOLD, false);

        List<Row> rows = rows(story);
        int y = t + 30;
        for (Row row : rows) {
            if (y > t + PANEL_H - 16) break;
            if (row.quest() == null) {
                g.drawString(this.font, row.text(), l + 8, y + 2, GOLD, false);
            } else {
                boolean isSelected = row.quest().id().equals(selected);
                boolean tracked = story.tracked().map(row.quest().id()::equals).orElse(false);
                if (isSelected) g.fill(l + 6, y, l + 8 + LIST_W, y + ROW_H, 0x40E8B64A);
                String prefix = tracked ? "◆ " : "  ";
                g.drawString(this.font, this.font.plainSubstrByWidth(prefix + row.text().getString(), LIST_W - 4), l + 10, y + 2,
                        row.quest().completed() ? DONE : PARCHMENT, false);
            }
            y += ROW_H;
        }
        if (rows.isEmpty()) {
            g.drawString(this.font, Component.translatable("gui.sofe.journal.no_quests"), l + 8, t + 32, MUTED, false);
        }

        Optional<SyncStoryPacket.Quest> quest = story.quests().stream().filter(q -> q.id().equals(selected)).findFirst();
        trackButton.visible = quest.isPresent() && !quest.get().completed();
        trackButton.active = quest.isPresent() && !story.tracked().map(selected::equals).orElse(false);
        int dx = l + LIST_W + 20, dw = PANEL_W - LIST_W - 28;
        quest.ifPresentOrElse(q -> renderQuest(g, q, dx, t + 28, dw), () -> renderFates(g, story, dx, t + 28, dw));
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void renderQuest(GuiGraphics g, SyncStoryPacket.Quest q, int x, int y, int w) {
        g.drawString(this.font, Component.translatable(QuestDefinition.translationKey(q.id())), x, y, GOLD, false);
        y += 14;
        for (int step = 0; step <= q.step() && step < q.steps(); step++) {
            boolean done = q.completed() || step < q.step();
            Component text = Component.literal(done ? "✔ " : "▸ ").append(Component.translatable(QuestDefinition.stepKey(q.id(), step)));
            if (!done && q.required() > 1) text = text.copy().append(" (" + q.count() + "/" + q.required() + ")");
            for (FormattedCharSequence s : this.font.split(text, w)) {
                g.drawString(this.font, s, x, y, done ? DONE : PARCHMENT, false);
                y += 10;
            }
            y += 3;
        }
    }

    /** With no quest selected, the right side shows the fates (Fallout style epilogue choices). */
    private void renderFates(GuiGraphics g, SyncStoryPacket story, int x, int y, int w) {
        g.drawString(this.font, Component.translatable("gui.sofe.journal.fates"), x, y, GOLD, false);
        y += 14;
        for (Map.Entry<String, String> fate : story.fates().entrySet()) {
            Component text = Component.translatable("region.sofe." + fate.getKey()).append(": ")
                    .append(Component.translatable("fate.sofe." + fate.getKey() + "." + fate.getValue()));
            for (FormattedCharSequence s : this.font.split(text, w)) {
                g.drawString(this.font, s, x, y, PARCHMENT, false);
                y += 10;
            }
        }
        for (String region : story.openFates()) {
            Component text = Component.translatable("region.sofe." + region).append(": ")
                    .append(Component.translatable("gui.sofe.journal.fate_open"));
            g.drawString(this.font, text, x, y, MUTED, false);
            y += 10;
        }
        if (story.fates().isEmpty() && story.openFates().isEmpty()) {
            g.drawString(this.font, Component.translatable("gui.sofe.journal.no_fates"), x, y, MUTED, false);
        }
        y += 10;
        g.drawString(this.font, Component.translatable("gui.sofe.journal.pact"), x, y, GOLD, false);
        var members = com.sofe.client.ClientPactData.members();
        if (members.isEmpty()) {
            g.drawString(this.font, Component.translatable("gui.sofe.journal.no_pact"), x, y + 12, MUTED, false);
            g.drawString(this.font, Component.translatable("gui.sofe.journal.pact_how"), x, y + 24, MUTED, false);
            return;
        }
        int row = y + 12;
        for (var m : members) {
            Component line = Component.literal((m.leader() ? "★ " : "• ") + m.name() + "  ")
                    .append(m.bearer().isEmpty() ? Component.literal("") : Component.translatable("class.sofe." + m.bearer()));
            g.drawString(this.font, line, x, row, m.online() ? PARCHMENT : MUTED, false);
            row += 10;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
