package com.sofe.registry;

import com.sofe.SoFEMod;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/** Archsins, Broken Oaths, NPCs, summons and mobs are added here from Sprint 3 on. */
public final class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SoFEMod.MOD_ID);

    private EntityRegistry() {
    }
}
