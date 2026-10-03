package com.sofe.client.screen;

import com.sofe.SoFEMod;
import com.sofe.client.ClientClassData;
import com.sofe.client.ClientProgressData;
import com.sofe.network.LearnSkillPacket;
import com.sofe.network.SoFENetwork;
import com.sofe.network.SyncProgressPacket;
import com.sofe.player.PlayerClass;
import com.sofe.skill.SkillBook;
import com.sofe.skill.SkillCatalog;
import com.sofe.skill.SkillInfo;
import com.sofe.skill.SkillType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Diablo II style skill tree (docs/Clases.md): 30 skills in three branch tabs of 10, one row per unlock
 * level, arrows from each skill to the ones it opens, the rank on every skill and the points left.
 * Clicking a skill asks the server to put a point in it; the server applies the rules.
 */
public class SkillTreeScreen extends Screen {
    /** The skill under the mouse, for placing it on the Combat Bar with 1-5. */
    private SkillInfo hoveredSkill;
    private static final int PANEL_W = 290, PANEL_H = 214;
    private static final int CELL = 20, COLUMN_GAP = 54, ROW_GAP = 27;
    private static final int GOLD = 0xFFE8B64A, PARCHMENT = 0xE6DCC8, MUTED = 0xA89F8E;

    /** The branch shown: 0, 1 or 2. */
    private int tab;

    public SkillTreeScreen() {
        super(Component.translatable("key.sofe.skill_tree"));
    }

    private int left() {
        return (this.width - PANEL_W) / 2;
    }

    private int top() {
        return (this.height - PANEL_H) / 2;
    }

    @Override
    protected void init() {
        int x = left() + PANEL_W - 104;
        String cls = playerClass().map(PlayerClass::id).orElse("knight");
        for (int i = 0; i < 3; i++) {
            int branch = i;
            addRenderableWidget(Button.builder(Component.translatable("gui.sofe.skill_tree.branch." + cls + "." + i), b -> tab = branch)
                    .bounds(x, top() + 22 + i * 22, 100, 18).build());
        }
    }

    private Optional<PlayerClass> playerClass() {
        return ClientClassData.get();
    }

    private List<SkillInfo> visibleSkills() {
        return playerClass().map(SkillCatalog::forClass).orElse(List.of()).stream()
                .filter(s -> s.tab() == tab)
                .toList();
    }

    /** Where a skill sits in its branch: by its column and the row of its level. */
    private int cellX(SkillInfo s) {
        return left() + 16 + s.column() * COLUMN_GAP;
    }

    private int cellY(SkillInfo s) {
        return top() + 22 + s.row() * ROW_GAP;
    }

    private SkillBook book(SyncProgressPacket progress) {
        SkillBook book = new SkillBook();
        book.load(progress.ranks(), progress.slots());
        return book;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int l = left(), t = top();
        graphics.fill(l - 2, t - 2, l + PANEL_W + 2, t + PANEL_H + 2, GOLD);
        graphics.fill(l, t, l + PANEL_W, t + PANEL_H, 0xF0181410);

        Optional<PlayerClass> playerClass = playerClass();
        Optional<SyncProgressPacket> progress = ClientProgressData.get();
        if (playerClass.isEmpty() || progress.isEmpty()) {
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        SyncProgressPacket p = progress.get();
        SkillBook book = book(p);

        graphics.drawString(this.font, Component.translatable("gui.sofe.skill_tree.title",
                Component.translatable(playerClass.get().translationKey())), l + 8, t + 8, GOLD, false);
        graphics.drawString(this.font, Component.translatable("gui.sofe.skill_tree.points", p.skillPoints()),
                l + PANEL_W - 104, t + 96, p.skillPoints() > 0 ? GOLD : PARCHMENT, false);
        graphics.drawString(this.font, Component.translatable("gui.sofe.skill_tree.level", p.level()), l + PANEL_W - 104, t + 108, MUTED, false);

        List<SkillInfo> skills = visibleSkills();
        // Arrows first, so the skills are drawn over them
        for (SkillInfo s : skills) {
            if (s.parent() == null) continue;
            SkillCatalog.byId(s.parent()).filter(skills::contains).ifPresent(parent -> arrow(graphics, parent, s, book.rank(parent.id()) > 0));
        }

        SkillInfo hovered = null;
        this.hoveredSkill = null;
        for (SkillInfo s : skills) {
            int x = cellX(s), y = cellY(s);
            int rank = book.rank(s.id());
            boolean canLearn = book.check(s, playerClass.get(), p.level(), p.skillPoints()) == SkillBook.LearnResult.LEARNED;
            int border = canLearn ? 0xFF7BC96F : rank > 0 ? GOLD : 0xFF4A4034;
            graphics.fill(x - 1, y - 1, x + CELL + 1, y + CELL + 1, border);
            graphics.fill(x, y, x + CELL, y + CELL, 0xFF0C0A08);
            graphics.blit(SoFEMod.id(s.iconPath()), x + 1, y + 1, CELL - 2, CELL - 2, 0f, 0f, 32, 32, 32, 32);
            if (rank == 0) {
                graphics.fill(x + 1, y + 1, x + CELL - 1, y + CELL - 1, 0xA0000000);
            }
            String rankText = rank + "/" + s.maxRank();
            graphics.drawString(this.font, rankText, x + CELL - this.font.width(rankText), y + CELL - 6, rank > 0 ? 0xFFFFFF : MUTED, true);
            if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                hovered = s;
                this.hoveredSkill = s;
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
        if (hovered != null) {
            graphics.renderTooltip(this.font, tooltip(hovered, book, playerClass.get(), p), mouseX, mouseY);
        }
    }

    /** A line from the bottom of the parent to the top of the child, with an L when the columns differ. */
    private void arrow(GuiGraphics graphics, SkillInfo from, SkillInfo to, boolean open) {
        int color = open ? GOLD : 0xFF4A4034;
        int x1 = cellX(from) + CELL / 2, y1 = cellY(from) + CELL;
        int x2 = cellX(to) + CELL / 2, y2 = cellY(to);
        int mid = y2 - 4;
        graphics.fill(x1, y1, x1 + 2, mid, color);
        graphics.fill(Math.min(x1, x2), mid, Math.max(x1, x2) + 2, mid + 2, color);
        graphics.fill(x2, mid, x2 + 2, y2, color);
        graphics.fill(x2 - 2, y2 - 3, x2 + 4, y2 - 1, color); // arrow head
    }

    private List<FormattedCharSequence> tooltip(SkillInfo s, SkillBook book, PlayerClass playerClass, SyncProgressPacket p) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(s.translationKey()).withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("gui.sofe.skill_tree.type." + s.type().name().toLowerCase(Locale.ROOT))
                .append(" · ").append(Component.translatable("gui.sofe.skill_tree.level", s.level())).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("gui.sofe.skill_tree.rank", book.rank(s.id()), s.maxRank()));
        if (s.isUpgrade()) {
            Component improved = Component.empty();
            for (int i = 0; i < s.upgrades().size(); i++) {
                if (i > 0) improved = improved.copy().append(", ");
                improved = improved.copy().append(Component.translatable("skill.sofe." + s.upgrades().get(i)));
            }
            lines.add(Component.translatable("gui.sofe.skill_tree.improves", improved).withStyle(ChatFormatting.AQUA));
        }
        if (Language.getInstance().has(s.descriptionKey())) {
            lines.add(Component.translatable(s.descriptionKey()).withStyle(ChatFormatting.ITALIC));
        }
        SkillBook.LearnResult check = book.check(s, playerClass, p.level(), p.skillPoints());
        Component status = check == SkillBook.LearnResult.LEARNED
                ? Component.translatable("gui.sofe.skill_tree.click").withStyle(ChatFormatting.GREEN)
                : Component.translatable("message.sofe.learn." + check.name().toLowerCase(Locale.ROOT),
                Component.translatable(s.translationKey()), book.levelForNextRank(s)).withStyle(ChatFormatting.RED);
        lines.add(status);
        if (s.type() == com.sofe.skill.SkillType.ACTIVE && book.rank(s.id()) > 0) {
            lines.add(Component.translatable("gui.sofe.skill_tree.assign").withStyle(ChatFormatting.AQUA));
        }
        List<FormattedCharSequence> out = new ArrayList<>();
        lines.forEach(c -> out.addAll(this.font.split(c, 200)));
        return out;
    }

    /** Over a learned active skill, 1 to 5 put it in that Combat Bar slot. */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (hoveredSkill != null && keyCode >= org.lwjgl.glfw.GLFW.GLFW_KEY_1 && keyCode <= org.lwjgl.glfw.GLFW.GLFW_KEY_5) {
            SoFENetwork.sendToServer(new com.sofe.network.AssignSlotPacket(hoveredSkill.id(), keyCode - org.lwjgl.glfw.GLFW.GLFW_KEY_1));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (SkillInfo s : visibleSkills()) {
                int x = cellX(s), y = cellY(s);
                if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                    SoFENetwork.sendToServer(new LearnSkillPacket(s.id()));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
