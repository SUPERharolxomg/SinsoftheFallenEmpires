package com.sofe.world.zone;

import com.sofe.SoFEMod;
import com.sofe.config.SoFEConfig;
import com.sofe.world.lock.LockAccess;
import com.sofe.world.region.AetherisBiomeSource;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * Keeps the protected places of the story as they were built (docs/Mundo.md, W2 layer 3): players,
 * explosions, pistons, fluids and mobs cannot change them, except on the Bearer's Homestead.
 * Also puts the world spawn in the plaza of Sulthari when a journey is created.
 */
public final class ZoneProtectionHandler {

    private ZoneProtectionHandler() {
    }

    private static List<ProtectedZone> zones(LevelAccessor level) {
        if (!(level instanceof ServerLevel server) || server.dimension() != Level.OVERWORLD || !SoFEConfig.SERVER.protectZones.get()) {
            return List.of();
        }
        return ProtectedZoneData.get(server.getServer()).zones();
    }

    private static boolean allowed(LevelAccessor level, BlockPos pos, ZoneAction action, Entity entity) {
        List<ProtectedZone> zones = zones(level);
        if (zones.isEmpty()) return true;
        boolean bypass = entity instanceof Player player && LockAccess.bypasses(player);
        return ZoneRules.allowed(zones, pos.getX(), pos.getY(), pos.getZ(), action, bypass);
    }

    private static void tell(Entity entity) {
        if (entity instanceof Player player) {
            player.displayClientMessage(Component.translatable("message.sofe.protected_zone").withStyle(ChatFormatting.GOLD), true);
        }
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!allowed(event.getLevel(), event.getPos(), ZoneAction.BREAK, event.getPlayer())) {
            event.setCanceled(true);
            tell(event.getPlayer());
        }
    }

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        ZoneAction action = event.getEntity() instanceof Player ? ZoneAction.PLACE : ZoneAction.MOB_GRIEFING;
        if (!allowed(event.getLevel(), event.getPos(), action, event.getEntity())) {
            event.setCanceled(true);
            tell(event.getEntity());
        }
    }

    /** Buckets, flint and steel, hoes and the like; opening doors, shops and chests still works. */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().isEmpty()) return;
        BlockPos target = event.getHitVec().getBlockPos();
        if (!allowed(event.getLevel(), target, ZoneAction.USE_ITEM_ON_BLOCK, event.getEntity())
                || !allowed(event.getLevel(), target.relative(event.getFace() == null ? net.minecraft.core.Direction.UP : event.getFace()),
                ZoneAction.USE_ITEM_ON_BLOCK, event.getEntity())) {
            event.setUseItem(Event.Result.DENY);
        }
    }

    /** Explosions still happen, but the protected blocks are taken out of them. */
    public static void onExplosion(ExplosionEvent.Detonate event) {
        List<ProtectedZone> zones = zones(event.getLevel());
        if (zones.isEmpty()) return;
        event.getAffectedBlocks().removeIf(pos -> !ZoneRules.allowed(zones, pos.getX(), pos.getY(), pos.getZ(), ZoneAction.EXPLOSION, false));
    }

    /** A piston outside a zone cannot push or pull blocks in or out of it. */
    public static void onPiston(PistonEvent.Pre event) {
        List<ProtectedZone> zones = zones(event.getLevel());
        if (zones.isEmpty() || !(event.getLevel() instanceof Level level)) return;
        BlockPos piston = event.getPos();
        if (!ZoneRules.allowed(zones, piston.getX(), piston.getY(), piston.getZ(), ZoneAction.PISTON, false)) return; // a piston built into the zone works
        PistonStructureResolver resolver = event.getStructureHelper();
        if (resolver == null || !resolver.resolve()) return;
        for (BlockPos pos : resolver.getToPush()) {
            if (touchesZone(zones, pos, event.getDirection())) {
                event.setCanceled(true);
                return;
            }
        }
        for (BlockPos pos : resolver.getToDestroy()) {
            if (!ZoneRules.allowed(zones, pos.getX(), pos.getY(), pos.getZ(), ZoneAction.PISTON, false)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    private static boolean touchesZone(List<ProtectedZone> zones, BlockPos pos, net.minecraft.core.Direction direction) {
        BlockPos to = pos.relative(direction);
        return !ZoneRules.allowed(zones, pos.getX(), pos.getY(), pos.getZ(), ZoneAction.PISTON, false)
                || !ZoneRules.allowed(zones, to.getX(), to.getY(), to.getZ(), ZoneAction.PISTON, false);
    }

    public static void onFluid(BlockEvent.FluidPlaceBlockEvent event) {
        if (!allowed(event.getLevel(), event.getPos(), ZoneAction.FLUID, null)) {
            event.setNewState(event.getOriginalState());
        }
    }

    public static void onTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (!allowed(event.getLevel(), event.getPos(), ZoneAction.BREAK, event.getEntity())) event.setCanceled(true);
    }

    /** Endermen, Void creatures and creepers leave the city alone. */
    public static void onMobGriefing(EntityMobGriefingEvent event) {
        Entity entity = event.getEntity();
        if (entity == null || entity instanceof Player) return;
        if (!allowed(entity.level(), entity.blockPosition(), ZoneAction.MOB_GRIEFING, entity)) {
            event.setResult(Event.Result.DENY);
        }
    }

    /** The zones come from structure_positions.json; each is added once and never moved. */
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        if (!(server.overworld().getChunkSource().getGenerator().getBiomeSource() instanceof AetherisBiomeSource)) return;
        int added = ProtectedZoneData.get(server).addMissing(StructurePositions.get().zones());
        if (added > 0) SoFEMod.LOGGER.info("Registered {} new protected zones for this journey", added);
    }

    /** New players appear in the plaza of Sulthari, not somewhere in the region. */
    public static void onCreateSpawn(LevelEvent.CreateSpawnPosition event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) return;
        if (!(level.getChunkSource().getGenerator().getBiomeSource() instanceof AetherisBiomeSource)) return;
        StructurePositions.Layout layout = StructurePositions.get();
        int x = layout.spawnX(), z = layout.spawnZ();
        level.getChunk(x >> 4, z >> 4); // generate the column before asking its height
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        level.setDefaultSpawnPos(new BlockPos(x, y, z), 0.0f);
        event.setCanceled(true);
        SoFEMod.LOGGER.info("Journey spawn set in Sulthari at {} {} {}", x, y, z);
    }
}
