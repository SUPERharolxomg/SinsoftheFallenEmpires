package com.sofe.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sofe.SoFEMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Codex (docs/Anexos.md, "The Codex"), from the title screen or the Journal: a book of the story's lore, a
 * bestiary of the great foes and a gallery of the illustrations, each opening as the Bearer gets there
 * ({@link CodexProgress}: in a world, its story; from the menu, every local world). The right page lists, the left
 * page shows what is picked (the Codex's cover until then). Texts from scripts/make_codex.py.
 */
public class CodexScreen extends Screen {
    private enum Tab { STORY, BESTIARY, GALLERY }

    /** The chapters (scripts/make_codex.py): the act each opens at, -1 once Nahrazel has fallen. */
    private static final int[] CHAPTER_ACTS = {0, 0, 0, 0, 1, 2, 3, 4, 5, -1};
    /** The great foes in the order the story meets them. */
    static final List<String> BEASTS = List.of("brass_sentinel", "kaleth", "serath", "vorath", "mirael", "thessyn", "luxara", "dormiel",
            "morthis", "goldarc", "nixara", "avarok", "fenrath", "gularth", "shadeyn", "envyris", "solrath", "prython", "nahrazel");
    private static final int PARCHMENT = 0xFFE8DCC0, INK = 0xFF3A2814, GOLD = 0xFFC9A04A, ROW = 20;

    private final Screen parent;
    private final CodexProgress progress = CodexProgress.current();
    private Tab tab = Tab.STORY;
    private int picked = -1, scroll;
    private final List<Gallery> gallery = new ArrayList<>();
    private Button view;

    /** An illustration the gallery may show, and whether the Bearer has unlocked it. */
    private record Gallery(String path, Splash.Image image) {
    }

    public CodexScreen(Screen parent) {
        super(Component.translatable("codex.sofe.title"));
        this.parent = parent;
        collectGallery();
    }

    private void collectGallery() {
        List<String> open = new ArrayList<>(List.of("intro/night_of_the_eclipse", "codex/cover", "map/codex_map"));
        for (String hero : List.of("cassian", "ankhareth", "shirin", "rurik", "azhar")) open.add("hero/" + hero);
        String[][] empires = {{"sulthari", "1"}, {"nordrath", "2"}, {"parsivan", "3"}, {"khemet", "3"}, {"aureum", "4"}};
        for (String[] e : empires) if (progress.act() >= Integer.parseInt(e[1])) open.add("empire/" + e[0]);
        for (String beast : BEASTS) if (beaten(beast)) open.add("boss/" + beast);
        if (beaten("nahrazel")) {
            for (String[] f : new String[][]{{"sulthari", "bazaar", "observatory"}, {"nordrath", "peace", "war"}, {"parsivan", "awakened", "asleep"},
                    {"khemet", "rest", "guardians"}, {"aureum", "law", "shared"}}) {
                for (int i = 1; i < f.length; i++) open.add("fate/" + f[0] + "_" + f[i]);
                open.add("fate/" + f[0] + "_unsettled");
            }
            for (String hero : List.of("cassian", "ankhareth", "shirin", "rurik", "azhar")) {
                open.add("epilogue/" + hero + "_full");
                open.add("epilogue/" + hero + "_unfinished");
            }
        }
        for (String path : open) Splash.find(path).ifPresent(img -> gallery.add(new Gallery(path, img)));
    }

    /** Opens a tab with an entry picked (TitleShot's codex check). */
    public void pick(int tabIndex, int entry) {
        tab = Tab.values()[tabIndex];
        picked = Math.min(entry, entries() - 1);
        updateView();
    }

    private boolean beaten(String beast) {
        return progress.bosses().contains("sofe:" + beast);
    }

    private boolean chapterOpen(int i) {
        int act = CHAPTER_ACTS[i];
        return act < 0 ? beaten("nahrazel") : progress.act() >= act;
    }

    // --- layout: the open book, its two pages

    private int bookW() {
        return Math.min(this.width - 24, 560);
    }

    private int bookH() {
        return Math.min(this.height - 64, 320);
    }

    private int bookX() {
        return (this.width - bookW()) / 2;
    }

    private int bookY() {
        return (this.height - bookH()) / 2 - 4;
    }

    private int pageW() {
        return bookW() / 2 - 12;
    }

    private int leftX() {
        return bookX() + 8;
    }

    private int rightX() {
        return bookX() + bookW() / 2 + 4;
    }

    private int listTop() {
        return bookY() + 30;
    }

    private int listRows() {
        return (bookY() + bookH() - 16 - listTop()) / ROW;
    }

    @Override
    protected void init() {
        int tabW = (pageW() - 8) / 3, x = rightX();
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            Tab t = tabs[i];
            addRenderableWidget(Button.builder(Component.translatable("codex.sofe.tab." + t.name().toLowerCase(java.util.Locale.ROOT)), b -> {
                tab = t;
                picked = -1;
                scroll = 0;
                updateView();
            }).bounds(x + i * (tabW + 4), bookY() + 6, tabW, 18).build());
        }
        view = addRenderableWidget(Button.builder(Component.translatable("codex.sofe.view"), b -> showArt())
                .bounds(leftX() + pageW() / 2 - 60, bookY() + bookH() - 26, 120, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(this.width / 2 - 50, bookY() + bookH() + 8, 100, 18).build());
        updateView();
    }

    /** The illustration of what is picked, if it has one. */
    private Optional<Splash.Image> pickedArt() {
        if (picked < 0) return Optional.empty();
        return switch (tab) {
            case BESTIARY -> beaten(BEASTS.get(picked)) ? Splash.find("boss/" + BEASTS.get(picked)) : Optional.empty();
            case GALLERY -> Optional.of(gallery.get(picked).image());
            default -> Optional.empty();
        };
    }

    private void updateView() {
        if (view != null) view.visible = pickedArt().isPresent();
    }

    private void showArt() {
        pickedArt().ifPresent(art -> Minecraft.getInstance().setScreen(new ArtView(this, art)));
    }

    private int entries() {
        return switch (tab) {
            case STORY -> CHAPTER_ACTS.length;
            case BESTIARY -> BEASTS.size();
            case GALLERY -> gallery.size();
        };
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (mx >= rightX() && mx < rightX() + pageW() && my >= listTop() && my < listTop() + listRows() * ROW) {
            int i = scroll + (int) ((my - listTop()) / ROW);
            if (i < entries()) {
                picked = i;
                updateView();
                Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                        net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, 1f));
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        scroll = Mth.clamp(scroll - (int) Math.signum(delta), 0, Math.max(0, entries() - listRows()));
        return true;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    // --- drawing

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float t = (System.currentTimeMillis() % 100000) / 1000f;
        SceneArt.sky(g, this.width, this.height, 0xFF06040E, 0xFF20102E);
        SceneArt.stars(g, this.width, this.height, t, 120, 1f);
        int x = bookX(), y = bookY(), w = bookW(), h = bookH();
        g.fill(x - 4, y - 4, x + w + 4, y + h + 4, 0xFF4A2C14); // the binding
        g.fill(x - 3, y - 3, x + w + 3, y + h + 3, 0xFF6A4220);
        g.fill(x, y, x + w / 2 - 1, y + h, PARCHMENT);
        g.fill(x + w / 2 + 1, y, x + w, y + h, 0xFFE2D4B4);
        g.fill(x + w / 2 - 1, y, x + w / 2 + 1, y + h, 0xFF8A6A40);
        g.drawCenteredString(this.font, this.title, this.width / 2, y - 16, GOLD);
        leftPage(g);
        rightPage(g, mouseX, mouseY);
        Component status = Component.translatable("codex.sofe.progress", Math.max(progress.act(), 1),
                BEASTS.stream().filter(this::beaten).count(), BEASTS.size());
        String fitted = this.font.substrByWidth(status, pageW() - 4).getString();
        g.drawString(this.font, fitted, rightX() + pageW() / 2 - this.font.width(fitted) / 2, y + h - 11, 0xFF8A7A5A, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void leftPage(GuiGraphics g) {
        int x = leftX(), y = bookY() + 8, w = pageW(), h = bookH() - 16;
        if (picked < 0) { // the cover
            Splash.find("codex/cover").ifPresentOrElse(art -> contain(g, art, x, y, w, h),
                    () -> g.drawCenteredString(this.font, this.title, x + w / 2, y + h / 2, INK));
            return;
        }
        switch (tab) {
            case STORY -> {
                boolean open = chapterOpen(picked);
                Component title = open ? Component.translatable("codex.sofe.chapter." + (picked + 1)) : Component.literal("? ? ?");
                g.drawString(this.font, title, x + 4, y + 4, INK, false);
                g.fill(x + 4, y + 15, x + w - 4, y + 16, GOLD);
                paragraph(g, Component.translatable(open ? "codex.sofe.chapter." + (picked + 1) + ".text" : "codex.sofe.locked"), x + 4, y + 22, w - 8);
            }
            case BESTIARY -> {
                String beast = BEASTS.get(picked);
                boolean known = beaten(beast);
                int size = Math.min(96, w / 2);
                portrait(g, beast, x + (w - size) / 2, y + 2, size, known);
                int ty = y + size + 8;
                Component name = known ? Component.translatable("entity.sofe." + beast) : Component.literal("? ? ?");
                g.drawCenteredString(this.font, name, x + w / 2, ty, 0xFF6A1A10);
                if (known) {
                    var title = this.font.split(Component.translatable("codex.sofe.beast." + beast + ".title"), w - 8);
                    for (int i = 0; i < title.size(); i++) g.drawString(this.font, title.get(i), x + w / 2 - this.font.width(title.get(i)) / 2, ty + 11 + i * 9, 0xFF6A5A3A, false);
                    paragraph(g, Component.translatable("codex.sofe.beast." + beast + ".lore"), x + 4, ty + 14 + title.size() * 9, w - 8);
                } else {
                    paragraph(g, Component.translatable("codex.sofe.locked_beast"), x + 4, ty + 14, w - 8);
                }
            }
            case GALLERY -> contain(g, gallery.get(picked).image(), x, y, w, h - 22);
        }
    }

    private void rightPage(GuiGraphics g, int mouseX, int mouseY) {
        int x = rightX(), w = pageW(), top = listTop();
        if (tab == Tab.GALLERY && gallery.isEmpty()) {
            paragraph(g, Component.translatable("codex.sofe.empty_gallery"), x + 4, top + 4, w - 8);
            return;
        }
        for (int row = 0; row < listRows() && scroll + row < entries(); row++) {
            int i = scroll + row, y = top + row * ROW;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + ROW;
            if (i == picked) g.fill(x, y, x + w, y + ROW - 2, 0x40A07030);
            else if (hover) g.fill(x, y, x + w, y + ROW - 2, 0x20A07030);
            Component label;
            int tx = x + 4;
            switch (tab) {
                case STORY -> label = chapterOpen(i) ? Component.translatable("codex.sofe.chapter." + (i + 1)) : Component.literal("? ? ?");
                case BESTIARY -> {
                    String beast = BEASTS.get(i);
                    portrait(g, beast, x + 2, y + 1, 16, beaten(beast));
                    tx = x + 22;
                    label = beaten(beast) ? Component.translatable("entity.sofe." + beast) : Component.literal("? ? ?");
                }
                default -> {
                    Splash.Image art = gallery.get(i).image();
                    Splash.cover(g, art, x + 2, y + 1, 28, 16);
                    tx = x + 34;
                    label = Component.literal(gallery.get(i).path().substring(gallery.get(i).path().indexOf('/') + 1).replace('_', ' '));
                }
            }
            g.drawString(this.font, this.font.substrByWidth(label, w - (tx - x) - 4).getString(), tx, y + 6, INK, false);
        }
        if (entries() > listRows()) { // the scroll bar
            int barH = listRows() * ROW, knob = Math.max(10, barH * listRows() / entries());
            int ky = top + (barH - knob) * scroll / Math.max(1, entries() - listRows());
            g.fill(x + w - 3, top, x + w - 1, top + barH, 0x30000000);
            g.fill(x + w - 3, ky, x + w - 1, ky + knob, 0xFF8A6A40);
        }
    }

    /** A boss's portrait (gui/portrait/placeholder/&lt;id&gt;.png), its shape alone while it is unknown. */
    private void portrait(GuiGraphics g, String beast, int x, int y, int size, boolean known) {
        ResourceLocation tex = SoFEMod.id("textures/gui/portrait/placeholder/" + beast + ".png");
        if (Minecraft.getInstance().getResourceManager().getResource(tex).isEmpty()) tex = SoFEMod.id("textures/gui/portrait/" + beast + ".png");
        if (!known) RenderSystem.setShaderColor(0.08f, 0.06f, 0.1f, 1f);
        g.blit(tex, x, y, size, size, 0, 0, 64, 64, 64, 64);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        g.renderOutline(x - 1, y - 1, size + 2, size + 2, GOLD);
    }

    private void paragraph(GuiGraphics g, Component text, int x, int y, int width) {
        List<FormattedCharSequence> lines = this.font.split(text, width);
        for (int i = 0; i < lines.size(); i++) g.drawString(this.font, lines.get(i), x, y + i * 10, INK, false);
    }

    /** The whole illustration inside the area, centred (letterboxed). */
    static void contain(GuiGraphics g, Splash.Image art, int x, int y, int w, int h) {
        float scale = Math.min(w / (float) art.width(), h / (float) art.height());
        int dw = Math.round(art.width() * scale), dh = Math.round(art.height() * scale);
        Splash.cover(g, art, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh);
        g.renderOutline(x + (w - dw) / 2 - 1, y + (h - dh) / 2 - 1, dw + 2, dh + 2, GOLD);
    }

    /** One illustration on the whole screen; any key or click goes back. */
    static class ArtView extends Screen {
        private final Screen parent;
        private final Splash.Image art;

        ArtView(Screen parent, Splash.Image art) {
            super(Component.translatable("codex.sofe.view"));
            this.parent = parent;
            this.art = art;
        }

        @Override
        public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            g.fill(0, 0, this.width, this.height, 0xFF000000);
            contain(g, art, 0, 0, this.width, this.height);
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            onClose();
            return true;
        }

        @Override
        public boolean keyPressed(int key, int scan, int mods) {
            onClose();
            return true;
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(parent);
        }
    }
}
