package com.sofe.player;

import com.sofe.SoFEMod;
import com.sofe.combat.CombatHandler;
import com.sofe.config.SoFEConfig;
import com.sofe.network.OpenClassSelectPacket;
import com.sofe.network.SoFENetwork;
import com.sofe.network.SyncClassPacket;
import com.sofe.progression.ProgressionHandler;
import com.sofe.quest.DialogueService;
import com.sofe.quest.QuestEngine;
import com.sofe.quest.StoryDataManager;
import com.sofe.world.SoFEWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Server side of choosing a Bearer (UC-01). The client only asks; the server checks that the
 * world is a journey and that the player has not chosen yet, then saves and syncs the class.
 */
public final class ClassSelectionHandler {
    /** The Night of the Eclipse intro shown before choosing a Bearer (UC-01). */
    public static final String FESTIVAL = "sofe:act1/festival";

    private ClassSelectionHandler() {
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
            boolean needsClass = PlayerClassCapability.get(player).map(d -> !d.hasClass()).orElse(false);
            if (needsClass && SoFEWorld.isJourney(player.server) && SoFEConfig.SERVER.openClassSelectOnJoin.get()) {
                // the Eclipse Festival intro plays first; closing or finishing it opens the selection
                if (StoryDataManager.dialogue(FESTIVAL).isPresent()) {
                    DialogueService.open(player, FESTIVAL, null);
                } else {
                    SoFENetwork.sendTo(player, new OpenClassSelectPacket());
                }
            }
        }
    }

    /** Respawning or changing dimension creates a new client player: resend the class. */
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
        }
    }

    public static void choose(ServerPlayer player, PlayerClass chosen) {
        if (!SoFEWorld.isJourney(player.server)) {
            SoFEMod.LOGGER.warn("{} tried to choose a Bearer outside a journey", player.getGameProfile().getName());
            return;
        }
        PlayerClassCapability.get(player).ifPresent(data -> {
            if (data.choose(chosen) == PlayerClassData.ChoiceResult.CHOSEN) {
                SoFEMod.LOGGER.info("{} became the {}", player.getGameProfile().getName(), chosen.id());
                player.sendSystemMessage(Component.translatable("gui.sofe.class_select.chosen",
                        Component.translatable(chosen.heroKey()), Component.translatable(chosen.translationKey()))
                        .withStyle(ChatFormatting.GOLD));
            }
            // Always sync: if the choice was refused, the client learns the real class and closes the screen
            sync(player);
            CombatHandler.refresh(player);
            ProgressionHandler.onBearerChosen(player);
            com.sofe.story.SoFEAdvancements.award(player, "story/root");
            giveFlask(player);
            QuestEngine.onBearerChosen(player);
        });
    }

    /** Every Bearer receives the Bearer's Flask in Act I (docs/Pociones.md), bound to them. */
    private static void giveFlask(ServerPlayer player) {
        var flask = com.sofe.registry.ItemRegistry.BEARERS_FLASK.get();
        if (player.getInventory().contains(new net.minecraft.world.item.ItemStack(flask))) return;
        var stack = new net.minecraft.world.item.ItemStack(flask);
        com.sofe.gear.GearNbt.bind(stack, player);
        if (!player.getInventory().add(stack)) player.drop(stack, false);
        com.sofe.economy.EconomyHandler.sync(player);
    }

    /** Opens the Bearer selection when the player still has none (in a journey). */
    public static void openIfNeeded(ServerPlayer player) {
        boolean needsClass = PlayerClassCapability.get(player).map(d -> !d.hasClass()).orElse(false);
        if (needsClass && SoFEWorld.isJourney(player.server)) SoFENetwork.sendTo(player, new OpenClassSelectPacket());
    }

    public static void sync(ServerPlayer player) {
        PlayerClassCapability.get(player).ifPresent(data -> {
            SoFENetwork.sendTo(player, new SyncClassPacket(data.get()));
            SoFENetwork.sendToTracking(player, new com.sofe.network.BearerOfPacket(player.getId(), data.get()));
        });
    }

    /** A client starts seeing another player: tell it their Bearer, for the outfit layer. */
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer viewer) {
            PlayerClassCapability.get(target).ifPresent(data ->
                    SoFENetwork.sendTo(viewer, new com.sofe.network.BearerOfPacket(target.getId(), data.get())));
        }
    }
}
