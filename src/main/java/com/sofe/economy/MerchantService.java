package com.sofe.economy;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sofe.SoFEMod;
import com.sofe.gear.GearData;
import com.sofe.gear.GearNbt;
import com.sofe.network.OpenMerchantPacket;
import com.sofe.network.SoFENetwork;
import com.sofe.story.StoryAct;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The SoFE merchants (docs/Anexos.md, A4): their offers by act, per-player stock with a daily
 * restock, selling, buyback of the last 5 items and Selim's emerald exchange. The client only shows
 * the screen; every price, Dinar and stock check happens here.
 */
public final class MerchantService {
    private static final double REACH = 8;
    private static volatile Map<String, MerchantOffer.Catalog> catalogs = Map.of();
    /** The merchant each player is trading with: npc id and entity id. */
    private static final Map<UUID, Integer> TRADING = new ConcurrentHashMap<>();

    private MerchantService() {
    }

    public static Optional<MerchantOffer.Catalog> catalog(String npc) {
        return Optional.ofNullable(catalogs.get(npc));
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimpleJsonResourceReloadListener(new Gson(), "merchant_offers") {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
                Map<String, MerchantOffer.Catalog> loaded = new HashMap<>();
                files.forEach((id, json) -> {
                    try {
                        loaded.put(id.getPath(), MerchantOffer.parse(id.getPath(), json.getAsJsonObject()));
                    } catch (RuntimeException e) {
                        SoFEMod.LOGGER.error("Skipping merchant offers {}: {}", id, e.getMessage());
                    }
                });
                catalogs = Map.copyOf(loaded);
            }
        });
    }

    public static boolean isMoneyChanger(MerchantOffer.Catalog catalog) {
        return catalog.role().equals("money_changer");
    }

    /** What the player could sell to this merchant: gear (any merchant) and the items in its "buys" list. */
    public static OptionalInt sellPrice(MerchantOffer.Catalog catalog, ItemStack stack) {
        if (stack.isEmpty() || com.sofe.item.Soulbound.is(stack)) return OptionalInt.empty();
        Optional<GearData> gear = GearNbt.read(stack);
        if (gear.isPresent()) return Prices.sellPrice(gear.get());
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        for (MerchantOffer.Buy buy : catalog.buys()) {
            if (id != null && buy.item().equals(id.toString())) return OptionalInt.of(buy.price() * stack.getCount());
        }
        return OptionalInt.empty();
    }

    // --- opening and refreshing the screen

    public static void open(ServerPlayer player, Entity merchant, String npc) {
        Optional<MerchantOffer.Catalog> catalog = catalog(npc);
        if (catalog.isEmpty()) return;
        TRADING.put(player.getUUID(), merchant.getId());
        refresh(player, npc);
    }

    private static void refresh(ServerPlayer player, String npc) {
        catalog(npc).ifPresent(catalog -> EconomyCapability.get(player).ifPresent(economy -> {
            economy.restockIfNewDay(day(player));
            int act = StoryAct.of(player);
            List<OpenMerchantPacket.Offer> offers = new ArrayList<>();
            List<MerchantOffer> available = catalog.available(act);
            for (int i = 0; i < available.size(); i++) {
                MerchantOffer o = available.get(i);
                offers.add(new OpenMerchantPacket.Offer(i, stackOf(o), o.price(), economy.left(o)));
            }
            List<OpenMerchantPacket.Sellable> sellables = new ArrayList<>();
            for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
                ItemStack stack = player.getInventory().items.get(slot);
                int s = slot;
                sellPrice(catalog, stack).ifPresent(price -> sellables.add(new OpenMerchantPacket.Sellable(s, stack.copy(), price)));
            }
            List<OpenMerchantPacket.Sellable> buyback = new ArrayList<>();
            List<EconomyData.Sold> sold = economy.buyback();
            for (int i = 0; i < sold.size(); i++) {
                buyback.add(new OpenMerchantPacket.Sellable(i, parse(sold.get(i).itemNbt()), sold.get(i).price()));
            }
            SoFENetwork.sendTo(player, new OpenMerchantPacket(npc, economy.dinars(), offers, sellables, buyback, isMoneyChanger(catalog)));
        }));
    }

    private static ItemStack stackOf(MerchantOffer offer) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(offer.item()));
        return item == null ? ItemStack.EMPTY : new ItemStack(item, offer.count());
    }

    private static ItemStack parse(String nbt) {
        try {
            return ItemStack.of(TagParser.parseTag(nbt));
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    private static long day(ServerPlayer player) {
        return player.level().getDayTime() / 24000L;
    }

    /** Whether the player still stands next to the merchant they opened. */
    private static boolean atMerchant(ServerPlayer player) {
        Integer id = TRADING.get(player.getUUID());
        Entity merchant = id == null ? null : player.level().getEntity(id);
        return merchant != null && merchant.isAlive() && merchant.distanceToSqr(player) <= REACH * REACH;
    }

    // --- the actions

    public enum Action { BUY, SELL, BUYBACK, EMERALD_TO_DINARS, DINARS_TO_EMERALD }

    public static void act(ServerPlayer player, String npc, Action action, int index) {
        Optional<MerchantOffer.Catalog> catalog = catalog(npc);
        Optional<EconomyData> economy = EconomyCapability.get(player);
        if (catalog.isEmpty() || economy.isEmpty() || !atMerchant(player)) return;
        EconomyData e = economy.get();
        switch (action) {
            case BUY -> buy(player, catalog.get(), e, index);
            case SELL -> sell(player, catalog.get(), e, index);
            case BUYBACK -> {
                EconomyData.Sold sold = e.takeBuyback(index);
                if (sold == null) tell(player, "message.sofe.trade.no_dinars", ChatFormatting.RED);
                else give(player, parse(sold.itemNbt()));
            }
            case EMERALD_TO_DINARS -> {
                if (!isMoneyChanger(catalog.get())) return;
                if (player.getInventory().countItem(Items.EMERALD) < 1) {
                    tell(player, "message.sofe.trade.no_emerald", ChatFormatting.RED);
                } else {
                    removeOne(player, Items.EMERALD);
                    e.addDinars(Prices.DINARS_PER_EMERALD_SOLD);
                }
            }
            case DINARS_TO_EMERALD -> {
                if (!isMoneyChanger(catalog.get())) return;
                if (e.spend(Prices.DINARS_PER_EMERALD_BOUGHT)) give(player, new ItemStack(Items.EMERALD));
                else tell(player, "message.sofe.trade.no_dinars", ChatFormatting.RED);
            }
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.5f, 1.6f);
        EconomyHandler.sync(player);
        refresh(player, npc);
    }

    private static void buy(ServerPlayer player, MerchantOffer.Catalog catalog, EconomyData e, int index) {
        List<MerchantOffer> available = catalog.available(StoryAct.of(player));
        if (index < 0 || index >= available.size()) return;
        MerchantOffer offer = available.get(index);
        EconomyData.BuyResult result = e.buy(offer, StoryAct.of(player), day(player));
        switch (result) {
            case BOUGHT -> {
                ItemStack stack = stackOf(offer);
                Component name = stack.getHoverName();
                give(player, stack);
                player.displayClientMessage(Component.translatable("message.sofe.trade.bought", name).withStyle(ChatFormatting.GOLD), true);
            }
            case NO_DINARS -> tell(player, "message.sofe.trade.no_dinars", ChatFormatting.RED);
            case SOLD_OUT -> tell(player, "message.sofe.trade.sold_out", ChatFormatting.GRAY);
            case LOCKED -> tell(player, "message.sofe.trade.locked", ChatFormatting.GRAY);
        }
    }

    private static void sell(ServerPlayer player, MerchantOffer.Catalog catalog, EconomyData e, int slot) {
        if (slot < 0 || slot >= player.getInventory().items.size()) return;
        ItemStack stack = player.getInventory().items.get(slot);
        OptionalInt price = sellPrice(catalog, stack);
        if (price.isEmpty()) {
            tell(player, "message.sofe.trade.cannot_sell", ChatFormatting.RED);
            return;
        }
        CompoundTag saved = stack.save(new CompoundTag());
        player.getInventory().items.set(slot, ItemStack.EMPTY);
        e.addDinars(price.getAsInt());
        e.sold(new EconomyData.Sold(saved.toString(), price.getAsInt()));
        player.displayClientMessage(Component.translatable("message.sofe.trade.sold", price.getAsInt()).withStyle(ChatFormatting.GOLD), true);
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!stack.isEmpty() && !player.getInventory().add(stack)) player.drop(stack, false);
    }

    private static void removeOne(ServerPlayer player, Item item) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                stack.shrink(1);
                return;
            }
        }
    }

    private static void tell(ServerPlayer player, String key, ChatFormatting color) {
        player.displayClientMessage(Component.translatable(key).withStyle(color), true);
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        TRADING.remove(event.getEntity().getUUID());
    }
}
