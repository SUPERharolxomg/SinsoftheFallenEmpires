package com.sofe.progression;

import com.sofe.SoFEMod;
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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Attaches {@link ProgressionData} to players, saves it and keeps it on death (levels are never lost). */
public final class ProgressionCapability {
    public static final Capability<ProgressionData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final ResourceLocation KEY = SoFEMod.id("progression");

    private ProgressionCapability() {
    }

    public static Optional<ProgressionData> get(Player player) {
        return player.getCapability(CAPABILITY).resolve();
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ProgressionData.class);
    }

    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            Provider provider = new Provider();
            event.addCapability(KEY, provider);
            event.addListener(provider.optional::invalidate);
        }
    }

    public static void onClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        get(event.getOriginal()).ifPresent(old -> get(event.getEntity()).ifPresent(copy -> {
            copy.load(old.level(), old.xp(), old.skillPoints(), old.attributePoints(), old.startingPointsGranted());
            copy.skills().load(old.skills().ranks(), old.skills().slots());
            copy.attributes().load(old.attributes().addedPoints());
        }));
        event.getOriginal().invalidateCaps();
    }

    public static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final ProgressionData data = new ProgressionData();
        private final LazyOptional<ProgressionData> optional = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            return CAPABILITY.orEmpty(capability, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("level", data.level());
            tag.putLong("xp", data.xp());
            tag.putInt("skill_points", data.skillPoints());
            tag.putInt("attribute_points", data.attributePoints());
            tag.putBoolean("starting_points", data.startingPointsGranted());
            CompoundTag ranks = new CompoundTag();
            data.skills().ranks().forEach(ranks::putInt);
            tag.put("skill_ranks", ranks);
            ListTag slots = new ListTag();
            data.skills().slots().forEach(s -> slots.add(StringTag.valueOf(s == null ? "" : s)));
            tag.put("skill_slots", slots);
            CompoundTag attributes = new CompoundTag();
            data.attributes().addedPoints().forEach((a, v) -> attributes.putInt(a.id(), v));
            tag.put("attributes", attributes);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            data.load(tag.getInt("level"), tag.getLong("xp"), tag.getInt("skill_points"),
                    tag.getInt("attribute_points"), tag.getBoolean("starting_points"));
            CompoundTag ranks = tag.getCompound("skill_ranks");
            Map<String, Integer> savedRanks = new HashMap<>();
            ranks.getAllKeys().forEach(k -> savedRanks.put(k, ranks.getInt(k)));
            ListTag slots = tag.getList("skill_slots", Tag.TAG_STRING);
            List<String> savedSlots = new ArrayList<>();
            for (int i = 0; i < slots.size(); i++) savedSlots.add(slots.getString(i));
            data.skills().load(savedRanks, savedSlots);
            CompoundTag attributes = tag.getCompound("attributes");
            Map<CharacterAttribute, Integer> savedAttributes = new EnumMap<>(CharacterAttribute.class);
            for (CharacterAttribute a : CharacterAttribute.values()) {
                if (attributes.contains(a.id())) savedAttributes.put(a, attributes.getInt(a.id()));
            }
            data.attributes().load(savedAttributes);
        }
    }
}
