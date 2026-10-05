package com.sofe.death;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Death, seen from above: when a Bearer dies, "You have died" fills the screen (with how), they come back at once as
 * a spectator over the place where they fell, to watch how the fight goes on, and a countdown of ten seconds runs;
 * then they stand at their respawn point again in the game mode they had. The death screen is skipped (the
 * doImmediateRespawn rule); the corpse, the soulbound items and everything else of a death happen as before.
 */
public final class DeathSpectate {
    public static final int SECONDS = 10;

    /** Where and how a Bearer fell. */
    private record Death(ResourceKey<Level> dimension, Vec3 at, float yaw, GameType mode) {
    }

    /** A Bearer watching: where they will come back, in which mode, and how long is left. */
    private record Watching(ResourceKey<Level> backDimension, Vec3 back, float backYaw, GameType mode, int ticksLeft) {
        Watching tick() {
            return new Watching(backDimension, back, backYaw, mode, ticksLeft - 1);
        }
    }

    private static final Map<UUID, Death> DEATHS = new ConcurrentHashMap<>();
    private static final Map<UUID, Watching> WATCHING = new ConcurrentHashMap<>();

    private DeathSpectate() {
    }

    /** No death screen: the Bearer comes back at once, to watch. */
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        server.getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(true, server);
    }

    public static void onDeath(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || player.level().getLevelData().isHardcore()) return;
        if (player.isSpectator()) return;
        DEATHS.put(player.getUUID(), new Death(player.level().dimension(), player.position(), player.getYRot(), player.gameMode.getGameModeForPlayer()));
        Component how = event.getSource().getLocalizedDeathMessage(player);
        player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 50, 15));
        player.connection.send(new ClientboundSetSubtitleTextPacket(how.copy().withStyle(ChatFormatting.GRAY)));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("message.sofe.death.title").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)));
    }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered() || !(event.getEntity() instanceof ServerPlayer player)) return;
        Death death = DEATHS.remove(player.getUUID());
        if (death == null) return;
        WATCHING.put(player.getUUID(), new Watching(player.level().dimension(), player.position(), player.getYRot(), death.mode(), SECONDS * 20));
        ServerLevel where = player.server.getLevel(death.dimension());
        player.setGameMode(GameType.SPECTATOR);
        if (where != null) player.teleportTo(where, death.at().x, death.at().y + 3, death.at().z, death.yaw(), 35);
        countdown(player, SECONDS);
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || WATCHING.isEmpty()) return;
        MinecraftServer server = event.getServer();
        for (UUID id : java.util.List.copyOf(WATCHING.keySet())) {
            Watching w = WATCHING.get(id).tick();
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null) {
                WATCHING.remove(id);
                continue;
            }
            if (w.ticksLeft() <= 0) {
                finish(player, w);
                continue;
            }
            WATCHING.put(id, w);
            if (w.ticksLeft() % 20 == 0) countdown(player, w.ticksLeft() / 20);
        }
    }

    private static void countdown(ServerPlayer player, int seconds) {
        player.displayClientMessage(Component.translatable("message.sofe.death.countdown", seconds).withStyle(ChatFormatting.GOLD), true);
    }

    /** Back at the respawn point, in the mode the Bearer had. */
    private static void finish(ServerPlayer player, Watching w) {
        WATCHING.remove(player.getUUID());
        ServerLevel back = player.server.getLevel(w.backDimension());
        if (back != null) player.teleportTo(back, w.back().x, w.back().y, w.back().z, w.backYaw(), 0);
        player.setGameMode(w.mode() == GameType.SPECTATOR ? GameType.SURVIVAL : w.mode());
        player.displayClientMessage(Component.translatable("message.sofe.death.back").withStyle(ChatFormatting.GREEN), true);
    }

    /** Leaving while watching: they are put back at once, never saved as a spectator. */
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && WATCHING.containsKey(player.getUUID())) finish(player, WATCHING.get(player.getUUID()));
        DEATHS.remove(event.getEntity().getUUID());
    }

    public static boolean isWatching(ServerPlayer player) {
        return WATCHING.containsKey(player.getUUID());
    }
}
