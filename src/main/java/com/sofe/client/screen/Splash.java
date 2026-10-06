package com.sofe.client.screen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.sofe.SoFEMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The final illustrations (docs/ArteFinal.md): {@code textures/gui/splash/<path>.png}, used wherever a screen asks for
 * one as soon as the file exists, and the screen's drawn stand-in otherwise. A resource pack can add or replace them.
 * {@code splash/dialogues.json} names the illustration behind a cinematic conversation.
 */
public final class Splash {
    /** An illustration and its size in pixels. */
    public record Image(ResourceLocation texture, int width, int height) {
    }

    private static final Map<String, Optional<Image>> FOUND = new HashMap<>();
    private static Map<String, String> dialogues;

    private Splash() {
    }

    /** The illustration at splash/&lt;path&gt;.png (for example "fate/khemet_rest"), if the game has it. */
    public static Optional<Image> find(String path) {
        return FOUND.computeIfAbsent(path, p -> {
            ResourceLocation texture = SoFEMod.id("textures/gui/splash/" + p + ".png");
            var resource = Minecraft.getInstance().getResourceManager().getResource(texture);
            if (resource.isEmpty()) return Optional.empty();
            try (InputStream in = resource.get().open(); NativeImage image = NativeImage.read(in)) {
                return Optional.of(new Image(texture, image.getWidth(), image.getHeight()));
            } catch (Exception e) {
                SoFEMod.LOGGER.warn("Could not read the illustration {}: {}", texture, e.getMessage());
                return Optional.empty();
            }
        });
    }

    /** The illustration behind a cinematic conversation, from splash/dialogues.json. */
    public static Optional<Image> forDialogue(String dialogueId) {
        if (dialogues == null) {
            dialogues = new HashMap<>();
            Minecraft.getInstance().getResourceManager().getResource(SoFEMod.id("splash/dialogues.json")).ifPresent(r -> {
                try (InputStream in = r.open()) {
                    JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                    json.entrySet().forEach(e -> { if (!e.getKey().startsWith("_")) dialogues.put(e.getKey(), e.getValue().getAsString()); });
                } catch (Exception e) {
                    SoFEMod.LOGGER.warn("Could not read splash/dialogues.json: {}", e.getMessage());
                }
            });
        }
        String path = dialogues.get(dialogueId);
        return path == null ? Optional.empty() : find(path);
    }

    /** Forgets what was found (a resource reload: packs may have added or removed illustrations). */
    public static void clear() {
        FOUND.clear();
        dialogues = null;
    }

    /** Draws the illustration filling the area, cropped to keep its middle (like CSS "cover"). */
    public static void cover(GuiGraphics g, Image image, int x, int y, int w, int h) {
        float scale = Math.max(w / (float) image.width(), h / (float) image.height());
        int srcW = Math.round(w / scale), srcH = Math.round(h / scale);
        int u = (image.width() - srcW) / 2, v = (image.height() - srcH) / 2;
        Minecraft.getInstance().getTextureManager().getTexture(image.texture()).setFilter(true, false); // a painting, scaled smoothly
        g.blit(image.texture(), x, y, w, h, u, v, srcW, srcH, image.width(), image.height());
    }
}
