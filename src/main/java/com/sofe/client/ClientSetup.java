package com.sofe.client;

import com.sofe.client.hud.CombatHudOverlay;
import com.sofe.client.hud.QuestCompassOverlay;
import com.sofe.client.hud.RegionTitleOverlay;
import com.sofe.client.screen.SoFEConfigScreen;
import com.sofe.registry.SoFEBlocks;
import com.sofe.world.lock.LockAccess;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
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

    private static final int SEALED_VEIL = 0x7A2A6A, OPEN_VEIL = 0x9FD8FF;

    private ClientSetup() {
    }

    public static void init(IEventBus modBus, ModLoadingContext context) {
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new SoFEConfigScreen(parent)));
        ClientLockData.install();
        modBus.addListener(ClientSetup::registerOverlays);
        modBus.addListener(ClientSetup::registerBlockColors);
        modBus.addListener(com.sofe.client.render.SoFEEntityRenderers::registerLayers);
        modBus.addListener(com.sofe.client.render.SoFEEntityRenderers::registerRenderers);
        modBus.addListener(com.sofe.client.render.SoFEEntityRenderers::addLayers);
        MinecraftForge.EVENT_BUS.addListener(CombatHudOverlay::onRenderOverlay);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.client.hud.BossHealthBar::onBossBar);
        MinecraftForge.EVENT_BUS.addListener(GearClient::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(GearClient::onRenderLevel);
        modBus.addListener(SoFEKeys::register);
        MinecraftForge.EVENT_BUS.addListener(TitleScreenHandler::onScreenOpening);
        MinecraftForge.EVENT_BUS.addListener(SoFEKeys::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(ClientSetup::onLoggingOut);
    }

    /** The Seal Veil is dark red-violet while sealed for the local player and a faint shimmer once open. */
    private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (pos == null || minecraft.player == null) return SEALED_VEIL;
            return LockAccess.canPassVeil(minecraft.player, pos.getX(), pos.getZ()) ? OPEN_VEIL : SEALED_VEIL;
        }, SoFEBlocks.SEAL_VEIL.get());
    }

    private static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.TITLE_TEXT.id(), "region_title", RegionTitleOverlay::render);
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "combat", CombatHudOverlay::render);
        event.registerAbove(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id(), "quest_compass", QuestCompassOverlay::render);
    }

    /** Leaving a world: forget the last world's Bearer and combat state. */
    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientClassData.set(Optional.empty());
        ClientCombatData.clear();
        ClientProgressData.clear();
        ClientStoryData.clear();
        ClientLockData.clear();
        ClientBearers.clear();
        ClientEconomyData.clear();
    }
}
