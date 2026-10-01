package com.sofe.registry;

import com.sofe.SoFEMod;
import com.sofe.item.CodexShardItem;
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

    public static final RegistryObject<Item> CODEX_SHARD = ITEMS.register("codex_shard",
            () -> new CodexShardItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    /** Nether materials of the Burning Deep (docs/Anexos.md, A3). */
    public static final RegistryObject<Item> INFERNAL_EMBER = ITEMS.register("infernal_ember", () -> new Item(new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> WAILING_SOUL = ITEMS.register("wailing_soul", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> VOID_WRETCH_SPAWN_EGG = ITEMS.register("void_wretch_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.VOID_WRETCH, 0x1A0F24, 0x8E3FD6, new Item.Properties()));
    public static final RegistryObject<Item> VOID_STALKER_SPAWN_EGG = ITEMS.register("void_stalker_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.VOID_STALKER, 0x120A1A, 0xC05CFF, new Item.Properties()));
    public static final RegistryObject<Item> BRASS_SENTINEL_SPAWN_EGG = ITEMS.register("brass_sentinel_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.BRASS_SENTINEL, 0xB5863A, 0x3FD6C4, new Item.Properties()));

    public static final RegistryObject<Item> KALETH_SPAWN_EGG = ITEMS.register("kaleth_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.KALETH, 0x3A2A22, 0xFF7A1A, new Item.Properties()));
    public static final RegistryObject<Item> SERATH_SPAWN_EGG = ITEMS.register("serath_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.SERATH, 0x2A0A10, 0xB0141E, new Item.Properties()));
    public static final RegistryObject<Item> VORATH_SPAWN_EGG = ITEMS.register("vorath_spawn_egg",
            () -> new ForgeSpawnEggItem(EntityRegistry.VORATH, 0x1A0A08, 0xE0401A, new Item.Properties()));

    private ItemRegistry() {
    }

    /** Items other than materials and blocks, for the creative tab. */
    public static List<Item> creativeItems() {
        return List.of(RETURN_SCROLL.get(), CODEX_SHARD.get(), INFERNAL_EMBER.get(), WAILING_SOUL.get(),
                VOID_WRETCH_SPAWN_EGG.get(), VOID_STALKER_SPAWN_EGG.get(), BRASS_SENTINEL_SPAWN_EGG.get(),
                KALETH_SPAWN_EGG.get(), SERATH_SPAWN_EGG.get(), VORATH_SPAWN_EGG.get());
    }

    /** Spawn eggs use the vanilla template model. */
    public static List<RegistryObject<Item>> spawnEggs() {
        return List.of(VOID_WRETCH_SPAWN_EGG, VOID_STALKER_SPAWN_EGG, BRASS_SENTINEL_SPAWN_EGG,
                KALETH_SPAWN_EGG, SERATH_SPAWN_EGG, VORATH_SPAWN_EGG);
    }

    /** Plain items with a flat model and textures/item/&lt;id&gt;.png. */
    public static List<RegistryObject<Item>> flatItems() {
        return List.of(RETURN_SCROLL, CODEX_SHARD, INFERNAL_EMBER, WAILING_SOUL);
    }
}
