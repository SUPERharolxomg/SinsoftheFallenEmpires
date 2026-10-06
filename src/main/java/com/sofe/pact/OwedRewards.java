package com.sofe.pact;

import com.sofe.SoFEMod;
import com.sofe.entity.boss.SoFEBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The rewards of bosses that fell while a participant was away from the game (UC-32: "if the boss dies, the player's
 * reward waits for them"), saved with the world and given when they come back.
 */
public class OwedRewards extends SavedData {
    private static final String NAME = "sofe_owed_rewards";
    private final Map<UUID, List<String>> owed = new HashMap<>();

    public static OwedRewards get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(OwedRewards::load, OwedRewards::new, NAME);
    }

    public void owe(UUID player, String bossType) {
        owed.computeIfAbsent(player, k -> new ArrayList<>()).add(bossType);
        setDirty();
    }

    public List<String> take(UUID player) {
        List<String> out = owed.remove(player);
        if (out != null) setDirty();
        return out == null ? List.of() : out;
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        for (String type : get(player.server).take(player.getUUID())) {
            var entityType = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(type));
            if (entityType != null && entityType.create(player.serverLevel()) instanceof SoFEBossEntity boss) {
                boss.creditLate(player.serverLevel(), player);
                boss.discard();
                SoFEMod.LOGGER.info("{} came back and got the reward of {}", player.getGameProfile().getName(), type);
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag map = new CompoundTag();
        owed.forEach((id, list) -> {
            ListTag l = new ListTag();
            list.forEach(b -> l.add(StringTag.valueOf(b)));
            map.put(id.toString(), l);
        });
        tag.put("owed", map);
        return tag;
    }

    private static OwedRewards load(CompoundTag tag) {
        OwedRewards data = new OwedRewards();
        CompoundTag map = tag.getCompound("owed");
        for (String key : map.getAllKeys()) {
            List<String> list = new ArrayList<>();
            for (Tag t : map.getList(key, Tag.TAG_STRING)) list.add(t.getAsString());
            data.owed.put(UUID.fromString(key), list);
        }
        return data;
    }
}
