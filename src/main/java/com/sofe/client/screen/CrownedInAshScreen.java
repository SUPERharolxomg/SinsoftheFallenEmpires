package com.sofe.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sofe.client.ClientClassData;
import com.sofe.network.ScenePackets;
import com.sofe.network.SoFENetwork;
import com.sofe.player.PlayerClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/**
 * "Crowned in Ash", the secret bad ending of Prython's offer (docs/Jugabilidad.md): the player's own Bearer, in
 * their own skin and gear, crowned before a throne while the city of their empire burns under a false sun. Each of
 * the five heroes has their own city on the skyline, their own crowd at the foot of the throne and four lines of
 * their own. Drawn by code, about 26 seconds; Escape skips it. Then the server opens the offer again.
 */
public class CrownedInAshScreen extends Screen {
    /** Title card, four beats of five seconds, the waking line. */
    private static final float TITLE = 3.5f, BEAT = 5f, BEATS = 4, WAKE = 4.5f;
    private static final float LENGTH = TITLE + BEAT * BEATS + WAKE;
    private final PlayerClass bearer;
    private int ticks;
    private boolean finished;

    public CrownedInAshScreen() {
        this(0);
    }

    /** Starting part of the way through (PlaceShots' scene:&lt;bearer&gt;:&lt;second&gt;). */
    public CrownedInAshScreen(int startTick) {
        super(Component.translatable("scene.sofe.crowned_in_ash"));
        this.bearer = ClientClassData.get().orElse(PlayerClass.KING);
        this.ticks = Math.max(0, startTick);
    }

    private String key(String part) {
        return "scene.sofe.crowned_in_ash." + part;
    }

    @Override
    protected void init() {
        if (ticks == 0) sound(SoundEvents.BEACON_DEACTIVATE, 0.5f);
    }

    @Override
    public void tick() {
        ticks++;
        float t = ticks / 20f;
        for (int beat = 0; beat < BEATS; beat++) {
            if (ticks == (int) ((TITLE + beat * BEAT) * 20)) sound(beat == BEATS - 1 ? SoundEvents.WITHER_AMBIENT : SoundEvents.FIRECHARGE_USE, 0.6f);
        }
        if (ticks == (int) ((TITLE + BEAT * BEATS) * 20)) sound(SoundEvents.BELL_RESONATE, 0.8f);
        if (t >= LENGTH) finish();
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
            return true;
        }
        return false;
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
        float t = (ticks + partialTick) / 20f;
        int w = width, h = height;
        sky(g, w, h, t);
        falseSun(g, w / 2, (int) (h * 0.26f), Math.max(14, h / 9), t);
        skyline(g, w, h, t, 0.62f, 0.35f, 0xFF2A0C08, 7); // far
        flamesOnSkyline(g, w, h, t);
        if (bearer == PlayerClass.SORCERESS) fallingStars(g, w, h, t);
        skyline(g, w, h, t, 0.74f, 0.9f, 0xFF140504, 13); // near
        int floor = (int) (h * 0.78f);
        g.fill(0, floor, w, h, 0xFF0C0302);
        int cx = w / 2;
        throne(g, cx, floor, h);
        crowd(g, cx, floor, w, h, t);
        bearer(g, cx, floor, h, t);
        // what follows lies in front of the Bearer's model, which the GUI draws some depth forward
        g.pose().pushPose();
        g.pose().translate(0, 0, 400);
        embers(g, w, h, t);
        ash(g, w, h, t);
        letterbox(g, w, h);
        captions(g, w, h, t);
        // fade in from black, and out to black before the waking line
        float fade = t < 1.2f ? 1 - t / 1.2f : 0;
        float end = TITLE + BEAT * BEATS;
        if (t > end - 1 && t < end + 1.2f) fade = Math.max(fade, 1 - Math.abs(t - end) / 1.2f);
        if (t > end + 0.5f) fade = Math.max(fade, 0.82f);
        if (fade > 0) g.fill(0, 0, w, h, argb(fade, 0));
        if (t > end + 0.5f) wakeLine(g, w, h, t - end - 0.5f);
        g.pose().popPose();
    }

    private static int argb(float alpha, int rgb) {
        return (Mth.clamp((int) (alpha * 255), 0, 255) << 24) | (rgb & 0xFFFFFF);
    }

    private static int lerpColor(int a, int b, float f) {
        int r = (int) Mth.lerp(f, (a >> 16) & 255, (b >> 16) & 255), gr = (int) Mth.lerp(f, (a >> 8) & 255, (b >> 8) & 255);
        int bl = (int) Mth.lerp(f, a & 255, b & 255);
        return 0xFF000000 | r << 16 | gr << 8 | bl;
    }

    /** A cheap hash in [0, 1) for scattering things the same way every frame. */
    private static float rand(int i, int salt) {
        int x = i * 374761393 + salt * 668265263;
        x = (x ^ (x >>> 13)) * 1274126177;
        return ((x ^ (x >>> 16)) & 0xFFFFFF) / (float) 0x1000000;
    }

    private void sky(GuiGraphics g, int w, int h, float t) {
        float pulse = 0.5f + 0.5f * Mth.sin(t * 1.3f);
        int top = 0xFF120404, bottom = lerpColor(0xFF7A1A08, 0xFFA83A0C, pulse);
        int bands = 24;
        for (int i = 0; i < bands; i++) {
            int y0 = h * i / bands, y1 = h * (i + 1) / bands;
            g.fill(0, y0, w, y1, lerpColor(top, bottom, (float) Math.pow(i / (float) (bands - 1), 1.6)));
        }
    }

    /** Prython's false sun: a black disc in a burning ring. */
    private void falseSun(GuiGraphics g, int cx, int cy, int r, float t) {
        int corona = r + 6 + (int) (2 * Mth.sin(t * 3));
        disc(g, cx, cy, corona + 6, 0x40FFB040);
        disc(g, cx, cy, corona, 0xC0FFD070);
        disc(g, cx, cy, r + 2, 0xFFFFF0C0);
        disc(g, cx, cy, r, 0xFF060000);
    }

    private static void disc(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int half = (int) Math.sqrt(r * r - dy * dy);
            g.fill(cx - half, cy + dy, cx + half, cy + dy + 1, color);
        }
    }

    /** The skyline of the Bearer's empire, scrolling slowly (parallax): base at {@code baseY} of the height. */
    private void skyline(GuiGraphics g, int w, int h, float t, float baseY, float speed, int color, int salt) {
        int base = (int) (h * baseY);
        int unit = Math.max(4, h / 60);
        int offset = (int) (t * speed * unit) % (unit * 40);
        g.fill(0, base, w, h, color);
        for (int x = -unit * 40 - offset, i = 0; x < w + unit * 20; i++) {
            int width = unit * (3 + (int) (rand(i, salt) * 5));
            int height = unit * (3 + (int) (rand(i, salt + 1) * (salt > 10 ? 6 : 9)));
            building(g, x, base, width, height, unit, color, rand(i, salt + 2));
            x += width + unit * (int) (rand(i, salt + 3) * 3);
        }
    }

    private void building(GuiGraphics g, int x, int base, int w, int h, int unit, int color, float kind) {
        switch (bearer) {
            case KNIGHT -> { // Aureum: temples with pediments, the Colosseum's arches
                g.fill(x, base - h, x + w, base, color);
                if (kind < 0.5f) triangle(g, x - unit / 2, base - h, w + unit, unit * 2, color);
                else for (int a = x + unit / 2; a < x + w - unit / 2; a += unit) g.fill(a, base - h + unit, a + unit / 2, base - h + unit * 2, 0x30FF6020);
            }
            case NECROMANCER -> { // Khemet: pyramids and obelisks
                if (kind < 0.6f) triangle(g, x, base, w * 2, h, color);
                else {
                    g.fill(x + w / 2 - unit / 2, base - h - unit * 2, x + w / 2 + unit / 2, base, color);
                    triangle(g, x + w / 2 - unit / 2, base - h - unit * 2, unit, unit, color);
                }
            }
            case SORCERESS -> { // Parsivan: domes and minarets
                g.fill(x, base - h, x + w, base, color);
                domeShape(g, x + w / 2, base - h, w / 2, color);
                if (kind < 0.4f) g.fill(x + w + unit / 2, base - h - unit * 4, x + w + unit, base, color);
            }
            case THIEF -> { // Nordrath: longhouses under steep roofs
                g.fill(x, base - h / 2, x + w, base, color);
                triangle(g, x - unit / 2, base - h / 2, w + unit, h, color);
            }
            default -> { // Sulthari: domes, minarets and the Great Observatory
                g.fill(x, base - h, x + w, base, color);
                if (kind < 0.5f) domeShape(g, x + w / 2, base - h, w / 2, color);
                else g.fill(x + w / 3, base - h - unit * 5, x + w / 3 + unit / 2 + 1, base - h, color);
            }
        }
    }

    /** A triangle standing on (x, base), {@code w} wide and {@code h} high. */
    private static void triangle(GuiGraphics g, int x, int base, int w, int h, int color) {
        for (int i = 0; i < h; i++) {
            int half = (int) (w / 2f * (1 - i / (float) h));
            g.fill(x + w / 2 - half, base - i - 1, x + w / 2 + half, base - i, color);
        }
    }

    private static void domeShape(GuiGraphics g, int cx, int base, int r, int color) {
        for (int i = 0; i < r; i++) {
            int half = (int) Math.sqrt(r * r - i * i);
            g.fill(cx - half, base - i - 1, cx + half, base - i, color);
        }
    }

    /** The city burns: tongues of fire flickering along the far skyline. */
    private void flamesOnSkyline(GuiGraphics g, int w, int h, float t) {
        int base = (int) (h * 0.62f);
        int unit = Math.max(3, h / 80);
        g.fill(0, base - unit * 2, w, base, 0x30FF5010); // the glow over the burning roofs
        for (int i = 0; i < w / unit; i++) {
            if (rand(i, 2) < 0.62f) continue; // fires here and there, not everywhere
            float flick = 0.5f + 0.5f * Mth.sin(t * (5 + rand(i, 3) * 5) + i * 1.7f);
            int tall = (int) (unit * (2 + rand(i, 5) * 7) * (0.6f + 0.5f * flick));
            int wide = unit * (2 + (int) (rand(i, 6) * 3));
            int x = i * unit + (int) (Mth.sin(t * 2 + i) * unit * 0.3f);
            triangle(g, x, base, wide, tall, argb(0.85f, 0xC83A0C));
            triangle(g, x + wide / 4, base, wide / 2, tall * 2 / 3, argb(0.9f, 0xFF8A20));
            triangle(g, x + wide * 3 / 8, base, Math.max(1, wide / 4), tall / 3, argb(0.95f, 0xFFE070));
        }
    }

    /** The Sorceress's sky: the stars of Parsivan fall burning, one by one. */
    private void fallingStars(GuiGraphics g, int w, int h, float t) {
        for (int i = 0; i < 6; i++) {
            float phase = (t * 0.35f + rand(i, 41)) % 1f;
            int x0 = (int) (rand(i, 42) * w), y0 = (int) (h * 0.05f);
            for (int s = 0; s < 14; s++) {
                float p = phase - s * 0.012f;
                if (p < 0) break;
                int x = x0 - (int) (p * w * 0.3f), y = y0 + (int) (p * h * 0.45f);
                g.fill(x, y, x + 2, y + 2, argb(1 - s / 14f, s == 0 ? 0xFFFFFF : 0xFFC060));
            }
        }
    }

    /** A dais of steps and a tall throne behind the Bearer, in the colours of their empire. */
    private void throne(GuiGraphics g, int cx, int floor, int h) {
        int unit = Math.max(3, h / 70);
        int accent = switch (bearer) {
            case KNIGHT -> 0xFF2A4AB0;
            case NECROMANCER -> 0xFF2A9A8A;
            case SORCERESS -> 0xFF8A3AB0;
            case THIEF -> 0xFF5A7A9A;
            case KING -> 0xFFB08A2A;
        };
        for (int step = 0; step < 4; step++) {
            int half = unit * (14 - step * 3);
            g.fill(cx - half, floor - unit * (step + 1) * 2, cx + half, floor - unit * step * 2, step % 2 == 0 ? 0xFF241210 : 0xFF2E1814);
        }
        int seat = floor - unit * 8, back = unit * 17;
        g.fill(cx - unit * 5, seat - back, cx + unit * 5, seat, 0xFF1A0A08);
        g.fill(cx - unit * 4, seat - back + unit, cx + unit * 4, seat - unit, lerpColor(accent, 0xFF000000, 0.5f));
        g.fill(cx - unit * 7, seat - unit * 6, cx - unit * 5, seat, 0xFF1A0A08); // the arms
        g.fill(cx + unit * 5, seat - unit * 6, cx + unit * 7, seat, 0xFF1A0A08);
        // the crest of the throne: a crown of spikes, gilt, scorched
        for (int s = -2; s <= 2; s++) {
            int sx = cx + s * unit * 2;
            int tall = unit * (s == 0 ? 5 : Math.abs(s) == 1 ? 3 : 2);
            g.fill(sx - unit / 2, seat - back - tall, sx + unit / 2 + 1, seat - back, 0xFF8A6A20);
        }
        g.fill(cx - unit * 6, seat - back, cx + unit * 6, seat - back + unit, 0xFFC09A30);
    }

    /** Who kneels at the foot of the throne: the Bearer's own people, as dark shapes in the firelight. */
    private void crowd(GuiGraphics g, int cx, int floor, int w, int h, float t) {
        int unit = Math.max(3, h / 70);
        for (int row = 0; row < 2; row++) {
            int y = floor + unit * (1 + row * 3);
            for (int i = 0; i < 14; i++) {
                int side = i % 2 == 0 ? -1 : 1;
                int x = cx + side * (unit * (18 + (i / 2) * 6 + row * 3));
                if (x < 0 || x > w) continue;
                int bow = (int) (unit * 0.6f * Mth.sin(t * 1.5f + i));
                figure(g, x, y, unit, bow, i + row * 14);
            }
        }
    }

    private void figure(GuiGraphics g, int x, int y, int unit, int bow, int i) {
        int body = 0xFF070202;
        switch (bearer) {
            case KNIGHT -> { // the Order of the Scale, kneeling, spears down
                g.fill(x - unit, y - unit * 3, x + unit, y, body);
                g.fill(x - unit, y - unit * 4 + bow, x + unit, y - unit * 3 + bow, 0xFF1C1C24);
                g.fill(x + unit * 2, y - unit * 6, x + unit * 2 + 1, y, 0xFF2A2A30);
            }
            case NECROMANCER -> { // the dead of Khemet, standing in rows, eyes lit
                g.fill(x - unit, y - unit * 4, x + unit, y, 0xFF2A2420);
                g.fill(x - unit / 2, y - unit * 5, x + unit / 2 + 1, y - unit * 4, 0xFF4A4038);
                g.fill(x - unit / 2, y - unit * 5 + 1, x - unit / 2 + 1, y - unit * 5 + 2, 0xFF60FFD0);
                g.fill(x + unit / 2 - 1, y - unit * 5 + 1, x + unit / 2, y - unit * 5 + 2, 0xFF60FFD0);
            }
            case THIEF -> { // the clans in chains, bent under sacks of gold
                g.fill(x - unit, y - unit * 3 + bow, x + unit, y, body);
                g.fill(x - unit * 2, y - unit * 4 + bow, x, y - unit * 2 + bow, 0xFF8A6A18);
                if (i % 3 == 0) g.fill(x + unit, y - 1, x + unit * 4, y, 0xFF505058);
            }
            default -> { // courtiers and citizens, bowed to the floor
                g.fill(x - unit, y - unit * 2 + bow, x + unit * 2, y, body);
                g.fill(x + unit, y - unit * 3 + bow, x + unit * 2, y - unit * 2 + bow, body);
            }
        }
    }

    /** The player's own Bearer, standing before the throne with a crown on their head, the camera slowly closing in. */
    private void bearer(GuiGraphics g, int cx, int floor, int h, float t) {
        var player = Minecraft.getInstance().player;
        int unit = Math.max(3, h / 70);
        int feet = floor - unit * 8;
        float push = Mth.clamp(t / LENGTH, 0, 1);
        int scale = (int) (h / 6.2f * (1 + 0.18f * push));
        if (player != null) InventoryScreen.renderEntityInInventoryFollowsMouse(g, cx, feet, scale, 0f, -14f, player);
        // the crown, a little above the head (a player model is about two blocks tall)
        int top = feet - (int) (scale * 1.93f);
        int cw = Math.max(4, scale / 3);
        g.fill(cx - cw, top - 2, cx + cw, top + 1, 0xFFE0B040);
        for (int s = -2; s <= 2; s++) {
            int sx = cx + s * cw / 2;
            g.fill(sx - 1, top - 2 - (s % 2 == 0 ? 6 : 4), sx + 1, top - 2, 0xFFE0B040);
        }
        g.fill(cx - 1, top - 1, cx + 1, top, 0xFFFF3030);
    }

    /** Embers rising from the burning city. */
    private void embers(GuiGraphics g, int w, int h, float t) {
        for (int i = 0; i < 90; i++) {
            float speed = 18 + rand(i, 11) * 40;
            float y = h - ((t * speed + rand(i, 12) * h) % (h * 1.1f));
            float x = rand(i, 13) * w + 10 * Mth.sin(t * 1.7f + i);
            float life = 1 - y / h;
            int size = rand(i, 14) < 0.2f ? 2 : 1;
            g.fill((int) x, (int) y, (int) x + size, (int) y + size, argb(0.9f - life * 0.6f, rand(i, 15) < 0.5f ? 0xFFB030 : 0xFF5010));
        }
    }

    /** Ash falling on the throne. */
    private void ash(GuiGraphics g, int w, int h, float t) {
        for (int i = 0; i < 60; i++) {
            float y = (t * (8 + rand(i, 21) * 10) + rand(i, 22) * h) % h;
            float x = rand(i, 23) * w + 14 * Mth.sin(t * 0.8f + i * 0.7f);
            g.fill((int) x, (int) y, (int) x + 1, (int) y + 1, 0x90A09890);
        }
    }

    private void letterbox(GuiGraphics g, int w, int h) {
        int bar = h / 9;
        g.fill(0, 0, w, bar, 0xFF000000);
        g.fill(0, h - bar, w, h, 0xFF000000);
    }

    private void captions(GuiGraphics g, int w, int h, float t) {
        if (t < TITLE + 0.5f) {
            float a = Mth.clamp(Math.min(t - 0.6f, TITLE + 0.5f - t), 0, 1);
            if (a > 0.02f) bigTitle(g, w, h / 2 - 20, a);
            return;
        }
        int beat = (int) ((t - TITLE) / BEAT);
        if (beat >= BEATS) return;
        float in = (t - TITLE) - beat * BEAT;
        float a = Mth.clamp(Math.min(in / 0.8f, (BEAT - in) / 0.8f), 0, 1);
        if (a < 0.02f) return;
        Component line = Component.translatable(key(bearer.name().toLowerCase(Locale.ROOT) + "." + (beat + 1)));
        var lines = font.split(line, Math.min(w - 40, 420));
        int y = h - h / 20 - lines.size() * 11 / 2; // in the lower black bar, like a film's subtitles
        for (int i = 0; i < lines.size(); i++) {
            int lw = font.width(lines.get(i));
            g.drawString(font, lines.get(i), (w - lw) / 2, y + i * 11, argb(a, 0xF0E0C8), true);
        }
    }

    private void bigTitle(GuiGraphics g, int w, int y, float alpha) {
        Component title = Component.translatable(key("title"));
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(w / 2f, y, 0);
        pose.scale(2.5f, 2.5f, 1);
        g.drawString(font, title, -font.width(title) / 2, 0, argb(alpha, 0xFFC860), true);
        pose.popPose();
    }

    private void wakeLine(GuiGraphics g, int w, int h, float t) {
        float a = Mth.clamp(t / 1.0f, 0, 1);
        Component line = Component.translatable(key("wake"));
        var lines = font.split(line, Math.min(w - 40, 340));
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(font, lines.get(i), (w - font.width(lines.get(i))) / 2, h / 2 - 6 + i * 11, argb(a, 0xE8E0E8), true);
        }
    }
}
