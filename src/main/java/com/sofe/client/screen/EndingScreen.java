package com.sofe.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sofe.client.ClientClassData;
import com.sofe.client.ClientStoryData;
import com.sofe.network.ScenePackets;
import com.sofe.network.SoFENetwork;
import com.sofe.network.SyncStoryPacket;
import com.sofe.player.PlayerClass;
import com.sofe.story.Epilogue;
import com.sofe.world.region.Region;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.sofe.client.screen.SceneArt.*;

/**
 * The ending (README, 2.7; docs/Jugabilidad.md, "Fates and epilogues"; UC-31), on the player's own screen once Nahrazel
 * falls: the moment inside the Codex, where the seven locks turn and the Bearer's own sin tempts them one last time;
 * the waking in the ruins as the black aetherium runs clear; one slide per region, picked by the fate the Bearer chose
 * there (com.sofe.story.Epilogue); their own epilogue, full or unfinished by their Bearer quests, with their own figure
 * at dawn; the eighth lock (the sequel hook); the credits. Drawn by code, about a minute and a half; Space moves on,
 * Escape skips it all. The art here stands in for the splash arts of docs/Anexos.md, A2.
 */
public class EndingScreen extends Screen {
    private enum Kind { CODEX, WAKE, SECTION, SLIDE, BEARER, EIGHTH, CREDITS }

    /**
     * @param text  the lang key of the card's text
     * @param title a title above it (a region's name, a hero's name), or null
     * @param sub   a line under the title (the fate), or null
     * @param locks for the Codex cards: how many of the seven locks are turned, from start to end of the card
     */
    private record Card(Kind kind, float seconds, String text, Component title, Component sub, Region region, float locksFrom, float locksTo) {
    }

    private static final float FADE = 0.7f;
    private final PlayerClass bearer;
    private final List<Card> cards = new ArrayList<>();
    private int card, ticks;
    private boolean finished;

    public EndingScreen() {
        this(0);
    }

    /** Opening at card {@code start} (PlaceShots' ending:&lt;bearer&gt;:&lt;card&gt;). */
    public EndingScreen(int start) {
        super(Component.translatable("ending.sofe.credits.title"));
        bearer = ClientClassData.get().orElse(PlayerClass.KING);
        Map<String, String> fates = ClientStoryData.get().map(SyncStoryPacket::fates).orElse(Map.of());
        List<SyncStoryPacket.Quest> quests = ClientStoryData.get().map(SyncStoryPacket::quests).orElse(List.of());
        boolean full = Epilogue.full(bearer, id -> quests.stream().anyMatch(q -> q.id().equals(id) && q.completed()));
        String b = bearer.name().toLowerCase(Locale.ROOT);
        cards.add(new Card(Kind.CODEX, 6.5f, key("codex.1"), null, null, null, 0, 0));
        cards.add(new Card(Kind.CODEX, 7f, key("codex.2"), null, null, null, 0, 7));
        cards.add(new Card(Kind.CODEX, 8f, key("codex.sin." + b), null, null, null, 6, 6));
        cards.add(new Card(Kind.CODEX, 5.5f, key("codex.refuse"), null, null, null, 6, 7));
        cards.add(new Card(Kind.WAKE, 8f, key("wake"), null, null, null, 0, 0));
        cards.add(new Card(Kind.SECTION, 3.5f, key("title.fates"), null, null, null, 0, 0));
        for (Region region : Epilogue.REGIONS) {
            String fate = Epilogue.fateShown(region, fates);
            Component sub = Epilogue.UNSETTLED.equals(fate) ? Component.translatable(key("unsettled"))
                    : Component.translatable(com.sofe.story.RegionFates.translationKey(region, fate));
            cards.add(new Card(Kind.SLIDE, 8f, Epilogue.slideKey(region, fates), Component.translatable(region.translationKey()), sub, region, 0, 0));
        }
        cards.add(new Card(Kind.SECTION, 3.5f, key("title.bearer"), null, null, null, 0, 0));
        cards.add(new Card(Kind.BEARER, 11f, Epilogue.epilogueKey(bearer, full), Component.translatable(bearer.heroKey()), null, null, 0, 0));
        cards.add(new Card(Kind.EIGHTH, 8.5f, key("eighth.2"), Component.translatable(key("eighth.1")), null, null, 0, 0));
        cards.add(new Card(Kind.CREDITS, 9f, key("credits.thanks"), Component.translatable(key("credits.title")),
                Component.translatable(key("credits.by"), "SUPERharolxomg"), null, 0, 0));
        card = Mth.clamp(start, 0, cards.size() - 1);
    }

    private static String key(String part) {
        return "ending.sofe." + part;
    }

    @Override
    protected void init() {
        if (card == 0 && ticks == 0) sound(SoundEvents.BEACON_POWER_SELECT, 0.6f);
    }

    @Override
    public void tick() {
        if (++ticks >= cards.get(card).seconds() * 20) next();
    }

    private void next() {
        if (card + 1 >= cards.size()) {
            finish();
            return;
        }
        card++;
        ticks = 0;
        switch (cards.get(card).kind()) {
            case WAKE -> sound(SoundEvents.BEACON_ACTIVATE, 0.8f);
            case SLIDE, SECTION -> sound(SoundEvents.BOOK_PAGE_TURN, 0.8f);
            case BEARER -> sound(SoundEvents.BELL_RESONATE, 1.2f);
            case EIGHTH -> sound(SoundEvents.WARDEN_HEARTBEAT, 0.6f);
            default -> sound(SoundEvents.ENCHANTMENT_TABLE_USE, 0.7f);
        }
    }

    private void sound(SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, 0.7f));
    }

    private void finish() {
        if (finished) return;
        finished = true;
        Minecraft.getInstance().setScreen(null);
        SoFENetwork.sendToServer(new ScenePackets.Done());
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            finish();
        } else if (key == GLFW.GLFW_KEY_SPACE || key == GLFW.GLFW_KEY_ENTER) {
            next();
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        next();
        return true;
    }

    @Override
    public void onClose() {
        finish();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------------------------------------------------------------------------------------------ drawing

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Card c = cards.get(card);
        float t = (ticks + partialTick) / 20f, p = Mth.clamp(t / c.seconds(), 0, 1);
        int w = width, h = height;
        switch (c.kind()) {
            case CODEX -> codex(g, w, h, t, c, p);
            case WAKE -> wake(g, w, h, t, p);
            case SLIDE -> slide(g, w, h, t, c);
            case BEARER -> bearer(g, w, h, t, p);
            case EIGHTH -> eighth(g, w, h, t);
            default -> g.fill(0, 0, w, h, 0xFF000000);
        }
        g.pose().pushPose();
        g.pose().translate(0, 0, 400); // in front of the Bearer's model
        float a = Mth.clamp(Math.min(t / FADE, (c.seconds() - t) / FADE), 0, 1);
        switch (c.kind()) {
            case SECTION -> big(g, Component.translatable(c.text()), w / 2, h / 2 - 12, 2.2f, argb(a, 0xF0D890));
            case CREDITS -> credits(g, w, h, t, c, a);
            case EIGHTH -> {
                big(g, c.title(), w / 2, (int) (h * 0.66f), 1.6f, argb(a, 0xE8E0F0));
                paragraph(g, Component.translatable(c.text()), w / 2, (int) (h * 0.66f) + 26, Math.min(w - 60, 380), argb(a, 0xB8B0C8));
            }
            default -> subtitles(g, w, h, c, a);
        }
        g.drawString(font, Component.translatable(key("skip")), w - font.width(Component.translatable(key("skip"))) - 6, 6, 0x60FFFFFF, false);
        float black = 1 - Mth.clamp(Math.min(t / FADE, (c.seconds() - t) / FADE), 0, 1);
        if (black > 0.01f) g.fill(0, 0, w, h, argb(black * 0.85f, 0));
        g.pose().popPose();
    }

    /** The text of most cards in the lower black bar, with its title and subtitle above. */
    private void subtitles(GuiGraphics g, int w, int h, Card c, float a) {
        int lines = font.split(Component.translatable(c.text()), Math.min(w - 60, 440)).size();
        int bar = Math.max(h / 5, 14 + lines * 11 + (c.title() != null ? 15 : 0) + (c.sub() != null ? 13 : 0));
        g.fill(0, h - bar, w, h, 0xC8000000);
        int y = h - bar + 8;
        if (c.title() != null) {
            g.drawCenteredString(font, c.title(), w / 2, y, argb(a, 0xF0D890));
            y += 11;
        }
        if (c.sub() != null) {
            g.drawCenteredString(font, c.sub(), w / 2, y, argb(a, 0xA8C8D8));
            y += 13;
        } else if (c.title() != null) {
            y += 4;
        }
        paragraph(g, Component.translatable(c.text()), w / 2, y, Math.min(w - 60, 440), argb(a, 0xF0E8DC));
    }

    private void paragraph(GuiGraphics g, Component text, int cx, int y, int width, int color) {
        List<FormattedCharSequence> lines = font.split(text, width);
        for (int i = 0; i < lines.size(); i++) g.drawString(font, lines.get(i), cx - font.width(lines.get(i)) / 2, y + i * 11, color, true);
    }

    private void big(GuiGraphics g, Component text, int cx, int y, float scale, int color) {
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(cx, y, 0);
        pose.scale(scale, scale, 1);
        g.drawString(font, text, -font.width(text) / 2, 0, color, true);
        pose.popPose();
    }

    /** Inside the Codex: an open book of light under an arc of seven locks that turn one by one. */
    private void codex(GuiGraphics g, int w, int h, float t, Card c, float p) {
        sky(g, w, h, 0xFF06040E, 0xFF241038);
        stars(g, w, h, t, 120, 1f);
        int cx = w / 2, cy = (int) (h * 0.46f), unit = Math.max(3, h / 80);
        int pw = unit * 30, ph = unit * 20;
        g.fill(cx - pw - unit, cy - ph / 2 - unit, cx + pw + unit, cy + ph / 2 + unit, 0xFF8A6A20); // the gilt binding
        g.fill(cx - pw, cy - ph / 2, cx - 1, cy + ph / 2, 0xFFF4ECD8);
        g.fill(cx + 1, cy - ph / 2, cx + pw, cy + ph / 2, 0xFFEFE6CE);
        g.fill(cx - 1, cy - ph / 2, cx + 1, cy + ph / 2, 0xFFB09060);
        // lines of the seal, written as the card goes on
        for (int line = 0; line < 9; line++) {
            for (int side = 0; side < 2; side++) {
                float written = Mth.clamp(p * 1.4f - (line + side * 9) / 18f * 0.9f, 0, 1);
                int x0 = side == 0 ? cx - pw + unit * 2 : cx + unit * 2;
                int len = (int) ((pw - unit * 4) * (0.6f + 0.4f * rand(line, side + 50)) * written);
                g.fill(x0, cy - ph / 2 + unit * 2 + line * unit * 2, x0 + len, cy - ph / 2 + unit * 2 + line * unit * 2 + 1, 0xFF5A3A70);
            }
        }
        disc(g, cx, cy, unit * 3 + (int) (unit * Mth.sin(t * 2)), 0x30C890FF); // the glow of the Quill
        float turned = Mth.lerp(p, c.locksFrom(), c.locksTo());
        for (int i = 0; i < 7; i++) {
            double ang = Math.PI * (0.12 + 0.76 * i / 6.0);
            int lx = cx - (int) (Math.cos(ang) * pw * 1.15), ly = cy - ph / 2 - unit * 4 - (int) (Math.sin(ang) * unit * 10);
            boolean lit = turned > i + 0.5f;
            boolean tempting = c.locksFrom() == 6 && c.locksTo() == 6 && i == 6; // the Bearer's own sin, on the last page
            int ring = tempting ? argb(0.6f + 0.4f * Mth.sin(t * 6), 0xE03040) : lit ? 0xFFF0D070 : 0xFF4A3A5A;
            disc(g, lx, ly, unit * 2 + 1, ring);
            disc(g, lx, ly, unit * 2 - 1, lit ? 0xFF6A4A10 : 0xFF140C1C);
            g.fill(lx - 1, ly - unit / 2 - 1, lx + 1, ly + unit, lit ? 0xFFF0D070 : 0xFF6A5A7A); // the keyhole
        }
        motes(g, w, h, t, 50, 14, 0xD8B0FF, 0xF0E0A0);
    }

    /** The ruins of the Inverted Throne at dawn, and the black aetherium running clear from the ground up. */
    private void wake(GuiGraphics g, int w, int h, float t, float p) {
        p = (float) Math.sqrt(p); // the light comes quickly, then slowly
        sky(g, w, h, lerpColor(0xFF000000, 0xFF2A3A5A, p), lerpColor(0xFF080410, 0xFF9AD8E8, p));
        skyline(g, City.SULTHARI, w, h, t, 0.62f, 0.2f, lerpColor(0xFF120A18, 0xFF2A3446, p), 7);
        int clear = (int) (h * (1 - 0.55f * p));
        g.fill(0, clear, w, h, argb(0.25f + 0.25f * p, 0x8AF0F0)); // the aetherium clearing, rising
        skyline(g, City.SULTHARI, w, h, t, 0.76f, 0.5f, 0xFF0C0A12, 13);
        motes(g, w, h, t, 70, 20, 0xB0F8FF, 0xFFFFFF);
    }

    /** A region at dawn, its skyline whole, in its own colours; greyer when nothing was settled there. */
    private void slide(GuiGraphics g, int w, int h, float t, Card c) {
        int[] sky = switch (c.region()) {
            case NORDRATH -> new int[]{0xFF2A4058, 0xFFD8E8F0, 0xFF3A4A60, 0xFF1C2838};
            case PARSIVAN -> new int[]{0xFF2A1A48, 0xFFE8A0C8, 0xFF4A2A60, 0xFF24143A};
            case KHEMET -> new int[]{0xFF3A2410, 0xFFF0C878, 0xFF6A4A24, 0xFF3A2814};
            case AUREUM -> new int[]{0xFF1A2A5A, 0xFFF0E0C0, 0xFF3A4A7A, 0xFF1E2A4A};
            default -> new int[]{0xFF1A2A3A, 0xFFF0B868, 0xFF4A3A2A, 0xFF2A2018};
        };
        boolean unsettled = c.text().endsWith("." + Epilogue.UNSETTLED);
        float grey = unsettled ? 0.45f : 0;
        sky(g, w, h, lerpColor(sky[0], 0xFF303030, grey), lerpColor(sky[1], 0xFF8A8A8A, grey));
        disc(g, w / 2 + (int) (w * 0.18f), (int) (h * 0.5f), Math.max(12, h / 10), argb(unsettled ? 0.35f : 0.8f, 0xFFF0C0)); // the sun, rising
        City city = City.of(c.region());
        skyline(g, city, w, h, t, 0.56f, 0.3f, lerpColor(sky[2], 0xFF404040, grey), 7);
        skyline(g, city, w, h, t, 0.66f, 0.7f, lerpColor(sky[3], 0xFF202020, grey), 13);
        if (!unsettled) motes(g, w, h, t, 40, 10, 0xFFF0C0, 0xFFFFFF);
    }

    /** The Bearer's epilogue: their own figure on a hill of their land, at dawn. */
    private void bearer(GuiGraphics g, int w, int h, float t, float p) {
        sky(g, w, h, 0xFF1A2440, lerpColor(0xFFE09060, 0xFFF0D8A0, p));
        stars(g, w, h, t, 40, 0.3f);
        City city = City.of(bearer);
        skyline(g, city, w, h, t, 0.6f, 0.15f, 0xFF4A3A40, 7);
        int hill = (int) (h * 0.72f), rise = h / 9, halfW = (int) (w * 0.42f);
        for (int i = 0; i < rise; i++) { // a low hill, the Bearer on its top
            int half = (int) (halfW * Math.sqrt(1 - (i / (float) rise) * (i / (float) rise)));
            g.fill(w / 2 - half, hill + rise - i - 1, w / 2 + half, hill + rise - i, 0xFF1A1418);
        }
        g.fill(0, hill + rise, w, h, 0xFF1A1418);
        var player = Minecraft.getInstance().player;
        int scale = (int) (h / 6.5f);
        if (player != null) InventoryScreen.renderEntityInInventoryFollowsMouse(g, w / 2, hill, scale, -30f, -8f, player);
        motes(g, w, h, t, 40, 8, 0xFFE0A0, 0xFFFFFF);
    }

    /** The sequel hook: eight locks in a ring, seven turned and gilt, one still dark. */
    private void eighth(GuiGraphics g, int w, int h, float t) {
        g.fill(0, 0, w, h, 0xFF040208);
        stars(g, w, h, t, 60, 1f);
        int cx = w / 2, cy = (int) (h * 0.34f), r = Math.max(30, h / 6), unit = Math.max(3, h / 80);
        for (int i = 0; i < 8; i++) {
            double ang = Math.PI * 2 * i / 8 - Math.PI / 2;
            int lx = cx + (int) (Math.cos(ang) * r), ly = cy + (int) (Math.sin(ang) * r);
            boolean dark = i == 7;
            disc(g, lx, ly, unit * 2 + 1, dark ? argb(0.4f + 0.3f * Mth.sin(t * 2), 0x6A3AA0) : 0xFFF0D070);
            disc(g, lx, ly, unit * 2 - 1, dark ? 0xFF0A0610 : 0xFF6A4A10);
            g.fill(lx - 1, ly - unit / 2 - 1, lx + 1, ly + unit, dark ? 0xFF3A2A4A : 0xFFF0D070);
        }
    }

    private void credits(GuiGraphics g, int w, int h, float t, Card c, float a) {
        g.fill(0, 0, w, h, 0xFF000000);
        stars(g, w, h, t, 90, 1f);
        big(g, c.title(), w / 2, h / 2 - 34, 2.4f, argb(a, 0xF0D890));
        g.drawCenteredString(font, c.sub(), w / 2, h / 2 + 2, argb(a, 0xC8C8D8));
        g.drawCenteredString(font, Component.translatable(c.text()), w / 2, h / 2 + 22, argb(a, 0xE8E0D0));
    }
}
