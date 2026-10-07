package com.sofe.story;

import com.sofe.SoFEMod;
import com.sofe.world.region.Region;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Attaches {@link StoryProgress} to players, saves it and keeps it on death and when coming back from the End. */
public final class StoryCapability {
    public static final Capability<StoryProgress> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final ResourceLocation KEY = SoFEMod.id("story");

    private StoryCapability() {
    }

    public static Optional<StoryProgress> get(Player player) {
        return player.getCapability(CAPABILITY).resolve();
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(StoryProgress.class);
    }

    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            Provider provider = new Provider();
            event.addCapability(KEY, provider);
            // no invalidation listener: a dead player's data must survive invalidateCaps so reviveCaps can copy it to the respawned one
        }
    }

    public static void onClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        get(event.getOriginal()).ifPresent(old -> get(event.getEntity()).ifPresent(copy -> copy.load(old.act(),
                old.quests(), old.bosses(), old.fates(), old.trackedQuest().orElse(null))));
        get(event.getOriginal()).ifPresent(old -> get(event.getEntity()).ifPresent(copy -> copy.loadRelics(old.relics())));
        get(event.getOriginal()).ifPresent(old -> get(event.getEntity()).ifPresent(copy -> copy.loadPuzzles(old.puzzles())));
        event.getOriginal().invalidateCaps();
    }

    public static CompoundTag save(StoryProgress data) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("act", data.act());
        CompoundTag quests = new CompoundTag();
        data.quests().forEach((id, s) -> {
            CompoundTag q = new CompoundTag();
            q.putInt("step", s.step());
            q.putInt("count", s.count());
            q.putBoolean("completed", s.completed());
            quests.put(id, q);
        });
        tag.put("quests", quests);
        ListTag bosses = new ListTag();
        data.bosses().forEach(b -> bosses.add(StringTag.valueOf(b)));
        tag.put("bosses", bosses);
        ListTag relics = new ListTag();
        data.relics().forEach(r -> relics.add(StringTag.valueOf(r)));
        tag.put("relics", relics);
        ListTag puzzles = new ListTag();
        data.puzzles().forEach(z -> puzzles.add(StringTag.valueOf(z)));
        tag.put("puzzles", puzzles);
        CompoundTag fates = new CompoundTag();
        data.fates().forEach((r, f) -> fates.putString(r.id(), f));
        tag.put("fates", fates);
        data.trackedQuest().ifPresent(t -> tag.putString("tracked", t));
        return tag;
    }

    public static void load(StoryProgress data, CompoundTag tag) {
        Map<String, StoryProgress.QuestState> quests = new HashMap<>();
        CompoundTag q = tag.getCompound("quests");
        q.getAllKeys().forEach(id -> {
            CompoundTag s = q.getCompound(id);
            quests.put(id, new StoryProgress.QuestState(s.getInt("step"), s.getInt("count"), s.getBoolean("completed")));
        });
        Set<String> bosses = new HashSet<>();
        ListTag b = tag.getList("bosses", Tag.TAG_STRING);
        for (int i = 0; i < b.size(); i++) bosses.add(b.getString(i));
        Map<Region, String> fates = new EnumMap<>(Region.class);
        CompoundTag f = tag.getCompound("fates");
        f.getAllKeys().forEach(r -> Region.byId(r).ifPresent(region -> fates.put(region, f.getString(r))));
        data.load(tag.contains("act") ? tag.getInt("act") : 1, quests, bosses, fates, tag.contains("tracked") ? tag.getString("tracked") : null);
        Set<String> relics = new HashSet<>();
        ListTag r = tag.getList("relics", Tag.TAG_STRING);
        for (int i = 0; i < r.size(); i++) relics.add(r.getString(i));
        data.loadRelics(relics);
        Set<String> puzzles = new HashSet<>();
        ListTag z = tag.getList("puzzles", Tag.TAG_STRING);
        for (int i = 0; i < z.size(); i++) puzzles.add(z.getString(i));
        data.loadPuzzles(puzzles);
    }

    public static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final StoryProgress data = new StoryProgress();
        private final LazyOptional<StoryProgress> optional = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            return CAPABILITY.orEmpty(capability, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            return save(data);
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            load(data, tag);
        }
    }
}
