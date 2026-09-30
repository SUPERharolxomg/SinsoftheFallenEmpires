package com.sofe.world.lock;

import com.sofe.config.SoFEConfig;
import com.sofe.story.StoryAct;
import com.sofe.world.SoFEWorld;
import com.sofe.world.region.RegionMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * Who a lock applies to, on both sides. The server answers from the world and the player's story;
 * the client answers from what the server synced ({@link #setClientView}), so the Veil's collision
 * is the same on both and movement does not stutter.
 */
public final class LockAccess {

    /** The client's copy of the layout, act and bypass flag; installed by the client setup. */
    public interface ClientView {
        Optional<RegionMap> map();

        int act();

        boolean bypass();
    }

    private static volatile ClientView clientView;

    private LockAccess() {
    }

    public static void setClientView(ClientView view) {
        clientView = view;
    }

    /** Creative and spectator players, and operators when the server allows it, are never locked. */
    public static boolean bypasses(Player player) {
        if (player.isCreative() || player.isSpectator()) return true;
        if (player instanceof ServerPlayer server) {
            return SoFEConfig.SERVER.opsBypass.get() && server.hasPermissions(2);
        }
        ClientView view = clientView;
        return view != null && view.bypass();
    }

    public static Optional<RegionMap> map(Player player) {
        if (player.level().isClientSide()) {
            ClientView view = clientView;
            return view == null ? Optional.empty() : view.map();
        }
        return player.getServer() == null ? Optional.empty() : SoFEWorld.regionMap(player.getServer());
    }

    public static int act(Player player) {
        if (player.level().isClientSide()) {
            ClientView view = clientView;
            return view == null ? 1 : view.act();
        }
        return StoryAct.of(player);
    }

    /** Whether the Seal Veil at this column lets the player through. */
    public static boolean canPassVeil(Player player, int x, int z) {
        if (bypasses(player)) return true;
        return map(player).map(m -> RegionLocks.veilPassable(m, x, z, act(player))).orElse(true);
    }
}
