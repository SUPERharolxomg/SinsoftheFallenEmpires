package com.sofe.world.lock;

import com.sofe.registry.SoFEBlocks;
import com.sofe.world.SoFEWorld;
import com.sofe.world.region.Region;
import com.sofe.world.region.RegionMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Layer 2 of the locks (docs/Mundo.md, W2): the guarantee behind the Seal Veil. Once per second
 * the server checks where each player stands; a player inside a sealed region (by elytra, a boat
 * around the coast or any other way) goes back to their last safe place. No damage, no item loss.
 */
public final class RegionEnforcer {
    private static final int CHECK_EVERY_TICKS = 20;
    private static final int VEIL_MESSAGE_COOLDOWN = 60;
    /** Nether positions map to the overworld at 8 times their coordinates, as in vanilla. */
    private static final int NETHER_SCALE = 8;

    private record SafePlace(ResourceKey<Level> dimension, Vec3 position) {
    }

    private static final Map<UUID, SafePlace> SAFE = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> LAST_VEIL_MESSAGE = new ConcurrentHashMap<>();

    private RegionEnforcer() {
    }

    /** Overworld column a position counts as, or empty in dimensions without regions (the End). */
    static Optional<int[]> column(Level level, double x, double z) {
        if (level.dimension() == Level.OVERWORLD) return Optional.of(new int[]{(int) Math.floor(x), (int) Math.floor(z)});
        if (level.dimension() == Level.NETHER) return Optional.of(new int[]{(int) Math.floor(x * NETHER_SCALE), (int) Math.floor(z * NETHER_SCALE)});
        return Optional.empty();
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 5 == 0) warnAtVeil(player);
        if (player.tickCount % CHECK_EVERY_TICKS != 0) return;
        Optional<RegionMap> map = SoFEWorld.regionMap(player.server);
        Optional<int[]> column = column(player.level(), player.getX(), player.getZ());
        if (map.isEmpty() || column.isEmpty()) {
            if (map.isPresent() && player.onGround() && !player.isInLava()) {
                SAFE.put(player.getUUID(), new SafePlace(player.level().dimension(), player.position()));
            }
            return;
        }

        int act = LockAccess.act(player);
        int x = column.get()[0], z = column.get()[1];
        if (LockAccess.bypasses(player) || RegionLocks.canBeAt(map.get(), x, z, act)) {
            if ((player.onGround() || player.isInWater() || player.isPassenger()) && !player.isInLava()) {
                SAFE.put(player.getUUID(), new SafePlace(player.level().dimension(), player.position()));
            }
            return;
        }
        rebuke(player, map.get().regionAt(x, z));
    }

    /** Sends the player back and tells them why (the Void Rebuke). */
    private static void rebuke(ServerPlayer player, Region region) {
        SafePlace safe = SAFE.get(player.getUUID());
        com.sofe.SoFEMod.LOGGER.info("The seal of {} sent {} back from {}, {}", region.id(), player.getGameProfile().getName(),
                player.getBlockX(), player.getBlockZ());
        if (player.isPassenger()) player.stopRiding();
        player.stopFallFlying();
        if (safe != null && safe.dimension() == player.level().dimension()) {
            player.teleportTo(safe.position().x, safe.position().y, safe.position().z);
        } else {
            ServerLevel overworld = player.server.overworld();
            BlockPos spawn = overworld.getSharedSpawnPos();
            player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
        }
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2, false, true));
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1f, 0.5f);
        player.displayClientMessage(sealedMessage(region).withStyle(ChatFormatting.DARK_PURPLE), true);
    }

    /** "The seal of Nordrath holds. (Unlocks in Act II)", or the open-sea warning. */
    public static net.minecraft.network.chat.MutableComponent sealedMessage(Region region) {
        if (region == Region.OCEAN) return Component.translatable("message.sofe.sea_sealed");
        return Component.translatable("message.sofe.veil_holds", Component.translatable(region.translationKey()),
                Component.translatable("act.sofe.short." + region.opensAtAct()));
    }

    /** Cancels ender pearls and chorus fruit that would land in a sealed place. */
    public static void onTeleport(EntityTeleportEvent event) {
        if (!(event instanceof EntityTeleportEvent.EnderPearl) && !(event instanceof EntityTeleportEvent.ChorusFruit)) return;
        LivingEntity entity = event instanceof EntityTeleportEvent.EnderPearl pearl ? pearl.getPlayer() : ((EntityTeleportEvent.ChorusFruit) event).getEntityLiving();
        if (!(entity instanceof ServerPlayer player) || LockAccess.bypasses(player)) return;
        Optional<RegionMap> map = SoFEWorld.regionMap(player.server);
        Optional<int[]> column = column(player.level(), event.getTargetX(), event.getTargetZ());
        if (map.isEmpty() || column.isEmpty()) return;
        int x = column.get()[0], z = column.get()[1];
        if (!RegionLocks.canBeAt(map.get(), x, z, LockAccess.act(player))) {
            event.setCanceled(true);
            player.displayClientMessage(sealedMessage(map.get().regionAt(x, z)).withStyle(ChatFormatting.DARK_PURPLE), true);
        }
    }

    /** Walking into a sealed Veil says what it is waiting for. */
    private static void warnAtVeil(ServerPlayer player) {
        if (!player.horizontalCollision || LockAccess.bypasses(player)) return;
        int now = player.tickCount;
        Integer last = LAST_VEIL_MESSAGE.get(player.getUUID());
        if (last != null && now - last < VEIL_MESSAGE_COOLDOWN && now >= last) return;
        Optional<RegionMap> map = SoFEWorld.regionMap(player.server);
        if (map.isEmpty()) return;
        BlockPos feet = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(feet.offset(-1, 0, -1), feet.offset(1, 1, 1))) {
            if (!player.level().getBlockState(pos).is(SoFEBlocks.SEAL_VEIL.get())) continue;
            Optional<Region> sealed = RegionLocks.sealedBehind(map.get(), pos.getX(), pos.getZ(), LockAccess.act(player), LockAccess.scale(player.level()));
            if (sealed.isPresent()) {
                player.displayClientMessage(sealedMessage(sealed.get()).withStyle(ChatFormatting.LIGHT_PURPLE), true);
                LAST_VEIL_MESSAGE.put(player.getUUID(), now);
                return;
            }
        }
    }

    /** The last place this player stood safely in their current dimension (for bodies lost in lava or the void). */
    public static Optional<Vec3> lastSafe(ServerPlayer player) {
        SafePlace safe = SAFE.get(player.getUUID());
        return safe != null && safe.dimension() == player.level().dimension() ? Optional.of(safe.position()) : Optional.empty();
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SAFE.remove(event.getEntity().getUUID());
        LAST_VEIL_MESSAGE.remove(event.getEntity().getUUID());
    }
}
