package com.sofe.player;

import com.sofe.SoFEMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

import java.util.Optional;

/** Attaches {@link PlayerClassData} to every player, saves it with the player and keeps it on death. */
public final class PlayerClassCapability {
    public static final Capability<PlayerClassData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final ResourceLocation KEY = SoFEMod.id("player_class");
    private static final String TAG_CLASS = "class";

    private PlayerClassCapability() {
    }

    public static Optional<PlayerClassData> get(Player player) {
        return player.getCapability(CAPABILITY).resolve();
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PlayerClassData.class);
    }

    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            Provider provider = new Provider();
            event.addCapability(KEY, provider);
            event.addListener(provider.optional::invalidate);
        }
    }

    /** Respawn after death (and returning from the End) creates a new player object: copy the class over. */
    public static void onClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        get(event.getOriginal()).ifPresent(old -> get(event.getEntity()).ifPresent(copy -> copy.copyFrom(old)));
        event.getOriginal().invalidateCaps();
    }

    public static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final PlayerClassData data = new PlayerClassData();
        private final LazyOptional<PlayerClassData> optional = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            return CAPABILITY.orEmpty(capability, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            data.get().ifPresent(c -> tag.putString(TAG_CLASS, c.id()));
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            data.set(PlayerClass.byId(tag.getString(TAG_CLASS)).orElse(null));
        }
    }
}
