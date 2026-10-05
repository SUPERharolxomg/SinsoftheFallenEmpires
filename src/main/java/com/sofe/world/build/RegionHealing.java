package com.sofe.world.build;

import com.sofe.SoFEMod;
import com.sofe.world.region.Region;
import com.sofe.world.zone.ProtectedZone;
import com.sofe.world.zone.ProtectedZoneData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The landscape heals (docs/Mundo.md, W6): when the Archsin of a region falls for the first time in a world, every
 * corrupted building block in that region's cities, dungeons and arenas turns back into its intact block
 * (EmpireBlocks). The work is spread over ticks (a slice of a place each tick) so the server never stalls, and each
 * region heals once per world.
 */
public final class RegionHealing extends SavedData {
    private static final String NAME = SoFEMod.MOD_ID + "_region_healing";
    /** Blocks looked at per tick. */
    public static final int BUDGET = 24_000;
    /** The Archsin whose fall heals each region (as RegionStatus). */
    private static final Map<String, Region> HEALS = Map.of(
            "sofe:vorath", Region.NORDRATH, "sofe:luxara", Region.PARSIVAN,
            "sofe:morthis", Region.KHEMET, "sofe:envyris", Region.AUREUM);

    private final Set<String> healed = new HashSet<>();
    private static final Deque<Job> QUEUE = new ArrayDeque<>();

    /** One place being healed: its box, and how far through it the work has come (an index over its blocks). */
    private static final class Job {
        final ProtectedZone zone;
        long next;

        Job(ProtectedZone zone) {
            this.zone = zone;
        }

        long size() {
            return (long) (zone.maxX() - zone.minX() + 1) * (zone.maxY() - zone.minY() + 1) * (zone.maxZ() - zone.minZ() + 1);
        }
    }

    public static RegionHealing get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(RegionHealing::load, RegionHealing::new, NAME);
    }

    private static RegionHealing load(CompoundTag tag) {
        RegionHealing data = new RegionHealing();
        for (var t : tag.getList("healed", StringTag.TAG_STRING)) data.healed.add(t.getAsString());
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        healed.forEach(r -> list.add(StringTag.valueOf(r)));
        tag.put("healed", list);
        return tag;
    }

    public boolean isHealed(Region region) {
        return healed.contains(region.name());
    }

    /** The region an Archsin's fall heals, if it heals one. */
    public static Region healedBy(String bossId) {
        return HEALS.get(bossId);
    }

    /** A boss fell: if it was a region's Archsin and the region has not healed yet, start healing it. */
    public static void bossFell(ServerLevel level, String bossId) {
        Region region = HEALS.get(bossId);
        if (region == null) return;
        heal(level.getServer(), region);
    }

    /** Starts healing a region (once per world); returns how many places were queued. */
    public static int heal(MinecraftServer server, Region region) {
        RegionHealing data = get(server);
        if (!data.healed.add(region.name())) return 0;
        data.setDirty();
        String prefix = SoFEMod.MOD_ID + ":" + region.name().toLowerCase(Locale.ROOT) + "/";
        int queued = 0;
        for (ProtectedZone zone : ProtectedZoneData.get(server).zones()) {
            // places not built yet are healed when they are (afterBuilt)
            if (zone.id().startsWith(prefix) && com.sofe.world.StoryPlacements.isBuilt(server, zone.id())) {
                QUEUE.add(new Job(zone));
                queued++;
            }
        }
        Component message = Component.translatable("message.sofe.region_heals", Component.translatable(region.translationKey()))
                .withStyle(ChatFormatting.GREEN);
        server.getPlayerList().broadcastSystemMessage(message, false);
        SoFEMod.LOGGER.info("{} is liberated: healing {} places", region, queued);
        return queued;
    }

    /** A place was just built: if its region already healed, it is healed too. */
    public static void afterBuilt(MinecraftServer server, String structureId) {
        RegionHealing data = get(server);
        for (Region region : Region.values()) {
            String prefix = SoFEMod.MOD_ID + ":" + region.name().toLowerCase(Locale.ROOT) + "/";
            if (!structureId.startsWith(prefix) || !data.isHealed(region)) continue;
            ProtectedZoneData.get(server).zones().stream().filter(z -> z.id().equals(structureId)).findFirst().ifPresent(z -> QUEUE.add(new Job(z)));
        }
    }

    /** Heals everything queued at once (tests and admin commands). */
    public static void finishNow(ServerLevel level) {
        while (!QUEUE.isEmpty()) work(level, Integer.MAX_VALUE);
    }

    public static boolean busy() {
        return !QUEUE.isEmpty();
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || QUEUE.isEmpty()) return;
        work(event.getServer().overworld(), BUDGET);
    }

    private static void work(ServerLevel level, int budget) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        while (budget > 0 && !QUEUE.isEmpty()) {
            Job job = QUEUE.peek();
            ProtectedZone z = job.zone;
            int sx = z.maxX() - z.minX() + 1, sz = z.maxZ() - z.minZ() + 1;
            long size = job.size();
            while (budget-- > 0 && job.next < size) {
                long i = job.next++;
                int x = (int) (i % sx), zz = (int) ((i / sx) % sz), y = (int) (i / ((long) sx * sz));
                pos.set(z.minX() + x, z.minY() + y, z.minZ() + zz);
                var state = level.getBlockState(pos);
                EmpireBlocks.heal(state).ifPresent(intact -> level.setBlock(pos, intact, 2));
            }
            if (job.next >= size) QUEUE.poll();
        }
    }
}
