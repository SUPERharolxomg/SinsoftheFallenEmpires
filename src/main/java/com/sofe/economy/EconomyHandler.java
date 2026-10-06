package com.sofe.economy;

import com.sofe.network.SoFENetwork;
import com.sofe.network.SyncEconomyPacket;
import com.sofe.registry.ItemRegistry;
import com.sofe.story.StoryCapability;
import com.sofe.world.region.Region;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Set;

/** Dinars, the Bearer's Flask refills and keeping the client up to date. */
public final class EconomyHandler {
    /** The Archsins whose fall adds a Flask charge (docs/Pociones.md, "The Bearer's Flask"). */
    private static final Set<String> ARCHSINS = Set.of("sofe:vorath", "sofe:luxara", "sofe:morthis", "sofe:avarok",
            "sofe:gularth", "sofe:envyris", "sofe:prython");

    private EconomyHandler() {
    }

    public static int flaskMax(ServerPlayer player) {
        int archsins = StoryCapability.get(player).map(s -> (int) ARCHSINS.stream().filter(s::hasDefeated).count()).orElse(0);
        int pillars = com.sofe.companion.ClassConcord.pillars(com.sofe.companion.ClassConcord.of(player)) ? 1 : 0; // the Five Pillars
        return EconomyData.flaskMax(archsins) + pillars;
    }

    public static void sync(ServerPlayer player) {
        EconomyCapability.get(player).ifPresent(e -> SoFENetwork.sendTo(player, new SyncEconomyPacket(e.dinars(), e.flaskCharges(), flaskMax(player), e.blueprints())));
    }

    public static void addDinars(ServerPlayer player, long amount) {
        EconomyCapability.get(player).ifPresent(e -> e.addDinars(amount));
        sync(player);
    }

    /** Dinars picked up go straight into the Wallet instead of the inventory. */
    public static void onPickup(EntityItemPickupEvent event) {
        ItemStack stack = event.getItem().getItem();
        if (!stack.is(ItemRegistry.DINAR.get()) || !(event.getEntity() instanceof ServerPlayer player)) return;
        int amount = stack.getCount();
        event.getItem().discard();
        event.setCanceled(true);
        addDinars(player, amount);
        player.level().playSound(null, player.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.5f, 1.8f);
        player.displayClientMessage(Component.translatable("message.sofe.dinars", amount).withStyle(ChatFormatting.GOLD), true);
    }

    /** The Flask refills when the player comes back to Sulthari, or when an elite falls. */
    public static void refillFlask(ServerPlayer player) {
        EconomyCapability.get(player).ifPresent(e -> {
            int max = flaskMax(player);
            if (e.flaskCharges() >= max) return;
            e.refillFlask(max);
            player.displayClientMessage(Component.translatable("message.sofe.flask.refilled").withStyle(ChatFormatting.AQUA), true);
        });
        sync(player);
    }

    public static void onEnteredRegion(ServerPlayer player, Region region) {
        if (region == Region.SULTHARI) refillFlask(player);
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }
}
