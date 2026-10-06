package com.sofe.quest;

import com.sofe.network.ScenePackets;
import com.sofe.network.SoFENetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Scenes on one player's screen (a "play_scene" effect): "Crowned in Ash", the secret bad ending of Prython's offer
 * (docs/Jugabilidad.md). Only that player sees it. While it plays, and while they weigh an offer ({@link #HELD}), no
 * creature can hurt them: Prython waits for the answer. When the client says the scene ended, or after
 * {@link #MAX_TICKS} if it never does, the dialogue that follows opens.
 */
public final class SceneService {
    /** The longest a scene may hold the player safe. */
    public static final int MAX_TICKS = 20 * 60;
    /** Conversations during which the player is held out of the fight. */
    public static final java.util.Set<String> HELD = java.util.Set.of("sofe:act5/prythons_offer");
    private static final Map<UUID, Playing> PLAYING = new ConcurrentHashMap<>();

    private record Playing(String scene, String then, int ticksLeft) {
    }

    private SceneService() {
    }

    public static void play(ServerPlayer player, String scene, String then) {
        PLAYING.put(player.getUUID(), new Playing(scene, then, MAX_TICKS));
        SoFENetwork.sendTo(player, new ScenePackets.Play(scene));
    }

    /** The scene a player is watching, if any. */
    public static Optional<String> watching(ServerPlayer player) {
        return Optional.ofNullable(PLAYING.get(player.getUUID())).map(Playing::scene);
    }

    public static void done(ServerPlayer player) {
        Playing playing = PLAYING.remove(player.getUUID());
        if (playing != null && playing.then() != null) DialogueService.open(player, playing.then(), null);
    }

    /** Whether the player is held out of the fight: in a scene, or weighing an offer. */
    public static boolean shielded(ServerPlayer player) {
        return PLAYING.containsKey(player.getUUID())
                || DialogueService.current(player).map(at -> HELD.contains(at.substring(0, at.indexOf('#')))).orElse(false);
    }

    /** Blows from creatures and players do not land on a shielded player; falls, fire and the void still do. */
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getSource().getEntity() != null && shielded(player)) {
            event.setCanceled(true);
        }
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        Playing playing = PLAYING.get(player.getUUID());
        if (playing == null) return;
        if (playing.ticksLeft() <= 0) {
            done(player);
        } else {
            PLAYING.put(player.getUUID(), new Playing(playing.scene(), playing.then(), playing.ticksLeft() - 1));
        }
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PLAYING.remove(event.getEntity().getUUID());
    }
}
