package com.sofe.player;

import com.sofe.SoFEMod;
import com.sofe.config.SoFEConfig;
import com.sofe.network.OpenClassSelectPacket;
import com.sofe.network.SoFENetwork;
import com.sofe.network.SyncClassPacket;
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

    private ClassSelectionHandler() {
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sync(player);
            boolean needsClass = PlayerClassCapability.get(player).map(d -> !d.hasClass()).orElse(false);
            if (needsClass && SoFEWorld.isJourney(player.server) && SoFEConfig.SERVER.openClassSelectOnJoin.get()) {
                SoFENetwork.sendTo(player, new OpenClassSelectPacket());
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
        });
    }

    public static void sync(ServerPlayer player) {
        PlayerClassCapability.get(player).ifPresent(data -> SoFENetwork.sendTo(player, new SyncClassPacket(data.get())));
    }
}
