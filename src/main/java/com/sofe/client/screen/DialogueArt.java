package com.sofe.client.screen;

import com.google.gson.JsonArray;
import com.mojang.blaze3d.platform.NativeImage;
import com.sofe.SoFEMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The art of the dialogue box per style (docs/Arte.md, "Dialogue box, one style per empire"): the
 * wide bar when the style has one, otherwise the square 9-slice box and the portrait frame.
 */
final class DialogueArt {
    private static final String FALLBACK_STYLE = "sulthari";

    /** A wide bar and its transparent portrait window, in texture pixels. */
    record Bar(ResourceLocation texture, int width, int height, int windowX, int windowY, int windowW, int windowH) {
    }

    private static final Map<String, Optional<Bar>> BARS = new HashMap<>();
    private static final Map<ResourceLocation, Boolean> EXISTS = new HashMap<>();
    private static final Map<ResourceLocation, int[]> SIZES = new HashMap<>();

    private DialogueArt() {
    }

    /** Forget the cache when resource packs reload. */
    static void clear() {
        BARS.clear();
        EXISTS.clear();
        SIZES.clear();
    }

    static boolean exists(ResourceLocation texture) {
        return EXISTS.computeIfAbsent(texture, t -> Minecraft.getInstance().getResourceManager().getResource(t).isPresent());
    }

    /** Width and height of a texture, read once: portraits can come at any resolution. */
    static int[] size(ResourceLocation texture) {
        return SIZES.computeIfAbsent(texture, t -> {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(t);
            if (resource.isEmpty()) return new int[]{64, 64};
            try (InputStream in = resource.get().open(); NativeImage image = NativeImage.read(in)) {
                return new int[]{image.getWidth(), image.getHeight()};
            } catch (IOException e) {
                return new int[]{64, 64};
            }
        });
    }

    static Optional<Bar> bar(String style) {
        return BARS.computeIfAbsent(style, DialogueArt::loadBar);
    }

    static ResourceLocation box(String style) {
        ResourceLocation box = SoFEMod.id("textures/gui/dialogue/" + style + "_box.png");
        return exists(box) ? box : SoFEMod.id("textures/gui/dialogue/" + FALLBACK_STYLE + "_box.png");
    }

    static ResourceLocation portraitFrame(String style) {
        ResourceLocation frame = SoFEMod.id("textures/gui/dialogue/" + style + "_portrait.png");
        return exists(frame) ? frame : SoFEMod.id("textures/gui/dialogue/" + FALLBACK_STYLE + "_portrait.png");
    }

    /** Reads the bar once to learn its size and where its transparent portrait window is. */
    private static Optional<Bar> loadBar(String style) {
        ResourceLocation texture = SoFEMod.id("textures/gui/dialogue/" + style + "_bar.png");
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(texture);
        if (resource.isEmpty()) return Optional.empty();
        try (InputStream in = resource.get().open(); NativeImage image = NativeImage.read(in)) {
            int w = image.getWidth(), h = image.getHeight();
            Optional<int[]> declared = declaredWindow(style);
            if (declared.isPresent()) {
                int[] r = declared.get();
                return Optional.of(new Bar(texture, w, h, r[0], r[1], r[2], r[3]));
            }
            int minX = w, maxX = -1, minY = h, maxY = -1;
            int scan = Math.min(w, h * 2); // the window sits in the left part of the bar
            int margin = Math.max(3, h / 12); // skip the transparent rounded corners outside the frame
            for (int y = margin; y < h - margin; y++) {
                for (int x = margin; x < scan; x++) {
                    int alpha = (image.getPixelRGBA(x, y) >>> 24) & 0xFF;
                    if (alpha < 20) {
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
            if (maxX < 0) { // no window found: use a square at the left
                minX = h / 5;
                minY = h / 5;
                maxX = minX + h * 3 / 5;
                maxY = minY + h * 3 / 5;
            }
            return Optional.of(new Bar(texture, w, h, minX, minY, maxX - minX + 1, maxY - minY + 1));
        } catch (IOException e) {
            SoFEMod.LOGGER.warn("Could not read {}: {}", texture, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * The window rectangle written next to the bar (&lt;style&gt;_bar.json, {"window": [x, y, w, h]}) by
     * scripts/split_dialogue_bars.py; bars without one are scanned for their window instead.
     */
    private static Optional<int[]> declaredWindow(String style) {
        ResourceLocation file = SoFEMod.id("textures/gui/dialogue/" + style + "_bar.json");
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(file);
        if (resource.isEmpty()) return Optional.empty();
        try (Reader reader = resource.get().openAsReader()) {
            JsonArray window = GsonHelper.getAsJsonArray(GsonHelper.parse(reader), "window");
            int[] rect = new int[4];
            for (int i = 0; i < 4; i++) rect[i] = window.get(i).getAsInt();
            return Optional.of(rect);
        } catch (IOException | RuntimeException e) {
            SoFEMod.LOGGER.warn("Could not read {}: {}", file, e.getMessage());
            return Optional.empty();
        }
    }

    /** Draws a 9-slice texture: corners kept, edges and center stretched. */
    static void nineSlice(GuiGraphics g, ResourceLocation texture, int x, int y, int w, int h, int border, int texSize) {
        int c = texSize - border * 2;
        int iw = w - border * 2, ih = h - border * 2;
        // corners
        g.blit(texture, x, y, border, border, 0, 0, border, border, texSize, texSize);
        g.blit(texture, x + w - border, y, border, border, texSize - border, 0, border, border, texSize, texSize);
        g.blit(texture, x, y + h - border, border, border, 0, texSize - border, border, border, texSize, texSize);
        g.blit(texture, x + w - border, y + h - border, border, border, texSize - border, texSize - border, border, border, texSize, texSize);
        // edges
        g.blit(texture, x + border, y, iw, border, border, 0, c, border, texSize, texSize);
        g.blit(texture, x + border, y + h - border, iw, border, border, texSize - border, c, border, texSize, texSize);
        g.blit(texture, x, y + border, border, ih, 0, border, border, c, texSize, texSize);
        g.blit(texture, x + w - border, y + border, border, ih, texSize - border, border, border, c, texSize, texSize);
        // center
        g.blit(texture, x + border, y + border, iw, ih, border, border, c, c, texSize, texSize);
    }
}
