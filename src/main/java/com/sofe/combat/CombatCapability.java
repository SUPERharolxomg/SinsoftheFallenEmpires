package com.sofe.combat;

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

/** Attaches {@link CombatData} to players. Only the resource value is saved; cooldowns and runes are short-lived. */
public final class CombatCapability {
    public static final Capability<CombatData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    private static final ResourceLocation KEY = SoFEMod.id("combat");
    private static final String TAG_RESOURCE = "resource";

    private CombatCapability() {
    }

    public static Optional<CombatData> get(Player player) {
        return player.getCapability(CAPABILITY).resolve();
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(CombatData.class);
    }

    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            Provider provider = new Provider();
            event.addCapability(KEY, provider);
            // no invalidation listener: a dead player's data must survive invalidateCaps so reviveCaps can copy it to the respawned one
        }
    }

    /** Coming back from the End keeps the resource; a death starts again from the class's start value. */
    public static void onClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) return;
        event.getOriginal().reviveCaps();
        get(event.getOriginal()).flatMap(CombatData::resource).ifPresent(old ->
                get(event.getEntity()).ifPresent(copy -> copy.loadSavedValue(old.current())));
        event.getOriginal().invalidateCaps();
    }

    public static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final CombatData data = new CombatData();
        private final LazyOptional<CombatData> optional = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            return CAPABILITY.orEmpty(capability, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            data.resource().ifPresent(r -> tag.putFloat(TAG_RESOURCE, r.current()));
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag.contains(TAG_RESOURCE)) {
                data.loadSavedValue(tag.getFloat(TAG_RESOURCE));
            }
        }
    }
}
