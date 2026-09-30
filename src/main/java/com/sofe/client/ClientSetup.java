package com.sofe.client;

import com.sofe.client.hud.CombatHudOverlay;
import com.sofe.client.hud.RegionTitleOverlay;
import com.sofe.client.screen.SoFEConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

import java.util.Optional;

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
        modBus.addListener(SoFEKeys::register);
        MinecraftForge.EVENT_BUS.addListener(TitleScreenHandler::onScreenOpening);
        MinecraftForge.EVENT_BUS.addListener(SoFEKeys::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ClientSetup::onLoggingOut);
    }

    private static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.TITLE_TEXT.id(), "region_title", RegionTitleOverlay::render);
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "combat", CombatHudOverlay::render);
    }

    /** Leaving a world: forget the last world's Bearer and combat state. */
    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientClassData.set(Optional.empty());
        ClientCombatData.clear();
    }
}
