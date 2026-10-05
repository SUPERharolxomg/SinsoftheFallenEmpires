package com.sofe.economy;

import com.sofe.SoFEMod;
import com.sofe.entity.npc.MerchantNpcEntity;
import com.sofe.world.SoFEWorld;
import com.sofe.world.StoryPlacements;
import com.sofe.world.build.RegionHealing;
import com.sofe.world.region.Region;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Zahir the Wanderer (docs/Anexos.md, A4): a caravan of rare goods that stops at one liberated camp for three days,
 * then moves on to another (any camp whose region has been liberated in this world). Only one Zahir walks the world:
 * when he moves, the old one is gone, and an old copy found in land loaded later never comes back.
 */
public final class ZahirCaravan extends SavedData {
    public static final String NPC = "zahir";
    public static final int DAYS = 3;
    private static final String NAME = SoFEMod.MOD_ID + "_zahir";
    private UUID zahir;
    private String camp = "";
    private long arrivedDay = -DAYS;

    public static ZahirCaravan get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(ZahirCaravan::load, ZahirCaravan::new, NAME);
    }

    private static ZahirCaravan load(CompoundTag tag) {
        ZahirCaravan data = new ZahirCaravan();
        if (tag.hasUUID("zahir")) data.zahir = tag.getUUID("zahir");
        data.camp = tag.getString("camp");
        data.arrivedDay = tag.getLong("arrived");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (zahir != null) tag.putUUID("zahir", zahir);
        tag.putString("camp", camp);
        tag.putLong("arrived", arrivedDay);
        return tag;
    }

    public String camp() {
        return camp;
    }

    /** The camps Zahir may stop at: built, and in a liberated region. */
    public static List<StructurePositions.Structure> openCamps(MinecraftServer server) {
        List<StructurePositions.Structure> out = new ArrayList<>();
        RegionHealing healing = RegionHealing.get(server);
        for (Region region : Region.values()) {
            String id = SoFEMod.MOD_ID + ":" + region.name().toLowerCase(Locale.ROOT) + "/camp";
            StructurePositions.get().structure(id).ifPresent(s -> {
                if (healing.isHealed(region) && StoryPlacements.isBuilt(server, id)) out.add(s);
            });
        }
        return out;
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 200 != 0) return;
        MinecraftServer server = event.getServer();
        if (!SoFEWorld.isJourney(server)) return;
        ZahirCaravan data = get(server);
        long day = server.overworld().getDayTime() / 24000L;
        if (day - data.arrivedDay < DAYS && data.zahir != null) return;
        List<StructurePositions.Structure> camps = openCamps(server);
        if (camps.isEmpty()) return;
        move(server.overworld(), data, camps.get(server.overworld().getRandom().nextInt(camps.size())), day);
    }

    /** Sends the caravan to a camp now (and away from where it was). */
    public static void move(ServerLevel level, ZahirCaravan data, StructurePositions.Structure camp, long day) {
        if (data.zahir != null && level.getEntity(data.zahir) instanceof MerchantNpcEntity old) old.discard();
        var spawned = StoryPlacements.spawnNpc(level, new StructurePositions.Npc(NPC, "merchant", camp.x(), camp.z() + 3, 180f, "wanderer"));
        if (spawned.isEmpty()) return;
        data.zahir = spawned.get().getUUID();
        data.camp = camp.id();
        data.arrivedDay = day;
        data.setDirty();
        String region = camp.id().substring(camp.id().indexOf(':') + 1, camp.id().indexOf('/'));
        level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("message.sofe.zahir.arrives",
                Component.translatable("waystone.sofe." + region + ".camp")).withStyle(ChatFormatting.GOLD), false);
    }

    /** An old Zahir in land loaded again is not the one on the road now: he never rejoins the world. */
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof MerchantNpcEntity merchant) || !NPC.equals(merchant.npcId())) return;
        if (!(event.getLevel() instanceof ServerLevel level) || !event.loadedFromDisk()) return;
        UUID current = get(level.getServer()).zahir;
        if (current == null || !current.equals(merchant.getUUID())) event.setCanceled(true);
    }
}
