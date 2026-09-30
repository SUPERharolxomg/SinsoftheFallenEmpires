package com.sofe.client;

import com.sofe.client.hud.RegionTitleOverlay;
import com.sofe.client.screen.SoFEConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Client-only wiring. Only referenced behind a Dist.CLIENT check, so a dedicated
 * server never loads these classes.
 */
public final class ClientSetup {

    private ClientSetup() {
    }

    public static void init(IEventBus modBus, ModLoadingContext context) {
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new SoFEConfigScreen(parent)));
        modBus.addListener(ClientSetup::registerOverlays);
    }

    private static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.TITLE_TEXT.id(), "region_title", RegionTitleOverlay::render);
    }
}
