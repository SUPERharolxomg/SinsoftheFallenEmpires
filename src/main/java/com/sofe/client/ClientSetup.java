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
        modBus.addListener(com.sofe.client.render.SoFEEntityRenderers::registerRenderers);
        modBus.addListener(com.sofe.client.render.SoFEEntityRenderers::addLayers);
        MinecraftForge.EVENT_BUS.addListener(CombatHudOverlay::onRenderOverlay);
        MinecraftForge.EVENT_BUS.addListener(CastPoses::onRenderPlayer);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.client.hud.BossHealthBar::onBossBar);
        MinecraftForge.EVENT_BUS.addListener(GearClient::onTooltip);
        MinecraftForge.EVENT_BUS.addListener(GearClient::onRenderLevel);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.client.screen.ThemedMenus::onInit);
        MinecraftForge.EVENT_BUS.addListener(SoFEMusic::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(QuestPath::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.client.screen.ThemedMenus::onBackground);
        MinecraftForge.EVENT_BUS.addListener(com.sofe.client.screen.ThemedMenus::onRender);
        if (ArmorShots.enabled() || SkillShots.enabled() || PlaceShots.enabled() || DeathKeysCheck.enabled() || ProtectCheck.enabled()
                || CompatCheck.enabled()) {
            MinecraftForge.EVENT_BUS.addListener(ShotsWorld::onClientTick);
        }
        if (DeathKeysCheck.enabled()) MinecraftForge.EVENT_BUS.addListener(DeathKeysCheck::onClientTick);
        if (ProtectCheck.enabled()) MinecraftForge.EVENT_BUS.addListener(ProtectCheck::onClientTick);
        if (CompatCheck.enabled()) MinecraftForge.EVENT_BUS.addListener(CompatCheck::onClientTick);
        if (TitleShot.enabled()) MinecraftForge.EVENT_BUS.addListener(TitleShot::onClientTick);
        if (TitleShot.joinEnabled()) MinecraftForge.EVENT_BUS.addListener(TitleShot::onJoinTick);
        if (PlaceShots.enabled()) MinecraftForge.EVENT_BUS.addListener(PlaceShots::onClientTick);
        if (ArmorShots.enabled()) MinecraftForge.EVENT_BUS.addListener(ArmorShots::onClientTick);
        if (SkillShots.enabled()) MinecraftForge.EVENT_BUS.addListener(SkillShots::onClientTick);
        modBus.addListener(SoFEKeys::register);
        modBus.addListener((net.minecraftforge.client.event.RegisterClientReloadListenersEvent e) ->
                e.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) rm -> com.sofe.client.screen.Splash.clear()));
        modBus.addListener(ClientSetup::registerItemProperties);
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
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "pact", com.sofe.client.hud.PactHudOverlay::render);
        event.registerAbove(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id(), "quest_compass", QuestCompassOverlay::render);
    }

    /** Leaving a world: forget the last world's Bearer and combat state. */
    private static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientClassData.set(Optional.empty());
        ClientCombatData.clear();
        ClientProgressData.clear();
        ClientStoryData.clear();
        ClientLockData.clear();
        com.sofe.client.render.CorruptedEyesLayer.clear();
        ClientBearers.clear();
        ClientEconomyData.clear();
        ClientPactData.clear();
    }

    /** The empire shields switch to their raised model while blocking, as the vanilla shield does. */
    private static void registerItemProperties(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> com.sofe.registry.ItemRegistry.shields().forEach(shield ->
                net.minecraft.client.renderer.item.ItemProperties.register(shield.get(), net.minecraft.resources.ResourceLocation.withDefaultNamespace("blocking"),
                        (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1f : 0f)));
        event.enqueueWork(ClientSetup::rangedProperties);
        event.enqueueWork(ClientSetup::curioRenderers);
    }

    /** Every ring, necklace and charm is seen on the Bearer who wears it (Curios). */
    private static void curioRenderers() {
        net.minecraftforge.registries.ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item instanceof com.sofe.gear.GearItems.Trinket)
                .forEach(item -> top.theillusivec4.curios.api.client.CuriosRendererRegistry.register(item, () -> com.sofe.client.render.JewelryRenderer.INSTANCE));
    }

    /** The bows and crossbows of batch 3 pull back as the vanilla ones do; each bow by its own draw time. */
    private static void rangedProperties() {
        var pull = net.minecraft.resources.ResourceLocation.withDefaultNamespace("pull");
        var pulling = net.minecraft.resources.ResourceLocation.withDefaultNamespace("pulling");
        for (var bow : com.sofe.registry.ItemRegistry.bows()) {
            int draw = ((com.sofe.gear.ranged.RangedItems.SoFEBow) bow.get()).drawTicks();
            net.minecraft.client.renderer.item.ItemProperties.register(bow.get(), pull, (stack, level, entity, seed) ->
                    entity == null || entity.getUseItem() != stack ? 0f : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / (float) draw);
            net.minecraft.client.renderer.item.ItemProperties.register(bow.get(), pulling, (stack, level, entity, seed) ->
                    entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1f : 0f);
        }
        for (var crossbow : com.sofe.registry.ItemRegistry.crossbows()) {
            net.minecraft.client.renderer.item.ItemProperties.register(crossbow.get(), pull, (stack, level, entity, seed) ->
                    entity == null || net.minecraft.world.item.CrossbowItem.isCharged(stack) ? 0f
                            : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / (float) net.minecraft.world.item.CrossbowItem.getChargeDuration(stack));
            net.minecraft.client.renderer.item.ItemProperties.register(crossbow.get(), pulling, (stack, level, entity, seed) ->
                    entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !net.minecraft.world.item.CrossbowItem.isCharged(stack) ? 1f : 0f);
            net.minecraft.client.renderer.item.ItemProperties.register(crossbow.get(), net.minecraft.resources.ResourceLocation.withDefaultNamespace("charged"),
                    (stack, level, entity, seed) -> net.minecraft.world.item.CrossbowItem.isCharged(stack) ? 1f : 0f);
            net.minecraft.client.renderer.item.ItemProperties.register(crossbow.get(), net.minecraft.resources.ResourceLocation.withDefaultNamespace("firework"),
                    (stack, level, entity, seed) -> net.minecraft.world.item.CrossbowItem.isCharged(stack)
                            && net.minecraft.world.item.CrossbowItem.containsChargedProjectile(stack, net.minecraft.world.item.Items.FIREWORK_ROCKET) ? 1f : 0f);
        }
    }
}
