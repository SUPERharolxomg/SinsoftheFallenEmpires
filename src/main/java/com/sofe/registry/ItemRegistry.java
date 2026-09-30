package com.sofe.registry;

import com.sofe.SoFEMod;
import com.sofe.travel.ReturnScrollItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SoFEMod.MOD_ID);

    public static final RegistryObject<Item> RETURN_SCROLL = ITEMS.register("return_scroll",
            () -> new ReturnScrollItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> VOID_WRETCH_SPAWN_EGG = ITEMS.register("void_wretch_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.VOID_WRETCH, 0x1A0F24, 0x8E3FD6, new Item.Properties()));
    public static final RegistryObject<Item> VOID_STALKER_SPAWN_EGG = ITEMS.register("void_stalker_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.VOID_STALKER, 0x120A1A, 0xC05CFF, new Item.Properties()));
    public static final RegistryObject<Item> BRASS_SENTINEL_SPAWN_EGG = ITEMS.register("brass_sentinel_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.BRASS_SENTINEL, 0xB5863A, 0x3FD6C4, new Item.Properties()));

    private ItemRegistry() {
    }

    /** Items other than materials and blocks, for the creative tab. */
    public static List<Item> creativeItems() {
        return List.of(RETURN_SCROLL.get(), VOID_WRETCH_SPAWN_EGG.get(), VOID_STALKER_SPAWN_EGG.get(), BRASS_SENTINEL_SPAWN_EGG.get());
    }

    /** Spawn eggs use the vanilla template model. */
    public static List<RegistryObject<Item>> spawnEggs() {
        return List.of(VOID_WRETCH_SPAWN_EGG, VOID_STALKER_SPAWN_EGG, BRASS_SENTINEL_SPAWN_EGG);
    }
}
