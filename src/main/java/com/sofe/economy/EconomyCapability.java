package com.sofe.economy;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Saves {@link EconomyData} (the Wallet, stock, buyback, Blueprints, Flask charges) with the player. */
public final class EconomyCapability {
    public static final Capability<EconomyData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final ResourceLocation KEY = SoFEMod.id("economy");

    private EconomyCapability() {
    }

    public static Optional<EconomyData> get(Player player) {
        return player.getCapability(CAPABILITY).resolve();
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(EconomyData.class);
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

    public static CompoundTag save(EconomyData data) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("dinars", data.dinars());
        CompoundTag bought = new CompoundTag();
        data.boughtToday().forEach(bought::putInt);
        tag.put("bought", bought);
        tag.putLong("stock_day", data.stockDay());
        ListTag buyback = new ListTag();
        for (EconomyData.Sold sold : data.buyback()) {
            CompoundTag s = new CompoundTag();
            s.putString("item", sold.itemNbt());
            s.putInt("price", sold.price());
            buyback.add(s);
        }
        tag.put("buyback", buyback);
        ListTag blueprints = new ListTag();
        data.blueprints().forEach(b -> blueprints.add(StringTag.valueOf(b)));
        tag.put("blueprints", blueprints);
        tag.putInt("flask", data.flaskCharges());
        CompoundTag favor = new CompoundTag();
        data.favor().forEach(favor::putInt);
        tag.put("favor", favor);
        return tag;
    }

    public static void load(EconomyData data, CompoundTag tag) {
        Map<String, Integer> bought = new HashMap<>();
        CompoundTag b = tag.getCompound("bought");
        b.getAllKeys().forEach(k -> bought.put(k, b.getInt(k)));
        List<EconomyData.Sold> buyback = new ArrayList<>();
        ListTag list = tag.getList("buyback", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag s = list.getCompound(i);
            buyback.add(new EconomyData.Sold(s.getString("item"), s.getInt("price")));
        }
        Set<String> blueprints = new HashSet<>();
        ListTag bp = tag.getList("blueprints", Tag.TAG_STRING);
        for (int i = 0; i < bp.size(); i++) blueprints.add(bp.getString(i));
        data.load(tag.getLong("dinars"), bought, tag.contains("stock_day") ? tag.getLong("stock_day") : -1, buyback, blueprints,
                tag.contains("flask") ? tag.getInt("flask") : EconomyData.FLASK_START);
        Map<String, Integer> favor = new HashMap<>();
        CompoundTag f = tag.getCompound("favor");
        f.getAllKeys().forEach(k -> favor.put(k, f.getInt(k)));
        data.loadFavor(favor);
    }

    public static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final EconomyData data = new EconomyData();
        private final LazyOptional<EconomyData> optional = LazyOptional.of(() -> data);

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
