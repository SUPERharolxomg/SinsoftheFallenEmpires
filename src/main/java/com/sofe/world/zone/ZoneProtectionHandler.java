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
 * explosions, pistons, fluids and mobs cannot change them, except on the Bearer's Homestead. Nothing
 * can be taken from them either: their chests, drawers, pots, lecterns, armor stands, item frames and
 * paintings are part of the place. No one is let off: not an operator, not a player in creative.
 * Only SoFE's own blocks (the stations, the Personal Vault, the Reward Coffers, the Waystones) and
 * doors, gates, buttons, beds and seats can be used.
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
        return ZoneRules.allowed(zones, pos.getX(), pos.getY(), pos.getZ(), action);
    }

    private static void tell(Entity entity) {
        if (entity instanceof Player player) {
            player.displayClientMessage(Component.translatable("message.sofe.protected_zone").withStyle(ChatFormatting.GOLD), true);
        }
    }

    public static void onBreak(BlockEvent.BreakEvent event) {
        if (isFarmCrop(event.getState())) return; // the town's fields are there to be harvested
        if (!allowed(event.getLevel(), event.getPos(), ZoneAction.BREAK, event.getPlayer())) {
            event.setCanceled(true);
            tell(event.getPlayer());
        }
    }

    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player && isFarmCrop(event.getPlacedBlock())) return; // and sown again
        ZoneAction action = event.getEntity() instanceof Player ? ZoneAction.PLACE : ZoneAction.MOB_GRIEFING;
        if (!allowed(event.getLevel(), event.getPos(), action, event.getEntity())) {
            event.setCanceled(true);
            tell(event.getEntity());
        }
    }

    /**
     * Buckets, flint and steel, hoes and the like; and with or without an item, whatever holds something (a chest, a
     * drawer, a pot, a lectern, berries, a sign). Doors, gates, buttons, beds, seats and SoFE's blocks still work.
     */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockPos target = event.getHitVec().getBlockPos();
        if (farming(event.getLevel(), target, event.getItemStack())) return; // sowing, bone meal, picking berries
        if (holdsSomething(event.getLevel(), target) && !allowed(event.getLevel(), target, ZoneAction.TAKE, event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
            tell(event.getEntity());
            return;
        }
        if (event.getItemStack().isEmpty()) return;
        // the Void Gate's frames take their Eyes of Ender even inside the city
        if (event.getItemStack().is(net.minecraft.world.item.Items.ENDER_EYE)
                && event.getLevel().getBlockState(target).is(net.minecraft.world.level.block.Blocks.END_PORTAL_FRAME)) return;
        if (!allowed(event.getLevel(), target, ZoneAction.USE_ITEM_ON_BLOCK, event.getEntity())
                || !allowed(event.getLevel(), target.relative(event.getFace() == null ? net.minecraft.core.Direction.UP : event.getFace()),
                ZoneAction.USE_ITEM_ON_BLOCK, event.getEntity())) {
            event.setUseItem(Event.Result.DENY);
        }
    }

    /**
     * What grows in a field: wheat, carrots, potatoes, beetroots, melons, pumpkins, sugar cane, cocoa, berries.
     * In a town a Bearer may harvest the fields and sow them again (the rest of the town stays as it is).
     */
    static boolean isFarmCrop(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.CROPS) || state.is(net.minecraft.world.level.block.Blocks.MELON)
                || state.is(net.minecraft.world.level.block.Blocks.PUMPKIN) || state.is(net.minecraft.world.level.block.Blocks.SUGAR_CANE)
                || state.is(net.minecraft.world.level.block.Blocks.COCOA) || state.is(net.minecraft.world.level.block.Blocks.SWEET_BERRY_BUSH)
                || state.is(net.minecraft.world.level.block.Blocks.NETHER_WART);
    }

    /** Right-clicking a field: seeds on farmland, bone meal or a bare hand on a crop. */
    private static boolean farming(Level level, BlockPos target, net.minecraft.world.item.ItemStack stack) {
        var state = level.getBlockState(target);
        if (isFarmCrop(state)) return stack.isEmpty() || stack.is(net.minecraft.world.item.Items.BONE_MEAL);
        return state.is(net.minecraft.world.level.block.Blocks.FARMLAND) && stack.getItem() instanceof net.minecraft.world.item.BlockItem item
                && isFarmCrop(item.getBlock().defaultBlockState());
    }

    /** Explosions still happen, but the protected blocks, armor stands, frames and paintings are taken out of them. */
    public static void onExplosion(ExplosionEvent.Detonate event) {
        List<ProtectedZone> zones = zones(event.getLevel());
        if (zones.isEmpty()) return;
        event.getAffectedBlocks().removeIf(pos -> !ZoneRules.allowed(zones, pos.getX(), pos.getY(), pos.getZ(), ZoneAction.EXPLOSION));
        event.getAffectedEntities().removeIf(e -> isFurnishing(e) && !ZoneRules.allowed(zones, e.getBlockX(), e.getBlockY(), e.getBlockZ(), ZoneAction.EXPLOSION));
    }

    /**
     * A block that holds something someone could take or change: anything with an inventory (chests, barrels,
     * furnaces, the drawers and cabinets of the furniture mods), pots, lecterns, jukeboxes, chiseled bookshelves,
     * berries, cakes, composters, cauldrons, signs and note blocks. SoFE's own blocks are meant to be used.
     */
    static boolean holdsSomething(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        var block = state.getBlock();
        var id = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block);
        if (id != null && id.getNamespace().equals(SoFEMod.MOD_ID)) return false;
        var be = level.getBlockEntity(pos);
        if (be instanceof net.minecraft.world.Container) return true;
        if (be != null && be.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).isPresent()) return true;
        return block instanceof net.minecraft.world.level.block.FlowerPotBlock || block instanceof net.minecraft.world.level.block.LecternBlock
                || block instanceof net.minecraft.world.level.block.JukeboxBlock || block instanceof net.minecraft.world.level.block.CaveVines
                || block instanceof net.minecraft.world.level.block.SweetBerryBushBlock || block instanceof net.minecraft.world.level.block.CakeBlock
                || block instanceof net.minecraft.world.level.block.CandleCakeBlock || block instanceof net.minecraft.world.level.block.ComposterBlock
                || block instanceof net.minecraft.world.level.block.SignBlock || block instanceof net.minecraft.world.level.block.NoteBlock
                || block instanceof net.minecraft.world.level.block.ChiseledBookShelfBlock || block instanceof net.minecraft.world.level.block.BeehiveBlock
                || block instanceof net.minecraft.world.level.block.AbstractCauldronBlock;
    }

    /** The furnishings that are entities: armor stands (the statues), item frames, paintings, boats and carts. */
    static boolean isFurnishing(Entity entity) {
        return entity instanceof net.minecraft.world.entity.decoration.ArmorStand || entity instanceof net.minecraft.world.entity.decoration.HangingEntity
                || entity instanceof net.minecraft.world.entity.vehicle.Boat || entity instanceof net.minecraft.world.entity.vehicle.AbstractMinecart;
    }

    private static boolean furnishingProtected(Entity entity, Entity actor) {
        return isFurnishing(entity) && !allowed(entity.level(), entity.blockPosition(), ZoneAction.TAKE, actor);
    }

    /** No one can strike down a statue, a frame, a painting, a boat or a cart in a protected place. */
    public static void onAttackEntity(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        if (furnishingProtected(event.getTarget(), event.getEntity())) {
            event.setCanceled(true);
            tell(event.getEntity());
        }
    }

    /** Nor take what a statue wears or a frame holds, or turn the frame (a boat can still be ridden). */
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof net.minecraft.world.entity.vehicle.Boat) && furnishingProtected(event.getTarget(), event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
        }
    }

    public static void onInteractEntityAt(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getTarget() instanceof net.minecraft.world.entity.vehicle.Boat) && furnishingProtected(event.getTarget(), event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
        }
    }

    /** Arrows, tridents and fireballs do not break them either. */
    public static void onProjectileImpact(net.minecraftforge.event.entity.ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult hit && furnishingProtected(hit.getEntity(), event.getProjectile())) {
            event.setImpactResult(net.minecraftforge.event.entity.ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
        }
    }

    /** Nor anything else that hurts a statue: fire, lava, a blow of a mob. */
    public static void onLivingAttack(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.decoration.ArmorStand
                && furnishingProtected(event.getEntity(), event.getSource().getEntity())) {
            event.setCanceled(true);
        }
    }

    /** A piston outside a zone cannot push or pull blocks in or out of it. */
    public static void onPiston(PistonEvent.Pre event) {
        List<ProtectedZone> zones = zones(event.getLevel());
        if (zones.isEmpty() || !(event.getLevel() instanceof Level level)) return;
        BlockPos piston = event.getPos();
        if (!ZoneRules.allowed(zones, piston.getX(), piston.getY(), piston.getZ(), ZoneAction.PISTON)) return; // a piston built into the zone works
        PistonStructureResolver resolver = event.getStructureHelper();
        if (resolver == null || !resolver.resolve()) return;
        for (BlockPos pos : resolver.getToPush()) {
            if (touchesZone(zones, pos, event.getDirection())) {
                event.setCanceled(true);
                return;
            }
        }
        for (BlockPos pos : resolver.getToDestroy()) {
            if (!ZoneRules.allowed(zones, pos.getX(), pos.getY(), pos.getZ(), ZoneAction.PISTON)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    private static boolean touchesZone(List<ProtectedZone> zones, BlockPos pos, net.minecraft.core.Direction direction) {
        BlockPos to = pos.relative(direction);
        return !ZoneRules.allowed(zones, pos.getX(), pos.getY(), pos.getZ(), ZoneAction.PISTON)
                || !ZoneRules.allowed(zones, to.getX(), to.getY(), to.getZ(), ZoneAction.PISTON);
    }

    public static void onFluid(BlockEvent.FluidPlaceBlockEvent event) {
        if (!allowed(event.getLevel(), event.getPos(), ZoneAction.FLUID, null)) {
            event.setNewState(event.getOriginalState());
        }
    }

    public static void onTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (!allowed(event.getLevel(), event.getPos(), ZoneAction.BREAK, event.getEntity())) event.setCanceled(true);
    }

    /**
     * Fire never takes hold in a city, camp, dungeon or arena: a fire that spreads or is lit there goes out at once
     * (before it can burn anything). Fires the builders set (on the Citadel's towers) are placed without telling
     * their neighbours, so they keep burning.
     */
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getState().getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock)) return;
        if (!allowed(event.getLevel(), event.getPos(), ZoneAction.FIRE, null)) {
            event.getLevel().setBlock(event.getPos(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        }
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

    /**
     * No monsters spawn by themselves inside a city (the palace hall is big and its corners dark).
     * The Void creatures of the story still come: quests spawn them as events.
     */
    public static void onSpawnCheck(net.minecraftforge.event.entity.living.MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getEntityType().getCategory() != net.minecraft.world.entity.MobCategory.MONSTER) return;
        var type = event.getSpawnType();
        if (type != net.minecraft.world.entity.MobSpawnType.NATURAL && type != net.minecraft.world.entity.MobSpawnType.CHUNK_GENERATION
                && type != net.minecraft.world.entity.MobSpawnType.PATROL) return;
        ServerLevel level = event.getLevel().getLevel();
        if (level.dimension() != Level.OVERWORLD) return;
        BlockPos pos = event.getPos();
        for (ProtectedZone zone : ProtectedZoneData.get(level.getServer()).zones()) {
            if (zone.kind() == ProtectedZone.Kind.CITY && zone.contains(pos.getX(), pos.getY(), pos.getZ())) {
                event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
                return;
            }
        }
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
