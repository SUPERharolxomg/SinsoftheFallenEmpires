package com.sofe.client.screen;

import com.sofe.client.ClientStoryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.event.ScreenEvent;

import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * The vanilla screens in the mod's look (docs/Anexos.md, "Other screens with the mod's look"): the pause menu gets the
 * Journal and the Codex, and the loading screens show one of the story's illustrations with a lore tip
 * ({@code tip.sofe.<n>}, a new one each time a loading screen opens).
 */
public final class ThemedMenus {
    public static final int TIPS = 16;
    /** The illustrations a loading screen may show, the first that exist (docs/ArteFinal.md). */
    private static final List<String> LOADING_ART = List.of("intro/night_of_the_eclipse", "empire/sulthari", "empire/nordrath",
            "empire/parsivan", "empire/khemet", "empire/aureum", "codex/cover");
    private static final WeakHashMap<Screen, Integer> TIP_OF = new WeakHashMap<>();
    private static final WeakHashMap<Screen, Optional<Splash.Image>> ART_OF = new WeakHashMap<>();

    private ThemedMenus() {
    }

    public static boolean isLoading(Screen screen) {
        return screen instanceof LevelLoadingScreen || screen instanceof ReceivingLevelScreen || screen instanceof ConnectScreen
                || screen instanceof GenericDirtMessageScreen;
    }

    /** The pause menu: the Journal (in a journey) and the Codex, under the vanilla buttons. */
    public static void onInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof PauseScreen pause)) return;
        Minecraft mc = Minecraft.getInstance();
        int w = 98, y = event.getScreen().height - 30, cx = event.getScreen().width / 2;
        boolean journey = ClientStoryData.get().isPresent();
        if (journey) {
            event.addListener(Button.builder(Component.translatable("gui.sofe.journal"), b -> mc.setScreen(new JournalScreen()))
                    .bounds(cx - w - 2, y, w, 20).build());
        }
        event.addListener(Button.builder(Component.translatable("menu.sofe.codex"), b -> mc.setScreen(new CodexScreen(pause)))
                .bounds(journey ? cx + 2 : cx - w / 2, y, w, 20).build());
    }

    /** A loading screen: an illustration behind its progress, dimmed. */
    public static void onBackground(ScreenEvent.BackgroundRendered event) {
        Screen screen = event.getScreen();
        if (!isLoading(screen)) return;
        ART_OF.computeIfAbsent(screen, s -> {
            List<Splash.Image> found = LOADING_ART.stream().map(Splash::find).flatMap(Optional::stream).toList();
            return found.isEmpty() ? Optional.empty() : Optional.of(found.get((int) (Math.random() * found.size())));
        }).ifPresent(art -> {
            Splash.cover(event.getGuiGraphics(), art, 0, 0, screen.width, screen.height);
            event.getGuiGraphics().fill(0, 0, screen.width, screen.height, 0x90000000);
        });
    }

    /** A loading screen: a lore tip at the bottom. */
    public static void onRender(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (!isLoading(screen)) return;
        int tip = TIP_OF.computeIfAbsent(screen, s -> 1 + (int) (Math.random() * TIPS));
        var font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = font.split(Component.translatable("tip.sofe." + tip), Math.min(screen.width - 40, 420));
        int y = screen.height - 18 - lines.size() * 10;
        event.getGuiGraphics().drawCenteredString(font, Component.translatable("tip.sofe.title"), screen.width / 2, y - 12, 0xC9A04A);
        for (int i = 0; i < lines.size(); i++) {
            event.getGuiGraphics().drawString(font, lines.get(i), screen.width / 2 - font.width(lines.get(i)) / 2, y + i * 10, 0xE8DCC0, true);
        }
    }
}
