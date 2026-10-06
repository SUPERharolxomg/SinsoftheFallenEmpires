package com.sofe.client.screen;

import com.sofe.player.PlayerClass;
import com.sofe.world.region.Region;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * The drawing kit of the story's scenes (CrownedInAshScreen, EndingScreen): skies, the skylines of the five
 * empires, embers and stars, all drawn by code in flat shapes so they read like the mod's pixel art.
 */
final class SceneArt {
    /** Whose skyline: the five lands of Aetheris. */
    enum City {
        SULTHARI, NORDRATH, PARSIVAN, KHEMET, AUREUM;

        /** A Bearer's own land: Aureum for the Knight, Khemet for the Necromancer and so on. */
        static City of(PlayerClass bearer) {
            return switch (bearer) {
                case KNIGHT -> AUREUM;
                case NECROMANCER -> KHEMET;
                case SORCERESS -> PARSIVAN;
                case THIEF -> NORDRATH;
                case KING -> SULTHARI;
            };
        }

        static City of(Region region) {
            return switch (region) {
                case NORDRATH -> NORDRATH;
                case PARSIVAN -> PARSIVAN;
                case KHEMET -> KHEMET;
                case AUREUM -> AUREUM;
                default -> SULTHARI;
            };
        }
    }

    private SceneArt() {
    }

    static int argb(float alpha, int rgb) {
        return (Mth.clamp((int) (alpha * 255), 0, 255) << 24) | (rgb & 0xFFFFFF);
    }

    static int lerpColor(int a, int b, float f) {
        int r = (int) Mth.lerp(f, (a >> 16) & 255, (b >> 16) & 255), gr = (int) Mth.lerp(f, (a >> 8) & 255, (b >> 8) & 255);
        int bl = (int) Mth.lerp(f, a & 255, b & 255);
        return 0xFF000000 | r << 16 | gr << 8 | bl;
    }

    /** A cheap hash in [0, 1) for scattering things the same way every frame. */
    static float rand(int i, int salt) {
        int x = i * 374761393 + salt * 668265263;
        x = (x ^ (x >>> 13)) * 1274126177;
        return ((x ^ (x >>> 16)) & 0xFFFFFF) / (float) 0x1000000;
    }

    /** A sky in bands from {@code top} to {@code bottom}. */
    static void sky(GuiGraphics g, int w, int h, int top, int bottom) {
        int bands = 24;
        for (int i = 0; i < bands; i++) {
            g.fill(0, h * i / bands, w, h * (i + 1) / bands, lerpColor(top, bottom, (float) Math.pow(i / (float) (bands - 1), 1.6)));
        }
    }

    static void disc(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int half = (int) Math.sqrt(r * r - dy * dy);
            g.fill(cx - half, cy + dy, cx + half, cy + dy + 1, color);
        }
    }

    /** A triangle standing on (x, base), {@code w} wide and {@code h} high. */
    static void triangle(GuiGraphics g, int x, int base, int w, int h, int color) {
        for (int i = 0; i < h; i++) {
            int half = (int) (w / 2f * (1 - i / (float) h));
            g.fill(x + w / 2 - half, base - i - 1, x + w / 2 + half, base - i, color);
        }
    }

    static void dome(GuiGraphics g, int cx, int base, int r, int color) {
        for (int i = 0; i < r; i++) {
            int half = (int) Math.sqrt(r * r - i * i);
            g.fill(cx - half, base - i - 1, cx + half, base - i, color);
        }
    }

    /** A city's skyline, scrolling slowly (parallax): its base at {@code baseY} of the height. */
    static void skyline(GuiGraphics g, City city, int w, int h, float t, float baseY, float speed, int color, int salt) {
        int base = (int) (h * baseY);
        int unit = Math.max(4, h / 60);
        int offset = (int) (t * speed * unit) % (unit * 40);
        g.fill(0, base, w, h, color);
        for (int x = -unit * 40 - offset, i = 0; x < w + unit * 20; i++) {
            int width = unit * (3 + (int) (rand(i, salt) * 5));
            int height = unit * (3 + (int) (rand(i, salt + 1) * (salt > 10 ? 6 : 9)));
            building(g, city, x, base, width, height, unit, color, rand(i, salt + 2));
            x += width + unit * (int) (rand(i, salt + 3) * 3);
        }
    }

    private static void building(GuiGraphics g, City city, int x, int base, int w, int h, int unit, int color, float kind) {
        switch (city) {
            case AUREUM -> { // temples with pediments, the Colosseum's arches
                g.fill(x, base - h, x + w, base, color);
                if (kind < 0.5f) triangle(g, x - unit / 2, base - h, w + unit, unit * 2, color);
                else for (int a = x + unit / 2; a < x + w - unit / 2; a += unit) g.fill(a, base - h + unit, a + unit / 2, base - h + unit * 2, 0x30FF6020);
            }
            case KHEMET -> { // pyramids and obelisks
                if (kind < 0.6f) triangle(g, x, base, w * 2, h, color);
                else {
                    g.fill(x + w / 2 - unit / 2, base - h - unit * 2, x + w / 2 + unit / 2, base, color);
                    triangle(g, x + w / 2 - unit / 2, base - h - unit * 2, unit, unit, color);
                }
            }
            case PARSIVAN -> { // domes and minarets
                g.fill(x, base - h, x + w, base, color);
                dome(g, x + w / 2, base - h, w / 2, color);
                if (kind < 0.4f) g.fill(x + w + unit / 2, base - h - unit * 4, x + w + unit, base, color);
            }
            case NORDRATH -> { // longhouses under steep roofs
                g.fill(x, base - h / 2, x + w, base, color);
                triangle(g, x - unit / 2, base - h / 2, w + unit, h, color);
            }
            case SULTHARI -> { // domes, minarets and the Great Observatory
                g.fill(x, base - h, x + w, base, color);
                if (kind < 0.5f) dome(g, x + w / 2, base - h, w / 2, color);
                else g.fill(x + w / 3, base - h - unit * 5, x + w / 3 + unit / 2 + 1, base - h, color);
            }
        }
    }

    /** Motes rising (embers) or drifting up slowly (motes of light), in the two colours given. */
    static void motes(GuiGraphics g, int w, int h, float t, int count, float speed, int colorA, int colorB) {
        for (int i = 0; i < count; i++) {
            float s = speed * (0.45f + rand(i, 11));
            float y = h - ((t * s + rand(i, 12) * h) % (h * 1.1f));
            float x = rand(i, 13) * w + 10 * Mth.sin(t * 1.7f + i);
            float life = 1 - y / h;
            int size = rand(i, 14) < 0.2f ? 2 : 1;
            g.fill((int) x, (int) y, (int) x + size, (int) y + size, argb(0.9f - life * 0.6f, rand(i, 15) < 0.5f ? colorA : colorB));
        }
    }

    /** Still stars that twinkle. */
    static void stars(GuiGraphics g, int w, int h, float t, int count, float maxY) {
        for (int i = 0; i < count; i++) {
            float tw = 0.5f + 0.5f * Mth.sin(t * (1 + rand(i, 31) * 3) + i);
            g.fill((int) (rand(i, 32) * w), (int) (rand(i, 33) * h * maxY), (int) (rand(i, 32) * w) + 1, (int) (rand(i, 33) * h * maxY) + 1,
                    argb(0.3f + 0.7f * tw, 0xF0F0FF));
        }
    }
}
