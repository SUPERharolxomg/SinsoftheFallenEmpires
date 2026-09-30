package com.sofe.client;

import com.sofe.client.screen.SoFETitleScreen;
import com.sofe.config.SoFEConfig;
import com.sofe.world.gen.SoFEWorldPresets;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraftforge.client.event.ScreenEvent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Swaps the vanilla title screen for the SoFE one (config replaceTitleScreen), and selects the
 * sofe:aetheris world type when the create-world screen opens: always after "Begin the
 * Journey", and for any new world when preselectJourneyPreset is on.
 */
public final class TitleScreenHandler {
    /** Create-world screens already handled, so returning from a sub-screen keeps the player's own choice. */
    private static final Set<CreateWorldScreen> HANDLED = Collections.newSetFromMap(new WeakHashMap<>());
    private static boolean journeyRequested;

    private TitleScreenHandler() {
    }

    public static void requestJourney() {
        journeyRequested = true;
    }

    public static void onScreenOpening(ScreenEvent.Opening event) {
        Screen screen = event.getNewScreen();
        if (screen != null && screen.getClass() == TitleScreen.class && SoFEConfig.CLIENT.replaceTitleScreen.get()) {
            event.setNewScreen(new SoFETitleScreen());
            return;
        }
        if (screen instanceof CreateWorldScreen createWorld && HANDLED.add(createWorld)) {
            if (journeyRequested || SoFEConfig.CLIENT.preselectJourneyPreset.get()) {
                selectJourneyPreset(createWorld.getUiState());
            }
            journeyRequested = false;
        }
    }

    private static void selectJourneyPreset(WorldCreationUiState uiState) {
        uiState.getNormalPresetList().stream()
                .filter(entry -> entry.preset() != null && entry.preset().is(SoFEWorldPresets.AETHERIS))
                .findFirst()
                .ifPresent(uiState::setWorldType);
    }
}
