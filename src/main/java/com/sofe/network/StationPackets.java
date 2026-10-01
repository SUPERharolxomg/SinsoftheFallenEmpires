package com.sofe.network;

import com.sofe.crafting.StationRecipe;
import com.sofe.crafting.StationService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Packets of the stations, the Bearer's Flask and the potion belt. */
public final class StationPackets {

    private StationPackets() {
    }

    /** Server → client: open the station's recipe list (the recipes themselves are synced with the datapack). */
    public record Open(StationRecipe.Kind kind) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeEnum(kind);
        }

        public static Open decode(FriendlyByteBuf buf) {
            return new Open(buf.readEnum(StationRecipe.Kind.class));
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientPacketHandlers.openStation(kind));
        }
    }

    /** Client → server: make a recipe at the open station. */
    public record Make(ResourceLocation recipe) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(recipe);
        }

        public static Make decode(FriendlyByteBuf buf) {
            return new Make(buf.readResourceLocation());
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            ServerPlayer player = context.get().getSender();
            if (player != null) StationService.make(player, recipe);
        }
    }

    /** Client → server: drink from the Bearer's Flask (key H) or from a potion belt slot (Alt + 7 to 0). */
    public record Drink(int beltSlot) {
        public static final int FLASK = -1;

        public void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(beltSlot + 1);
        }

        public static Drink decode(FriendlyByteBuf buf) {
            return new Drink(buf.readVarInt() - 1);
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;
            if (beltSlot == FLASK) com.sofe.item.ConsumableItems.drinkFlask(player);
            else com.sofe.item.PotionBelt.drink(player, beltSlot);
        }
    }
}
