package com.sofe.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.sofe.SoFEMod;
import com.sofe.client.TitleScreenHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.client.gui.ModListScreen;
import net.minecraftforge.fml.ModList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/**
 * The SoFE title screen and main menu (docs/Anexos.md, A1).
 *
 * Uses the art from docs/Arte.md as soon as the files exist: textures/gui/title/keyart.png
 * (1920x1080) as the background and textures/gui/title/logo.png (any size; its real size is
 * read so it is never stretched). Until then: the vanilla panorama and a text logo.
 */
public class SoFETitleScreen extends Screen {
    private static final PanoramaRenderer PANORAMA = new PanoramaRenderer(TitleScreen.CUBE_MAP);
    private static final ResourceLocation KEY_ART = SoFEMod.id("textures/gui/title/keyart.png");
    private static final ResourceLocation LOGO = SoFEMod.id("textures/gui/title/logo.png");
    private static final int KEY_ART_W = 1920, KEY_ART_H = 1080;
    private static final int LOGO_MAX_W = 300;
    private static final int GOLD = 0xE8B64A;
    private static final int PARCHMENT = 0xE6DCC8;

    /** Kept between openings so the menu does not jump when coming back to it. */
    private static String latestWorld;
    private SplashRenderer splash;
    private boolean hasKeyArt;
    /** Pixel size of logo.png, or null when there is no logo file. */
    private int[] logoSize;
    private int buttonsTop;

    public SoFETitleScreen() {
        super(Component.translatable("menu.sofe.title"));
    }

    @Override
    protected void init() {
        if (splash == null) {
            splash = this.minecraft.getSplashManager().getSplash();
        }
        hasKeyArt = this.minecraft.getResourceManager().getResource(KEY_ART).isPresent();
        logoSize = imageSize(LOGO).orElse(null);

        int w = 200;
        int x = this.width / 2 - w / 2;
        int y = buttonsTop = this.height / 4 + 48;

        addRenderableWidget(Button.builder(Component.translatable("menu.sofe.begin_journey"), b -> {
            TitleScreenHandler.requestJourney();
            CreateWorldScreen.openFresh(this.minecraft, this);
        }).bounds(x, y, w, 20).build());
        y += 24;

        if (latestWorld != null) {
            String world = latestWorld;
            addRenderableWidget(Button.builder(Component.translatable("menu.sofe.continue"),
                    b -> this.minecraft.createWorldOpenFlows().loadLevel(this, world)).bounds(x, y, w, 20).build());
            y += 24;
        }

        addRenderableWidget(Button.builder(Component.translatable("menu.sofe.join_expedition"),
                b -> this.minecraft.setScreen(new JoinMultiplayerScreen(this))).bounds(x, y, w, 20).build());
        y += 36;

        addRenderableWidget(Button.builder(Component.translatable("menu.options"),
                b -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))).bounds(x, y, 98, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("fml.menu.mods"),
                b -> this.minecraft.setScreen(new ModListScreen(this))).bounds(x + 102, y, 98, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.translatable("menu.quit"),
                b -> this.minecraft.stop()).bounds(x, y, w, 20).build());

        findLatestWorld();
    }

    private Optional<int[]> imageSize(ResourceLocation location) {
        Optional<Resource> resource = this.minecraft.getResourceManager().getResource(location);
        if (resource.isEmpty()) return Optional.empty();
        try (InputStream in = resource.get().open(); NativeImage image = NativeImage.read(in)) {
            return Optional.of(new int[]{image.getWidth(), image.getHeight()});
        } catch (IOException e) {
            SoFEMod.LOGGER.warn("Could not read {}", location, e);
            return Optional.empty();
        }
    }

    /** Continue points at the world played last; the menu is rebuilt if the answer changes. */
    private void findLatestWorld() {
        try {
            var source = this.minecraft.getLevelSource();
            source.loadLevelSummaries(source.findLevelCandidates()).thenAcceptAsync(worlds -> {
                String found = worlds.stream()
                        .filter(s -> !s.isLocked() && !s.isDisabled() && !s.requiresManualConversion())
                        .max(Comparator.comparingLong(LevelSummary::getLastPlayed))
                        .map(LevelSummary::getLevelId)
                        .orElse(null);
                if (!Objects.equals(found, latestWorld)) {
                    latestWorld = found;
                    if (this.minecraft.screen == this) {
                        rebuildWidgets();
                    }
                }
            }, this.minecraft);
        } catch (LevelStorageException e) {
            SoFEMod.LOGGER.warn("Could not list worlds for the Continue button", e);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackdrop(graphics, partialTick);

        // The logo and the subtitle share the space above the buttons
        int subtitleY = buttonsTop - 16;
        int[] logo = renderLogo(graphics, 6, subtitleY - 4);
        graphics.drawCenteredString(this.font, Component.translatable("menu.sofe.subtitle"), this.width / 2, subtitleY, PARCHMENT);

        if (splash != null) {
            // The vanilla splash draws at (width / 2 + 123, 69); move it onto the logo's lower right corner
            graphics.pose().pushPose();
            graphics.pose().translate(logo[0] - 8 - (this.width / 2f + 123), logo[1] - 10 - 69, 0);
            splash.render(graphics, this.width, this.font, 0xFF000000);
            graphics.pose().popPose();
        }

        String version = ModList.get().getModContainerById(SoFEMod.MOD_ID)
                .map(c -> c.getModInfo().getVersion().toString()).orElse("?");
        graphics.drawString(this.font, Component.translatable("menu.sofe.version", version, SharedConstants.getCurrentVersion().getName()),
                2, this.height - 10, 0x9A9080);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /** Key art scaled to cover the screen (cropping the sides or top), or the vanilla panorama. */
    private void renderBackdrop(GuiGraphics graphics, float partialTick) {
        if (hasKeyArt) {
            float scale = Math.max(this.width / (float) KEY_ART_W, this.height / (float) KEY_ART_H);
            int w = Mth.ceil(KEY_ART_W * scale), h = Mth.ceil(KEY_ART_H * scale);
            graphics.blit(KEY_ART, (this.width - w) / 2, (this.height - h) / 2, w, h, 0f, 0f, KEY_ART_W, KEY_ART_H, KEY_ART_W, KEY_ART_H);
            graphics.fillGradient(0, 0, this.width, this.height, 0x22000000, 0x99120A06);
        } else {
            PANORAMA.render(partialTick, 1f);
            graphics.fillGradient(0, 0, this.width, this.height, 0x66000000, 0xCC120A06);
        }
    }

    /**
     * Draws the logo centered between top and bottom, as large as fits (never wider than
     * LOGO_MAX_W) without changing its aspect ratio. Returns its right and bottom edges.
     */
    private int[] renderLogo(GuiGraphics graphics, int top, int bottom) {
        int maxW = Math.min(LOGO_MAX_W, this.width - 20);
        int maxH = Math.max(20, bottom - top);
        if (logoSize != null) {
            float scale = Math.min(maxW / (float) logoSize[0], maxH / (float) logoSize[1]);
            int w = Math.round(logoSize[0] * scale), h = Math.round(logoSize[1] * scale);
            int x = (this.width - w) / 2, y = top + (maxH - h) / 2;
            graphics.blit(LOGO, x, y, w, h, 0f, 0f, logoSize[0], logoSize[1], logoSize[0], logoSize[1]);
            return new int[]{x + w, y + h};
        }
        // Text logo, scaled so it always fits whatever the GUI scale
        Component title = this.title.copy().withStyle(ChatFormatting.BOLD);
        float scale = Math.min(3f, maxW / (float) this.font.width(title));
        int y = top + (maxH - Mth.ceil(9 * scale)) / 2;
        graphics.pose().pushPose();
        graphics.pose().translate(this.width / 2f, y, 0);
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawCenteredString(this.font, title, 0, 0, GOLD);
        graphics.pose().popPose();
        return new int[]{this.width / 2 + Math.round(this.font.width(title) * scale / 2), y + Mth.ceil(9 * scale)};
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
