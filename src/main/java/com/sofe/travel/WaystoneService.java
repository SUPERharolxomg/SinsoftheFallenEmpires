package com.sofe.travel;

import com.sofe.network.OpenWaystonesPacket;
import com.sofe.network.SoFENetwork;
import com.sofe.registry.SoFEBlocks;
import com.sofe.world.SoFEWorld;
import com.sofe.world.lock.LockAccess;
import com.sofe.world.zone.ProtectedZone;
import com.sofe.world.zone.ProtectedZoneData;
import com.sofe.world.zone.StructurePositions;
import com.sofe.world.zone.ZoneRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Activating Waystones, travelling between them, the Return Scroll and respawning at the last one. */
public final class WaystoneService {
    /** The Waystone each player opened the list from; travel is only allowed from there. */
    private static final Map<UUID, BlockPos> OPENED_AT = new ConcurrentHashMap<>();

    private WaystoneService() {
    }

    /** The lang key of a Waystone listed in structure_positions.json, or null for one placed by a player. */
    public static String nameKey(BlockPos pos) {
        return StructurePositions.get().waystones().entrySet().stream()
                .filter(e -> Math.abs(e.getValue().getX() - pos.getX()) <= 1 && Math.abs(e.getValue().getZ() - pos.getZ()) <= 1)
                .map(e -> "waystone." + e.getKey().replace(':', '.').replace('/', '.'))
                .findFirst().orElse(null);
    }

    public static void activate(ServerPlayer player, BlockPos pos) {
        if (player.level().dimension() != Level.OVERWORLD) return;
        TravelCapability.get(player).ifPresent(travel -> {
            if (travel.isActivated(pos)) return;
            travel.activate(pos, nameKey(pos));
            player.level().playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1f, 1.2f);
            player.displayClientMessage(Component.translatable("message.sofe.waystone.activated", name(pos, nameKey(pos)))
                    .withStyle(ChatFormatting.AQUA), true);
        });
    }

    public static Component name(BlockPos pos, String nameKey) {
        return nameKey != null ? Component.translatable(nameKey)
                : Component.translatable("waystone.sofe.unnamed", pos.getX(), pos.getZ());
    }

    public static void openList(ServerPlayer player, BlockPos from) {
        TravelCapability.get(player).ifPresent(travel -> {
            OPENED_AT.put(player.getUUID(), from.immutable());
            List<OpenWaystonesPacket.Entry> entries = travel.waystoneList().stream()
                    .map(w -> new OpenWaystonesPacket.Entry(w.pos(), w.nameKey() == null ? "" : w.nameKey(), w.pos().equals(from),
                            check(player, w.pos()) == TravelRules.Result.OK))
                    .toList();
            SoFENetwork.sendTo(player, new OpenWaystonesPacket(entries));
        });
    }

    public static TravelRules.Result check(ServerPlayer player, BlockPos target) {
        boolean activated = TravelCapability.get(player).map(t -> t.isActivated(target)).orElse(false);
        int sinceCombat = Math.min(player.tickCount - player.getLastHurtByMobTimestamp(), player.tickCount - player.getLastHurtMobTimestamp());
        if (player.getLastHurtByMobTimestamp() == 0 && player.getLastHurtMobTimestamp() == 0) sinceCombat = Integer.MAX_VALUE;
        boolean inArena = ZoneRules.zoneAt(ProtectedZoneData.get(player.server).zones(), player.getBlockX(), player.getBlockY(), player.getBlockZ())
                .map(z -> z.kind() == ProtectedZone.Kind.ARENA).orElse(false);
        return TravelRules.check(activated, sinceCombat, inArena, SoFEWorld.regionMap(player.server).orElse(null),
                target.getX(), target.getZ(), LockAccess.act(player), LockAccess.bypasses(player));
    }

    /** Travel requested from the Waystone screen. */
    public static void travel(ServerPlayer player, BlockPos target) {
        BlockPos from = OPENED_AT.get(player.getUUID());
        if (from == null || from.distSqr(player.blockPosition()) > 64 || !player.level().getBlockState(from).is(SoFEBlocks.WAYSTONE.get())) {
            return; // the screen must be opened at a Waystone, and the player must still be there
        }
        TravelRules.Result result = check(player, target);
        if (result != TravelRules.Result.OK) {
            player.displayClientMessage(Component.translatable("message.sofe.waystone." + result.name().toLowerCase(Locale.ROOT))
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        OPENED_AT.remove(player.getUUID());
        teleport(player, target.above());
    }

    public static void teleport(ServerPlayer player, BlockPos to) {
        ServerLevel overworld = player.server.overworld();
        overworld.getChunk(to.getX() >> 4, to.getZ() >> 4);
        player.teleportTo(overworld, to.getX() + 0.5, to.getY(), to.getZ() + 0.5, player.getYRot(), player.getXRot());
        overworld.playSound(null, to, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.1f);
    }

    /** The Return Scroll: back to Sulthari (the world spawn, in the city plaza). */
    public static void returnToSulthari(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        teleport(player, overworld.getSharedSpawnPos());
    }

    /** Respawning without a bed: at the last activated Waystone, or in Sulthari (docs/Jugabilidad.md, G1). */
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered() || !(event.getEntity() instanceof ServerPlayer player) || !SoFEWorld.isJourney(player.server)) return;
        if (player.getRespawnPosition() != null) return; // a bed or anchor wins
        TravelCapability.get(player).flatMap(TravelData::lastActivated)
                .filter(w -> player.server.overworld().getBlockState(w.pos()).is(SoFEBlocks.WAYSTONE.get()) || !player.server.overworld().isLoaded(w.pos()))
                .ifPresent(w -> teleport(player, w.pos().above()));
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        OPENED_AT.remove(event.getEntity().getUUID());
    }
}
