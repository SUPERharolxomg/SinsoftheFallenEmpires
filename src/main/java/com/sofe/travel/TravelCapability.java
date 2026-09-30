package com.sofe.travel;

import com.sofe.SoFEMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Saves {@link TravelData} with the player and keeps it on death. */
public final class TravelCapability {
    public static final Capability<TravelData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final ResourceLocation KEY = SoFEMod.id("travel");

    private TravelCapability() {
    }

    public static Optional<TravelData> get(Player player) {
        return player.getCapability(CAPABILITY).resolve();
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(TravelData.class);
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
        get(event.getOriginal()).ifPresent(old -> get(event.getEntity()).ifPresent(copy -> copy.copyFrom(old)));
        event.getOriginal().invalidateCaps();
    }

    public static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final TravelData data = new TravelData();
        private final LazyOptional<TravelData> optional = LazyOptional.of(() -> data);

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

    public static CompoundTag save(TravelData data) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        data.waystones().values().forEach(w -> {
            CompoundTag t = new CompoundTag();
            t.putLong("pos", w.pos().asLong());
            if (w.nameKey() != null) t.putString("name", w.nameKey());
            list.add(t);
        });
        tag.put("waystones", list);
        data.lastActivated().ifPresent(w -> tag.putLong("last", w.pos().asLong()));
        tag.putInt("vault_rows", data.vaultRows());
        ListTag items = new ListTag();
        for (int i = 0; i < data.vault().getContainerSize(); i++) {
            ItemStack stack = data.vault().getItem(i);
            if (stack.isEmpty()) continue;
            CompoundTag item = stack.save(new CompoundTag());
            item.putByte("slot", (byte) i);
            items.add(item);
        }
        tag.put("vault", items);
        return tag;
    }

    public static void load(TravelData data, CompoundTag tag) {
        Map<Long, TravelData.Waystone> saved = new LinkedHashMap<>();
        ListTag list = tag.getList("waystones", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag t = list.getCompound(i);
            BlockPos pos = BlockPos.of(t.getLong("pos"));
            saved.put(pos.asLong(), new TravelData.Waystone(pos, t.contains("name") ? t.getString("name") : null));
        }
        data.restore(saved, tag.contains("last") ? tag.getLong("last") : null, tag.contains("vault_rows") ? tag.getInt("vault_rows") : TravelData.VAULT_ROWS);
        data.vault().clearContent();
        ListTag items = tag.getList("vault", Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag item = items.getCompound(i);
            int slot = item.getByte("slot") & 0xFF;
            if (slot < data.vault().getContainerSize()) data.vault().setItem(slot, ItemStack.of(item));
        }
    }
}
